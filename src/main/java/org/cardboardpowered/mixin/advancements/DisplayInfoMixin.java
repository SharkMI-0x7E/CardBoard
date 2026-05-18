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
package org.cardboardpowered.mixin.advancements;

import io.papermc.paper.advancement.AdvancementDisplay;
import net.minecraft.advancements.DisplayInfo;
import org.cardboardpowered.bridge.advancements.DisplayInfoBridge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(DisplayInfo.class)
public class DisplayInfoMixin implements DisplayInfoBridge {
    @Unique
    public final io.papermc.paper.advancement.AdvancementDisplay paper = new io.papermc.paper.advancement.PaperAdvancementDisplay((DisplayInfo) (Object) this); // Paper - Add more advancement API

    @Override
    public AdvancementDisplay cardboard$getPaper() {
        return this.paper;
    }
}