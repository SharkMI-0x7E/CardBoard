/**
 * Cardboard - Spigot/Paper for Fabric
 * Copyright (C) 2020-2024 CardboardPowered.org and contributors
 * Copyright (C) 2025-2026 SharkMI and contributors
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
package org.cardboardpowered.bridge.world.entity.monster.zombie;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ConversionParams;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.entity.monster.zombie.ZombieVillager;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.level.block.LevelEvent;
import org.jspecify.annotations.Nullable;

public interface ZombieBridge {
    public static @Nullable ZombieVillager convertVillagerToZombieVillager(ServerLevel level, Villager villager, net.minecraft.core.BlockPos blockPosition, boolean silent, org.bukkit.event.entity.EntityTransformEvent.TransformReason transformReason, org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason creatureSpawnReason) {
        ZombieVillager zombieVillager = villager.convertTo(EntityType.ZOMBIE_VILLAGER, ConversionParams.single(villager, true, true), mob -> {
            mob.finalizeSpawn(level, level.getCurrentDifficultyAt(mob.blockPosition()), EntitySpawnReason.CONVERSION, new Zombie.ZombieGroupData(false, true));
            mob.setVillagerData(villager.getVillagerData());
            mob.setGossips(villager.getGossips().copy());
            mob.setTradeOffers(villager.getOffers().copy());
            mob.setVillagerXp(villager.getVillagerXp());
            // CraftBukkit start
            if (!silent) {
                level.levelEvent(null, LevelEvent.SOUND_ZOMBIE_INFECTED, blockPosition, 0);
            }
        });//, transformReason, creatureSpawnReason); // TODO
        return zombieVillager;
        // CraftBukkit end
    }
}
