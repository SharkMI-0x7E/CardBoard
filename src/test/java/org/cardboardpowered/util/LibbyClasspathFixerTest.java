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
package org.cardboardpowered.util;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.bukkit.plugin.java.PluginClassLoader;
import org.junit.jupiter.api.Test;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

/**
 * Behavioural proof for {@link LibbyClasspathFixer}:
 * synthetic helpers whose constructor/addToClasspath are guaranteed to throw
 * become loadable and route the URL into {@link PluginLibraryHost}; plus
 * negative cases, idempotency and a conditional deep test against the real
 * Libby class shaded into the local Citizens jar.
 */
class LibbyClasspathFixerTest {

    private static final String[] RELOCATION_PREFIXES = {
        "fake/libby/classloader/URLClassLoaderHelper",
        "net/byteflux/libby/classloader/URLClassLoaderHelper",
        "clib/net/byteflux/libby/classloader/URLClassLoaderHelper"
    };

    /** Records every URL appended via the bridge method. */
    public static class TrackingLoader extends URLClassLoader implements PluginLibraryHost {
        final List<URL> added = Collections.synchronizedList(new ArrayList<>());

        TrackingLoader() {
            this(new URL[0]);
        }

        TrackingLoader(URL[] urls) {
            super(urls, LibbyClasspathFixerTest.class.getClassLoader());
        }

        @Override
        public void cardboard$addPluginLibrary(URL url) {
            added.add(url);
        }

        Class<?> define(String name, byte[] bytes) {
            return defineClass(name, bytes, 0, bytes.length);
        }
    }

    @Test
    void pluginClassLoaderImplementsBridge() {
        assertTrue(PluginLibraryHost.class.isAssignableFrom(PluginClassLoader.class));
    }

    @Test
    void rewrittenSyntheticHelperConstructsAndRoutesUrl() throws Exception {
        URL probe = new URL("file:/probe/relocated.jar");

        for (String internalName : RELOCATION_PREFIXES) {
            byte[] raw = brokenHelper(internalName);
            String dotted = internalName.replace('/', '.');

            // Control: without the rewrite, construction blows up. Raw and fixed need
            // separate loaders because they carry the same binary class name.
            TrackingLoader rawTracker = new TrackingLoader();
            Class<?> broken = rawTracker.define(dotted, raw);
            Constructor<?> brokenCtor = broken.getDeclaredConstructor(URLClassLoader.class, Object.class);
            InvocationTargetException ex = assertThrows(InvocationTargetException.class,
                    () -> brokenCtor.newInstance(rawTracker, null));
            assertTrue(ex.getCause() instanceof RuntimeException, "raw constructor must throw");

            // Rewritten: constructor survives (no escalation) and URL reaches the host.
            TrackingLoader fixedTracker = new TrackingLoader();
            byte[] fixed = LibbyClasspathFixer.apply(dotted, raw);
            assertNotSame(raw, fixed);
            Class<?> fixedClass = fixedTracker.define(dotted, fixed);
            Object helper = fixedClass.getDeclaredConstructor(URLClassLoader.class, Object.class)
                    .newInstance(fixedTracker, null);
            Method addToClasspath = fixedClass.getMethod("addToClasspath", URL.class);
            assertDoesNotThrow(() -> addToClasspath.invoke(helper, probe));
            assertTrue(fixedTracker.added.contains(probe),
                    "URL must be routed through PluginLibraryHost for " + internalName);
        }
    }

    @Test
    void nonFamilyClassesReturnedUntouched() {
        byte[] raw = brokenHelper("fake/other/NoSuffix");
        assertSame(raw, LibbyClasspathFixer.apply("fake.other.NoSuffix", raw));
        assertSame(raw, LibbyClasspathFixer.apply(null, raw));
        org.junit.jupiter.api.Assertions.assertNull(LibbyClasspathFixer.apply("fake.other.NoSuffix", null));

        // Correct simple name but missing the libby/classloader path segment.
        byte[] wrongSegment = brokenHelper("foo/URLClassLoaderHelper");
        assertSame(wrongSegment, LibbyClasspathFixer.apply("foo.URLClassLoaderHelper", wrongSegment));
    }

