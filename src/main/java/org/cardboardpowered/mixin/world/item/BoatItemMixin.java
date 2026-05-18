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
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.cardboardpowered.mixin.world.item;

import org.bukkit.craftbukkit.event.CraftEventFactory;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.cardboardpowered.util.MixinInfo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BoatItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

@MixinInfo(events = {"PlayerInteractEvent"})
@Mixin(BoatItem.class)
/**
 * Intercepts boat item placement to fire Bukkit's {@link PlayerInteractEvent}.
 *
 * <p>When a player right-clicks with a boat item, this mixin intercepts
 * the {@code use} method at HEAD, fires the Bukkit event, and cancels
 * the vanilla action if the event is cancelled.</p>
 *
 * <p><b>Compatibility:</b> This was originally an {@code @Overwrite} that conflicted
 * with carpet-tis-addition. It has been refactored to use {@code @Inject(at="HEAD", cancellable=true)}
 * for conflict-free coexistence.</p>
 *
 * @see PlayerInteractEvent
 * @since 1.21.11
 */
public abstract class BoatItemMixin extends Item {

    public BoatItemMixin(net.minecraft.world.item.Item.Properties settings) {
        super(settings);
    }

    /**
     * Intercepts boat item use to fire {@link PlayerInteractEvent}.
     *
     * <p>Called before vanilla boat placement logic. If the Bukkit event
     * is cancelled by a plugin, the method returns {@link InteractionResult#PASS}
     * to prevent boat placement.</p>
     *
     * @param world  the world the player is in
     * @param user   the player using the boat item
     * @param hand   the hand holding the boat item
     * @param cir    callback to cancel the vanilla method
     */
    @Inject(method = "use", at = @At("HEAD"), cancellable = true)
    private void cardboard$onPlayerInteract(Level world, Player user, InteractionHand hand, CallbackInfoReturnable<InteractionResult> cir) {
        ItemStack itemstack = user.getItemInHand(hand);
        BlockHitResult movingobjectpositionblock = BoatItem.getPlayerPOVHitResult(world, user, ClipContext.Fluid.ANY);

        if (movingobjectpositionblock.getType() != HitResult.Type.BLOCK) {
            return;
        }

        PlayerInteractEvent event = CraftEventFactory.callPlayerInteractEvent(
            (ServerPlayer) user,
            Action.RIGHT_CLICK_BLOCK,
            movingobjectpositionblock.getBlockPos(),
            movingobjectpositionblock.getDirection(),
            itemstack,
            false,
            hand
        );

        if (event.isCancelled()) {
            cir.setReturnValue(InteractionResult.PASS);
        }
    }
}
