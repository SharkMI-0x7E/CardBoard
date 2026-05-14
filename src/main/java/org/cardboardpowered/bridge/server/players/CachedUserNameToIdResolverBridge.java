package org.cardboardpowered.bridge.server.players;

/**
 * 1.21.9 has moved lots of GameProfile over to PlayerConfigEntry
 */
@Deprecated(forRemoval = true)
public interface CachedUserNameToIdResolverBridge {

	/*
    Optional<GameProfile> card_getByUuid(UUID uuid);

    Optional<GameProfile> card_findByName(String name);
    */

}