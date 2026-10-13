package org.bukkit.plugin.java;

import com.google.common.base.Preconditions;
import com.mohistmc.dynamicenum.MohistDynamEnum;
import net.md_5.specialsource.repo.RuntimeRepo;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.plugin.InvalidPluginException;
import org.bukkit.plugin.PluginDescriptionFile;
import org.cardboardpowered.util.GhostClassGenerator;
import org.bukkit.plugin.SimplePluginManager;
import org.cardboardpowered.mohistremap.ClassLoaderContext;
import org.cardboardpowered.mohistremap.ClassMapping;
import org.cardboardpowered.mohistremap.RemapUtilProvider;
import org.cardboardpowered.util.LibbyClasspathFixer;
import org.cardboardpowered.util.MyPluginFixManager;
import org.cardboardpowered.util.PluginLibraryHost;
import org.cardboardpowered.util.nms.RemapUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.JarURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.security.CodeSigner;
import java.security.CodeSource;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.logging.Level;

/**
 * A ClassLoader for plugins, to allow shared classes across multiple plugins
 */
public class PluginClassLoader extends URLClassLoader implements PluginLibraryHost {
    private final JavaPluginLoader loader;
    private final Map<String, Class<?>> classes = new ConcurrentHashMap<String, Class<?>>();
    private final PluginDescriptionFile description;
    private final File dataFolder;
    private final File file;
    private final JarFile jar;
    private final Manifest manifest;
    private final URL url;
    private final ClassLoader libraryLoader;
    final JavaPlugin plugin;
    private JavaPlugin pluginInit;
    private IllegalStateException pluginState;
    private final Set<String> seenIllegalAccess = Collections.newSetFromMap(new ConcurrentHashMap<>());
    private final Set<Package> packageCache = Collections.newSetFromMap(new ConcurrentHashMap<>());

    // External dependency jars under plugins/libraries, lazily loaded as a last-resort
    // fallback when every other class loading path has failed.
    private static final String LIBRARY_DIR = "plugins/libraries";
    private volatile List<JarFile> libraryJars;

    static {
        ClassLoader.registerAsParallelCapable();
    }
    
    @Nullable
    public JavaPlugin getPlugin() {
       return this.plugin;
    }

    PluginClassLoader(@NotNull final JavaPluginLoader loader, @Nullable final ClassLoader parent, @NotNull final PluginDescriptionFile description, @NotNull final File dataFolder, @NotNull final File file, @Nullable ClassLoader libraryLoader) throws IOException, InvalidPluginException, MalformedURLException {
        super(new URL[] {file.toURI().toURL()}, parent);
        Preconditions.checkArgument(loader != null, "Loader cannot be null");

        this.loader = loader;
        this.description = description;
        this.dataFolder = dataFolder;
        this.file = file;
        this.jar = new JarFile(file);
        this.manifest = jar.getManifest();
        this.url = file.toURI().toURL();
        this.libraryLoader = libraryLoader;

        try {
            Class<?> jarClass;
            try {
                jarClass = Class.forName(description.getMain(), true, this);
            } catch (ClassNotFoundException ex) {
                throw new InvalidPluginException("Cannot find main class `" + description.getMain() + "'", ex);
            }

            Class<? extends JavaPlugin> pluginClass;
            try {
                pluginClass = jarClass.asSubclass(JavaPlugin.class);
            } catch (ClassCastException ex) {
                throw new InvalidPluginException("main class `" + description.getMain() + "' does not extend JavaPlugin", ex);
            }

            plugin = pluginClass.newInstance();
        } catch (IllegalAccessException ex) {
            throw new InvalidPluginException("No public constructor", ex);
        } catch (InstantiationException ex) {
            throw new InvalidPluginException("Abnormal plugin type", ex);
        }
    }

    // private String mapCraftBukkit(String name) {
    //     if (name.startsWith("org.bukkit.craftbukkit.")) {
    //         int nameStart = name.indexOf('.', 23);
    //         if (nameStart != -1) {
    //             return "org.bukkit.craftbukkit" + name.substring(nameStart);
    //         }
    //     } else if(name.startsWith("org/bukkit/craftbukkit")) {
    //         int nameStart = name.indexOf('/', 23);
    //         if (nameStart != -1) {
    //             return "org/bukkit/craftbukkit" + name.substring(nameStart);
    //         }
    //     }
    //
    //     return name;
    // }

