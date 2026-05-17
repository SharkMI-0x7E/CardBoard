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
package org.cardboardpowered.mixin.server;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.google.common.io.FileWriteMode;
import com.google.common.io.Files;
import com.llamalad7.mixinextras.sugar.Local;

import net.minecraft.SharedConstants;
import net.minecraft.server.Main;
import net.minecraft.server.packs.PackType;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.world.level.storage.LevelStorageSource;

/**
 * Mixin of {@link net.minecraft.server.Main}
 * 
 * @implSpec https://github.com/PaperMC/Paper/blob/main/paper-server/patches/sources/net/minecraft/server/Main.java.patch
 */
@Mixin(value = Main.class)
public class MainMixin {

	@Inject(method = "main", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/packs/repository/ServerPacksSource;createPackRepository(Lnet/minecraft/world/level/storage/LevelStorageSource$LevelStorageAccess;)Lnet/minecraft/server/packs/repository/PackRepository;"))
    private static void cardboard$create_bukkit_datapack(String[] strings, CallbackInfo ci, @Local LevelStorageSource.LevelStorageAccess levelStorageAccess) {

		// Paper start - Create Bukkit Datapack
		
		File bukkitDataPackFolder = new File(levelStorageAccess.getLevelPath(LevelResource.DATAPACK_DIR).toFile(), "bukkit");
        if (!bukkitDataPackFolder.exists()) {
           bukkitDataPackFolder.mkdirs();
        }
		
        File mcMeta = new File(bukkitDataPackFolder, "pack.mcmeta");

        try {
           int major = SharedConstants.getCurrentVersion().packVersion(PackType.SERVER_DATA).major();
           int minor = SharedConstants.getCurrentVersion().packVersion(PackType.SERVER_DATA).minor();
           Files.asCharSink(mcMeta, StandardCharsets.UTF_8, new FileWriteMode[0])
              .write(
                 "{\n    \"pack\": {\n        \"description\": \"Data pack for resources provided by Bukkit plugins\",\n        \"min_format\": [%d, %d],\n        \"max_format\": [%d, %d]\n    }\n}\n"
                    .formatted(major, minor, major, minor)
              );
        } catch (IOException err) {
           throw new RuntimeException("Could not initialize Bukkit datapack", err);
        }
        // Paper end - Create Bukkit Datapack
    }
	
}
