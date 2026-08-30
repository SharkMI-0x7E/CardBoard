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
package org.cardboardpowered.util.nms;

/**
 * Tiny member-name resolver: translates "old" (named/official) member names
 * into the runtime intermediary names (field_xxx / method_xxx) used by the
 * reobfuscated server. Used by Denizen-style plugins that reflect on internal
 * server classes with pre-reobf names (map from reobf.tiny).
 * Lazy, thread-safe, no-op when the tiny file is unavailable.
 */
public final class MemberNameResolver {

    private static volatile boolean loaded = false;
    private static volatile java.util.Map<String, java.util.Map<String, String>> FIELD_BY_CLASS; // runtimeClass -> requestName -> runtimeField
    private static volatile java.util.Map<String, java.util.List<String[]>> METHOD_INDEX;       // runtimeClass -> [requestName, runtimeMethod, desc]
    private static volatile java.util.Map<String, String> CLASS_BY_NAMED;                       // named -> intermediary (runtime)

    private MemberNameResolver() {}

    public static void ensureLoaded() {
        if (loaded) return;
        synchronized (MemberNameResolver.class) {
            if (loaded) return;
            java.util.Map<String, java.util.Map<String, String>> fmap = new java.util.concurrent.ConcurrentHashMap<>();
            java.util.Map<String, java.util.List<String[]>> mmap = new java.util.concurrent.ConcurrentHashMap<>();
            java.util.Map<String, String> cmap = new java.util.concurrent.ConcurrentHashMap<>();
            try {
                java.io.InputStream in = MemberNameResolver.class.getClassLoader().getResourceAsStream("mappings/reobf.tiny");
                if (in == null) {
                    in = MemberNameResolver.class.getClassLoader().getResourceAsStream("META-INF/mappings/reobf.tiny");
                }
                if (in != null) {
                    try (java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.InputStreamReader(in))) {
                        String line;
                        String cur = null;
                        while ((line = reader.readLine()) != null) {
                            if (line.startsWith("tiny")) continue;
                            String[] p = line.split("\t");
                            if (p.length < 3) continue;
                            if (p[0].equals("c")) {
                                if (p.length < 4) continue;
                                cur = p[2];
                                cmap.putIfAbsent(p[3], p[2]); // named -> intermediary (incl. inner classes with $)
                            } else if (p[0].equals("f") && cur != null && p.length >= 4) {
                                // f | desc | official | intermediary | named
                                String official = p[2], intermediary = p[3];
                                String named = p.length > 4 ? p[4] : intermediary;
                                java.util.Map<String, String> fm = fmap.computeIfAbsent(cur, k -> new java.util.concurrent.ConcurrentHashMap<>());
                                fm.putIfAbsent(named, intermediary);
                                fm.putIfAbsent(official, intermediary);
                            } else if (p[0].equals("m") && cur != null && p.length >= 4) {
                                // m | desc | official | intermediary | named
                                String desc = p[1], official = p[2], intermediary = p[3];
                                String named = p.length > 4 ? p[4] : intermediary;
                                java.util.List<String[]> list = mmap.computeIfAbsent(cur, k -> java.util.Collections.synchronizedList(new java.util.ArrayList<>()));
                                if (official.equals(named)) { // unnamed in named namespace
                                    list.add(new String[]{official, intermediary, desc});
                                } else {
                                    list.add(new String[]{named, intermediary, desc});
                                    list.add(new String[]{official, intermediary, desc});
                                }
                            }
                        }
                    }
                }
            } catch (Throwable t) {
                // nothing - fall back to no-op
            }
            FIELD_BY_CLASS = fmap;
            METHOD_INDEX = mmap;
            CLASS_BY_NAMED = cmap;
            loaded = true;
        }
    }

    /** Translate a named (mojang) class name to the runtime intermediary name; returns null if unknown. */
    public static String translateClass(String named) {
        if (named == null) return null;
        ensureLoaded();
        if (CLASS_BY_NAMED == null) return null;
        return CLASS_BY_NAMED.get(named);
    }

    /** Translate a requested field name to the runtime delegate field name (same-or-superclass aware). */
    public static String translateField(Class<?> clazz, String requested) {
        if (clazz == null || requested == null) return null;
        ensureLoaded();
        if (FIELD_BY_CLASS == null) return null;
        String ic = clazz.getName().replace('.', '/');
        java.util.Map<String, String> m = FIELD_BY_CLASS.get(ic);
        if (m == null) return null;
        String res = m.get(requested);
        if (res != null) return res;
        // fall back: scan the tiny class hierarchy by walking superclasses/ifaces
        Class<?> c = clazz.getSuperclass();
        while (c != null && c != Object.class) {
            java.util.Map<String, String> sm = FIELD_BY_CLASS.get(c.getName().replace('.', '/'));
            if (sm != null) {
                String r = sm.get(requested);
                if (r != null) return r;
            }
            c = c.getSuperclass();
        }
        return null;
    }

    /** Translate a requested method name to the runtime delegate method name (exact-descriptor aware). */
    public static String translateMethod(Class<?> clazz, String requested, String desc) {
        if (clazz == null || requested == null) return null;
        ensureLoaded();
        if (METHOD_INDEX == null) return null;
        String ic = clazz.getName().replace('.', '/');
        java.util.List<String[]> list = METHOD_INDEX.get(ic);
        if (list == null) return null;
        String fallback = null;
        for (String[] row : list) {
            if (row[0].equals(requested)) {
                if (desc != null && row[2].equals(desc)) { // exact descriptor match wins
                    return row[1];
                }
                if (fallback == null) fallback = row[1]; // name-only match
            }
        }
        return fallback;
    }

    /** Reverse lookup: aliases (named/official) of a given RUNTIME field name on a class (incl. superclasses). */
    public static java.util.List<String> fieldAliases(Class<?> clazz, String runtimeName) {
        java.util.List<String> out = new java.util.ArrayList<>();
        if (clazz == null || runtimeName == null) return out;
        ensureLoaded();
        if (FIELD_BY_CLASS == null) return out;
        Class<?> c = clazz;
        while (c != null && c != Object.class) {
            java.util.Map<String, String> m = FIELD_BY_CLASS.get(c.getName().replace('.', '/'));
            if (m != null) {
                for (java.util.Map.Entry<String, String> e : m.entrySet()) {
                    if (e.getValue().equals(runtimeName)) out.add(e.getKey());
                }
            }
            c = c.getSuperclass();
        }
        return out;
    }

    /** Reverse lookup: aliases (named/official) of a given RUNTIME method name on a class (exact-desc aware, incl. superclasses). */
    public static java.util.List<String> methodAliases(Class<?> clazz, String runtimeName, String desc) {
        java.util.List<String> out = new java.util.ArrayList<>();
        if (clazz == null || runtimeName == null) return out;
        ensureLoaded();
        if (METHOD_INDEX == null) return out;
        Class<?> c = clazz;
        while (c != null && c != Object.class) {
            java.util.List<String[]> list = METHOD_INDEX.get(c.getName().replace('.', '/'));
            if (list != null) {
                for (String[] row : list) {
                    if (row[1].equals(runtimeName) && (desc == null || row[2].equals(desc))) out.add(row[0]);
                }
            }
            c = c.getSuperclass();
        }
        return out;
    }
}