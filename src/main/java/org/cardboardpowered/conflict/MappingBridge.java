package org.cardboardpowered.conflict;

import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.MappingResolver;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.Map;

/**
 * Mapping bridge for normalizing Mixin target names across different mapping namespaces.
 * Uses FabricLoader MappingResolver with caching and graceful fallback.
 */
public class MappingBridge {

    private static final Logger LOGGER = LogManager.getLogger("Cardboard-MappingBridge");
    private static final String UNRESOLVED_PREFIX = "intermediary(unresolved):";

    private final Map<String, String> classCache = new HashMap<>();
    private final Map<String, String> methodCache = new HashMap<>();
    private int classCacheHits;
    private int classCacheMisses;
    private int methodCacheHits;
    private int methodCacheMisses;

    /**
     * Get the MappingResolver from FabricLoader.
     */
    private MappingResolver getResolver() {
        return FabricLoader.getInstance().getMappingResolver();
    }

    /**
     * Convert intermediary class name to named (Mojang) format.
     * e.g. "class_1792" -> "BoatItem"
     */
    public String toNamed(String intermediaryClassName) {
        if (intermediaryClassName == null || intermediaryClassName.isEmpty()) {
            return "";
        }
        // Already in named format (doesn't contain class_ and isn't unresolved)
        if (!intermediaryClassName.contains("class_") && !intermediaryClassName.startsWith(UNRESOLVED_PREFIX)) {
            return intermediaryClassName;
        }

        String cacheKey = "intermediary:" + intermediaryClassName;
        String cached = classCache.get(cacheKey);
        if (cached != null) {
            classCacheHits++;
            return cached;
        }
        classCacheMisses++;

        try {
            MappingResolver resolver = getResolver();
            // Normalize class name: if it starts with class_ and doesn't contain /, prefix with net/minecraft/
            String normalizedClassName = intermediaryClassName;
            if (intermediaryClassName.startsWith("class_") && !intermediaryClassName.contains("/")) {
                normalizedClassName = "net/minecraft/" + intermediaryClassName;
                LOGGER.debug("Normalizing class name: {} -> {}", intermediaryClassName, normalizedClassName);
            }
            
            LOGGER.debug("Mapping class: {} (normalized: {})", intermediaryClassName, normalizedClassName);
            String result = resolver.mapClassName("intermediary", normalizedClassName);
            classCache.put(cacheKey, result);
            return result;
        } catch (Exception e) {
            LOGGER.warn("Failed to map class \"{}\": {}", intermediaryClassName, e.getMessage());
            String fallback = UNRESOLVED_PREFIX + intermediaryClassName;
            classCache.put(cacheKey, fallback);
            return fallback;
        }
    }

    /**
     * Convert named (Mojang) class name to intermediary format.
     * e.g. "BoatItem" -> "class_1792"
     */
    public String toIntermediary(String namedClassName) {
        if (namedClassName == null || namedClassName.isEmpty()) {
            return "";
        }
        // Already in intermediary format (starts with class_)
        if (namedClassName.startsWith("class_")) {
            // Still need to normalize if it's a simple class name without path
            if (!namedClassName.contains("/")) {
                String normalized = "net/minecraft/" + namedClassName;
                LOGGER.debug("Normalizing intermediary class name: {} -> {}", namedClassName, normalized);
                return normalized;
            }
            return namedClassName;
        }

        String cacheKey = "named:" + namedClassName;
        String cached = classCache.get(cacheKey);
        if (cached != null) {
            classCacheHits++;
            return cached;
        }
        classCacheMisses++;

        try {
            MappingResolver resolver = getResolver();
            LOGGER.debug("Mapping class to intermediary: {}", namedClassName);
            String result = resolver.mapClassName("named", namedClassName);
            classCache.put(cacheKey, result);
            return result;
        } catch (Exception e) {
            LOGGER.warn("Failed to map class \"{}\": {}", namedClassName, e.getMessage());
            String fallback = UNRESOLVED_PREFIX + namedClassName;
            classCache.put(cacheKey, fallback);
            return fallback;
        }
    }

    /**
     * Map a method name from one namespace to another.
     * Falls back to original method name on failure.
     */
    public String mapMethodName(String namespace, String className, String methodName, String descriptor) {
        if (methodName == null || methodName.isEmpty()) {
            return "";
        }

        String cacheKey = namespace + ":" + className + ":" + methodName + ":" + (descriptor != null ? descriptor : "");
        String cached = methodCache.get(cacheKey);
        if (cached != null) {
            methodCacheHits++;
            return cached;
        }
        methodCacheMisses++;

        try {
            MappingResolver resolver = getResolver();
            String result = resolver.mapMethodName(namespace, className, methodName, descriptor);
            methodCache.put(cacheKey, result);
            return result;
        } catch (Exception e) {
            LOGGER.debug("Failed to map method \"{}.{}\": {}", className, methodName, e.getMessage());
            methodCache.put(cacheKey, methodName);
            return methodName;
        }
    }

    /**
     * Normalize a class name to the named (Mojang) namespace.
     * If className looks like an intermediary name (contains "class_"), convert it.
     * Otherwise return as-is.
     */
    public String normalizeClassName(String className) {
        if (className == null || className.isEmpty()) {
            return "";
        }
        // Check if it's an unresolved placeholder
        if (className.startsWith(UNRESOLVED_PREFIX)) {
            return className;
        }
        // Check if it looks like an intermediary name (starts with class_)
        if (className.startsWith("class_")) {
            // Normalize path if needed
            String normalized = className;
            if (!className.contains("/")) {
                normalized = "net/minecraft/" + className;
                LOGGER.debug("Normalizing class name in normalizeClassName: {} -> {}", className, normalized);
            }
            // Convert to named if it has proper path
            if (normalized.startsWith("net/minecraft/") || normalized.startsWith("net.minecraft.")) {
                return toNamed(normalized);
            }
        }
        // Already in named format or can't be normalized
        return className;
    }

    /**
     * Get cache statistics for debugging.
     */
    public String getCacheStats() {
        return String.format("MappingBridge Cache: class=%d size (hit=%d, miss=%d), method=%d size (hit=%d, miss=%d)",
            classCache.size(), classCacheHits, classCacheMisses,
            methodCache.size(), methodCacheHits, methodCacheMisses);
    }

    /**
     * Clear all caches.
     */
    public void clearCache() {
        classCache.clear();
        methodCache.clear();
        classCacheHits = 0;
        classCacheMisses = 0;
        methodCacheHits = 0;
        methodCacheMisses = 0;
    }
}
