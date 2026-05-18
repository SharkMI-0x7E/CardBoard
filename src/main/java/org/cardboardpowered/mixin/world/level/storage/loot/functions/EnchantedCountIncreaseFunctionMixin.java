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
package org.cardboardpowered.mixin.world.level.storage.loot.functions;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.EnchantedCountIncreaseFunction;

@Mixin(EnchantedCountIncreaseFunction.class)
public class EnchantedCountIncreaseFunctionMixin {

    @Shadow
    public int limit;

   // @Shadow
    //public UniformLootTableRange countRange;

    //@Overwrite
    public ItemStack process_TODO(ItemStack itemstack, LootContext loottableinfo) {
        /*Entity entity = (Entity) loottableinfo.get(LootContextParameters.KILLER_ENTITY);
        if (entity instanceof LivingEntity) {
            int i = EnchantmentHelper.getLooting((LivingEntity) entity);
            if (loottableinfo.hasParameter(IMixinLootContextParameters.LOOTING_MOD))
                i = loottableinfo.get(IMixinLootContextParameters.LOOTING_MOD);
            if (i <= 0) return itemstack; // CraftBukkit - account for possible negative looting values
            float f = (float) i * this.countRange.nextFloat(loottableinfo.getRandom());
            itemstack.increment(Math.round(f));
            if ((this.limit > 0) && itemstack.getCount() > this.limit) itemstack.setCount(this.limit);
        }
        return itemstack;*/return null; // TODO 1.17ify
    }

}