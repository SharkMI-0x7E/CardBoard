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
package org.cardboardpowered.mixin.network;

import java.net.SocketAddress;
import java.util.UUID;
import net.minecraft.network.Connection;
import org.spongepowered.asm.mixin.Mixin;
import org.cardboardpowered.bridge.network.ConnectionBridge;
import com.mojang.authlib.properties.Property;

@Mixin(Connection.class)
public class ConnectionMixin implements ConnectionBridge {

    public UUID spoofedUUID;
    public Property[] spoofedProfile;
    public boolean preparing = true;

    @Override
    public SocketAddress getRawAddress() {
        return ((Connection)(Object)this).channel.remoteAddress();
    }

    @Override
    public UUID getSpoofedUUID() {
        return spoofedUUID;
    }

    @Override
    public void setSpoofedUUID(UUID uuid) {
        this.spoofedUUID = uuid;
    }

    @Override
    public Property[] getSpoofedProfile() {
        return spoofedProfile;
    }

    @Override
    public void setSpoofedProfile(Property[] profile) {
        this.spoofedProfile = profile;
    }

}
