/**
 * Cardboard - Paper API for Fabric
 * Copyright (C) 2020-2025
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation; either 
 * version 3 of the License, or (at your option) any later version.
 */
package org.cardboardpowered.bridge.server.network;

import java.util.Set;
import net.minecraft.network.Connection;
import net.minecraft.world.entity.PositionMoveRotation;
import net.minecraft.world.entity.Relative;
import org.bukkit.Location;
import org.bukkit.craftbukkit.entity.CraftPlayer;

import io.papermc.paper.connection.PlayerGameConnection;

public interface ServerGamePacketListenerImplBridge {

    void chat(String message, boolean notDeprecated);

    void teleport(Location location);

    boolean isDisconnected();

	Connection cb_get_connection();

	void cardboard$internalTeleport(Location dest);

	void cardboard$internalTeleport(double x, double y, double z, float yRot, float xRot);

	void cardboard$internalTeleport(PositionMoveRotation positionmoverotation, Set<Relative> set);

	/**
	 */
	PlayerGameConnection cardboard$playerGameConnection();

	/**
	 * @return The CraftPlayer object for the Player of this network handler
	 * @since 1.21.10 - moved getPlayer() to this interface
	 */
	CraftPlayer getPlayer();

	/**
	 * player -> spigot -> reset
	 */
	void cardboard$spigot_player_respawn();

}