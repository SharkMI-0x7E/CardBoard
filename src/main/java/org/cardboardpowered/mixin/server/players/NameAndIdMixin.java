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
package org.cardboardpowered.mixin.server.players;

import com.mojang.authlib.GameProfile;
import net.minecraft.server.players.NameAndId;
import org.cardboardpowered.bridge.server.players.NameAndIdBridge;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.UUID;

@Mixin(NameAndId.class)
public class NameAndIdMixin implements NameAndIdBridge {
    @Shadow
    @Final
    private UUID id;

    @Shadow
    @Final
    private String name;

    // Paper start - utility method for common conversion back to the game profile
    @Override
    public GameProfile cardboard$toUncompletedGameProfile() {
        return new GameProfile(this.id, this.name);
    }
    // Paper end - utility method for common conversion back to the game profile
}
