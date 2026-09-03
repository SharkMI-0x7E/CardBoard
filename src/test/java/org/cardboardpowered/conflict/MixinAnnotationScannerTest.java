/*
 * Cardboard - Spigot/Paper for Fabric
 * Copyright (C) 2020-2026 CardboardPowered.org and contributors
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 */
package org.cardboardpowered.conflict;

import org.cardboardpowered.conflict.model.MixinClassInfo;
import org.junit.jupiter.api.Test;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Regression test for [014]: after Fabric Loom remapping, @Mixin lives in
 * RuntimeInvisibleAnnotations. MixinAnnotationScanner must scan both visible
 * and invisible annotations, or every mixin class reports isMixin()==false.
 */
class MixinAnnotationScannerTest {

    private final MixinAnnotationScanner scanner = new MixinAnnotationScanner();

    @Test
    void testInvisibleMixinAnnotationIsDetected() {
        byte[] bytes = buildMixinClass(false);
        MixinClassInfo info = scanner.analyzeClass(bytes, "test-mod", "test.jar");

        assertNotNull(info);
        assertTrue(info.isMixin(), "@Mixin in RuntimeInvisibleAnnotations must be detected");
        assertTrue(info.getTargetClasses().contains("net.minecraft.class_1234"),
                "Target class should be parsed, got: " + info.getTargetClasses());
    }

    @Test
    void testVisibleMixinAnnotationStillDetected() {
        byte[] bytes = buildMixinClass(true);
        MixinClassInfo info = scanner.analyzeClass(bytes, "test-mod", "test.jar");

        assertNotNull(info);
        assertTrue(info.isMixin(), "@Mixin in RuntimeVisibleAnnotations must be detected");
        assertTrue(info.getTargetClasses().contains("net.minecraft.class_1234"));
    }

    @Test
    void testInvisibleOverwriteAnnotationIsDetected() {
        byte[] bytes = buildClassWithInvisibleOverwrite(false);
        MixinClassInfo info = scanner.analyzeClass(bytes, "test-mod", "test.jar");

        assertNotNull(info);
        assertFalse(info.getOverwrites().isEmpty(),
                "@Overwrite in RuntimeInvisibleAnnotations must be detected");
        assertEquals("method_1", info.getOverwrites().get(0).getTargetMethods().get(0));
    }

    @Test
    void testNonMixinClassIsNotFlaggedAsMixin() {
        ClassWriter cw = new ClassWriter(0);
        cw.visit(Opcodes.V21, Opcodes.ACC_PUBLIC, "test/plain/Helper", null, "java/lang/Object", null);
        cw.visitEnd();

        MixinClassInfo info = scanner.analyzeClass(cw.toByteArray(), "test-mod", "test.jar");
        assertNotNull(info);
        assertFalse(info.isMixin());
        assertTrue(info.getTargetClasses().isEmpty());
    }

    private byte[] buildMixinClass(boolean visible) {
        ClassWriter cw = new ClassWriter(0);
        cw.visit(Opcodes.V21, Opcodes.ACC_PUBLIC, "test/mixin/TestMixin", null, "java/lang/Object", null);

        AnnotationVisitor av = cw.visitAnnotation("Lorg/spongepowered/asm/mixin/Mixin;", visible);
        av.visit("value", Type.getObjectType("net/minecraft/class_1234"));
        av.visitEnd();

        cw.visitEnd();
        return cw.toByteArray();
    }

    private byte[] buildClassWithInvisibleOverwrite(boolean visible) {
        ClassWriter cw = new ClassWriter(0);
        cw.visit(Opcodes.V21, Opcodes.ACC_PUBLIC, "test/mixin/OverwriteMixin", null, "java/lang/Object", null);

        AnnotationVisitor classAv = cw.visitAnnotation("Lorg/spongepowered/asm/mixin/Mixin;", false);
        classAv.visit("value", Type.getObjectType("net/minecraft/class_5678"));
        classAv.visitEnd();

        MethodVisitor mv = cw.visitMethod(Opcodes.ACC_PRIVATE, "cardboard$overwriteTester", "()V", null, null);
        AnnotationVisitor methodAv = mv.visitAnnotation("Lorg/spongepowered/asm/mixin/Overwrite;", visible);
        methodAv.visit("method", "method_1");
        methodAv.visitEnd();
        mv.visitEnd();

        cw.visitEnd();
        return cw.toByteArray();
    }
}