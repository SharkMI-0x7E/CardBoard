/**
 * Cardboard - Spigot/Paper for Fabric
 * Copyright (C) 2020-2026 CardboardPowered.org and contributors
 * Copyright (C) 2026 SharkMI and contributors
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.cardboardpowered.conflict;

import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import org.cardboardpowered.conflict.model.MixinClassInfo;
import org.cardboardpowered.conflict.model.MixinConfigData;
import org.cardboardpowered.conflict.model.MixinMethod;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class MixinAnnotationScanner {

    private static final Logger LOGGER = LogManager.getLogger("Cardboard-ASMScanner");
    private final Map<String, MixinClassInfo> cache = new HashMap<>();

    /**
     * Analyze a single .class file's bytecode and extract Mixin annotation info.
     */
    public MixinClassInfo analyzeClass(byte[] classBytes, String sourceModId, String sourceJarPath) {
        try {
            ClassNode classNode = new ClassNode();
            org.objectweb.asm.ClassReader reader = new org.objectweb.asm.ClassReader(classBytes);
            reader.accept(classNode, org.objectweb.asm.ClassReader.SKIP_CODE | org.objectweb.asm.ClassReader.SKIP_FRAMES);

            MixinClassInfo info = new MixinClassInfo();
            info.setClassName(classNode.name.replace('/', '.'));
            info.setSourceModId(sourceModId);
            info.setSourceJarPath(sourceJarPath);

            // Parse class-level annotations
            if (classNode.visibleAnnotations != null) {
                for (AnnotationNode ann : classNode.visibleAnnotations) {
                    if (MixinAnnotationDescriptor.MIXIN.equals(ann.desc)) {
                        parseMixinAnnotation(ann, info);
                    }
                }
            }

            // Parse method-level annotations
            if (classNode.methods != null) {
                for (MethodNode method : classNode.methods) {
                    if (method.visibleAnnotations != null) {
                        for (AnnotationNode ann : method.visibleAnnotations) {
                            parseMethodAnnotation(ann, method, info);
                        }
                    }
                }
            }

            return info;
        } catch (Exception e) {
            LOGGER.warn("ASM parsing failed for class in mod '{}': {}", sourceModId, e.getMessage());
            return null;
        }
    }

    /**
     * Batch scan all Mod JARs using Step 2's config data as input.
     */
    public List<MixinClassInfo> scanAllConfigs(List<MixinConfigData> configs) {
        LOGGER.info("Analyzing mixin classes from {} configs...", configs.size());
        List<MixinClassInfo> results = new ArrayList<>();
        int totalClasses = 0;

        // Build modId -> ModContainer map for quick lookup
        Map<String, ModContainer> modMap = new HashMap<>();
        for (ModContainer mod : FabricLoader.getInstance().getAllMods()) {
            modMap.put(mod.getMetadata().getId(), mod);
        }

        for (MixinConfigData config : configs) {
            ModContainer mod = modMap.get(config.getSourceModId());
            if (mod == null) {
                LOGGER.warn("Mod '{}' not found in FabricLoader", config.getSourceModId());
                continue;
            }

            Path rootPath = mod.getRootPath();
            List<MixinClassInfo> classInfos = scanModConfig(config, rootPath);
            results.addAll(classInfos);
            totalClasses += classInfos.size();
        }

        LOGGER.info("Analyzed {} mixin classes from {} configs", totalClasses, configs.size());
        return results;
    }

    /**
     * Scan a specific Mod's classes for a given MixinConfigData.
     */
    private List<MixinClassInfo> scanModConfig(MixinConfigData config, Path rootPath) {
        List<MixinClassInfo> results = new ArrayList<>();
        String packageName = config.getPackageName() != null ? config.getPackageName().replace('.', '/') : "";

        try {
            if (isJarPath(rootPath)) {
                results.addAll(scanJarClasses(rootPath, config));
            } else if (Files.isDirectory(rootPath)) {
                results.addAll(scanDirectoryClasses(rootPath, config));
            }
        } catch (IOException e) {
            LOGGER.warn("Failed to scan mod '{}' for mixin classes: {}", config.getSourceModId(), e.getMessage());
        }

        return results;
    }

    private boolean isJarPath(Path path) {
        String pathStr = path.toString().toLowerCase();
        return pathStr.endsWith(".jar") || pathStr.endsWith(".zip");
    }

    /**
     * Scan classes from a JAR file, filtering by mixin package.
     */
    private List<MixinClassInfo> scanJarClasses(Path jarPath, MixinConfigData config) throws IOException {
        List<MixinClassInfo> results = new ArrayList<>();
        Path realPath = jarPath;
        try {
            realPath = jarPath.toRealPath();
        } catch (java.nio.file.NoSuchFileException e) {
            // Fabric virtual paths may not support toRealPath
            LOGGER.debug("toRealPath failed for {}, using original path", jarPath);
        }
        String pathStr = realPath.toString();

        // Handle URI-style paths from FabricLoader
        if (pathStr.startsWith("jar:") || pathStr.startsWith("file:")) {
            String cleanPath = pathStr;
            if (cleanPath.startsWith("jar:")) cleanPath = cleanPath.substring(4);
            if (cleanPath.startsWith("file:")) cleanPath = cleanPath.substring(5);
            if (cleanPath.contains("!")) cleanPath = cleanPath.substring(0, cleanPath.indexOf("!"));

            Path jarFile = Path.of(cleanPath);
            if (Files.exists(jarFile) && Files.isRegularFile(jarFile)) {
                scanJarFile(jarFile, config, results);
            }
        } else if (Files.exists(realPath) && Files.isRegularFile(realPath)) {
            scanJarFile(realPath, config, results);
        }

        return results;
    }

    private void scanJarFile(Path jarFile, MixinConfigData config, List<MixinClassInfo> results) throws IOException {
        String packageName = config.getPackageName() != null ? config.getPackageName().replace('.', '/') : "";
        String sourceModId = config.getSourceModId();

        try (JarFile jar = new JarFile(jarFile.toFile())) {
            jar.stream()
               .filter(e -> !e.isDirectory())
               .filter(e -> e.getName().endsWith(".class"))
               .filter(e -> isMixinClass(e.getName(), packageName))
               .forEach(entry -> {
                   try (var in = jar.getInputStream(entry)) {
                       byte[] bytes = in.readAllBytes();
                       String className = entry.getName().replace(".class", "").replace('/', '.');
                       String cacheKey = sourceModId + ":" + className;

                       MixinClassInfo cached = cache.get(cacheKey);
                       if (cached != null) {
                           results.add(cached);
                           return;
                       }

                       MixinClassInfo info = analyzeClass(bytes, sourceModId, jarFile.toString());
                       if (info != null) {
                           cache.put(cacheKey, info);
                           results.add(info);
                       }
                   } catch (IOException e) {
                       LOGGER.warn("Failed to read class '{}' from JAR of mod '{}': {}", entry.getName(), sourceModId, e.getMessage());
                   }
               });
        }
    }

    /**
     * Scan classes from a directory (dev environment), filtering by mixin package.
     */
    private List<MixinClassInfo> scanDirectoryClasses(Path dirPath, MixinConfigData config) throws IOException {
        List<MixinClassInfo> results = new ArrayList<>();
        String packageName = config.getPackageName() != null ? config.getPackageName().replace('.', '/') : "";
        String sourceModId = config.getSourceModId();

        Files.walkFileTree(dirPath, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                String relativePath = dirPath.relativize(file).toString().replace('\\', '/');
                if (!relativePath.endsWith(".class")) {
                    return FileVisitResult.CONTINUE;
                }
                if (!isMixinClass(relativePath, packageName)) {
                    return FileVisitResult.CONTINUE;
                }

                try {
                    byte[] bytes = Files.readAllBytes(file);
                    String className = relativePath.replace(".class", "").replace('/', '.');
                    String cacheKey = sourceModId + ":" + className;

                    MixinClassInfo cached = cache.get(cacheKey);
                    if (cached != null) {
                        results.add(cached);
                        return FileVisitResult.CONTINUE;
                    }

                    MixinClassInfo info = analyzeClass(bytes, sourceModId, dirPath.toString());
                    if (info != null) {
                        cache.put(cacheKey, info);
                        results.add(info);
                    }
                } catch (Exception e) {
                    LOGGER.warn("Failed to analyze class '{}' in mod '{}': {}", relativePath, sourceModId, e.getMessage());
                }

                return FileVisitResult.CONTINUE;
            }
        });

        return results;
    }

    /**
     * Check if a class path belongs to a mixin package.
     * Only processes classes under the mixin package to skip non-mixin classes.
     */
    private boolean isMixinClass(String classPath, String packageName) {
        if (packageName != null && !packageName.isEmpty()) {
            return classPath.startsWith(packageName + "/");
        }
        // Fallback: look for /mixin/ in path
        return classPath.contains("/mixin/");
    }

    /**
     * Parse @Mixin class-level annotation.
     */
    private void parseMixinAnnotation(AnnotationNode ann, MixinClassInfo info) {
        info.setMixin(true);

        if (ann.values == null) return;

        for (int i = 0; i < ann.values.size(); i += 2) {
            String key = (String) ann.values.get(i);
            Object value = ann.values.get(i + 1);

            if ("value".equals(key) || "targets".equals(key)) {
                parseTargetClasses(value, info);
             } else if ("priority".equals(key)) {
                if (value instanceof Integer) {
                    info.setPriority((Integer) value);
                }
            }
        }
    }

    /**
     * Parse target classes from @Mixin value/targets field.
     * Handles both single Type and List<Type>.
     */
    private void parseTargetClasses(Object value, MixinClassInfo info) {
         if (value instanceof Type) {
            String className = ((Type) value).getClassName();
            info.getTargetClasses().add(className);
        } else if (value instanceof List) {
            for (Object item : (List<?>) value) {
                if (item instanceof Type) {
                    String className = ((Type) item).getClassName();
                    info.getTargetClasses().add(className);
                 } else if (item instanceof String) {
                    // String descriptor format
                    String desc = (String) item;
                    if (desc.startsWith("L") && desc.endsWith(";")) {
                        info.getTargetClasses().add(desc.substring(1, desc.length() - 1).replace('/', '.'));
                    } else {
                        info.getTargetClasses().add(desc.replace('/', '.'));
                    }
                }
            }
        } else if (value instanceof String) {
            String desc = (String) value;
             if (desc.startsWith("L") && desc.endsWith(";")) {
                info.getTargetClasses().add(desc.substring(1, desc.length() - 1).replace('/', '.'));
            } else {
                info.getTargetClasses().add(desc.replace('/', '.'));
            }
        }
    }

    /**
     * Parse method-level Mixin annotations and dispatch to specific handlers.
     */
    private void parseMethodAnnotation(AnnotationNode ann, MethodNode method, MixinClassInfo info) {
        String desc = ann.desc;

        if (MixinAnnotationDescriptor.SHADOW.equals(desc)) {
            // Skip @Shadow silently - not relevant for conflict detection
            return;
        }

        if (MixinAnnotationDescriptor.UNIQUE.equals(desc)) {
            // Skip @Unique - internal helper methods
            return;
        }

        MixinMethod mixinMethod = createMixinMethod(ann, method);
        if (mixinMethod == null) {
            // Unknown annotation - log debug and skip
            LOGGER.debug("Unknown mixin annotation: {} in {}.{}", desc, info.getClassName(), method.name);
            return;
        }

        dispatchMethodToList(mixinMethod, desc, info);
    }

    /**
     * Create a MixinMethod object from a method annotation.
     * Returns null if the annotation is not a known injection type.
     */
    private MixinMethod createMixinMethod(AnnotationNode ann, MethodNode method) {
        String desc = ann.desc;
        if (!MixinAnnotationDescriptor.KNOWN_INJECT_ANNOTATIONS.contains(desc)) {
            return null;
        }

         MixinMethod mm = new MixinMethod();
        mm.setName(method.name);
        mm.setDescriptor(method.desc);

        if (ann.values != null) {
            for (int i = 0; i < ann.values.size(); i += 2) {
                String key = (String) ann.values.get(i);
                Object value = ann.values.get(i + 1);

                if ("method".equals(key)) {
                    parseMethodTarget(value, mm);
                 } else if ("cancellable".equals(key)) {
                    if (value instanceof Boolean) {
                        mm.setCancellable((Boolean) value);
                    }
                } else if ("at".equals(key)) {
                    if (value instanceof AnnotationNode) {
                        parseAtAnnotation((AnnotationNode) value, mm);
                    }
                 } else if ("priority".equals(key)) {
                    if (value instanceof Integer) {
                        mm.setPriority((Integer) value);
                    }
                }
            }
        }

        return mm;
    }

    /**
     * Parse the 'method' field of an injection annotation.
     * Can be a single String or List<String>.
     */
    private void parseMethodTarget(Object value, MixinMethod mm) {
         if (value instanceof String) {
            mm.getTargetMethods().add((String) value);
        } else if (value instanceof List) {
            for (Object item : (List<?>) value) {
                 if (item instanceof String) {
                    mm.getTargetMethods().add((String) item);
                }
            }
        }
    }

    /**
     * Parse @At nested annotation.
     * Extracts 'value' and 'target' fields from the nested AnnotationNode.
     */
    private void parseAtAnnotation(AnnotationNode atNode, MixinMethod mm) {
        if (atNode.values == null) return;

        for (int i = 0; i < atNode.values.size(); i += 2) {
            String key = (String) atNode.values.get(i);
            Object value = atNode.values.get(i + 1);

             if ("value".equals(key)) {
                if (value instanceof String) {
                    mm.getAtValues().add((String) value);
                } else if (value instanceof List) {
                    for (Object item : (List<?>) value) {
                        if (item instanceof String) {
                            mm.getAtValues().add((String) item);
                        }
                    }
                }
             } else if ("target".equals(key)) {
                if (value instanceof String) {
                    mm.getAtTargets().add((String) value);
                } else if (value instanceof List) {
                    for (Object item : (List<?>) value) {
                        if (item instanceof String) {
                            mm.getAtTargets().add((String) item);
                        }
                    }
                }
            }
        }
    }

    /**
     * Dispatch a MixinMethod to the appropriate list in MixinClassInfo based on annotation type.
     */
    private void dispatchMethodToList(MixinMethod mm, String desc, MixinClassInfo info) {
        if (desc.equals(MixinAnnotationDescriptor.OVERWRITE)) {
            info.getOverwrites().add(mm);
            mm.setAnnotationType("Overwrite");
        } else if (desc.equals(MixinAnnotationDescriptor.INJECT)) {
            info.getInjections().add(mm);
            mm.setAnnotationType("Inject");
        } else if (desc.equals(MixinAnnotationDescriptor.REDIRECT)) {
            info.getRedirects().add(mm);
            mm.setAnnotationType("Redirect");
        } else if (desc.equals(MixinAnnotationDescriptor.MODIFY_ARG)) {
            info.getModifyArgs().add(mm);
            mm.setAnnotationType("ModifyArg");
        } else if (desc.equals(MixinAnnotationDescriptor.MODIFY_VARIABLE)) {
            info.getModifyVariables().add(mm);
            mm.setAnnotationType("ModifyVariable");
        } else if (desc.equals(MixinAnnotationDescriptor.MODIFY_RETURN_VALUE)) {
            info.getModifyReturnValues().add(mm);
            mm.setAnnotationType("ModifyReturnValue");
        } else if (desc.equals(MixinAnnotationDescriptor.WRAP_WITH_CONDITION) ||
                   desc.equals(MixinAnnotationDescriptor.ME_WRAP_WITH_CONDITION)) {
            info.getWrapWithConditions().add(mm);
            mm.setAnnotationType("WrapWithCondition");
        } else if (desc.equals(MixinAnnotationDescriptor.ME_WRAP_OPERATION)) {
            info.getWrapWithConditions().add(mm);
            mm.setAnnotationType("WrapOperation");
        } else if (desc.equals(MixinAnnotationDescriptor.ME_MODIFY_EXPRESSION_VALUE)) {
            info.getWrapWithConditions().add(mm);
            mm.setAnnotationType("ModifyExpressionValue");
        }
    }

    /**
     * Get the cache for external access.
     */
    public Map<String, MixinClassInfo> getCache() {
        return cache;
    }
}
