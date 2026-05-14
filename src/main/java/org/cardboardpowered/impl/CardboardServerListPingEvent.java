package org.cardboardpowered.impl;

import java.net.InetSocketAddress;
import java.util.Iterator;

import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.entity.Player;
import org.bukkit.event.server.ServerListPingEvent;
import org.cardboardpowered.impl.util.IconCacheImpl;

import org.cardboardpowered.bridge.server.level.ServerPlayerBridge;
import net.minecraft.network.Connection;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

public class CardboardServerListPingEvent extends ServerListPingEvent {
    
    public final Object[] players;
    public IconCacheImpl icon;
    
    //      public ServerListPingEvent(@NotNull InetAddress address, @NotNull String motd, boolean shouldSendChatPreviews, int numPlayers, int maxPlayers) {

    public CardboardServerListPingEvent(Connection connection, MinecraftServer server) {
        super("", ((InetSocketAddress) connection.getRemoteAddress()).getAddress(), server.getMotd(), server.getPlayerList().getPlayerCount(), server.getPlayerList().getMaxPlayers());
        this.players = server.getPlayerList().players.toArray();
        this.icon = CraftServer.INSTANCE.getServerIcon();
    }

    @Override
    public void setServerIcon(org.bukkit.util.CachedServerIcon icon) {
        if (!(icon instanceof IconCacheImpl)) throw new IllegalArgumentException(icon + " was not created by Bukkit");
    }

    public Iterator<Player> iterator() throws UnsupportedOperationException {
        return new Iterator<Player>() {
            int i;
            int ret = Integer.MIN_VALUE;
            ServerPlayer player;

            @Override
            public boolean hasNext() {
                if (player != null) {
                    return true;
                }
                final Object[] currentPlayers = players;
                for (int length = currentPlayers.length, i = this.i; i < length; i++) {
                    final ServerPlayer player = (ServerPlayer) currentPlayers[i];
                    if (player != null) {
                        this.i = i + 1;
                        this.player = player;
                        return true;
                    }
                }
                return false;
            }

            @Override
            public Player next() {
                if (!hasNext()) {
                    throw new java.util.NoSuchElementException();
                }
                final ServerPlayer player = this.player;
                this.player = null;
                this.ret = this.i - 1;
                return (Player) ((ServerPlayerBridge)player).getBukkitEntity();
            }

            @Override
            public void remove() {
                final Object[] currentPlayers = players;
                final int i = this.ret;
                if (i < 0 || currentPlayers[i] == null) {
                    throw new IllegalStateException();
                }
                currentPlayers[i] = null;
            }
        };
    }

}