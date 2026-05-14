/**
 * This file belongs to Cardboard.
 * Copyright (c) 2021 Cardboard Contributors
 */
package org.cardboardpowered.mixin.world.entity.ai.goal;

import org.cardboardpowered.util.MixinInfo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.BreakDoorGoal;
import net.minecraft.world.entity.ai.goal.DoorInteractGoal;
import org.bukkit.craftbukkit.event.CraftEventFactory;

@MixinInfo(events = {"EntityBreakDoorEvent"})
@Mixin(BreakDoorGoal.class)
public class BreakDoorGoalMixin extends DoorInteractGoal {

    public BreakDoorGoalMixin(Mob mob) {
        super(mob);
    }

    /**
     * Implements EntityBreakDoorEvent
     * 
     * @see {@link CraftEventFactory#callEntityBreakDoorEvent(Entity, BlockPos)}
     */
    @Inject(at = @At(value = "INVOKE", 
                     target = "Lnet/minecraft/world/level/Level;removeBlock(Lnet/minecraft/core/BlockPos;Z)Z"), 
            method = "tick", cancellable = true)
    public void cardboard_doEntityBreakDoorEvent(CallbackInfo ci) {
        if (CraftEventFactory.callEntityBreakDoorEvent(this.mob, this.doorPos).isCancelled()) {
            this.start();
            ci.cancel();
            return;
        }
    }

    @Shadow
    public void start() {}

}