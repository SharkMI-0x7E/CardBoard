package org.cardboardpowered.bridge.world.entity.projectile.throwableitemprojectile;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public interface ThrowableItemProjectileBridge {

    Item getDefaultItemPublic();

    @Deprecated
    ItemStack getItemBF();

}
