package org.cardboardpowered.mixin.world.item;

import net.minecraft.world.item.SnowballItem;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(value = SnowballItem.class, priority = 900)
public class SnowballItemMixin {
    // @Overwrite removed - this method was a pure copy of vanilla Minecraft logic
    // with no Bukkit events triggered. No longer needed.
}
