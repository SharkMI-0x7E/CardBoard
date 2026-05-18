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
package org.cardboardpowered.mixin.world.item.component;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.component.CustomData;
import org.cardboardpowered.bridge.world.item.component.CustomDataBridge;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(CustomData.class)
public class CustomDataMixin implements CustomDataBridge {
    @Shadow
    @Final
    private CompoundTag tag;

    // Paper start - expose unsafe internal compound tag for read only access
    @Deprecated
    @Override
    public CompoundTag cardboard$getUnsafe() {
        return this.tag;
    }

    @Override
    public boolean cardboard$contains(String key) {
        return this.tag.contains(key);
    }
    // Paper end - expose unsafe internal compound tag for read only access
}
