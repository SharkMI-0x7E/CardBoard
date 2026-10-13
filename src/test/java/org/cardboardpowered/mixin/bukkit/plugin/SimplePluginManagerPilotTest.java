/**
 * Cardboard - Spigot/Paper for Fabric
 * Copyright (C) 2020-2026 CardboardPowered.org and contributors
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
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.cardboardpowered.mixin.bukkit.plugin;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

/**
 * Pilot for migrating LOW-risk @Overwrite stubs in BukkitSimplePluginManagerMixin
 * to precise HEAD injections. Covers three shapes: constant stub (useTimings),
 * branched query (getDefaultPermSubscriptions), iterating forwarder (addPermissions).
 */
class SimplePluginManagerPilotTest {

    private static final String INJECT = "Lorg/spongepowered/asm/mixin/injection/Inject;";
    private static final String OVERWRITE = "Lorg/spongepowered/asm/mixin/Overwrite;";

    private static ClassNode readMixin() throws Exception {
        File classFile = new File("build/classes/java/main",
                "org/cardboardpowered/mixin/bukkit/plugin/BukkitSimplePluginManagerMixin.class");
        assertTrue(classFile.isFile(), "compiled mixin missing");
        ClassNode node = new ClassNode();
        new ClassReader(Files.readAllBytes(classFile.toPath())).accept(node, ClassReader.SKIP_FRAMES);
        return node;
    }

    private static List<AnnotationNode> annotations(MethodNode method) {
        List<AnnotationNode> all = new ArrayList<>();
        if (method.visibleAnnotations != null) {
            all.addAll(method.visibleAnnotations);
        }
        if (method.invisibleAnnotations != null) {
            all.addAll(method.invisibleAnnotations);
        }
        return all;
    }

    private static Object annotationValue(AnnotationNode annotation, String key) {
        if (annotation == null || annotation.values == null) {
            return null;
        }
        for (int i = 0; i < annotation.values.size(); i += 2) {
            if (key.equals(annotation.values.get(i))) {
                return annotation.values.get(i + 1);
            }
        }
        return null;
    }

    private static AnnotationNode injectTargeting(ClassNode clazz, String descriptorPart) {
        for (MethodNode method : clazz.methods) {
            for (AnnotationNode annotation : annotations(method)) {
                if (!INJECT.equals(annotation.desc)) {
                    continue;
                }
                Object raw = annotationValue(annotation, "method");
                List<String> targets = new ArrayList<>();
                if (raw instanceof List<?> list) {
                    list.forEach(element -> targets.add(String.valueOf(element)));
                } else if (raw != null) {
                    targets.add(String.valueOf(raw));
                }
                if (targets.stream().anyMatch(t -> t.contains(descriptorPart))
                        && Boolean.TRUE.equals(annotationValue(annotation, "cancellable"))) {
                    return annotation;
                }
            }
        }
        return null;
    }

    @Test
    void pilotMethodsAreCancellableHeadInjects() throws Exception {
        ClassNode clazz = readMixin();
        assertNotNull(injectTargeting(clazz, "useTimings()Z"));
        assertNotNull(injectTargeting(clazz, "getDefaultPermSubscriptions(Z)Ljava/util/Set;"));
        assertNotNull(injectTargeting(clazz, "addPermissions(Ljava/util/List;)V"));
    }

    @Test
    void pilotMethodsNoLongerOverwritten() throws Exception {
        ClassNode clazz = readMixin();
        for (MethodNode method : clazz.methods) {
            boolean isOverwrite = annotations(method).stream().anyMatch(a -> OVERWRITE.equals(a.desc));
            if (isOverwrite) {
                assertFalse(List.of("useTimings", "getDefaultPermSubscriptions", "addPermissions")
                                .contains(method.name),
                        "pilot method must not keep @Overwrite: " + method.name);
            }
        }
    }
}
