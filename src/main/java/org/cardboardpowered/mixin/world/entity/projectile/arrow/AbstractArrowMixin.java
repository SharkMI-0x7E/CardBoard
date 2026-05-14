package org.cardboardpowered.mixin.world.entity.projectile.arrow;

import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow.Pickup;
import net.minecraft.world.item.ItemStack;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.event.player.PlayerPickupArrowEvent;
import org.bukkit.craftbukkit.entity.CraftItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import org.cardboardpowered.bridge.world.entity.EntityBridge;
import org.cardboardpowered.bridge.world.entity.projectile.arrow.AbstractArrowBridge;

@Mixin(AbstractArrow.class)
public abstract class AbstractArrowMixin implements AbstractArrowBridge {

    // @Shadow public boolean inGround;
    @Shadow public int life;
    // @Shadow public int punch;
    @Shadow public AbstractArrow.Pickup pickup;

    @Override
    public int getPunchBF() {
        // API Removed
    	return 0;
    }

    @Override
    public boolean getInGroundBF() {
        return isInGround();
    }

    @Override
    public void setLifeBF(int value) {
        this.life = value;
    }
    
    @Shadow
    public boolean isInGround() {
        return false; // Shadowed
    }

    private AbstractArrow getBF() {
        return (AbstractArrow)(Object)this;
    }

    @SuppressWarnings("deprecation")
    @Inject(at = @At("HEAD"), method = "playerTouch", cancellable = true)
    public void doBukkitEvent_PlayerPickupArrowEvent(Player entityhuman, CallbackInfo ci) {
        if (!getBF().level().isClientSide() && (this.isInGround() || getBF().isNoPhysics()) && getBF().shakeTime <= 0) {
            ItemStack itemstack = this.getPickupItem();
            if (this.pickup == Pickup.ALLOWED && !itemstack.isEmpty()) {
                ItemEntity item = new ItemEntity(getBF().level(), getBF().getX(), getBF().getY(), getBF().getZ(), itemstack);
                PlayerPickupArrowEvent event = new PlayerPickupArrowEvent((org.bukkit.entity.Player) ((EntityBridge)entityhuman).getBukkitEntity(), new CraftItem(CraftServer.INSTANCE, item), (org.bukkit.entity.AbstractArrow) ((EntityBridge)this).getBukkitEntity());
                Bukkit.getServer().getPluginManager().callEvent(event);

                if (event.isCancelled()) {
                    ci.cancel();
                    return;
                }
                itemstack = item.getItem();
            }
            boolean flag = this.pickup == AbstractArrow.Pickup.ALLOWED || this.pickup == AbstractArrow.Pickup.CREATIVE_ONLY && entityhuman.getAbilities().instabuild || getBF().isNoPhysics() && getBF().getOwner().getUUID() == entityhuman.getUUID();
            if (!flag) {
                ci.cancel();
                return;
            }
        }
    }

    @Shadow
    public abstract ItemStack getPickupItem();

}
