package org.bukkit.craftbukkit.util;

import org.bukkit.Bukkit;

public final class Versioning {
    private static final String API_VERSION = "1.21.11";

    public static String getBukkitVersion() {
        return Bukkit.getVersion();
    }

    public static String getCurrentApiVersion() {
        return API_VERSION;
    }
}
