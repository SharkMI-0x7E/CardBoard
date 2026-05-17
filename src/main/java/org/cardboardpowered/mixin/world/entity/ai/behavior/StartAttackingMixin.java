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
package org.cardboardpowered.mixin.world.entity.ai.behavior;

import org.spongepowered.asm.mixin.Mixin;

import net.minecraft.world.entity.ai.behavior.StartAttacking;

@Mixin(StartAttacking.class)
public class StartAttackingMixin {
   
   // TODO: Fix 1.20.4 merge conflict
   
//<<<<<<< HEAD
 //   @Inject(method = "method_47123", at = @At(value = "INVOKE",
  //          target = "Lnet/minecraft/entity/ai/brain/MemoryQueryResult;remember(Ljava/lang/Object;)V"),
//=======
	// Lnet/minecraft/entity/ai/brain/task/UpdateAttackTargetTask;method_47123(Ljava/util/function/Predicate;Ljava/util/function/Function;Lnet/minecraft/entity/ai/brain/MemoryQueryResult;Lnet/minecraft/entity/ai/brain/MemoryQueryResult;Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/entity/mob/MobEntity;J)Z
	
    /*@Inject(method = "method_47123", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/entity/ai/behavior/declarative/MemoryAccessor;set(Ljava/lang/Object;)V"),
//>>>>>>> upstream/ver/1.20
            locals = LocalCapture.CAPTURE_FAILHARD, cancellable = true)
    private static <E extends MobEntity> void banner$targetEvent(Predicate<E> predicate, Function<E, Optional<? extends LivingEntity>> function,
                                           MemoryQueryResult memoryAccessor, MemoryQueryResult memoryAccessor2,
                                           ServerWorld serverLevel, MobEntity mob, long l, CallbackInfoReturnable<Boolean> cir,
                                           Optional optional, LivingEntity livingEntity) {
        // CraftBukkit start
        EntityTargetEvent event = CraftEventFactory.callEntityTargetLivingEvent(mob, livingEntity, (livingEntity instanceof ServerPlayerEntity) ? EntityTargetEvent.TargetReason.CLOSEST_PLAYER : EntityTargetEvent.TargetReason.CLOSEST_ENTITY);
        if (event.isCancelled()) {
            cir.setReturnValue(false);
        }
        if (event.getTarget() == null) {
            memoryAccessor.forget();
            cir.setReturnValue(true);
        }
        livingEntity = ((LivingEntityImpl) event.getTarget()).getHandle();
        // CraftBukkit end
    }*/

}
