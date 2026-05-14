package org.cardboardpowered.mixin.world.item;

import net.minecraft.world.item.EnderpearlItem;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(value = EnderpearlItem.class, priority = 900)
public class EnderpearlItemMixin {
    // @Overwrite removed - this method was a pure copy of vanilla Minecraft logic
    // with no Bukkit events triggered. No longer needed.
}
