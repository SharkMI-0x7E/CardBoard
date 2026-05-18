/**
 * Cardboard - Spigot/Paper for Fabric
 * Copyright (C) 2020-2024 CardboardPowered.org and contributors
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
package org.cardboardpowered.bridge.world.item.enchantment;

import net.minecraft.core.component.DataComponentType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.ItemEnchantments;

import java.util.function.Consumer;

public interface EnchantmentHelperBridge {
    public static ItemEnchantments updateEnchantments(ItemStack stack, Consumer<ItemEnchantments.Mutable> updater, final boolean createComponentIfMissing) {
        DataComponentType<ItemEnchantments> componentType = EnchantmentHelper.getComponentType(stack);
        ItemEnchantments itemEnchantments = createComponentIfMissing ? stack.getOrDefault(componentType, ItemEnchantments.EMPTY) : stack.get(componentType); // Paper - allowing updating enchantments on items without component
        if (itemEnchantments == null) {
            return ItemEnchantments.EMPTY;
        } else {
            ItemEnchantments.Mutable mutable = new ItemEnchantments.Mutable(itemEnchantments);
            updater.accept(mutable);
            ItemEnchantments itemEnchantments1 = mutable.toImmutable();
            stack.set(componentType, itemEnchantments1);
            return itemEnchantments1;
        }
    }
}
