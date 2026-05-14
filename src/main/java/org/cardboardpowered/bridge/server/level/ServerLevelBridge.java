package org.cardboardpowered.bridge.server.level;

import java.util.UUID;
import net.minecraft.server.level.progress.LevelLoadListener;
import net.minecraft.world.level.storage.ServerLevelData;
import org.bukkit.craftbukkit.CraftServer;
import org.cardboardpowered.impl.world.CraftWorld;

public interface ServerLevelBridge {

    ServerLevelData cardboard_worldProperties();

	default CraftServer getCraftServer() {
		return CraftServer.INSTANCE;
	}
	
	public void cardboard$set_uuid(UUID id);
	
	public UUID cardboard$get_uuid();

	/**
	 */
	LevelLoadListener cardboard$levelLoadListener();

}