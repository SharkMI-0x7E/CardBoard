/**
 * CardboardPowered - Bukkit/Spigot for Fabric
 * Copyright (C) CardboardPowered.org and contributors
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation; either 
 * version 3 of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.cardboardpowered.mixin.advancements;

import net.minecraft.advancements.AdvancementHolder;
import org.bukkit.craftbukkit.advancement.CraftAdvancement;
import org.cardboardpowered.bridge.advancements.AdvancementHolderBridge;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(AdvancementHolder.class)
public class AdvancementHolderMixin implements AdvancementHolderBridge {

    // @Unique
    private final CraftAdvancement bukkit = new CraftAdvancement((AdvancementHolder)(Object)this);

    @Override
    public CraftAdvancement getBukkitAdvancement() {
        return bukkit;
    }
    
    @Override
    public final org.bukkit.advancement.Advancement toBukkit() {
    	return bukkit;
    }

}
