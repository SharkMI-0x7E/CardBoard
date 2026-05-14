package org.cardboardpowered.mixin.world.entity.animal.cow;

import org.cardboardpowered.util.MixinInfo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.animal.cow.MushroomCow;
import net.minecraft.world.entity.animal.sheep.Sheep;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.bukkit.craftbukkit.event.CraftEventFactory;

@MixinInfo(events = {"PlayerShearEntityEvent"})
@Mixin(MushroomCow.class)
public class MushroomCowMixin {

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/animal/cow/MushroomCow;shear(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/sounds/SoundSource;Lnet/minecraft/world/item/ItemStack;)V"), method = "mobInteract", cancellable = true)
    public void doBukkitEvent_PlayerShearEntityEvent(Player player, InteractionHand hand, CallbackInfoReturnable<InteractionResult> ci) {
        ItemStack itemstack = player.getItemInHand(hand);
        if (!CraftEventFactory.handlePlayerShearEntityEvent(player, (Sheep)(Object)this, itemstack, hand)) {
            ci.setReturnValue(InteractionResult.PASS);
            return;
        }
    }

}