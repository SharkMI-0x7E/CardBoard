/**
 * Copyright (C) 2026 SharkMI and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 */
package org.cardboardpowered.mixin.world.entity.decoration;

import org.bukkit.craftbukkit.event.CraftEventFactory;
import org.cardboardpowered.util.MixinInfo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import java.util.Iterator;
import java.util.List;
import net.minecraft.network.protocol.game.ClientboundSetEntityLinkPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity.RemovalReason;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.decoration.LeashFenceKnotEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;

@MixinInfo(events = {"PlayerLeashEntityEvent", "PlayerUnleashEntityEvent"})
@Mixin(value = LeashFenceKnotEntity.class, priority = 900)
public class LeashFenceKnotEntityMixin {

    private LeashFenceKnotEntity getBF() {
        return (LeashFenceKnotEntity)(Object)this;
    }

    /**
     * @author Cardboard mod
     * @reason PlayerLeashEntityEvent
     *
     * TODO: Cannot replace with @Inject - this @Overwrite completely rewrites the
     * interact method to fire PlayerLeashEntityEvent and PlayerUnleashEntityEvent
     * with cancellation support. The original method handles both leashing and
     * unleashing logic with event checks that cannot be split into separate
     * injection points.
     */
    @Overwrite
    public InteractionResult interact(Player entityhuman, InteractionHand enumhand) {
        if (getBF().level().isClientSide()) return InteractionResult.SUCCESS;

        boolean flag = false;
        List<Mob> list = getBF().level().getEntitiesOfClass(Mob.class, new AABB(getBF().getX() - 7.0D, getBF().getY() - 7.0D, getBF().getZ() - 7.0D, getBF().getX() + 7.0D, getBF().getY() + 7.0D, getBF().getZ() + 7.0D));
        Iterator<Mob> iterator = list.iterator();
        Mob entityinsentient;
        while (iterator.hasNext()) {
            entityinsentient = (Mob) iterator.next();
            if (entityinsentient.getLeashHolder() == entityhuman) {
                if (CraftEventFactory.callPlayerLeashEntityEvent(entityinsentient, ((LeashFenceKnotEntity)(Object)this), entityhuman).isCancelled()) {
                    ((ServerPlayer) entityhuman).connection.send(new ClientboundSetEntityLinkPacket(entityinsentient, entityinsentient.getLeashHolder()));
                    continue;
                }
                entityinsentient.setLeashedTo((LeashFenceKnotEntity)(Object)this, true);
                flag = true;
            }
        }
        if (flag) return InteractionResult.CONSUME;
        boolean die = true;
        iterator = list.iterator();
        while (iterator.hasNext()) {
            entityinsentient = (Mob) iterator.next();
            if (entityinsentient.isLeashed() && entityinsentient.getLeashHolder() == getBF()) {
                if (CraftEventFactory.callPlayerUnleashEntityEvent(entityinsentient, entityhuman).isCancelled()) {
                    die = false;
                    continue;
                }
                // entityinsentient.detachLeash(true, !entityhuman.getAbilities().creativeMode);
                entityinsentient.dropLeash();
            }
        }
        if (die) getBF().remove(RemovalReason.KILLED);
        return InteractionResult.CONSUME;
    }

}
