package org.cardboardpowered.bridge.world.level.block.entity.vault;

import java.util.UUID;

public interface VaultServerDataBridge {
    boolean cardboard$addToRewardedPlayers(java.util.UUID player);

    boolean cardboard$removeFromRewardedPlayers(UUID uuid);
}
