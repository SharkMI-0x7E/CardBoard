/**
 * CardboardPowered - Bukkit/Spigot for Fabric
 * Copyright (C) CardboardPowered.org and contributors
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation; either 
 * version 3 of the License, or (at your option) any later version.
 */
package org.cardboardpowered.mixin.world.item.enchantment;

import net.minecraft.world.item.enchantment.Enchantments;
import org.spongepowered.asm.mixin.Mixin;

/**
 * Mixin for frost walker enchantment water freezing logic.
 *
 * TODO: The original @Overwrite of freezeWater fires CraftEventFactory.handleBlockFormEvent
 * (BlockFormEvent). This requires @Overwrite because the event cancellation must prevent
 * the entire water freezing loop. Cannot be refactored to @Inject without duplicating
 * the full method body.
 *
 * @author CardboardPowered
 */
@Mixin(Enchantments.class)
public class EnchantmentsMixin {

}
