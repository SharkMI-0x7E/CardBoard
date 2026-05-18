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
package org.cardboardpowered.mixin.resources;

import java.util.Map;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import com.mojang.serialization.Lifecycle;

import io.papermc.paper.registry.PaperRegistryAccess;
import net.minecraft.core.WritableRegistry;
//import net.minecraft.registry.RegistryLoader.Loader;
import net.minecraft.resources.RegistryDataLoader;
import net.minecraft.resources.ResourceKey;

@Mixin(RegistryDataLoader.RegistryData.class)
public class RegistryDataLoader_RegistryDataMixin {

	/*
    Loader<T> getLoader(Lifecycle lifecycle, Map<RegistryKey<?>, Exception> errors) {

        SimpleRegistry writableRegistry = new SimpleRegistry(this.key, lifecycle);
        PaperRegistryAccess.instance().registerRegistry(this.key, writableRegistry);
        return new Loader(this, writableRegistry, errors);
    }
    */

	// Lnet/minecraft/registry/RegistryLoader$Entry;getLoader(Lcom/mojang/serialization/Lifecycle;Ljava/util/Map;)Lnet/minecraft/registry/RegistryLoader$Loader;
	
    @Inject(
    		at = @At(value = "RETURN"),
    		method = "create(Lcom/mojang/serialization/Lifecycle;Ljava/util/Map;)Lnet/minecraft/resources/RegistryDataLoader$Loader;",
    		locals = LocalCapture.CAPTURE_FAILHARD
    )
    public void cardboard$register_paper_registry(Lifecycle lifecycle, Map<ResourceKey<?>, Exception> errors, CallbackInfoReturnable ci, WritableRegistry writableRegistry) {
    	RegistryDataLoader.RegistryData thiz = (RegistryDataLoader.RegistryData) (Object) this;
    	PaperRegistryAccess.instance().registerRegistry(thiz.key(), writableRegistry);
    	
    }
    
}
