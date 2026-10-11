/**
 * Cardboard - Spigot/Paper for Fabric
 * Copyright (C) 2020-2026 CardboardPowered.org and contributors*
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.cardboardpowered.mixin.server.network;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.util.CraftChatMessage;
import org.cardboardpowered.impl.CardboardServerListPingEvent;
import org.cardboardpowered.util.MixinInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import net.minecraft.SharedConstants;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.status.ServerStatus;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerStatusPacketListenerImpl;
import net.minecraft.server.players.NameAndId;

@MixinInfo(events = {"ServerListPingEvent"})
@Mixin(ServerStatusPacketListenerImpl.class)
public class ServerStatusPacketListenerImplMixin {

    private static final Logger LOGGER = LoggerFactory.getLogger(ServerStatusPacketListenerImplMixin.class);

    private static final boolean MOTD_DEBUG = false;
    @Shadow
    private Connection connection;

    /**
     * Modifies the server status response to fire Bukkit's {@code ServerListPingEvent}.
     *
     * <p>This method uses {@link ModifyArg} instead of {@code @Overwrite} to avoid
     * conflicts with MiniMOTD and other mods that also modify the server status.
     * By using {@code @ModifyArg} with {@code require = 0}, Cardboard gracefully
     * handles cases where another mod has already {@code @Overwrite}n the target method.</p>
     *
     * <p>Cardboard only rebuilds the {@link ServerStatus} when a plugin actually
     * changed a value through the event. Otherwise the exact object built by
     * vanilla + other mods is returned untouched, so their changes (e.g. MiniMOTD's
     * MOTD) survive regardless of {@code @ModifyArg} chaining order. A custom server
     * icon is deliberately not a reason to rebuild: vanilla already loads
     * {@code server-icon.png} into {@code originalStatus.favicon()}, and rebuilding
     * just to force the same icon would overwrite another mod's description.</p>
     *
     * @param originalStatus the server status built by vanilla + other mods
     * @return the modified server status, or the untouched original if no plugin change
     * @see CardboardServerListPingEvent
     * @since 1.21.11
     */
    @ModifyArg(
        method = "handleStatusRequest",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/network/protocol/status/ClientboundStatusResponsePacket;<init>(Lnet/minecraft/network/protocol/status/ServerStatus;)V"
        ),
        require = 0
    )
    private ServerStatus cardboard$modifyServerStatus(ServerStatus originalStatus) {
        if (MOTD_DEBUG) {
            LOGGER.info("[MOTD-DEBUG] Entering cardboard$modifyServerStatus, original MOTD: {}", originalStatus.description().getString());
        }
        MinecraftServer server = CraftServer.server;
        CardboardServerListPingEvent event = new CardboardServerListPingEvent(this.connection, server);
        CraftServer.INSTANCE.getPluginManager().callEvent(event);

        // The event is seeded with the server's current values (see the
        // CardboardServerListPingEvent constructor), so any difference means a plugin
        // actually changed the value through ServerListPingEvent.
        boolean motdChanged = !event.getMotd().equals(server.getMotd());
        boolean maxPlayersChanged = event.getMaxPlayers() != server.getPlayerList().getMaxPlayers();
        if (MOTD_DEBUG) {
            LOGGER.info("[MOTD-DEBUG] motdChanged={}, maxPlayersChanged={}, event.motd='{}'", motdChanged, maxPlayersChanged, event.getMotd());
        }

        // Nothing for Cardboard to apply: return the exact ServerStatus we received.
        // Rebuilding here is what used to clobber other mods (e.g. MiniMOTD) depending
        // on @ModifyArg chaining order.
        if (!motdChanged && !maxPlayersChanged) {
            if (MOTD_DEBUG) {
                LOGGER.info("[MOTD-DEBUG] Returning originalStatus (no plugin change)");
            }
            return originalStatus;
        }

        // Build player list
        ArrayList<NameAndId> profiles = new ArrayList<>(event.players.length);
        for (Object player : event.players) {
            if (player != null) {
                profiles.add(((ServerPlayer) player).nameAndId());
            }
        }
        List<NameAndId> profiles2 = (server.hidesOnlinePlayers()) ? Collections.emptyList() : profiles;
        ServerStatus.Players samp = new ServerStatus.Players(event.getMaxPlayers(), profiles.size(), profiles2);

        // Keep the description received from other mods unless a plugin replaced it.
        Component motdComponent = motdChanged
                ? CraftChatMessage.fromString(event.getMotd(), true)[0]
                : originalStatus.description();
        if (MOTD_DEBUG) {
            LOGGER.info("[MOTD-DEBUG] Returning new ServerStatus, MOTD='{}'", motdComponent.getString());
        }
        return new ServerStatus(
                motdComponent,
                Optional.of(samp),
                originalStatus.version().or(() -> Optional.of(new ServerStatus.Version(server.getServerModName() + " " + server.getServerVersion(), SharedConstants.getCurrentVersion().protocolVersion()))),
                // Preserve the favicon (vanilla already applied server-icon.png, and other
                // mods may have replaced it) instead of forcing Cardboard's own icon.
                originalStatus.favicon(),
                originalStatus.enforcesSecureChat()
        );
    }
}
