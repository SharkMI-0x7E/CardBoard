/**
 * Copyright (C) 2026 SharkMI and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 */
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
