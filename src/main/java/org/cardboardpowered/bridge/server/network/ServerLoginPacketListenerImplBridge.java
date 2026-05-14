package org.cardboardpowered.bridge.server.network;

import java.util.UUID;
import net.minecraft.network.Connection;
import net.minecraft.server.level.ServerPlayer;

public interface ServerLoginPacketListenerImplBridge {

    String getHostname();

    void setHostname(String hostname);

	Connection cb_get_connection();

	ServerPlayer cardboard$get_player();

	/**
	 * @since 1.21.10
	 */
	UUID cardboard$requestedUuid();

	/**
	 */
	String cardboard$profileName();

	/**
	 */
	boolean cardboard$transferred();

}