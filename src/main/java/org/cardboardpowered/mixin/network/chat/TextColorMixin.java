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
package org.cardboardpowered.mixin.network.chat;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.TextColor;
import org.cardboardpowered.bridge.network.chat.TextColorBridge;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TextColor.class)
public class TextColorMixin implements TextColorBridge {
    @Nullable
    @Unique
    public ChatFormatting format;

    @Inject(method = "<init>(ILjava/lang/String;)V", at = @At("RETURN"))
    private void initFormat(int value, String name, CallbackInfo ci) {
        if (name != null) {
            this.format = ChatFormatting.getByName(name);
        } else {
            this.format = null;
        }
    }

    @Inject(method = "<init>(I)V", at = @At("RETURN"))
    private void initFormat(int value, CallbackInfo ci) {
        this.format = null;
    }

    @Override
    public @Nullable ChatFormatting cardboard$getFormat() {
        return format;
    }
}