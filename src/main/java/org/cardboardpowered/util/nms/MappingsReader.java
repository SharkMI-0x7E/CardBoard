/**
 * The Bukkit for Fabric Project
 * Copyright (C) 2020-2023
 */
package org.cardboardpowered.util.nms;

import java.io.File;

import org.cardboardpowered.mohistremap.RemapUtilProvider;

import net.fabricmc.loader.api.FabricLoader;

/**
 * @deprecated Replaced
 */
@Deprecated
public class MappingsReader {

    @Deprecated
	public static String dev(String s) {
        if (!FabricLoader.getInstance().isDevelopmentEnvironment()) return s;
        return FabricLoader.getInstance().getMappingResolver().mapClassName("intermediary", s);
    }

    @Deprecated
	public static String obf(String s) {
        return dev(FabricLoader.getInstance().getMappingResolver().mapClassName("official", s));
    }

    // TODO
    public static String getIntermedClass(String spigot) {
        return RemapUtilProvider.get().map(spigot);
    }
    
    // TODO
    public static String getIntermedField_2(Class<?> c, String spigot) {
    	return RemapUtilProvider.get().mapFieldName(c, spigot);
    }

    @Deprecated
    public static String getIntermedField_old1(String c, String spigot) {
        return null;
    }

    @Deprecated
    public static String getIntermedField2_old1(String c, String spigot) {
        return null;
    }

    @Deprecated
    public static File exportResource(String res, File folder) {
    	return null;
    }

    @Deprecated
    public static String getIntermedMethod_old(String name, String spigot, Class<?>[] parms) {
        return obf(spigot);
    }

    @Deprecated
    public static String getIntermedMethod_old(String name, String spigot) {
        try {
            String iclazz = ReflectionRemapper.mapClassName(name);
            // Class<?> cl = Class.forName(iclazz);
            // Class<?> parent = cl.getSuperclass();
            return obf(spigot);
        } catch (Exception e) { return obf(spigot); }
    }

}