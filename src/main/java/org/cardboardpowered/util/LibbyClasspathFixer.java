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

import java.util.ArrayList;
import java.util.List;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TypeInsnNode;
import org.objectweb.asm.tree.VarInsnNode;

/**
 * Bytecode family rewriter for the Libby runtime-library loader
 * ({@code net.byteflux.libby.classloader.URLClassLoaderHelper}, under any
 * relocation prefix - e.g. Citizens shades it to {@code clib.net.byteflux...}).
 *
 * <p>Why this exists: on Java 9+ the helper escalates through a three-rung
 * ladder to force {@code URLClassLoader#addURL} open:
 * reflective {@code setAccessible} (needs {@code --add-opens}), then
 * {@code sun.misc.Unsafe} theft of {@code IMPL_LOOKUP}, then a runtime download
 * of byte-buddy-agent for dynamic self-attach via
 * {@code Instrumentation.redefineModule}. The last rung needs network and the
 * Attach API, which do not exist on Android/Termux, and dynamic agents are being
 * locked down by the JDK itself.
 *
 * <p>Cardboard's plugin loaders already extend {@code URLClassLoader} and expose
 * {@link PluginLibraryHost}, so none of that escalation is necessary. This
 * rewriter:
 * <ol>
 *   <li>replaces the helper constructor with a plain {@code super()} call,
 *       removing the whole escalation ladder; and</li>
 *   <li>replaces {@code addToClasspath(URL)} with a direct interface call on the
 *       loader held in the helper's {@code URLClassLoader} field.</li>
 * </ol>
 *
 * <p>Recognition is purely structural (class internal name suffix plus the
 * expected field/method shapes), never based on plugin or jar names, so every
 * Libby user benefits regardless of relocation. The {@code addToClasspath(Path)}
 * overload is left untouched: it delegates to the URL overload and therefore
 * follows the rewrite automatically.
 *
 * <p>Fail-safe contract: any structural mismatch or processing error returns
 * the input bytes unchanged, so a class is never blocked from loading.
 *
 * <p>Runtime precondition: the rewritten addToClasspath checkcasts the helper's
 * loader field to {@link PluginLibraryHost}. The rewrite-time fallback only
 * covers unknown bytecode shapes; a plugin that makes Libby use its own
 * URLClassLoader (not the Bukkit plugin loader) would hit a ClassCastException
 * at call time. The standard BukkitLibraryManager path passes the plugin's own
 * classloader, which always implements the host interface.
 */
public final class LibbyClasspathFixer {

    private static final String FAMILY_SUFFIX = "libby/classloader/URLClassLoaderHelper";
    private static final String URL_CLASSLOADER = "java/net/URLClassLoader";
    private static final String URL_DESC = "Ljava/net/URL;";
    private static final String HOST_INTERNAL = "org/cardboardpowered/util/PluginLibraryHost";
    private static final String HOST_METHOD = "cardboard$addPluginLibrary";
    private static final String HOST_METHOD_DESC = "(Ljava/net/URL;)V";

    private LibbyClasspathFixer() {
    }

    public static byte[] apply(String className, byte[] bytes) {
        if (className == null || bytes == null) {
            return bytes;
        }
        String internalName = className.replace('.', '/');
        if (!internalName.endsWith(FAMILY_SUFFIX)) {
            return bytes;
        }
        try {
            ClassReader reader = new ClassReader(bytes);
            ClassNode node = new ClassNode();
            reader.accept(node, ClassReader.EXPAND_FRAMES);

            String loaderField = findLoaderField(node);
            MethodNode ctor = findUrlClassLoaderConstructor(node);
            MethodNode addUrl = findMethod(node, "addToClasspath", "(Ljava/net/URL;)V");
            if (loaderField == null || ctor == null || addUrl == null) {
                // Unknown family variant - keep the original escalation logic
                // rather than risk unverifiable bytecode.
                return bytes;
            }

            rewriteConstructor(ctor, node.name, loaderField);
            rewriteAddToClasspath(addUrl, node.name, loaderField);

            ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
            node.accept(writer);
            return writer.toByteArray();
        } catch (Throwable t) {
            return bytes;
        }
    }

    // Exactly one instance field of type URLClassLoader is required; this is how
    // the target loader is located without hardcoding the (relocatable) field name.
    private static String findLoaderField(ClassNode node) {
        List<String> matches = new ArrayList<>();
        for (FieldNode field : node.fields) {
            boolean isInstance = (field.access & Opcodes.ACC_STATIC) == 0;
            Type fieldType = Type.getType(field.desc);
            if (isInstance && fieldType.getSort() == Type.OBJECT
                    && URL_CLASSLOADER.equals(fieldType.getInternalName())) {
                matches.add(field.name);
            }
        }
        return matches.size() == 1 ? matches.get(0) : null;
    }

    private static MethodNode findUrlClassLoaderConstructor(ClassNode node) {
        for (MethodNode method : node.methods) {
            if ("<init>".equals(method.name)) {
                Type[] args = Type.getArgumentTypes(method.desc);
                if (args.length >= 1 && URL_CLASSLOADER.equals(args[0].getInternalName())) {
                    return method;
                }
            }
        }
        return null;
    }

    private static MethodNode findMethod(ClassNode node, String name, String desc) {
        for (MethodNode method : node.methods) {
            if (name.equals(method.name) && desc.equals(method.desc)) {
                return method;
            }
        }
        return null;
    }

    private static void clearBody(MethodNode method) {
        method.instructions.clear();
        // Old handlers/locals reference labels that no longer exist.
        method.tryCatchBlocks.clear();
        if (method.localVariables != null) {
            method.localVariables.clear();
        }
        if (method.visibleLocalVariableAnnotations != null) {
            method.visibleLocalVariableAnnotations.clear();
        }
        if (method.invisibleLocalVariableAnnotations != null) {
            method.invisibleLocalVariableAnnotations.clear();
        }
    }

    private static void rewriteConstructor(MethodNode ctor, String ownerClass, String loaderField) {
        clearBody(ctor);
        ctor.instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
        ctor.instructions.add(new MethodInsnNode(
                Opcodes.INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false));
        // Store the supplied URLClassLoader (arg 0) in the helper's field, preserving
        // the original constructor contract. It is the plugin loader itself, which
        // implements PluginLibraryHost. The field is final, and a putfield in the
        // constructor is the legal place to assign it.
        ctor.instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
        ctor.instructions.add(new VarInsnNode(Opcodes.ALOAD, 1));
        ctor.instructions.add(new FieldInsnNode(
                Opcodes.PUTFIELD, ownerClass, loaderField, "L" + URL_CLASSLOADER + ";"));
        ctor.instructions.add(new InsnNode(Opcodes.RETURN));
    }

    private static void rewriteAddToClasspath(MethodNode method, String ownerClass, String loaderField) {
        clearBody(method);
        // host = (PluginLibraryHost) this.<loaderField>;
        // host.cardboard$addPluginLibrary(url);
        method.instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
        method.instructions.add(new FieldInsnNode(
                Opcodes.GETFIELD, ownerClass, loaderField, "L" + URL_CLASSLOADER + ";"));
        method.instructions.add(new TypeInsnNode(Opcodes.CHECKCAST, HOST_INTERNAL));
        method.instructions.add(new VarInsnNode(Opcodes.ALOAD, 1));
        method.instructions.add(new MethodInsnNode(
                Opcodes.INVOKEINTERFACE, HOST_INTERNAL, HOST_METHOD, HOST_METHOD_DESC, true));
        method.instructions.add(new InsnNode(Opcodes.RETURN));
    }
}
