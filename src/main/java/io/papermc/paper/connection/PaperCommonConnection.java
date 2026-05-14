package io.papermc.paper.connection;

import com.destroystokyo.paper.ClientOption;
import io.papermc.paper.adventure.PaperAdventure;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Map;
import net.kyori.adventure.text.Component;
import net.minecraft.network.protocol.common.ClientboundCustomReportDetailsPacket;
import net.minecraft.network.protocol.common.ClientboundStoreCookiePacket;
import net.minecraft.network.protocol.common.ClientboundTransferPacket;
import net.minecraft.server.level.ClientInformation;
import net.minecraft.server.network.ServerCommonPacketListenerImpl;
import org.bukkit.NamespacedKey;
import org.bukkit.ServerLinks;
// import org.bukkit.craftbukkit.CraftServerLinks;
import org.bukkit.craftbukkit.util.CraftNamespacedKey;
import org.jspecify.annotations.Nullable;

public abstract class PaperCommonConnection<T extends ServerCommonPacketListenerImpl> extends ReadablePlayerCookieConnectionImpl implements PlayerCommonConnection {
    protected final T handle;

    public PaperCommonConnection(T serverConfigurationPacketListenerImpl) {
        super(((ServerCommonPacketListenerImpl)serverConfigurationPacketListenerImpl).connection);
        this.handle = serverConfigurationPacketListenerImpl;
    }

    public void sendReportDetails(Map<String, String> details) {
        ((ServerCommonPacketListenerImpl)this.handle).send(new ClientboundCustomReportDetailsPacket(details));
    }

    public void sendLinks(ServerLinks links) {
        // TODO
    	// ((ServerCommonNetworkHandler)this.handle).sendPacket(new ServerLinksS2CPacket(((CraftServerLinks)links).getServerLinks().getLinks()));
    }

    public void transfer(String host, int port) {
        ((ServerCommonPacketListenerImpl)this.handle).send(new ClientboundTransferPacket(host, port));
    }

    public <T> T getClientOption(ClientOption<T> type) {
        ClientInformation information = this.getClientInformation();
        
        // TODO
        /*
        if (ClientOption.SKIN_PARTS == type) {
            return type.getType().cast(new PaperSkinParts(information.playerModelParts()));
        }
        */
        if (ClientOption.CHAT_COLORS_ENABLED == type) {
            return type.getType().cast(information.chatColors());
        }
        if (ClientOption.CHAT_VISIBILITY == type) {
            return type.getType().cast(ClientOption.ChatVisibility.valueOf((String)information.chatVisibility().name()));
        }
        if (ClientOption.LOCALE == type) {
            return type.getType().cast(information.language());
        }
        if (ClientOption.MAIN_HAND == type) {
            return type.getType().cast(information.mainHand());
        }
        if (ClientOption.VIEW_DISTANCE == type) {
            return type.getType().cast(information.viewDistance());
        }
        if (ClientOption.TEXT_FILTERING_ENABLED == type) {
            return type.getType().cast(information.textFilteringEnabled());
        }
        if (ClientOption.ALLOW_SERVER_LISTINGS == type) {
            return type.getType().cast(information.allowsListing());
        }
        if (ClientOption.PARTICLE_VISIBILITY == type) {
            return type.getType().cast(ClientOption.ParticleVisibility.valueOf((String)information.particleStatus().name()));
        }
        throw new RuntimeException("Unknown settings type");
    }

    public void disconnect(Component component) {
        ((ServerCommonPacketListenerImpl)this.handle).disconnect(PaperAdventure.asVanilla(component)/*, DisconnectionReason.UNKNOWN*/);
    }

    public boolean isTransferred() {
        return false; // TODO
    	// return ((ServerCommonNetworkHandler)this.handle).isTransferred();
    }

    public SocketAddress getAddress() {
        return ((ServerCommonPacketListenerImpl)this.handle).connection.getRemoteAddress();
    }

    public InetSocketAddress getClientAddress() {
        return (InetSocketAddress)((ServerCommonPacketListenerImpl)this.handle).connection.channel.remoteAddress();
    }

    public @Nullable InetSocketAddress getVirtualHost() {
        return null; // TODO
    	// return ((ServerCommonNetworkHandler)this.handle).connection.virtualHost;
    }

    public @Nullable InetSocketAddress getHAProxyAddress() {
        return null; // TODO
    	// InetSocketAddress inetSocketAddress;
        // SocketAddress socketAddress = ((ServerCommonNetworkHandler)this.handle).connection.haProxyAddress;
        // return socketAddress instanceof InetSocketAddress ? (inetSocketAddress = (InetSocketAddress)socketAddress) : null;
    }

    public void storeCookie(NamespacedKey key, byte[] value) {
        ((ServerCommonPacketListenerImpl)this.handle).send(new ClientboundStoreCookiePacket(CraftNamespacedKey.toMinecraft(key), value));
    }

    public abstract ClientInformation getClientInformation();
}

