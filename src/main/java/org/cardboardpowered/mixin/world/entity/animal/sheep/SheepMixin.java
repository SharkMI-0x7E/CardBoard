package org.cardboardpowered.mixin.world.entity.animal.sheep;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.animal.sheep.Sheep;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.bukkit.craftbukkit.event.CraftEventFactory;
import org.cardboardpowered.bridge.world.entity.EntityBridge;

@Mixin(Sheep.class)
public class SheepMixin {

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/animal/sheep/Sheep;shear(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/sounds/SoundSource;Lnet/minecraft/world/item/ItemStack;)V") , method = "mobInteract", cancellable = true)
    public void doBukkitEvent_PlayerShearEntityEvent(Player player, InteractionHand hand, CallbackInfoReturnable<InteractionResult> ci) {
        ItemStack itemstack = player.getItemInHand(hand);
        if (!CraftEventFactory.handlePlayerShearEntityEvent(player, (Sheep)(Object)this, itemstack, hand)) {
            ci.setReturnValue(InteractionResult.PASS);
            return;
        }
    }
    
    // Lnet/minecraft/entity/Shearable;sheared(Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/sound/SoundCategory;Lnet/minecraft/item/ItemStack;)V

    @Inject(at = @At("HEAD"),
    		method = "shear(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/sounds/SoundSource;Lnet/minecraft/world/item/ItemStack;)V")
    public void cardboardForceDrops_START(ServerLevel world, SoundSource a, ItemStack stack, CallbackInfo ci) {
        ((EntityBridge)(Object)this).cardboard_setForceDrops(true);
    }

    @Inject(at = @At("TAIL"),
    		method = "shear(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/sounds/SoundSource;Lnet/minecraft/world/item/ItemStack;)V")
    public void cardboardForceDrops_END(ServerLevel world, SoundSource a, ItemStack stack, CallbackInfo ci) {
        ((EntityBridge)(Object)this).cardboard_setForceDrops(false);
    }

}