    @Test
    void familyNameButWrongStructureReturnsOriginalBytes() {
        // Family suffix, but no URLClassLoader instance field.
        byte[] noField = helperWithoutLoaderField("a/libby/classloader/URLClassLoaderHelper");
        assertSame(noField, LibbyClasspathFixer.apply("a.libby.classloader.URLClassLoaderHelper", noField));

        // Family suffix and field, but addToClasspath(URL) missing.
        byte[] noMethod = helperWithoutAddMethod("b/libby/classloader/URLClassLoaderHelper");
        assertSame(noMethod, LibbyClasspathFixer.apply("b.libby.classloader.URLClassLoaderHelper", noMethod));
    }

    @Test
    void rewriteIsIdempotent() {
        for (String internalName : RELOCATION_PREFIXES) {
            byte[] raw = brokenHelper(internalName);
            byte[] once = LibbyClasspathFixer.apply(internalName.replace('/', '.'), raw);
            byte[] twice = LibbyClasspathFixer.apply(internalName.replace('/', '.'), once);
            assertArrayEquals(once, twice, "second pass must be stable for " + internalName);
        }
    }

    @Test
    void realCitizensLibbyClassIsNeutralizedAndRouted() throws Exception {
        File jar = new File("Citizens2/Citizens-2.0.41-b4138.jar");
        org.junit.jupiter.api.Assumptions.assumeTrue(jar.isFile(),
                "Citizens test jar absent; deep test skipped");

        byte[] raw;
        try (ZipFile zip = new ZipFile(jar)) {
            ZipEntry entry = zip.getEntry("clib/net/byteflux/libby/classloader/URLClassLoaderHelper.class");
            assertNotNullEntry(entry);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            try (var in = zip.getInputStream(entry)) {
                in.transferTo(out);
            }
            raw = out.toByteArray();
        }

        String dotted = "clib.net.byteflux.libby.classloader.URLClassLoaderHelper";
        byte[] fixed = LibbyClasspathFixer.apply(dotted, raw);
        assertNotSame(raw, fixed);

        // Structural assertions on the two rewritten methods.
        ClassNode node = new ClassNode();
        new ClassReader(fixed).accept(node, 0);
        MethodNode ctor = null;
        MethodNode addUrl = null;
        for (MethodNode method : node.methods) {
            if ("<init>".equals(method.name)
                    && method.desc.startsWith("(Ljava/net/URLClassLoader;")) {
                ctor = method;
            }
            if ("addToClasspath".equals(method.name) && "(Ljava/net/URL;)V".equals(method.desc)) {
                addUrl = method;
            }
        }
        org.junit.jupiter.api.Assertions.assertNotNull(ctor);
        org.junit.jupiter.api.Assertions.assertNotNull(addUrl);

        String loaderField = singleUrlClassLoaderFieldName(node);
        assertTrue(containsFieldAccess(ctor, Opcodes.PUTFIELD, loaderField),
                "constructor must assign the loader field");
        assertTrue(containsInterfaceCall(addUrl),
                "addToClasspath(URL) must invoke PluginLibraryHost");
        assertNoEscalationSymbols(ctor);
        assertNoEscalationSymbols(addUrl);

        // Behavioural check against the real class. The jar is on this loader's URL
        // path so the untouched Libby neighbour classes (e.g. LibraryManager) resolve.
        TrackingLoader tracker = new TrackingLoader(new URL[]{jar.toURI().toURL()});
        Class<?> realFixed = tracker.define(dotted, fixed);
        Constructor<?> realCtor = null;
        for (Constructor<?> candidate : realFixed.getDeclaredConstructors()) {
            if (candidate.getParameterCount() >= 1
                    && candidate.getParameterTypes()[0] == URLClassLoader.class) {
                realCtor = candidate;
                break;
            }
        }
        org.junit.jupiter.api.Assertions.assertNotNull(realCtor);
        realCtor.setAccessible(true);
        Object[] initArgs = new Object[realCtor.getParameterCount()];
        initArgs[0] = tracker; // rewritten constructor ignores the remaining args
        Object helper = realCtor.newInstance(initArgs);
        URL probe = new URL("file:/probe/real-citizens.jar");
        realFixed.getMethod("addToClasspath", URL.class).invoke(helper, probe);
        assertTrue(tracker.added.contains(probe));
    }

    // ---- synthetic bytecode fixtures --------------------------------------

