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
package org.cardboardpowered.mixin.world.level.block;

import net.minecraft.world.level.block.NetherPortalBlock;
import org.cardboardpowered.util.MixinInfo;
import org.spongepowered.asm.mixin.Mixin;

/**
 * 1.21.10 Note: now uses iCommonLib API
 * I forgot about this in 1.21.9
 */
@MixinInfo(events = {"EntityPortalEnterEvent"})
@Mixin(NetherPortalBlock.class)
@Deprecated
public class NetherPortalBlockMixin {

	/*
    @Inject(at = @At("HEAD"), method = "onEntityCollision")
    public void callBukkitEvent(BlockState state, World world, BlockPos pos, Entity entity, EntityCollisionHandler ech, CallbackInfo ci) {
        if (!entity.hasVehicle() && !entity.hasPassengers() && entity.canUsePortals(true)) {
            EntityPortalEnterEvent event = new EntityPortalEnterEvent(((IMixinEntity)entity).getBukkitEntity(), new org.bukkit.Location(((IMixinWorld)world).getCraftWorld(), pos.getX(), pos.getY(), pos.getZ()));
            Bukkit.getPluginManager().callEvent(event);
        }
    }
    */

}