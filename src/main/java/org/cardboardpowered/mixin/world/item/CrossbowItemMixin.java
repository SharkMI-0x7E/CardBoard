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
package org.cardboardpowered.mixin.world.item;

import net.minecraft.world.item.CrossbowItem;
import org.cardboardpowered.util.MixinInfo;
import org.spongepowered.asm.mixin.Mixin;

@MixinInfo(events = {"EntityShootBowEvent"})
@Mixin(value = CrossbowItem.class, priority = 900)
public class CrossbowItemMixin {

    // private static transient boolean bukkitCapturedBoolean;

    // Note: EntityShootBowEvent in CrossbowItem super class now
    
    // TODO: io.papermc.paper.event.entity.EntityLoadCrossbowEvent
    
    /*
    @Inject(method = "shoot", cancellable = true, locals = LocalCapture.CAPTURE_FAILHARD, at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;damage(ILnet/minecraft/entity/LivingEntity;Ljava/util/function/Consumer;)V"))
    private static void bukkitShoot(World world, LivingEntity shooter, Hand hand, ItemStack crossbow, ItemStack projectile, float soundPitch, boolean creative, float speed, float divergence, float simulated, CallbackInfo ci, boolean bl, ProjectileEntity projectileEntity) {
        EntityShootBowEvent event = CraftEventFactory.callEntityShootBowEvent(shooter, crossbow, projectile, projectileEntity, shooter.getActiveHand(), soundPitch, true);
        if (event.isCancelled()) {
            event.getProjectile().remove();
            ci.cancel();
        }
        bukkitCapturedBoolean = event.getProjectile() == ((IMixinEntity) projectileEntity).getBukkitEntity();
    }

    @Redirect(method = "shoot", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;spawnEntity(Lnet/minecraft/entity/Entity;)Z"))
    private static boolean bukkitSpawnEntity(World world, Entity entityIn, World worldIn, LivingEntity shooter) {
        if (bukkitCapturedBoolean) {
            if (!world.spawnEntity(entityIn)) {
                if (shooter instanceof ServerPlayerEntity) {
                    ((IMixinServerEntityPlayer) shooter).getBukkit().updateInventory();
                }
                bukkitCapturedBoolean = true;
            } else {
                bukkitCapturedBoolean = false;
            }
        }
        return true;
    }

    @Inject(method = "shoot", cancellable = true, at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;playSound(Lnet/minecraft/entity/player/PlayerEntity;DDDLnet/minecraft/sound/SoundEvent;Lnet/minecraft/sound/SoundCategory;FF)V"))
    private static void bukkitReturnIfFail(World worldIn, LivingEntity shooter, Hand handIn, ItemStack crossbow, ItemStack projectile, float soundPitch, boolean isCreativeMode, float velocity, float inaccuracy, float projectileAngle, CallbackInfo ci) {
        if (bukkitCapturedBoolean) {
            ci.cancel();
        }
        bukkitCapturedBoolean = false;
    }*/
    
    
}
