/**
 * Cardboard - Spigot/Paper for Fabric
 * Copyright (C) 2020-2026 CardboardPowered.org and contributors*
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

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import org.cardboardpowered.conflict.model.MixinConfigData;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class MixinConfigScanner {

    private static final Logger LOGGER = LogManager.getLogger("Cardboard-ConflictScanner");
    private static final Set<String> SKIP_MOD_IDS = Set.of("minecraft", "fabricloader", "java", "cardboard");
    private static final String CONFIG_SUFFIX = ".mixins.json";

    public List<MixinConfigData> scanAllMods() {
        LOGGER.info("Scanning mods for mixin configs...");
        List<MixinConfigData> results = new ArrayList<>();
        int scannedMods = 0;
        int totalConfigs = 0;

        for (ModContainer mod : FabricLoader.getInstance().getAllMods()) {
            String modId = mod.getMetadata().getId();
            if (SKIP_MOD_IDS.contains(modId)) {
                continue;
            }

            // P1-2: Skip client-only mods
            // Check mod environment - only scan SERVER and UNIVERSAL mods
            net.fabricmc.loader.api.metadata.ModEnvironment environment = mod.getMetadata().getEnvironment();
            if (environment == net.fabricmc.loader.api.metadata.ModEnvironment.CLIENT) {
                LOGGER.debug("Skipping client-only mod: {}", modId);
                continue;
            }

            scannedMods++;
            Path rootPath = mod.getRootPath();
            List<MixinConfigData> modConfigs = scanModForConfigs(rootPath, modId);
            totalConfigs += modConfigs.size();
            results.addAll(modConfigs);
        }

        results.sort((a, b) -> {
            int cmp = a.getSourceModId().compareTo(b.getSourceModId());
            if (cmp != 0) return cmp;
            return a.getConfigFileName().compareTo(b.getConfigFileName());
        });

        LOGGER.info("Scanned {} mods, found {} mixin configs", scannedMods, totalConfigs);

        return results;
    }

    private List<MixinConfigData> scanModForConfigs(Path rootPath, String modId) {
        List<MixinConfigData> configs = new ArrayList<>();

        try {
            if (isJarPath(rootPath)) {
                configs.addAll(scanJarForConfigs(rootPath, modId));
            } else if (Files.isDirectory(rootPath)) {
                configs.addAll(scanDirectoryForConfigs(rootPath, modId));
            }
        } catch (IOException e) {
            LOGGER.warn("Failed to scan mod '{}' for mixin configs: {}", modId, e.getMessage());
        }

        return configs;
    }

    private boolean isJarPath(Path path) {
        String pathStr = path.toString().toLowerCase();
        return pathStr.endsWith(".jar") || pathStr.endsWith(".zip");
    }

    private List<MixinConfigData> scanJarForConfigs(Path jarPath, String modId) throws IOException {
        List<MixinConfigData> configs = new ArrayList<>();

        // Try to resolve the real path, but fall back to the original path if it fails
        Path realPath = jarPath;
        try {
            realPath = jarPath.toRealPath();
        } catch (java.nio.file.NoSuchFileException e) {
            // This can happen with Fabric virtual paths
            // Use the path as-is if it looks like a valid JAR file
            String pathStr = jarPath.toString();
            if (pathStr.toLowerCase().endsWith(".jar") || pathStr.toLowerCase().endsWith(".zip")) {
                realPath = jarPath;
            } else {
                // Not a JAR file path, skip
                return configs;
            }
        }

        String pathStr = realPath.toString();

        if (pathStr.startsWith("jar:") || pathStr.startsWith("file:")) {
            // Handle URI-style paths from FabricLoader
            String cleanPath = pathStr;
            if (cleanPath.startsWith("jar:")) cleanPath = cleanPath.substring(4);
            if (cleanPath.startsWith("file:")) cleanPath = cleanPath.substring(5);
            if (cleanPath.contains("!")) cleanPath = cleanPath.substring(0, cleanPath.indexOf("!"));

            Path jarFile = Path.of(cleanPath);
            if (Files.exists(jarFile) && Files.isRegularFile(jarFile)) {
                configs.addAll(scanJarFile(jarFile, modId));
            }
        } else if (Files.exists(realPath) && Files.isRegularFile(realPath)) {
            configs.addAll(scanJarFile(realPath, modId));
        }

        return configs;
    }

    private List<MixinConfigData> scanJarFile(Path jarFile, String modId) throws IOException {
        List<MixinConfigData> configs = new ArrayList<>();

        try (ZipFile zip = new ZipFile(jarFile.toFile())) {
            var entries = zip.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                String name = entry.getName();
                if (name.endsWith(CONFIG_SUFFIX) && !entry.isDirectory()) {
                    try (InputStream is = zip.getInputStream(entry);
                         BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
                        String content = reader.lines().collect(Collectors.joining("\n"));
                        String fileName = name.contains("/") ? name.substring(name.lastIndexOf('/') + 1) : name;
                        MixinConfigData config = parseConfigJson(content, modId, fileName, name);
                        if (config != null) {
                            LOGGER.debug("Found mixin config '{}' in mod '{}'", fileName, modId);
                            configs.add(config);
                        }
                    } catch (Exception e) {
                        // Log warning but continue
                    }
                }
            }
        }

        return configs;
    }

    private List<MixinConfigData> scanDirectoryForConfigs(Path dirPath, String modId) throws IOException {
        List<MixinConfigData> configs = new ArrayList<>();

        Files.walkFileTree(dirPath, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                String fileName = file.getFileName().toString();
                if (fileName.endsWith(CONFIG_SUFFIX)) {
                    try {
                        String content = Files.readString(file, StandardCharsets.UTF_8);
                        String relativePath = dirPath.relativize(file).toString();
                        MixinConfigData config = parseConfigJson(content, modId, fileName, relativePath);
                        if (config != null) {
                            LOGGER.debug("Found mixin config '{}' in mod '{}'", fileName, modId);
                            configs.add(config);
                        }
                    } catch (Exception e) {
                        // Log warning but continue
                    }
                }
                return FileVisitResult.CONTINUE;
            }
        });

        return configs;
    }

    private MixinConfigData parseConfigJson(String jsonContent, String modId, String configFileName, String configFilePath) {
        if (jsonContent == null || jsonContent.isEmpty()) {
            return null;
        }

        try {
            JsonElement element = JsonParser.parseString(jsonContent);
            if (!element.isJsonObject()) {
                return null;
            }

            JsonObject root = element.getAsJsonObject();
            MixinConfigData config = new MixinConfigData();
            config.setSourceModId(modId);
            config.setConfigFileName(configFileName);
            config.setConfigFilePath(configFilePath);

            if (root.has("package") && root.get("package").isJsonPrimitive()) {
                config.setPackageName(root.get("package").getAsString());
            }

            if (root.has("mixins") && root.get("mixins").isJsonArray()) {
                config.setMixins(parseStringList(root.getAsJsonArray("mixins")));
            }

            if (root.has("server") && root.get("server").isJsonArray()) {
                config.setServer(parseStringList(root.getAsJsonArray("server")));
            }

            if (root.has("client") && root.get("client").isJsonArray()) {
                config.setClient(parseStringList(root.getAsJsonArray("client")));
            }

            if (root.has("refmap") && root.get("refmap").isJsonPrimitive()) {
                config.setRefmap(root.get("refmap").getAsString());
            }

            if (root.has("required") && root.get("required").isJsonPrimitive()) {
                config.setRequired(root.get("required").getAsBoolean());
            }

            if (root.has("minVersion") && root.get("minVersion").isJsonPrimitive()) {
                config.setMinVersion(root.get("minVersion").getAsString());
            }

            return config;
        } catch (Exception e) {
            // JSON parsing failed, skip this config
            return null;
        }
    }

    private List<String> parseStringList(JsonArray array) {
        List<String> result = new ArrayList<>();
        for (JsonElement elem : array) {
            if (elem.isJsonPrimitive()) {
                result.add(elem.getAsString());
            }
        }
        return result;
    }
}