    private static byte[] brokenHelper(String internalName) {
        ClassWriter cw = new ClassWriter(0);
        cw.visit(Opcodes.V1_8, Opcodes.ACC_PUBLIC, internalName, null, "java/lang/Object", null);
        cw.visitField(Opcodes.ACC_PRIVATE | Opcodes.ACC_FINAL,
                "classLoader", "Ljava/net/URLClassLoader;", null, null).visitEnd();

        throwBody(cw, "<init>", "(Ljava/net/URLClassLoader;Ljava/lang/Object;)V", "ESCALATION");
        throwBody(cw, "addToClasspath", "(Ljava/net/URL;)V", "MH");
        cw.visitEnd();
        return cw.toByteArray();
    }

    private static void throwBody(ClassWriter cw, String name, String desc, String message) {
        org.objectweb.asm.MethodVisitor mv = cw.visitMethod(Opcodes.ACC_PUBLIC, name, desc, null, null);
        mv.visitCode();
        // Legal super() call first so the class passes bytecode verification;
        // the method still unconditionally throws immediately afterwards.
        if ("<init>".equals(name)) {
            mv.visitVarInsn(Opcodes.ALOAD, 0);
            mv.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
        }
        mv.visitTypeInsn(Opcodes.NEW, "java/lang/RuntimeException");
        mv.visitInsn(Opcodes.DUP);
        mv.visitLdcInsn(message);
        mv.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/RuntimeException",
                "<init>", "(Ljava/lang/String;)V", false);
        mv.visitInsn(Opcodes.ATHROW);
        mv.visitMaxs(3, name.equals("<init>") ? 3 : 2);
        mv.visitEnd();
    }

    private static byte[] helperWithoutLoaderField(String internalName) {
        ClassWriter cw = new ClassWriter(0);
        cw.visit(Opcodes.V1_8, Opcodes.ACC_PUBLIC, internalName, null, "java/lang/Object", null);
        cw.visitField(Opcodes.ACC_PRIVATE | Opcodes.ACC_FINAL,
                "classLoader", "Ljava/lang/Object;", null, null).visitEnd();
        throwBody(cw, "addToClasspath", "(Ljava/net/URL;)V", "MH");
        cw.visitEnd();
        return cw.toByteArray();
    }

    private static byte[] helperWithoutAddMethod(String internalName) {
        ClassWriter cw = new ClassWriter(0);
        cw.visit(Opcodes.V1_8, Opcodes.ACC_PUBLIC, internalName, null, "java/lang/Object", null);
        cw.visitField(Opcodes.ACC_PRIVATE | Opcodes.ACC_FINAL,
                "classLoader", "Ljava/net/URLClassLoader;", null, null).visitEnd();
        cw.visitEnd();
        return cw.toByteArray();
    }

    // ---- ASM structural helpers -------------------------------------------

    private static String singleUrlClassLoaderFieldName(ClassNode node) {
        String found = null;
        for (FieldNode field : node.fields) {
            if ((field.access & Opcodes.ACC_STATIC) == 0
                    && "Ljava/net/URLClassLoader;".equals(field.desc)) {
                assertFalse(found != null, "expected exactly one loader field");
                found = field.name;
            }
        }
        return found;
    }

    private static boolean containsFieldAccess(MethodNode method, int opcode, String fieldName) {
        for (AbstractInsnNode insn : method.instructions) {
            if (insn instanceof FieldInsnNode f && f.getOpcode() == opcode && f.name.equals(fieldName)) {
                return true;
            }
        }
        return false;
    }

    private static boolean containsInterfaceCall(MethodNode method) {
        for (AbstractInsnNode insn : method.instructions) {
            if (insn instanceof MethodInsnNode m
                    && m.getOpcode() == Opcodes.INVOKEINTERFACE
                    && "org/cardboardpowered/util/PluginLibraryHost".equals(m.owner)
                    && "cardboard$addPluginLibrary".equals(m.name)) {
                return true;
            }
        }
        return false;
    }

    private static void assertNoEscalationSymbols(MethodNode method) {
        for (AbstractInsnNode insn : method.instructions) {
            if (insn instanceof MethodInsnNode m) {
                String ref = m.owner + "#" + m.name;
                assertFalse(ref.contains("Unsafe") || ref.contains("redefineModule")
                        || ref.contains("bytebuddy") || ref.contains("addOpens"),
                        "escalation symbol leaked into rewritten method: " + ref);
            }
        }
    }

    private static void assertNotNullEntry(ZipEntry entry) {
        org.junit.jupiter.api.Assertions.assertNotNull(entry, "real Libby class entry missing");
    }
}
