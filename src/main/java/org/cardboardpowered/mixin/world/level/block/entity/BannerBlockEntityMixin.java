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
package org.cardboardpowered.mixin.world.level.block.entity;

import net.minecraft.world.level.block.entity.BannerBlockEntity;
import net.minecraft.world.level.block.entity.BannerPatternLayers;
import org.cardboardpowered.bridge.world.level.block.entity.BannerBlockEntityBridge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(BannerBlockEntity.class)
public class BannerBlockEntityMixin implements BannerBlockEntityBridge {
    @Shadow
    private BannerPatternLayers patterns;

    // CraftBukkit start
    @Override
    public void cardboard$setPatterns(BannerPatternLayers bannerPatternLayers) {
        if (bannerPatternLayers.layers().size() > 20) {
            bannerPatternLayers = new BannerPatternLayers(java.util.List.copyOf(bannerPatternLayers.layers().subList(0, 20)));
        }
        this.patterns = bannerPatternLayers;
    }
    // CraftBukkit end
}
