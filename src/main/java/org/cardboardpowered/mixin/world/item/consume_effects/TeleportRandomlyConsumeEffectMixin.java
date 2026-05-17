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
package org.cardboardpowered.mixin.world.item.consume_effects;

import net.minecraft.world.item.consume_effects.TeleportRandomlyConsumeEffect;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(value = TeleportRandomlyConsumeEffect.class, priority = 900)
public class TeleportRandomlyConsumeEffectMixin {

    // TODO: This Mixin requires @Overwrite to fire Bukkit PlayerTeleportEvent.
    // The commented code below shows the implementation that intercepts chorus fruit
    // teleportation and fires PlayerTeleportEvent with cancellation support.
    // Re-enable when ready: uncomment and add @Overwrite annotation.

    /*
    @Overwrite
    public ItemStack finishUsing(ItemStack itemstack, World world, LivingEntity entity) {
        ItemStack itemstack1 = super.finishUsing(itemstack, world, entity);
        if (world.isClient) return itemstack1;

        for (int i = 0; i < 16; ++i) {
            double d3 = entity.getX() + (entity.getRandom().nextDouble() - 0.5D) * 16.0D;
            double d4 = MathHelper.clamp(entity.getY() + (double) (entity.getRandom().nextInt(16) - 8), 0.0D, (double) (world.getHeight() - 1));
            double d5 = entity.getZ() + (entity.getRandom().nextDouble() - 0.5D) * 16.0D;

            if (entity instanceof ServerPlayerEntity) {
                Player player = (Player)((IMixinServerEntityPlayer)((ServerPlayerEntity) entity)).getBukkitEntity();
                PlayerTeleportEvent teleEvent = new PlayerTeleportEvent(player, player.getLocation(), new Location(player.getWorld(), d3, d4, d5), PlayerTeleportEvent.TeleportCause.CHORUS_FRUIT);
                Bukkit.getServer().getPluginManager().callEvent(teleEvent);
                if (teleEvent.isCancelled()) break;
                d3 = teleEvent.getTo().getX();
                d4 = teleEvent.getTo().getY();
                d5 = teleEvent.getTo().getZ();
            }

            if (entity.hasVehicle()) entity.stopRiding();

            if (entity.teleport(d3, d4, d5, true)) {
                world.playSound((PlayerEntity) null, entity.getX(), entity.getY(), entity.getZ(), SoundEvents.ITEM_CHORUS_FRUIT_TELEPORT, SoundCategory.MASTER, 1.0F, 1.0F);
                entity.playSound(SoundEvents.ITEM_CHORUS_FRUIT_TELEPORT, 1.0F, 1.0F);
                break;
            }
        }
        if (entity instanceof PlayerEntity) ((PlayerEntity) entity).getItemCooldownManager().set(itemstack, 20);

        return itemstack1;
    }
    */

}
