package com.sk89q.wepif;

public final class PermissionsResolverManager {

    private static final PermissionsResolverManager INSTANCE = new PermissionsResolverManager();

    private Object resolver;
    private boolean initialized;

    private PermissionsResolverManager() {
    }

    public static PermissionsResolverManager getInstance() {
        return INSTANCE;
    }

    public static void initialize() {
        INSTANCE.initialized = true;
    }

    public static void initialize(Object ignored) {
        INSTANCE.initialized = true;
    }

    public static void setResolver(Object resolver) {
        INSTANCE.resolver = resolver;
        INSTANCE.initialized = true;
    }

    public static Object getResolver() {
        return INSTANCE.resolver;
    }

    public static boolean isInitialized() {
        return INSTANCE.initialized;
    }

    public static void clear() {
        INSTANCE.resolver = null;
        INSTANCE.initialized = false;
    }
}
