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
