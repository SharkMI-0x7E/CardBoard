package org.cardboardpowered.mixin.world.entity.ai.behavior;

import org.cardboardpowered.util.MixinInfo;
import org.spongepowered.asm.mixin.Mixin;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.behavior.StopAttackingIfTargetInvalid;

@MixinInfo(events = {"EntityTargetEvent", "EntityTargetLivingEntityEvent"})
@Mixin(StopAttackingIfTargetInvalid.class)
public class StopAttackingIfTargetInvalidMixin<E extends Mob> {

    // TODO 1.19.4
	
	/*@Inject(at = @At("HEAD"), method = "forgetAttackTarget", cancellable = true)
    public void callTargetEvent(E e0, CallbackInfo ci) {
        LivingEntity old = e0.getBrain().getOptionalMemory(MemoryModuleType.ATTACK_TARGET).orElse(null);
        EntityTargetEvent event = CraftEventFactory.callEntityTargetLivingEvent(e0, old, (old != null && !old.isAlive()) ? EntityTargetEvent.TargetReason.TARGET_DIED : EntityTargetEvent.TargetReason.FORGOT_TARGET);
        if (event.isCancelled()) return;

        if (event.getTarget() != null) {
            e0.getBrain().remember(MemoryModuleType.ATTACK_TARGET, ((LivingEntityImpl) event.getTarget()).getHandle());
            ci.cancel();
            return;
        }
        e0.getBrain().forget(MemoryModuleType.ATTACK_TARGET);
    }*/

}