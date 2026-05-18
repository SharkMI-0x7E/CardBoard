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
package org.cardboardpowered.mixin.world.item;

import org.bukkit.craftbukkit.event.CraftEventFactory;
import org.cardboardpowered.bridge.world.entity.EntityBridge;
import org.cardboardpowered.bridge.server.level.ServerPlayerBridge;
import org.bukkit.Bukkit;
import org.bukkit.block.BlockFace;
import org.bukkit.craftbukkit.block.CraftBlock;
import org.bukkit.entity.Hanging;
import org.bukkit.entity.Player;
import org.bukkit.event.hanging.HangingPlaceEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.cardboardpowered.util.MixinInfo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import java.util.Iterator;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.decoration.LeashFenceKnotEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.LeadItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

@MixinInfo(events = {"HangingPlaceEvent"})
@Mixin(value = LeadItem.class, priority = 900)
public class LeadItemMixin extends Item {

    public LeadItemMixin(net.minecraft.world.item.Item.Properties settings) {
        super(settings);
    }

    /**
     * @author Cardboard
     * @reason Fire HangingPlaceEvent and PlayerLeashEntityEvent
     *
     * TODO: Cannot replace with @Inject - this @Overwrite completely rewrites the
     * bindPlayerMobs method to fire HangingPlaceEvent when creating a leash knot
     * and PlayerLeashEntityEvent for each mob being leashed, with cancellation
     * support for both events.
     */
    @Overwrite
    public static InteractionResult bindPlayerMobs(net.minecraft.world.entity.player.Player player, Level world, BlockPos pos) {
        LeashFenceKnotEntity leashKnotEntity = null;
        boolean bl = false;
        double d = 7.0;
        int i = pos.getX();
        int j = pos.getY();
        int k = pos.getZ();
        List<Mob> list = world.getEntitiesOfClass(Mob.class, new AABB((double)i - 7.0, (double)j - 7.0, (double)k - 7.0, (double)i + 7.0, (double)j + 7.0, (double)k + 7.0));
        Iterator var11 = list.iterator();

        while(var11.hasNext()) {
            Mob mobEntity = (Mob)var11.next();
            if (mobEntity.getLeashHolder() == player) {
                if (leashKnotEntity == null) {
                    leashKnotEntity = LeashFenceKnotEntity.getOrCreateKnot(world, pos);

                    HangingPlaceEvent event = new HangingPlaceEvent((Hanging) ((EntityBridge) leashKnotEntity).getBukkitEntity(), player != null ? (Player) ((ServerPlayerBridge) player).getBukkitEntity() : null, CraftBlock.at((ServerLevel) world, pos), BlockFace.SELF, EquipmentSlot.HAND);
                    Bukkit.getPluginManager().callEvent(event);

                    if (event.isCancelled()) {
                        leashKnotEntity.discard();
                        return InteractionResult.PASS;
                    }
                    leashKnotEntity.playPlacementSound();
                }
                if (CraftEventFactory.callPlayerLeashEntityEvent(mobEntity, leashKnotEntity, player).isCancelled()) {
                    continue;
                }
                mobEntity.setLeashedTo(leashKnotEntity, true);
                bl = true;
            }
        }

        return bl ? InteractionResult.SUCCESS : InteractionResult.PASS;
    }
}
