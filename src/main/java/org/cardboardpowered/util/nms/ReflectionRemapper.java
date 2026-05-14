/**
 * Cardboard - Bukkit/Spigot/Paper API for Fabric
 * Copyright (C) 2023-2026, CardboardPowered.org
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
package org.cardboardpowered.util.nms;

import org.cardboardpowered.CardboardMod;
import org.cardboardpowered.mohistremap.RemapUtilProvider;

import net.minecraft.SharedConstants;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerConnectionListener;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.plugin.PluginLoader;
import org.bukkit.plugin.SimplePluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.java.JavaPluginLoader;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Re-mapping of Reflection.
 */
public class ReflectionRemapper {

    public static final String NMS_VERSION = "v1_21_R7";
    public static JavaPlugin plugin;

    public static String mapClassName(String className) {
    	// TODO check why Essentials
    	if (className.startsWith("net.ess3.provider.providers.LegacyPotionMetaProvider")) {
    		return "net.ess3.provider.providers.ModernPotionMetaProvider";
    	}
    	
    	RemapUtils ru = (RemapUtils) RemapUtilProvider.get();

        if (className.startsWith("org.bukkit.craftbukkit." + NMS_VERSION + "."))
            return ru.map("org.bukkit.craftbukkit." + className.substring(23 + NMS_VERSION.length() + 1));

        if (className.startsWith("org.bukkit.craftbukkit.CraftServer."))
            return ru.map(className.replace("org.bukkit.craftbukkit.CraftServer.", "org.bukkit.craftbukkit."));

        if (className.startsWith("net.minecraft.server." + NMS_VERSION + "."))
            return ru.map(className.replace("net.minecraft.server." + NMS_VERSION + ".", "net.minecraft.server."));

        if (className.startsWith("net.minecraft.") && !className.startsWith("class_"))
            return ru.map(className);

        if (className.startsWith("org.bukkit.craftbukkit."))
            return ru.map(className); // We are not CraftBukkit, check for our own version of the class.

        if (className.startsWith("net.minecraft.server.CraftServer."))
            return ru.map(className.replace("net.minecraft.server.CraftServer.", "net.minecraft.server."));

        return className;
    }

    /**
     * @deprecated Old code
     */
    @Deprecated
    private static Class<?> getClassForName(String className) throws ClassNotFoundException {
        return getClassFromJPL(className);
    }

    @Deprecated
    public static Field getFieldByName(Class<?> calling, String f) throws ClassNotFoundException {
        try {
            Field field = calling.getDeclaredField(MappingsReader.getIntermedField_2(calling, f));
            field.setAccessible(true);
            return field;
        } catch (NoSuchFieldException | SecurityException e) {
            try {
                Field a = calling.getDeclaredField(MappingsReader.getIntermedField_2(calling, f));
                a.setAccessible(true);
                return a;
            } catch (NoSuchFieldException | SecurityException e1) {
                Class<?> whyIsAsmBroken = getClassFromJPL(getCallerClassName());
                try {
                    Field a = whyIsAsmBroken.getDeclaredField(MappingsReader.getIntermedField_2(whyIsAsmBroken, f));
                    a.setAccessible(true);
                    return a;
                } catch (NoSuchFieldException | SecurityException e2) {
                    if (f.contains("B_STATS_VERSION")) {
                        return getBstatsVersionField();
                    }
                    e2.printStackTrace();
                }
                return null;
            }
        }
    }

    private static int BV_CALLED = 0;
    public static Field getBstatsVersionField() {
        Field f = null;
        int i = 0;
        for (final Class<?> service : Bukkit.getServicesManager().getKnownServices()) {
            if (i < BV_CALLED) {
                i++;
                continue;
            }
            try {
                f = service.getField("B_STATS_VERSION"); // Identifies bStats classes
                break;
            } catch (final NoSuchFieldException ignored) {
            }
        }
        BV_CALLED++;
        return f;
    }

