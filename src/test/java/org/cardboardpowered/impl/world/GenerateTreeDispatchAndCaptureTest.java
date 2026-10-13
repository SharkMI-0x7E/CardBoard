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
package org.cardboardpowered.impl.world;

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
 * Regression guards for R1 review findings:
 * F-1 - CraftWorld must not re-declare the generateTree(Random,...) false stubs
 *       that shadowed CraftRegionAccessor's real Feature.place implementation;
 * F-2 - tree capture in LevelMixin must short-circuit setBlock (cancellable HEAD)
 *       and serve captured states from getBlockState.
 */
class GenerateTreeDispatchAndCaptureTest {

    private static ClassNode read(String internalName) throws Exception {
        File classFile = new File("build/classes/java/main", internalName + ".class");
        assertTrue(classFile.isFile(), "compiled class missing: " + classFile);
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

    @Test
    void craftWorldDoesNotShadowGenerateTreeWithFalseStubs() throws Exception {
        ClassNode craftWorld = read("org/cardboardpowered/impl/world/CraftWorld");
        for (MethodNode method : craftWorld.methods) {
            if ("generateTree".equals(method.name) && method.desc.contains("Ljava/util/Random;")) {
                assertFalse(method.desc.contains("Lorg/bukkit/TreeType;"),
                        "CraftWorld must inherit the real generateTree, not redeclare a stub: " + method.desc);
            }
        }
    }

    @Test
    void levelTreeCaptureShortCircuitsSetBlockAndReadsBack() throws Exception {
        ClassNode levelMixin = read("org/cardboardpowered/mixin/world/level/LevelMixin");
        AnnotationNode setBlockInject = null;
        AnnotationNode getBlockStateInject = null;
        for (MethodNode method : levelMixin.methods) {
            for (AnnotationNode annotation : annotations(method)) {
                if (!"Lorg/spongepowered/asm/mixin/injection/Inject;".equals(annotation.desc)) {
                    continue;
                }
                Object raw = value(annotation, "method");
                String target = String.valueOf(raw instanceof List<?> list && !list.isEmpty() ? list.get(0) : raw);
                if (target.contains("setBlock(Lnet/minecraft/core/BlockPos;")) {
                    setBlockInject = annotation;
                }
                if (target.contains("getBlockState(Lnet/minecraft/core/BlockPos;)")) {
                    getBlockStateInject = annotation;
                }
            }
        }
        assertNotNull(setBlockInject, "cancellable setBlock capture inject missing");
        assertNotNull(getBlockStateInject, "capture read-back getBlockState inject missing");
        assertTrue(Boolean.TRUE.equals(value(setBlockInject, "cancellable")));
        assertTrue(Boolean.TRUE.equals(value(getBlockStateInject, "cancellable")));

        // The captureBlockStates-only branch feeds ItemStackMixin#useOn's BlockPlaceEvent;
        // it must keep snapshotting OLD state through CraftBlock, and tree capture must
        // keep the Paper bedrock/end-portal protection (Blocks constants).
        boolean referencesCraftBlock = false;
        boolean referencesBedrock = false;
        for (MethodNode method : levelMixin.methods) {
            if (method.instructions == null) {
                continue;
            }
            for (org.objectweb.asm.tree.AbstractInsnNode insn : method.instructions) {
                if (insn instanceof org.objectweb.asm.tree.MethodInsnNode m
                        && "org/bukkit/craftbukkit/block/CraftBlock".equals(m.owner)
                        && "at".equals(m.name)) {
                    referencesCraftBlock = true;
                }
                if (insn instanceof org.objectweb.asm.tree.FieldInsnNode f
                        && "net/minecraft/world/level/block/Blocks".equals(f.owner)
                        && ("BEDROCK".equals(f.name) || "END_PORTAL".equals(f.name)
                                || "END_PORTAL_FRAME".equals(f.name))) {
                    referencesBedrock = true;
                }
            }
        }
        assertTrue(referencesCraftBlock,
                "captureBlockStates-only OLD-state snapshot branch (BlockPlaceEvent path) missing");
        assertTrue(referencesBedrock, "tree capture bedrock/end-portal protection missing");
    }
}
