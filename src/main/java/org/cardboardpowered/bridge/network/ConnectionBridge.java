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