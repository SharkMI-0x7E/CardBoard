package org.cardboardpowered.mixin.world.level.storage.loot.predicates;

import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceWithEnchantedBonusCondition;
import org.cardboardpowered.bridge.world.level.storage.loot.parameters.LootContextParamsBridge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LootItemRandomChanceWithEnchantedBonusCondition.class)
public class LootItemRandomChanceWithEnchantedBonusConditionMixin {
    //@Final @Shadow private float chance;
    //@Final @Shadow private float lootingMultiplier;

	// Lnet/minecraft/loot/condition/RandomChanceWithEnchantedBonusLootCondition;enchantedChance:Lnet/minecraft/enchantment/EnchantmentLevelBasedValue;
	
    @Inject(at = @At("RETURN"), method = "test(Lnet/minecraft/world/level/storage/loot/LootContext;)Z", cancellable = true)
    public void cardboard_test(LootContext loottableinfo, CallbackInfoReturnable<Boolean> ci) {
        if (loottableinfo.hasParameter(LootContextParamsBridge.LOOTING_MOD)) {
            int i = loottableinfo.getOptionalParameter(LootContextParamsBridge.LOOTING_MOD);
            
            LootItemRandomChanceWithEnchantedBonusCondition thiz = (LootItemRandomChanceWithEnchantedBonusCondition) (Object) this;
            
            float f2 = i > 0 ? thiz.enchantedChance().calculate(i) : thiz.unenchantedChance();
            
            ci.setReturnValue(loottableinfo.getRandom().nextFloat() < f2);
            return;
        }
    }

}
