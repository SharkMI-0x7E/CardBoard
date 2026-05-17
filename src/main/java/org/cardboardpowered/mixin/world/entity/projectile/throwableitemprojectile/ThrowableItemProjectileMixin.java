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
package org.cardboardpowered.mixin.world.entity.projectile.throwableitemprojectile;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.cardboardpowered.bridge.world.entity.projectile.throwableitemprojectile.ThrowableItemProjectileBridge;

@Mixin(ThrowableItemProjectile.class)
public abstract class ThrowableItemProjectileMixin implements ThrowableItemProjectileBridge {

    @Shadow
    public abstract Item getDefaultItem();

    @Override
    public Item getDefaultItemPublic() {
        return getDefaultItem();
    }

    @Override
    @Deprecated
    public ItemStack getItemBF() {
        return ((ThrowableItemProjectile) (Object) this).getItem();
    }

}