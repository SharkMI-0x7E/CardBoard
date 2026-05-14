package org.cardboardpowered.bridge.network;

import java.net.SocketAddress;
import java.util.UUID;

import com.mojang.authlib.properties.Property;

public interface ConnectionBridge {

    SocketAddress getRawAddress();

    UUID getSpoofedUUID();

    void setSpoofedUUID(UUID uuid);

    Property[] getSpoofedProfile();

    void setSpoofedProfile(Property[] profile);

}