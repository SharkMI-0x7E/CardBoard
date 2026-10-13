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
package org.cardboardpowered.mixin.event;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
 * Structural proof that the three previously-missing Bukkit events are wired as
 * precise, cancellable injections (no @Overwrite) and registered in the mixin
 * config. Mixin annotations are scanned in BOTH visible and invisible forms.
 */
class BedAndTameEventMixinTest {

    private static final File CLASSES = new File("build/classes/java/main");
    private static final File MIXINS_JSON = new File("src/main/resources/bukkitfabric.mixins.json");

    private static final String INJECT_DESC = "Lorg/spongepowered/asm/mixin/injection/Inject;";
    private static final String OVERWRITE_DESC = "Lorg/spongepowered/asm/mixin/Overwrite;";
    private static final String AT_DESC = "Lorg/spongepowered/asm/mixin/injection/At;";
    private static final String MIXIN_INFO_DESC = "Lorg/cardboardpowered/util/MixinInfo;";

    private static ClassNode readMixin(String internalName) throws Exception {
        File classFile = new File(CLASSES, internalName + ".class");
        assertTrue(classFile.isFile(), "compiled mixin missing: " + classFile);
        ClassNode node = new ClassNode();
        new ClassReader(Files.readAllBytes(classFile.toPath())).accept(node, ClassReader.SKIP_FRAMES);
        return node;
    }

    private static List<AnnotationNode> annotationsOn(MethodNode method) {
        List<AnnotationNode> all = new ArrayList<>();
        if (method.visibleAnnotations != null) {
            all.addAll(method.visibleAnnotations);
        }
        if (method.invisibleAnnotations != null) {
            all.addAll(method.invisibleAnnotations);
        }
        return all;
    }

    private static List<AnnotationNode> annotationsOn(ClassNode clazz) {
        List<AnnotationNode> all = new ArrayList<>();
        if (clazz.visibleAnnotations != null) {
            all.addAll(clazz.visibleAnnotations);
        }
        if (clazz.invisibleAnnotations != null) {
            all.addAll(clazz.invisibleAnnotations);
        }
        return all;
    }

    private static AnnotationNode findAnnotation(List<AnnotationNode> annotations, String desc) {
        for (AnnotationNode node : annotations) {
            if (desc.equals(node.desc)) {
                return node;
            }
        }
        return null;
    }

    private static Object value(AnnotationNode annotation, String key) {
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

    private static List<String> methodNames(AnnotationNode inject) {
        Object raw = value(inject, "method");
        List<String> names = new ArrayList<>();
        if (raw instanceof List<?> list) {
            for (Object element : list) {
                names.add(String.valueOf(element));
            }
        } else if (raw != null) {
            names.add(String.valueOf(raw));
        }
        return names;
    }

    private static AnnotationNode injectFor(ClassNode clazz, String methodNamePart) {
        for (MethodNode method : clazz.methods) {
            for (AnnotationNode annotation : annotationsOn(method)) {
                if (INJECT_DESC.equals(annotation.desc)
                        && methodNames(annotation).stream().anyMatch(n -> n.contains(methodNamePart))) {
                    return annotation;
                }
            }
        }
        return null;
    }

    // @Inject.at() is declared as an array, so ASM stores even a single @At as
    // a one-element List<AnnotationNode>.
    private static AnnotationNode atNode(AnnotationNode inject) {
        Object raw = value(inject, "at");
        if (raw instanceof List<?> list && !list.isEmpty() && list.get(0) instanceof AnnotationNode at) {
            return at;
        }
        return raw instanceof AnnotationNode at ? at : null;
    }

    private static void assertCancellableHeadInject(AnnotationNode inject, String expectedTargetPart) {
        assertNotNull(inject, "@Inject missing");
        assertEquals(Boolean.TRUE, value(inject, "cancellable"), "inject must be cancellable");
        AnnotationNode at = atNode(inject);
        assertNotNull(at, "@At missing");
        if (expectedTargetPart != null) {
            String target = String.valueOf(value(at, "target"));
            assertTrue(target.contains(expectedTargetPart), "unexpected @At target: " + target);
        }
    }

    @Test
    void tamableAnimalUsesHeadCancellableInjectWithoutOverwrite() throws Exception {
        ClassNode clazz = readMixin("org/cardboardpowered/mixin/world/entity/TamableAnimalMixin");

        AnnotationNode inject = injectFor(clazz, "tame");
        assertNotNull(inject, "tame @Inject missing");
        assertEquals(Boolean.TRUE, value(inject, "cancellable"));
        AnnotationNode at = atNode(inject);
        assertEquals("HEAD", String.valueOf(value(at, "value")));

        for (MethodNode method : clazz.methods) {
            assertFalse(annotationsOn(method).stream().anyMatch(a -> OVERWRITE_DESC.equals(a.desc)),
                    "no @Overwrite allowed on " + method.name);
        }

        AnnotationNode info = findAnnotation(annotationsOn(clazz), MIXIN_INFO_DESC);
        assertNotNull(info, "@MixinInfo missing");
        assertTrue(((List<?>) value(info, "events")).stream()
                .map(String::valueOf).anyMatch("EntityTameEvent"::equals));
    }

    @Test
    void bedEventsUseCancellableInjectsAtCorrectPoints() throws Exception {
        ClassNode clazz = readMixin("org/cardboardpowered/mixin/server/level/ServerPlayerMixin_BedEvents");

        AnnotationNode enter = injectFor(clazz, "startSleepInBed");
        assertCancellableHeadInject(enter, "net/minecraft/world/entity/player/Player;startSleepInBed");
        AnnotationNode enterAt = atNode(enter);
        assertEquals("INVOKE", String.valueOf(value(enterAt, "value")));

        AnnotationNode leave = injectFor(clazz, "stopSleepInBed");
        assertNotNull(leave, "stopSleepInBed @Inject missing");
        assertEquals(Boolean.TRUE, value(leave, "cancellable"));
        AnnotationNode leaveAt = atNode(leave);
        assertEquals("HEAD", String.valueOf(value(leaveAt, "value")));

        for (MethodNode method : clazz.methods) {
            assertFalse(annotationsOn(method).stream().anyMatch(a -> OVERWRITE_DESC.equals(a.desc)),
                    "no @Overwrite allowed on " + method.name);
        }

        AnnotationNode info = findAnnotation(annotationsOn(clazz), MIXIN_INFO_DESC);
        assertNotNull(info, "@MixinInfo missing");
        List<?> events = (List<?>) value(info, "events");
        assertTrue(events.stream().map(String::valueOf).anyMatch("PlayerBedEnterEvent"::equals));
        assertTrue(events.stream().map(String::valueOf).anyMatch("PlayerBedLeaveEvent"::equals));
    }

    @Test
    void bothMixinsRegisteredInConfig() throws Exception {
        String json = Files.readString(MIXINS_JSON.toPath());
        assertTrue(json.contains("\"server.level.ServerPlayerMixin_BedEvents\""));
        assertTrue(json.contains("\"world.entity.TamableAnimalMixin\""));
    }
}
