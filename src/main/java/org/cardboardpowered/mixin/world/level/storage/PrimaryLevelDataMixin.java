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
package org.cardboardpowered.mixin.world.level.storage;

import com.mojang.serialization.Dynamic;
import com.mojang.serialization.Lifecycle;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelSettings;
import net.minecraft.world.level.levelgen.WorldOptions;
import net.minecraft.world.level.storage.PrimaryLevelData;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.event.weather.ThunderChangeEvent;
import org.bukkit.event.weather.WeatherChangeEvent;
import org.cardboardpowered.bridge.world.level.storage.PrimaryLevelDataBridge;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

// @MixinInfo(events = {"ThunderChangeEvent","WeatherChangeEvent"})
@Mixin(PrimaryLevelData.class)
public class PrimaryLevelDataMixin implements PrimaryLevelDataBridge {

    @Shadow
    private LevelSettings settings;

    @Unique
    private static final String PAPER_RESPAWN_DIMENSION = "paperSpawnDimension"; // Paper
    @Unique
    public net.minecraft.resources.ResourceKey<net.minecraft.world.level.Level> respawnDimension = net.minecraft.world.level.Level.OVERWORLD; // Paper

    @Inject(at = @At("HEAD"), method = "setThundering")
    public void thunder(boolean flag, CallbackInfo info) {
        PrimaryLevelData p = (PrimaryLevelData)(Object) this;
        if (p.isThundering() == flag)
            return;
        World world = Bukkit.getWorld(p.getLevelName());
        if (world != null) {
            ThunderChangeEvent thunder = new ThunderChangeEvent(world, flag);
            Bukkit.getServer().getPluginManager().callEvent(thunder);
            if (thunder.isCancelled())
                return;
        }
    }

    @Inject(at = @At("HEAD"), method = "setRaining")
    public void rain(boolean flag, CallbackInfo info) {
        PrimaryLevelData p = (PrimaryLevelData)(Object) this;
        if (p.isRaining() == flag)
            return;
        World world = Bukkit.getWorld(p.getLevelName());
        if (world != null) {
            WeatherChangeEvent event = new WeatherChangeEvent(world, flag);
            Bukkit.getServer().getPluginManager().callEvent(event);
            if (event.isCancelled())
                return;
        }
    }

    @Override
    public void checkName(String name) {
    	if (!this.settings.levelName.equals(name)) {
    		this.settings.levelName = name;
    	}
    }

    @Override
    public ResourceKey<Level> cardboard$getRespawnDimension() {
        return respawnDimension;
    }

    @Override
    public void cardboard$setRespawnDimension(ResourceKey<Level> respawnDimension) {
        this.respawnDimension = respawnDimension;
    }

    @Inject(method = "parse", at = @At("RETURN"))
    private static <T> void parsePaper(Dynamic<T> dynamic, LevelSettings levelSettings, PrimaryLevelData.SpecialWorldProperty specialWorldProperty, WorldOptions worldOptions, Lifecycle lifecycle, CallbackInfoReturnable<PrimaryLevelData> cir) {
        ((PrimaryLevelDataBridge)cir.getReturnValue()).cardboard$setRespawnDimension(dynamic.get(PAPER_RESPAWN_DIMENSION)
                .read(net.minecraft.world.level.Level.RESOURCE_KEY_CODEC)
                .result()
                .orElse(cir.getReturnValue().getRespawnData().dimension()));
    }

    @Inject(method = "setTagData", at = @At(value = "INVOKE", target = "Lnet/minecraft/nbt/CompoundTag;store(Ljava/lang/String;Lcom/mojang/serialization/Codec;Ljava/lang/Object;)V", ordinal = 0, shift = At.Shift.AFTER))
    private void setTagDataPaper(RegistryAccess registryAccess, CompoundTag tag, CompoundTag compoundTag2, CallbackInfo ci) {
        tag.store(PAPER_RESPAWN_DIMENSION, net.minecraft.world.level.Level.RESOURCE_KEY_CODEC, this.respawnDimension); // Paper
    }
}