    @Override
    public URL getResource(String name) {
        return findResource(name);
    }

    @Override
    public Enumeration<URL> getResources(String name) throws IOException {
        return findResources(name);
    }

    @Override
    protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        return loadClass0(name, resolve, true, true);
    }

    Class<?> loadClass0(@NotNull String name, boolean resolve, boolean checkGlobal, boolean checkLibraries) throws ClassNotFoundException {
        // name = mapCraftBukkit(name);

        try {
            Class<?> result = super.loadClass(name, resolve);

            // SPIGOT-6749: Library classes will appear in the above, but we don't want to return them to other plugins
            if (checkGlobal || result.getClassLoader() == this) {
                return result;
            }
        } catch (ClassNotFoundException ex) {
        }

        if (checkLibraries && libraryLoader != null) {
            try {
                return libraryLoader.loadClass(name);
            } catch (ClassNotFoundException ex) {
            }
        }

        if (checkGlobal) {
            // This ignores the libraries of other plugins, unless they are transitive dependencies.
            Class<?> result = loader.getClassByName(name, resolve, description);

            if (result != null) {
                // If the class was loaded from a library instead of a PluginClassLoader, we can assume that its associated plugin is a transitive dependency and can therefore skip this check.
                if (result.getClassLoader() instanceof PluginClassLoader) {
                    PluginDescriptionFile provider = ((PluginClassLoader) result.getClassLoader()).description;

                    if (provider != description
                            && !seenIllegalAccess.contains(provider.getName())
                            && !((SimplePluginManager) loader.server.getPluginManager()).isTransitiveDepend(description, provider)) {

                        seenIllegalAccess.add(provider.getName());
                        if (plugin != null) {
                            plugin.getLogger().log(Level.WARNING, "Loaded class {0} from {1} which is not a depend or softdepend of this plugin.", new Object[]{name, provider.getFullName()});
                        } else {
                            // In case the bad access occurs on construction
                            loader.server.getLogger().log(Level.WARNING, "[{0}] Loaded class {1} from {2} which is not a depend or softdepend of this plugin.", new Object[]{description.getName(), name, provider.getFullName()});
                        }
                    }
                }

                return result;
            }
        }

        // Last-resort fallback: try external dependency jars under plugins/libraries.
        Class<?> fromLibrary = this.cardboard$loadFromLibraryDir(name);
        if (fromLibrary != null) {
            loader.setClass(name, fromLibrary);
            return fromLibrary;
        }

        throw new ClassNotFoundException(name);
    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        // name = mapCraftBukkit(name);

        ClassLoaderContext.put(this);
        Class<?> result;
        try {
            if (RemapUtilProvider.get().needRemap(name.replace('/','.'))) {
            	
            	RemapUtils remapUtils = (RemapUtils) RemapUtilProvider.get();
            	
                ClassMapping remappedClassMapping = remapUtils.jarMapping.byNMSName.get(name);
                if (remappedClassMapping != null) {
                    String remappedClass = remappedClassMapping.getMcpName();
                    return Class.forName(remappedClass);
                }

                // Fix C(1): plugins carry OLD intermediate inner-class names in their bytecode
                // (e.g. net.minecraft.class_7225$a, renamed to class_7225$class_7874 in modern
                // Yarn). Load the modern target class directly from the alias table (bypassing
                // the full map() pipeline), then, because JVM's Class.forName() checks that the
                // loader returns a Class with EXACTLY the requested binary name, synthesize a
                // "ghost" class carrying the old name (extends/implements the modern class).
                try {
                    String dotName = name.replace('/', '.');
                    String slashName = dotName.replace('.', '/');

                    // Candidate modern names: alias table first (slash key), then map().
                    java.util.LinkedHashSet<String> candidates = new java.util.LinkedHashSet<>();
                    try {
                        String patched = remapUtils.jarMapping.classes.get(slashName);
                        if (patched != null) {
                            candidates.add(patched.replace('/', '.'));
                        }
                    } catch (Throwable ignored) { }
                    try {
                        String mapped = remapUtils.map(dotName);
                        if (mapped != null) {
                            candidates.add(mapped.replace('/', '.'));
                        }
                    } catch (Throwable ignored) { }

                    ClassLoader[] loaders = {
                        CraftServer.server.getClass().getClassLoader(),
                        Thread.currentThread().getContextClassLoader()
                    };
                    for (String cand : candidates) {
                        if (cand == null || cand.equals(dotName)) {
                            continue;
                        }
                        for (ClassLoader l : loaders) {
                            if (l == null) continue;
                            try {
                                Class<?> alias = l.loadClass(cand);
                                if (alias != null) {
                                    if (!alias.getName().equals(dotName)) {
                                        Class<?> ghost = GhostClassGenerator.getOrCreate(dotName, alias);
                                        if (ghost != null) {
                                            return ghost;
                                        }
                                    }
                                    return alias;
                                }
                            } catch (Throwable ignored) { }
                        }
                    }
                } catch (NoClassDefFoundError | RuntimeException ignored) {
                    // mapping was a dead end; fall through to the normal load path below
                }

                // Fix A: when the mapping index misses (e.g. intermediary name queried against a
                // named-key index, or a class that simply has no entry), do NOT fail immediately.
                // Fall through to the normal class loading path below, which also tries the server
                // class loader (Knot) as a last resort. This restores plugins like Denizen that
                // reflectively load runtime Minecraft classes by name.
            }
            if (name.startsWith("org.bukkit.")) {
                throw new ClassNotFoundException(name);
            }
            result = classes.get(name);
            synchronized (name.intern()) {
                if (result == null) {
                    result = remappedFindClass(name);

                    if (result != null) {
                        loader.setClass(name, result);
                    }

                    if (result == null) {
                        try {
                            result = CraftServer.server.getClass().getClassLoader().loadClass(name);
                        } catch (Throwable throwable) {
                            throw new ClassNotFoundException(name, throwable);
                        }
                    }

                    loader.setClass(name, result);
                    classes.put(name, result);
                }
            }
        } finally {
            ClassLoaderContext.pop();
        }
        return result;
    }

    // Lazy-load and cache all *.jar files under plugins/libraries. Never throws: on any
    // failure (missing dir, unreadable jar) it silently degrades to an empty list.
    private List<JarFile> getLibraryJars() {
        if (libraryJars == null) {
            synchronized (this) {
                if (libraryJars == null) {
                    libraryJars = scanLibraryDir();
                }
            }
        }
        return libraryJars;
    }

    private List<JarFile> scanLibraryDir() {
        List<JarFile> jars = new ArrayList<>();
        File dir = new File(LIBRARY_DIR);
        if (dir.isDirectory()) {
            File[] files = dir.listFiles((d, n) -> n.endsWith(".jar"));
            if (files != null) {
                for (File f : files) {
                    try {
                        jars.add(new JarFile(f));
                    } catch (IOException ignored) {
                        // skip unreadable jar
                    }
                }
            }
        }
        return jars;
    }

    // Last-resort fallback: load a class untouched (no remap) from an external dependency
    // jar. A broken jar must only be skipped, never mask the original ClassNotFoundException.
    private Class<?> cardboard$loadFromLibraryDir(String name) {
        for (JarFile libJar : getLibraryJars()) {
            String path = name.replace('.', '/').concat(".class");
            JarEntry entry = libJar.getJarEntry(path);
            if (entry == null) {
                continue;
            }
            try {
                byte[] bytes;
                try (InputStream in = libJar.getInputStream(entry)) {
                    bytes = in.readAllBytes();
                }
                // Same reflection-only rewrite as the Fix B branch in remappedFindClass:
                // dependency jars loaded from plugins/libraries/ skip the remap pipeline,
                // but their reflection call sites still need the ProxyClass forwarding.
                bytes = RemapUtils.reflectRewriteOnly(bytes);
                bytes = LibbyClasspathFixer.apply(name, bytes);
                File jarFile = new File(libJar.getName());
                CodeSource source = new CodeSource(jarFile.toURI().toURL(), new CodeSigner[0]);
                return defineClass(name, bytes, 0, bytes.length, source);
            } catch (Throwable ignored) {
                // skip this jar; keep original failure behavior
            }
        }
        return null;
    }


    private Class<?> remappedFindClass(String name) {
        Class<?> result = null;
        // Untouched bytes from the library-only branch; used to retry defineClass with the
        // original bytecode if the JVM rejects the reflection-rewritten version (below).
        byte[] libraryFallback = null;

        try {
            // Load the resource to the name
            String path = name.replace('.', '/').concat(".class");
            URL url = this.findResource(path);
            if (url != null) {
                InputStream stream = url.openStream();
                if (stream != null) {
                    byte[] bytecode;
                    try (InputStream in = stream) {
                        bytecode = in.readAllBytes();
                    }
                    if (isLibraryOnlyClass(name, bytecode)) {
                        // Fix B: pure third-party library classes (no references to any server
                        // class) skip the remap pipeline. The pipeline is built on an old
                        // SpecialSource/ASM stack that can choke on modern (Java 21+) bytecode,
                        // silently producing ClassNotFoundException (e.g. Citizens'
                        // ch.ethz.globis.phtree.PhTreeHelper).
                        //
                        // They are not loaded fully untouched, though: reflection call sites
                        // (Class.forName/getDeclaredField/getDeclaredMethod/...) still get the
                        // ProxyClass rewrite. Library classes perform reflection too (e.g.
                        // denizencore's ReflectionHelper), and without this step their member
                        // lookups never reach the name translator. This pass is self-contained
                        // and falls back to the untouched bytes on any failure.
                        libraryFallback = bytecode;
                        bytecode = RemapUtils.reflectRewriteOnly(bytecode);
                        // Neutralize Libby's reflective addURL escalation (family match,
                        // any relocation prefix); no-op bytes for every other class.
                        bytecode = LibbyClasspathFixer.apply(name, bytecode);
                    } else {
                        bytecode = RemapUtilProvider.get().getJarRemapper().remapClassFile(new ByteArrayInputStream(bytecode), RuntimeRepo.getInstance());
                        
                        //if (path.contains("/worldedit/bukkit/adapter/impl/")) {
                        //	System.out.println("Debug: Processing class: " + path);
                        //}
                        
                        bytecode = loader.server.getUnsafe().processClass(description, path, bytecode);
                        bytecode = RemapUtilProvider.get().remapFindClass(bytecode);

                        bytecode = modifyByteCode(name, bytecode); // Mohist: add entry point for asm or mixin

                        bytecode = MyPluginFixManager.injectPluginFix(name, bytecode); // Mohist - Inject plugin fix
                    }

                    JarURLConnection jarURLConnection = (JarURLConnection) url.openConnection();
                    URL jarURL = jarURLConnection.getJarFileURL();

                    final Manifest manifest = jarURLConnection.getManifest();
                    fixPackage(manifest, url, name);

                    CodeSource codeSource = new CodeSource(jarURL, new CodeSigner[0]);
                    try {
                        result = this.defineClass(name, bytecode, 0, bytecode.length, codeSource);
                    } catch (LinkageError e) {
                        // The reflection-only rewrite is new for library classes - keep the
                        // "worst case = untouched bytes" contract airtight at defineClass too.
                        // If the JVM rejects the rewritten class and we still hold the original
                        // bytes, retry with those. (Same array reference means the rewrite
                        // already fell back to the original bytes - just rethrow.)
                        if (libraryFallback == null || libraryFallback == bytecode) {
                            throw e;
                        }
                        result = this.defineClass(name, libraryFallback, 0, libraryFallback.length, codeSource);
                    }
                    if (result != null) {
                        // Resolve it - sets the class loader of the class
                        this.resolveClass(result);
                    }
                    
                }
            }
        } catch (Exception t) {
            t.printStackTrace();
        }

        return result;
    }

    // Fix B: a class is "library-only" when it references no server-side type
    // (Minecraft/Bukkit/Spigot internals). Such classes bypass the full remap pipeline,
    // whose legacy ASM stack can reject modern bytecode; they still receive the
    // reflection-only rewrite (see the reflectRewriteOnly call sites in this file).
    private static boolean isLibraryOnlyClass(String name, byte[] bytecode) {
        if (name.startsWith("net.minecraft.") || name.startsWith("org.bukkit.") || name.startsWith("org.spigotmc.")) {
            return false;
        }
        // Cheap heuristic: every server class reference materializes in the constant pool as a
        // raw path string, so a substring scan over the raw bytes is sufficient to decide.
        String pool = new String(bytecode, java.nio.charset.StandardCharsets.ISO_8859_1);
        return pool.indexOf("net/minecraft/") == -1
                && pool.indexOf("org/bukkit/") == -1
                && pool.indexOf("org/spigotmc/") == -1;
    }

    // Mohist start: add entry point for asm or mixin
    private byte[] modifyByteCode(String className, byte[] bytes) {
        return bytes;
    }
    //Mohist end

    private void fixPackage(Manifest manifest, URL url, String name) {
        int dot = name.lastIndexOf('.');
        if (dot != -1) {
            String pkgName = name.substring(0, dot);
            Package pkg = getPackage(pkgName);
            if (pkg == null) {
                try {
                    if (manifest != null) {
                        pkg = definePackage(pkgName, manifest, url);
                    } else {
                        pkg = definePackage(pkgName, null, null, null, null, null, null, null);
                    }
                } catch (IllegalArgumentException ignored) {
                }
            }
            if (pkg != null && manifest != null) {
                if (!packageCache.contains(pkg)) {
                    Attributes attributes = manifest.getMainAttributes();
                    if (attributes != null) {
                        try {
                            try {
                                Object versionInfo = MohistDynamEnum.getField(pkg, Package.class.getDeclaredField("versionInfo"));
                                if (versionInfo != null) {
                                    Class<?> Package$VersionInfo = Class.forName("java.lang.Package$VersionInfo");
                                    MohistDynamEnum.setField(versionInfo, attributes.getValue(Attributes.Name.IMPLEMENTATION_TITLE), Package$VersionInfo.getDeclaredField("implTitle"));
                                    MohistDynamEnum.setField(versionInfo, attributes.getValue(Attributes.Name.IMPLEMENTATION_VERSION), Package$VersionInfo.getDeclaredField("implVersion"));
                                    MohistDynamEnum.setField(versionInfo, attributes.getValue(Attributes.Name.IMPLEMENTATION_VENDOR), Package$VersionInfo.getDeclaredField("implVendor"));
                                    MohistDynamEnum.setField(versionInfo, attributes.getValue(Attributes.Name.SPECIFICATION_TITLE), Package$VersionInfo.getDeclaredField("specTitle"));
                                    MohistDynamEnum.setField(versionInfo, attributes.getValue(Attributes.Name.SPECIFICATION_VERSION), Package$VersionInfo.getDeclaredField("specVersion"));
                                    MohistDynamEnum.setField(versionInfo, attributes.getValue(Attributes.Name.SPECIFICATION_VENDOR), Package$VersionInfo.getDeclaredField("specVendor"));
                                }
                            } catch (Exception ignored) {
                            }
                        } finally {
                            packageCache.add(pkg);
                        }
                    }
                }
            }
        }
    }

    @Override
    public void close() throws IOException {
        try {
            super.close();
        } finally {
            jar.close();
        }
    }

    @NotNull
    Collection<Class<?>> getClasses() {
        return classes.values();
    }

    synchronized void initialize(@NotNull JavaPlugin javaPlugin) {
        Preconditions.checkArgument(javaPlugin != null, "Initializing plugin cannot be null");
        Preconditions.checkArgument(javaPlugin.getClass()
                .getClassLoader() == this, "Cannot initialize plugin outside of this class loader");
        if (this.plugin != null || this.pluginInit != null) {
            throw new IllegalArgumentException("Plugin already initialized!", pluginState);
        }

        pluginState = new IllegalStateException("Initial initialization");
        this.pluginInit = javaPlugin;

        javaPlugin.init(loader, loader.server, description, dataFolder, file, this);
    }

    @Override
    public void cardboard$addPluginLibrary(URL url) {
        // Legal replacement for Libby's reflective URLClassLoader#addURL escalation.
        // addURL is protected in URLClassLoader; this subclass calls it directly.
        Preconditions.checkArgument(url != null, "url cannot be null");
        super.addURL(url);
    }
}
