package org.cardboardpowered.util;

import java.util.function.Consumer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.IntInsnNode;
import org.objectweb.asm.tree.LdcInsnNode;

public class MyPluginFixManager {

    public static byte[] injectPluginFix(String className, byte[] clazz) {
        //if (className.equals("com.onarandombox.MultiverseCore.utils.WorldManager")) {
        //    return patch(clazz, MultiverseCore::fix);
        //}
        Consumer<ClassNode> patcher = switch (className) {
            // case "com.sk89q.worldedit.bukkit.BukkitAdapter" -> WorldEdit::handleBukkitAdapter;
            case "com.sk89q.worldedit.bukkit.adapter.Refraction" -> WorldEdit::handlePickName;
            //case "com.sk89q.worldedit.bukkit.adapter.impl.v1_20_R3.PaperweightAdapter$SpigotWatchdog" -> WorldEdit::handleWatchdog;
            // case "com.earth2me.essentials.utils.VersionUtil" -> node -> helloWorld(node, 110, 109);
            // case "net.ess3.nms.refl.providers.ReflServerStateProvider" -> node -> helloWorld(node, "u", "U");
            case "net.Zrips.CMILib.Reflections" -> node -> helloWorld(node, "bR", "field_7512");
            default -> null;
        };
        return patcher == null ? clazz : patch(clazz, patcher);
    }

    private static byte[] patch(byte[] basicClass, Consumer<ClassNode> handler) {
        ClassNode node = new ClassNode();
        new ClassReader(basicClass).accept(node, 0);
        handler.accept(node);
        ClassWriter writer = new ClassWriter(0);
        node.accept(writer);
        return writer.toByteArray();
    }

    public static boolean isPaper() {
        return true;
    }

    private static void helloWorld(ClassNode node, String a, String b) {
        node.methods.forEach(method -> {
            for (AbstractInsnNode next : method.instructions) {
                if (next instanceof LdcInsnNode ldcInsnNode) {
                    if (ldcInsnNode.cst instanceof String str) {
                        if (a.equals(str)) {
                            ldcInsnNode.cst = b;
                        }
                    }
                }
            }
        });
    }

    private static void helloWorld(ClassNode node, int a, int b) {
        node.methods.forEach(method -> {
            for (AbstractInsnNode next : method.instructions) {
                if (next instanceof IntInsnNode ldcInsnNode) {
                    if (ldcInsnNode.operand == a) {
                        ldcInsnNode.operand = b;
                    }
                }
            }
        });
    }
}
