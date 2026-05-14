package org.cardboardpowered.bridge.server.players;

import com.mojang.authlib.GameProfile;

public interface NameAndIdBridge {
    GameProfile cardboard$toUncompletedGameProfile();
}
