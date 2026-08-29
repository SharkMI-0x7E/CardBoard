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
package org.cardboardpowered.util;

import java.lang.reflect.Method;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;

/**
 * Generates "ghost" classes so that plugins asking for an OLD/renamed
 * intermediate inner class name (e.g. net.minecraft.class_3231$a) can actually
 * load it. JVM's Class.forName() requires the loader to return a Class whose
 * binary name matches the requested name exactly - translating the name and
 * returning a different class yields NoClassDefFoundError. So we synthesize a
 * real class with the requested old name that extends/implements the modern
 * class it was renamed to.
 */
public final class GhostClassGenerator {

    private static final ConcurrentMap<String, Class<?>> CACHE = new ConcurrentHashMap<>();

    private GhostClassGenerator() { }

    /**
     * @param dottedName requested (old) binary name, e.g. "net.minecraft.class_3231$a"
     * @param original   the modern class it maps to, e.g. ServerEntity$Synchronizer
     * @return the generated class, or null on failure (caller should fall back)
     */
    public static Class<?> getOrCreate(String dottedName, Class<?> original) {
        if (dottedName == null || original == null) {
            return null;
        }
        Class<?> cached = CACHE.get(dottedName);
        if (cached != null) {
            return cached;
        }
        try {
            byte[] bytes = synthesize(dottedName, original);
            Class<?> ghost = define(dottedName, bytes, original.getClassLoader());
            Class<?> raced = CACHE.putIfAbsent(dottedName, ghost);
            return raced != null ? raced : ghost;
        } catch (Throwable t) {
            System.out.println("[GHOST-ERROR] failed to generate " + dottedName + " -> " + t);
            return null;
        }
    }

    private static byte[] synthesize(String dottedName, Class<?> original) {
        String internal = dottedName.replace('.', '/');
        String originalInternal = original.getName().replace('.', '/');
        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        if (original.isInterface()) {
            cw.visit(Opcodes.V21, Opcodes.ACC_PUBLIC | Opcodes.ACC_INTERFACE | Opcodes.ACC_ABSTRACT,
                    internal, null, "java/lang/Object", new String[] { originalInternal });
        } else {
            cw.visit(Opcodes.V21, Opcodes.ACC_PUBLIC | Opcodes.ACC_SUPER,
                    internal, null, originalInternal, null);
        }
        cw.visitEnd();
        return cw.toByteArray();
    }

    private static Class<?> define(String dottedName, byte[] bytes, ClassLoader loader) throws Exception {
        Method define = ClassLoader.class.getDeclaredMethod("defineClass",
                String.class, byte[].class, int.class, int.class);
        define.setAccessible(true);
        return (Class<?>) define.invoke(loader, dottedName, bytes, 0, bytes.length);
    }
}