    @Deprecated
    public static Field getDeclaredFieldByName(Class<?> calling, String f) throws ClassNotFoundException, NoSuchFieldException {
        try {
            return calling.getDeclaredField(MappingsReader.getIntermedField_2(calling, f));
        } catch (NoSuchFieldException | SecurityException e) {
            try {
                Field a = calling.getDeclaredField(MappingsReader.getIntermedField_2(calling, f));
                a.setAccessible(true);
                return a;
            } catch (NoSuchFieldException | SecurityException e1) {
                Class<?> whyIsAsmBroken = getClassFromJPL(getCallerClassName());
                try {
                    if (f.contains("connectedChannels")) {
                        Field a = ServerConnectionListener.class.getDeclaredField("connections");
                        a.setAccessible(true);
                        return a;
                    }
                    if (null == whyIsAsmBroken) {
                        System.out.println("CALLING: " + calling.getName() + ", F: " + f);
                        return null;
                    }
                    Field a = whyIsAsmBroken.getDeclaredField(MappingsReader.getIntermedField_2(whyIsAsmBroken, f));
                    a.setAccessible(true);
                    return a;
                } catch (NoSuchFieldException | SecurityException e2) {
                    throw e2;
                    //e1.printStackTrace();
                }
               // return null;
            }
        }
    }

    public static CraftServer getCraftServer() {
        return CraftServer.INSTANCE;
    }

    public static MinecraftServer getNmsServer() {
        return CraftServer.server;
    }

    public static Method[] getMethods(Class<?> calling) {
        Method[] r = calling.getMethods();
        if (calling.getSimpleName().contains("MinecraftServer")) {
            Method[] nr = new Method[r.length+1];
            for (int i = 0; i < r.length; i++) {
                nr[i] = r[i];
            }
            try {
                nr[r.length] = ReflectionRemapper.class.getMethod("getNmsServer");
            } catch (NoSuchMethodException | SecurityException e) {
                e.printStackTrace();
            }
            return nr;
        }
        return r;
    }

    /**
     * Deprecated old code
     */
    @SuppressWarnings("unchecked")
    @Deprecated
    public static Class<?> getClassFromJPL(String name) {
        try {
            SimplePluginManager pm = (SimplePluginManager) Bukkit.getPluginManager();
            Field fa = SimplePluginManager.class.getDeclaredField("fileAssociations");
            fa.setAccessible(true);
            Map<Pattern, PluginLoader> pl = (Map<Pattern, PluginLoader>) fa.get(pm);
            JavaPluginLoader jpl = null;
            for (PluginLoader loader : pl.values()) {
                if (loader instanceof JavaPluginLoader) {
                    jpl = (JavaPluginLoader) loader;
                    break;
                }
            }

            Method fc = JavaPluginLoader.class.getDeclaredMethod("getClassByName", String.class);
            fc.setAccessible(true);
            return (Class<?>) fc.invoke(jpl, name);
        } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
            CardboardMod.LOGGER.warning("SOMETHING EVERY WRONG! PLEASE REPORT THE EXCEPTION BELOW TO BUKKIT4FABRIC:");
            e.printStackTrace();
            return null;
        }
    }

    /**
     * @Deprecated old code
     */
    @Deprecated
    private static JavaPluginLoader getFirstJPL() {
        return null;
    }

    /**
     * @deprecated Old code
     */
    @Deprecated
    public static String getCallerClassName() { 
        StackTraceElement[] stElements = Thread.currentThread().getStackTrace();
        for (int i=1; i<stElements.length; i++) {
            StackTraceElement ste = stElements[i];
            if (!ste.getClassName().equals(ReflectionRemapper.class.getName()) && ste.getClassName().indexOf("java.lang.Thread")!=0)
                return ste.getClassName();
        }
        return null;
    }

    /**
     */
    public static String getPackageName(Package pkage) {
        String name = pkage.getName();
        if (name.startsWith("org.bukkit.craftbukkit"))
            name = name.replace("org.bukkit.craftbukkit", "org.bukkit.craftbukkit." + NMS_VERSION);
        return name;
    }

    /**
     */
    public static String getClassName(Class<?> clazz) {
        String name = clazz.getName();
        if (name.startsWith("org.bukkit.craftbukkit"))
            name = name.replace("org.bukkit.craftbukkit", "org.bukkit.craftbukkit." + NMS_VERSION);
        return name;
    }

    /**
     */
    public static String getCanonicalName(Class<?> clazz) {
        String name = clazz.getName();
        if (name.startsWith("org.bukkit.craftbukkit"))
            name = name.replace("org.bukkit.craftbukkit", "org.bukkit.craftbukkit." + NMS_VERSION);
        return name;
    }

    /**
     */
    public static String getMinecraftServerVersion() {
        return SharedConstants.getCurrentVersion().name();
    }
    
    /**
     * *
     * @param <E>
     * @param access
     * @param key
     * @return
     */
    public static <E> Registry<E> lookupOrThrow(RegistryAccess access, ResourceKey<? extends Registry<? extends E>> key) {
        return access.lookupOrThrow(key);
    }

}
