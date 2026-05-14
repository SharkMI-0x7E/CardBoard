/**
 * CardboardPowered - Bukkit/Spigot for Fabric
 * Copyright (C) CardboardPowered.org and contributors
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation; either 
 * version 3 of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package com.javazilla.bukkitfabric;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Objects;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.craftbukkit.CraftEquipmentSlot;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.util.CraftNamespacedKey;
import org.bukkit.entity.memory.MemoryKey;
import org.bukkit.inventory.EquipmentSlot;
import org.cardboardpowered.CardboardMod;
import org.cardboardpowered.impl.world.CraftWorld;
import org.cardboardpowered.bridge.world.level.LevelBridge;

import me.isaiah.common.cmixin.IMixinGlobalPos;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.level.Level;

public class Utils {

	@Deprecated
    public static EquipmentSlot getHand(InteractionHand h) {
        return CraftEquipmentSlot.getHand(h);
    }

    public static UUID getWorldUUID(File baseDir) {
        File file1 = new File(baseDir, "uid.dat");
        if (file1.exists()) {
            DataInputStream dis = null;
            try {
                dis = new DataInputStream(new FileInputStream(file1));
                return new UUID(dis.readLong(), dis.readLong());
            } catch (IOException ex) {
                CardboardMod.LOGGER.warning("Failed to read " + file1 + ", generating new random UUID. " + ex.getMessage());
            } finally { if (dis != null) try { dis.close(); } catch (IOException ex) {/*NOOP*/} }
        }
        UUID uuid = UUID.randomUUID();
        DataOutputStream dos = null;
        try {
            dos = new DataOutputStream(new FileOutputStream(file1));
            dos.writeLong(uuid.getMostSignificantBits());
            dos.writeLong(uuid.getLeastSignificantBits());
        } catch (IOException ex) {
            CardboardMod.LOGGER.warning("Failed to write " + file1.getAbsolutePath() + ", " + ex.getMessage());
        } finally { if (dos != null) try {dos.close();} catch (IOException ex) {/*NOOP*/} }
        return uuid;
    }

    @SuppressWarnings("unchecked")
    public static <T, U> MemoryModuleType<U> fromMemoryKey(MemoryKey<T> memoryKey) {
        return (MemoryModuleType<U>) BuiltInRegistries.MEMORY_MODULE_TYPE.getValue(CraftNamespacedKey.toMinecraft(memoryKey.getKey()));
    }

    public static <T, U> MemoryKey<?> toMemoryKey(MemoryModuleType<T> memoryModuleType) {
        return MemoryKey.getByKey(CraftNamespacedKey.fromMinecraft(BuiltInRegistries.MEMORY_MODULE_TYPE.getKey(memoryModuleType)));
    }

    public static Object fromNmsGlobalPos(Object object) {
        if (object instanceof GlobalPos) return fromNmsGlobalPos((GlobalPos) object);
        else if (object instanceof Long) return object;
        else if (object instanceof UUID) return object;
        else if (object instanceof Boolean) return object;
        throw new UnsupportedOperationException("Do not know how to map " + object);
    }

    public static Object toNmsGlobalPos(Object object) {
        if (object == null) return null;
        else if (object instanceof Location) return toNmsGlobalPos((Location) object);
        else if (object instanceof Long)     return object;
        else if (object instanceof UUID)     return object;
        else if (object instanceof Boolean)  return object;
        throw new UnsupportedOperationException("Do not know how to map " + object);
    }

    @SuppressWarnings("unchecked")
	public static Location fromNmsGlobalPos(GlobalPos globalPos) {
    	
    	IMixinGlobalPos ipos = (IMixinGlobalPos) (Object) globalPos;
    	
        return new org.bukkit.Location(((LevelBridge) Objects.requireNonNull(CraftServer.INSTANCE.getServer().getLevel((ResourceKey<Level>) ipos.IC$get_dimension()))).cardboard$getWorld(), ipos.IC$get_pos().getX(), ipos.IC$get_pos().getY(), ipos.IC$get_pos().getZ());
    }

    public static GlobalPos toNmsGlobalPos(Location location) {
        return GlobalPos.of(((CraftWorld) Objects.requireNonNull(location.getWorld())).getHandle().dimension(), BlockPos.containing(location.getX(), location.getY(), location.getZ()));
    }

}