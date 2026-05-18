/**
 * Cardboard - Spigot/Paper for Fabric
 * Copyright (C) 2020-2024 CardboardPowered.org and contributors
 * Copyright (C) 2025-2026 SharkMI and contributors
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
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
