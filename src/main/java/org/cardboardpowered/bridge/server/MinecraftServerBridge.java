/**
 * Cardboard
 * Copyright (C) 2023
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation; either 
 * version 3 of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 */
package org.cardboardpowered.bridge.server;

import java.util.Map;
import java.util.Queue;

import org.bukkit.craftbukkit.CraftServer;

import io.papermc.paper.world.PaperWorldLoader.WorldLoadingInfo;
import net.minecraft.commands.Commands;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.WorldLoader.DataLoadContext;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.WorldOptions;
import net.minecraft.world.level.storage.LevelStorageSource.LevelStorageAccess;
import net.minecraft.world.level.storage.PlayerDataStorage;
import net.minecraft.world.level.storage.PrimaryLevelData;
import net.minecraft.world.level.storage.ServerLevelData;
import net.minecraft.world.level.storage.WorldData;

public interface MinecraftServerBridge {

    void removeLevel(ServerLevel level);

    void addLevel(ServerLevel level);

    Queue<Runnable> getProcessQueue();

    Map<ResourceKey<net.minecraft.world.level.Level>, ServerLevel> getWorldMap();

    void convertWorld(String name);

    // WorldGenerationProgressListenerFactory getWorldGenerationProgressListenerFactory();

    Commands setCommandManager(Commands commandManager);

    static MinecraftServer getServer() {
        return CraftServer.server;
    }

    // void loadSpawn(WorldGenerationProgressListener worldGenerationProgressListener, ServerWorld internal);

    void initWorld(ServerLevel worldserver, ServerLevelData iworlddataserver, WorldData saveData, WorldOptions generatorsettings);

    PlayerDataStorage getSaveHandler_BF();

    LevelStorageAccess getSessionBF();

    void cardboard_runOnMainThread(Runnable r);

    /**
     * @since 1.21.9
     */
	DataLoadContext cardboard$worldLoaderContext();

	/**
	 * Prepare Levels 1.21.9
	 */
	void cardboard$prepareLevel(ServerLevel serverLevel);

	/**
	 * @implNote Paper MinecraftServer.java.patch
	 * @since 1.21.9
	 */
	void createLevel(LevelStem levelStem, WorldLoadingInfo loadingInfo, LevelStorageAccess levelStorageAccess,
			PrimaryLevelData serverLevelData);

    boolean cardboard$isDebugging();
}