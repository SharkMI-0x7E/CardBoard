/**
 * Cardboard - The Bukkit for Fabric Project
 * Copyright (C) 2020-2025 Cardboard contributors
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