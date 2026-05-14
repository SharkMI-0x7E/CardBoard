package io.papermc.paper.connection;

import com.destroystokyo.paper.profile.CraftPlayerProfile;
import com.destroystokyo.paper.profile.PlayerProfile;
import io.papermc.paper.adventure.PaperAdventure;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import net.kyori.adventure.text.Component;
import net.minecraft.network.Connection;
import net.minecraft.server.network.ServerLoginPacketListenerImpl;
import org.cardboardpowered.bridge.network.ConnectionBridge;
import org.cardboardpowered.bridge.server.network.ServerLoginPacketListenerImplBridge;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
public class PaperPlayerLoginConnection extends ReadablePlayerCookieConnectionImpl implements PlayerLoginConnection {

	private final ServerLoginPacketListenerImpl packetListener;
	
	private ConnectionBridge iconnection() {
		return (ConnectionBridge) packetListener_connection();
	}
	
	private ServerLoginPacketListenerImplBridge ipacketListener() {
		return ((ServerLoginPacketListenerImplBridge) this.packetListener);
	}
	
	private Connection packetListener_connection() {
		return ((ServerLoginPacketListenerImplBridge) this.packetListener).cb_get_connection();
	}

	public PaperPlayerLoginConnection(ServerLoginPacketListenerImpl packetListener) {
		super(((ServerLoginPacketListenerImplBridge) packetListener).cb_get_connection());
		this.packetListener = packetListener;
	}

	@Nullable
	public PlayerProfile getAuthenticatedProfile() {
		return this.packetListener.authenticatedProfile == null ? null : CraftPlayerProfile.asBukkitCopy(this.packetListener.authenticatedProfile);
	}

	@Nullable
	public PlayerProfile getUnsafeProfile() {
		return new CraftPlayerProfile(ipacketListener().cardboard$requestedUuid(), ipacketListener().cardboard$profileName());
	}

	public SocketAddress getAddress() {
		return packetListener_connection().getRemoteAddress();
	}

	public InetSocketAddress getClientAddress() {
		return (InetSocketAddress)packetListener_connection().channel.remoteAddress();
	}

	@Nullable
	public InetSocketAddress getVirtualHost() {
		return null; // TODO
		// return packetListener_connection().virtualHost;
	}

	@Nullable
	public InetSocketAddress getHAProxyAddress() {
		return null; // TODO
		// return packetListener_connection().haProxyAddress instanceof InetSocketAddress inetSocketAddress ? inetSocketAddress : null;
	}

	public boolean isTransferred() {
		return ipacketListener().cardboard$transferred();
	}

	public void disconnect(Component component) {
		this.packetListener.disconnect(PaperAdventure.asVanilla(component));
	}

	@Override
	public boolean isConnected() {
		// TODO Auto-generated method stub
		return packetListener_connection().isConnected();
	}

}