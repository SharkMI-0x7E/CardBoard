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
