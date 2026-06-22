/*
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
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */
package org.cardboardpowered.mixin.world.entity.ai.behavior;

import org.bukkit.event.entity.VillagerCareerChangeEvent;
import org.bukkit.event.entity.VillagerCareerChangeEvent.ChangeReason;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import net.minecraft.world.entity.ai.behavior.BehaviorControl;
import net.minecraft.world.entity.ai.behavior.ResetProfession;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.npc.villager.VillagerData;
import net.minecraft.world.entity.npc.villager.VillagerProfession;
import org.bukkit.craftbukkit.event.CraftEventFactory;
import org.bukkit.craftbukkit.entity.CraftVillager;
import org.cardboardpowered.util.MixinInfo;

@MixinInfo(events = {"VillagerCareerChangeEvent"})
@Mixin(value = ResetProfession.class, priority = 900)
public class ResetProfessionMixin {

    /**
     * @reason Fire VillagerCareerChangeEvent
     * @author cardboard
     *
     * TODO: Cannot replace with @Inject - this @Overwrite completely replaces the
     * create() method to fire VillagerCareerChangeEvent when a villager loses their
     * job site. It allows event cancellation and profession modification, which
     * requires rewriting the entire behavior logic rather than just injecting at
     * a specific point.
     */
    @Overwrite
    public static BehaviorControl<Villager> create() {
        return BehaviorBuilder.create(
           context -> context.group(context.absent(MemoryModuleType.JOB_SITE))
              .apply(
                 context,
                 jobSite -> (world, entity, time) -> {
                    VillagerData villagerData = entity.getVillagerData();
                    boolean flag = !villagerData.profession().is(VillagerProfession.NONE)
                       && !villagerData.profession().is(VillagerProfession.NITWIT);
                    if (flag && entity.getVillagerXp() == 0 && villagerData.level() <= 1) {
                       VillagerCareerChangeEvent event = CraftEventFactory.callVillagerCareerChangeEvent(
                          entity,
                          CraftVillager.CraftProfession.minecraftHolderToBukkit(world.registryAccess().getOrThrow(VillagerProfession.NONE)),
                          ChangeReason.LOSING_JOB
                       );
                       if (event.isCancelled()) {
                          return false;
                       } else {
                          entity.setVillagerData(
                             entity.getVillagerData().withProfession(CraftVillager.CraftProfession.bukkitToMinecraftHolder(event.getProfession()))
                          );
                          entity.refreshBrain(world);
                          return true;
                       }
                    } else {
                       return false;
                    }
                 }
              )
        );
     }

}