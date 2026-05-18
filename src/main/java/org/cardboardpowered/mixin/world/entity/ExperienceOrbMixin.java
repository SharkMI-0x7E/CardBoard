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
package org.cardboardpowered.mixin.world.entity;

import java.util.Optional;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantedItemInUse;
import net.minecraft.world.item.enchantment.EnchantmentEffectComponents;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import org.bukkit.event.player.PlayerExpCooldownChangeEvent.ChangeReason;
import org.cardboardpowered.bridge.world.entity.EntityBridge;
import org.cardboardpowered.bridge.server.level.ServerPlayerBridge;
import org.bukkit.event.player.PlayerItemMendEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.destroystokyo.paper.event.player.PlayerPickupExperienceEvent;

import org.bukkit.craftbukkit.event.CraftEventFactory;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.Player;

@Mixin(value = net.minecraft.world.entity.ExperienceOrb.class, priority = 900)
public class ExperienceOrbMixin extends EntityMixin {

	private static final ThreadLocal<net.minecraft.world.entity.player.Player> currentPlayer = new ThreadLocal<>();

	@Inject(method = "playerTouch", at = @At("HEAD"))
	private void capturePlayer(net.minecraft.world.entity.player.Player player, CallbackInfo ci) {
		currentPlayer.set(player);
	}
	
	@Inject(method = "playerTouch", at = @At("RETURN"))
	private void clearPlayer(net.minecraft.world.entity.player.Player player, CallbackInfo ci) {
		currentPlayer.remove();
	}
	
	@Shadow
	public int count = 1;

	@Inject(at = @At("HEAD"), method = "playerTouch", cancellable = true)
	public void cardboard$check_PlayerPickupExperienceEvent(net.minecraft.world.entity.player.Player player, CallbackInfo ci) {
		if (player instanceof ServerPlayer serverPlayer
				&& player.takeXpDelay == 0
				&& new PlayerPickupExperienceEvent(
						(Player) ((ServerPlayerBridge)serverPlayer).getBukkitEntity(),
						(ExperienceOrb) ((EntityBridge) ((net.minecraft.world.entity.ExperienceOrb) (Object) this)).getBukkitEntity()
					).callEvent()) {
			// Continue
		} else {
			ci.cancel();
			return;
		}
	}

	@ModifyVariable(
			method = "playerTouch",
			at = @At(
					value = "STORE"
					),
			ordinal = 0
			)
	private int cardboard$modifyPickupDelay_callPlayerXpCooldownEvent(int original) {
		net.minecraft.world.entity.player.Player player = currentPlayer.get();
		return CraftEventFactory.callPlayerXpCooldownEvent(player, original, ChangeReason.PICKUP_ORB).getNewCooldown();
	}

	@ModifyArg(
			method = "playerTouch",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/world/entity/player/Player;giveExperiencePoints(I)V"
					),
			index = 0
			)
	private int modifyExperienceAmount(int original) {
		net.minecraft.world.entity.player.Player player = currentPlayer.get();
		if (player == null) return original;

		return CraftEventFactory.callPlayerExpChangeEvent(
				player,
				(net.minecraft.world.entity.ExperienceOrb)(Object)this,
				original
				).getAmount();
	}

    @Redirect(at = @At(value = "INVOKE", target = "Ljava/lang/Math;min(II)I"), method = "repairPlayerItems")
    public int doBukkitEvent_PlayerItemMendEvent(int a, int b, ServerPlayer entityhuman) {
        
        Optional<EnchantedItemInUse> optional = EnchantmentHelper.getRandomItemWith(EnchantmentEffectComponents.REPAIR_WITH_XP, entityhuman, ItemStack::isDamaged);

        ItemStack itemstack = optional.get().itemStack();
        EquipmentSlot slot = optional.get().inSlot();

        int i = Math.min(a, b);
        PlayerItemMendEvent event = CraftEventFactory.callPlayerItemMendEvent(entityhuman, (net.minecraft.world.entity.ExperienceOrb)(Object)this, itemstack, i);
        i = event.getRepairAmount();
        if (!event.isCancelled()) {
            return i;
        } else return 0;
    }

    /*
    @Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/ExperienceOrbEntity;repairPlayerGears(Lnet/minecraft/server/network/ServerPlayerEntity;I)I"), method = "onPlayerCollision")
    public int doBukkitEvent_PlayerExpChangeEvent(ExperienceOrbEntity e, ServerPlayerEntity plr, int a) {
        return repairPlayerGears((ServerPlayerEntity) plr, CraftEventFactory.callPlayerExpChangeEvent(plr, (ExperienceOrbEntity)(Object)this).getAmount());
    }
    */

    @Shadow
    private int repairPlayerItems(ServerPlayer player, int amount) {
        return 0;
    }
}