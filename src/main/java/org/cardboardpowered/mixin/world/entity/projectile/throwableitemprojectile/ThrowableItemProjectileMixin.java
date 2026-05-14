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