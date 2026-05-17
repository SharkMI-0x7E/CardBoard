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
package org.cardboardpowered.mixin.world.item;

import net.minecraft.world.item.EnderpearlItem;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(value = EnderpearlItem.class, priority = 900)
public class EnderpearlItemMixin {
    // @Overwrite removed - this method was a pure copy of vanilla Minecraft logic
    // with no Bukkit events triggered. No longer needed.
}
