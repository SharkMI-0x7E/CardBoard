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
