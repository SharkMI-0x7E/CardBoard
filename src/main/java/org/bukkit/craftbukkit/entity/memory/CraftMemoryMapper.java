package org.bukkit.craftbukkit.entity.memory;

import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.craftbukkit.CraftServer;

import org.cardboardpowered.bridge.world.level.LevelBridge;

import me.isaiah.common.cmixin.IMixinGlobalPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.resources.ResourceKey;

public final class CraftMemoryMapper {
    private CraftMemoryMapper() {
    }

    public static Object fromNms(Object object) {
        if (object instanceof GlobalPos) {
            return CraftMemoryMapper.fromNms((GlobalPos)object);
        }
        if (object instanceof Long) {
            return (Long)object;
        }
        if (object instanceof UUID) {
            return (UUID)object;
        }
        if (object instanceof Boolean) {
            return (Boolean)object;
        }
        if (object instanceof Integer) {
            return (Integer)object;
        }
        throw new UnsupportedOperationException("Do not know how to map " + object);
    }

    public static Object toNms(Object object) {
        if (object == null) {
            return null;
        }
        if (object instanceof Location) {
            return CraftMemoryMapper.toNms((Location)object);
        }
        if (object instanceof Long) {
            return (Long)object;
        }
        if (object instanceof UUID) {
            return (UUID)object;
        }
        if (object instanceof Boolean) {
            return (Boolean)object;
        }
        if (object instanceof Integer) {
            return (Integer)object;
        }
        throw new UnsupportedOperationException("Do not know how to map " + object);
    }

    @SuppressWarnings("unchecked")
	public static Location fromNms(GlobalPos globalPos) {
    	IMixinGlobalPos ipos = (IMixinGlobalPos) (Object) globalPos;
        return new Location((World) ((LevelBridge)((CraftServer)Bukkit.getServer()).getServer().getLevel((ResourceKey<net.minecraft.world.level.Level>) ipos.IC$get_dimension())).cardboard$getWorld(), (double)ipos.IC$get_pos().getX(), (double)ipos.IC$get_pos().getY(), (double)ipos.IC$get_pos().getZ());
    }

   // public static GlobalPos toNms(Location location) {
   //     return GlobalPos.create(((CraftWorld)location.getWorld()).getHandle().getRegistryKey(), BlockPos.ofFloored(location.getX(), location.getY(), location.getZ()));
   // }
}

