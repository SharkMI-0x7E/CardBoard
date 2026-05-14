package org.cardboardpowered.mixin.world.entity.ai.behavior;

import org.bukkit.craftbukkit.event.CraftEventFactory;
import org.bukkit.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.Optional;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.behavior.VillagerMakeLove;
import net.minecraft.world.entity.npc.villager.Villager;

@Mixin(VillagerMakeLove.class)
public class VillagerMakeLoveMixin {

    @Redirect(method = "breed", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/npc/villager/Villager;setAge(I)V",
            ordinal = 0))
    private void moveDownSetAge0(Villager instance, int i) {}

    @Redirect(method = "breed", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/npc/villager/Villager;setAge(I)V",
            ordinal = 1))
    private void moveDownSetAge1(Villager instance, int i) {}

    @Inject(method = "breed", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/server/level/ServerLevel;addFreshEntityWithPassengers(Lnet/minecraft/world/entity/Entity;)V"),
            locals = LocalCapture.CAPTURE_FAILHARD, cancellable = true)
    private void breadEvent(ServerLevel world, Villager parent,
                            Villager partner, CallbackInfoReturnable<Optional<Villager>> cir,
                            Villager villagerEntity) {
        // CraftBukkit start - call EntityBreedEvent
        if (org.bukkit.craftbukkit.event.CraftEventFactory.callEntityBreedEvent(villagerEntity, parent, partner, null, null, 0).isCancelled()) {
            cir.setReturnValue(Optional.empty());
        }
        parent.setAge(6000);
        partner.setAge(6000);
        // CraftBukkit end - call EntityBreedEvent
    }
}
