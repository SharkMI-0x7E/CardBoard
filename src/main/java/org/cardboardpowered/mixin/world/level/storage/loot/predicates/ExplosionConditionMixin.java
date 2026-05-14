package org.cardboardpowered.mixin.world.level.storage.loot.predicates;

import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.ExplosionCondition;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ExplosionCondition.class)
public class ExplosionConditionMixin {

    @Inject(at = @At("HEAD"), method = "test(Lnet/minecraft/world/level/storage/loot/LootContext;)Z", cancellable = true)
    public void cardboard_test(LootContext loottableinfo, CallbackInfoReturnable<Boolean> ci) {
        Float ofloat = loottableinfo.getOptionalParameter(LootContextParams.EXPLOSION_RADIUS);
        if (ofloat == null) {
            ci.setReturnValue(true);
            return;
        }
        ci.setReturnValue(loottableinfo.getRandom().nextFloat() < (1.0F / ofloat));
        return;
    }

}
