package org.cardboardpowered.bridge.server.network;

import net.minecraft.server.level.ServerPlayer;

public interface ServerConfigurationPacketListenerImplBridge {
	void cardboard_setPlayer(ServerPlayer entity);
}
