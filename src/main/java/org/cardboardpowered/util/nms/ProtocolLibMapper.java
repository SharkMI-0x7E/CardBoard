/**
 * Cardboard - Spigot/Paper for Fabric
 * Copyright (C) 2020-2024 CardboardPowered.org and contributors
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
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.cardboardpowered.util.nms;

import com.google.common.base.Joiner;
import io.netty.channel.ChannelHandler;

/**
 * @see https://github.com/dmulloy2/ProtocolLib/issues/1146
 * @see https://github.com/CardboardPowered/cardboard/issues/82
 * @see https://github.com/CardboardPowered/cardboard/issues/120
 * @see https://github.com/CardboardPowered/cardboard/issues/173
 */
public class ProtocolLibMapper {

    /**
     * spigot=PacketDecompressor, intermediary=class_2532, yarn=PacketInflater
     * spigot=PacketCompressor,   intermediary=class_2534, yarn=PacketDeflater
     *
     * @see https://github.com/Mohist-Community/Mohist/commit/efe7cbfd7b8d36c4f55bed9f16bedea2797dfa9f
     * @see com.comphenix.protocol.injector.netty.ChannelInjector (Line 423; ProtocolLib v4.6.0)
     */
	public static boolean guessCompression(ChannelHandler handler) {
		String className = handler != null ? handler.getClass().getCanonicalName() : "";
        String[] names = {
            "Inflater", "Deflater",      // Yarn
            "class_2532", "class_2534",  // Intermediary
            "Compressor", "Decompressor" // Spigot
        };
		for (String name : names) {
            if (className.contains(name)) {
                return true;
            }
        }
        return false;
	}

    /**
     * ProtocolLib Reflection
     */
    public static Class<?> getCraftBukkitClass(String className) {
        try {
            return Class.forName(ReflectionRemapper.mapClassName("org.bukkit.craftbukkit." + className));
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Failed to find CraftBukkit class!!!!: " + className);
        }
    }

    /**
     * ProtocolLib Reflection
     */
    public static Class<?> getMinecraftClass(String className) {
        try {
            if (className.equals("ServerConnection")) {
                return Class.forName(ReflectionRemapper.mapClassName("net.minecraft.server.ServerNetworkIo"));
            }
            return Class.forName(ReflectionRemapper.mapClassName("net.minecraft." + className));
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Failed to find Minecraft class!!!!: " + className);
        }
    }

    /**
     * ProtocolLib Reflection
     */
    public static Class<?> getMinecraftClass(String className, String... aliases) {
        try {
            // Try the main class first
            return getMinecraftClass(className);
        } catch (RuntimeException e) {
            Class<?> success = null;

            // Try every alias too
            for (String alias : aliases) {
                try {
                    success = getMinecraftClass(alias);
                    break;
                } catch (RuntimeException e1) {
                }
            }

            if (success != null) {
                return success;
            } else {
                throw new RuntimeException(String.format("Unable to find " + className + " (%s)", Joiner.on(", ").join(aliases)));
            }
        }
    }

}