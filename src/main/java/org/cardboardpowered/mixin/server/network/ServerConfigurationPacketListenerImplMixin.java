package org.cardboardpowered.mixin.server.network;

import net.minecraft.server.level.ClientInformation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerConfigurationPacketListenerImpl;
import org.cardboardpowered.bridge.server.network.ServerConfigurationPacketListenerImplBridge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

@Mixin(ServerConfigurationPacketListenerImpl.class)
public class ServerConfigurationPacketListenerImplMixin implements ServerConfigurationPacketListenerImplBridge {

	@Unique
	private ServerPlayer cardboard$replacementPlayer;
	
	@Shadow
	private ClientInformation clientInformation;
	
	/*
	@Redirect(at = @At(value = "INVOKE",
	         target = "Lnet/minecraft/server/PlayerManager;checkCanJoin(Ljava/net/SocketAddress;Lnet/minecraft/server/PlayerConfigEntry;)Lnet/minecraft/text/Text;"),
	         method = "onReady(Lnet/minecraft/network/packet/c2s/config/ReadyC2SPacket;)V")
	public Text cardboard$onReady_checkCanJoin_redirect(PlayerManager man, SocketAddress a, PlayerConfigEntry b) {
		// Cardboard: Let's take over vanilla player creation.
		// TODO: check on CraftEventFactory.handleLoginResult(
		return null;
	}

	
	@Inject(at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/server/PlayerManager;checkCanJoin(Ljava/net/SocketAddress;Lnet/minecraft/server/PlayerConfigEntry;)Lnet/minecraft/text/Text;",
			shift = At.Shift.AFTER
			),
			method = "onReady(Lnet/minecraft/network/packet/c2s/config/ReadyC2SPacket;)V", cancellable = true)
	public void cardboard$onReady_checkCanJoin_after(ReadyC2SPacket packet, CallbackInfo ci) {
		// Cardboard: Let's take over Vanilla Player Creation.
		if (null != cardboard$replacementPlayer) {
			cardboard$replacementPlayer.setClientOptions(syncedOptions);
			CraftServer.console.getPlayerManager().onPlayerConnect(((ServerConfigurationNetworkHandler)(Object)this).connection, cardboard$replacementPlayer, ((ServerConfigurationNetworkHandler)(Object)this).createClientData(this.syncedOptions));
			ci.cancel();
			return;
		}
	}
	*/

	@Override
	public void cardboard_setPlayer(ServerPlayer entity) {
		this.cardboard$replacementPlayer = entity;
	}
	
}
