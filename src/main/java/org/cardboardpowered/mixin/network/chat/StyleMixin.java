/**
 * Cardboard - Spigot/Paper for Fabric
 * Copyright (C) 2020-2026 CardboardPowered.org and contributors
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
package org.cardboardpowered.mixin.network.chat;

import net.minecraft.network.chat.Style;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(Style.class)
public class StyleMixin {

    @Shadow
    public Boolean bold;

    public Style setStrikethrough(Boolean obool) {
        Style st = ((Style)(Object)this).withBold(this.bold);
        st.strikethrough = obool;
        return st;
    }

    public Style setUnderline(Boolean obool) {
        return ((Style)(Object)this).withBold(this.bold).withUnderlined(obool);
    }

    public Style setRandom(Boolean obool) {
        Style st = ((Style)(Object)this).withBold(this.bold);
        st.obfuscated = obool;
        return st;
    }

}