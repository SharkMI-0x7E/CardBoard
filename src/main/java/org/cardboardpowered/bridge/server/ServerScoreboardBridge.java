package org.cardboardpowered.bridge.server;

import net.minecraft.world.scores.PlayerTeam;

public interface ServerScoreboardBridge {
    boolean cardboard$addPlayersToTeam(java.util.Collection<String> players, PlayerTeam team);

    void cardboard$removePlayersFromTeam(java.util.Collection<String> players, PlayerTeam team);
}
