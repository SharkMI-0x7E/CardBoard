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
package org.cardboardpowered.bridge.world.level.block.entity;

import net.minecraft.world.level.block.entity.SignText;

/**
 * Bridge interface for {@link net.minecraft.world.level.block.entity.SignBlockEntity}.
 *
 * Exposes the SignText object itself (not the raw internal final array) so callers
 * can mutate it via the immutable {@code setMessage(int, Component)} API.
 *
 * @see org.cardboardpowered.mixin.world.level.block.entity.SignBlockEntityMixin
 */
public interface SignBlockEntityBridge {

    /**
     * Returns the front face text of the sign.
     */
    SignText cardboard$getFrontText();

    /**
     * Returns the back face text of the sign.
     */
    SignText cardboard$getBackText();

    /**
     * Note: bukkit adds method.
     */
    boolean cardboard$isFacingFrontText(double x, double z);

}
