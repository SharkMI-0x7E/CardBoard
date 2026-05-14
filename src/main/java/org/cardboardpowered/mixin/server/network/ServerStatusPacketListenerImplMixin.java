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

    @Shadow
    private Connection connection;

    @ModifyArg(
        method = "handleStatusRequest",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/network/protocol/status/ClientboundStatusResponsePacket;<init>(Lnet/minecraft/network/protocol/status/ServerStatus;)V"
        ),
        require = 0
    )
    private ServerStatus cardboard$modifyServerStatus(ServerStatus originalStatus) {
        LOGGER.info("[MOTD-DEBUG] Entering cardboard$modifyServerStatus, original MOTD: {}", originalStatus.description().getString());

        MinecraftServer server = CraftServer.server;
        CardboardServerListPingEvent event = new CardboardServerListPingEvent(this.connection, server);
        CraftServer.INSTANCE.getPluginManager().callEvent(event);

        // Check if plugin set custom MOTD
        boolean motdChanged = !event.getMotd().isEmpty() && !event.getMotd().equals("A Minecraft Server");
        boolean hasCustomIcon = event.icon != null && event.icon.value != null;

        LOGGER.info("[MOTD-DEBUG] motdChanged={}, hasCustomIcon={}, event.motd='{}'", motdChanged, hasCustomIcon, event.getMotd());

        // If no custom icon or MOTD, return original Status to preserve other mods' changes
        if (!hasCustomIcon && !motdChanged) {
            LOGGER.info("[MOTD-DEBUG] Returning originalStatus (no custom changes)");
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

        // Use originalStatus description by default (preserves MiniMOTD changes)
        // Only replace if plugin explicitly set a custom MOTD
        Component motdComponent = originalStatus.description();
        if (motdChanged) {
            motdComponent = CraftChatMessage.fromString(event.getMotd(), true)[0];
        }

        LOGGER.info("[MOTD-DEBUG] Returning new ServerStatus, MOTD='{}', hasCustomIcon={}", motdComponent.getString(), hasCustomIcon);
        return new ServerStatus(
                motdComponent,
                Optional.of(samp),
                originalStatus.version().or(() -> Optional.of(new ServerStatus.Version(server.getServerModName() + " " + server.getServerVersion(), SharedConstants.getCurrentVersion().protocolVersion()))),
                hasCustomIcon ? Optional.of(new ServerStatus.Favicon(event.icon.value)) : originalStatus.favicon(),
                originalStatus.enforcesSecureChat()
        );
    }
}
