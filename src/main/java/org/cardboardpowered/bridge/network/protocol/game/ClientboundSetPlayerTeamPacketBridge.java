package org.cardboardpowered.bridge.network.protocol.game;

import net.minecraft.network.protocol.game.ClientboundSetPlayerTeamPacket;
import net.minecraft.world.scores.PlayerTeam;

import java.util.Collection;
import java.util.Optional;

public interface ClientboundSetPlayerTeamPacketBridge {
    // Paper start - Multiple Entries with Scoreboards
    public static ClientboundSetPlayerTeamPacket createMultiplePlayerPacket(PlayerTeam team, Collection<String> players, ClientboundSetPlayerTeamPacket.Action action) {
        return new ClientboundSetPlayerTeamPacket(team.getName(), action == ClientboundSetPlayerTeamPacket.Action.ADD ? ClientboundSetPlayerTeamPacket.METHOD_JOIN : ClientboundSetPlayerTeamPacket.METHOD_LEAVE, Optional.empty(), players);
    }
    // Paper end - Multiple Entries with Scoreboards
}
