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
package org.cardboardpowered.mixin.world.level.block.entity.vault;

import net.minecraft.world.level.block.entity.vault.VaultServerData;
import org.cardboardpowered.bridge.world.level.block.entity.vault.VaultServerDataBridge;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Iterator;
import java.util.Set;
import java.util.UUID;

@Mixin(VaultServerData.class)
public abstract class VaultServerDataMixin implements VaultServerDataBridge {
    @Shadow
    @Final
    private Set<UUID> rewardedPlayers;

    @Shadow
    protected abstract void markChanged();

    @Override
    public boolean cardboard$addToRewardedPlayers(final java.util.UUID player) {
        final boolean removed = this.rewardedPlayers.add(player);
        if (this.rewardedPlayers.size() > 128) {
            Iterator<UUID> iterator = this.rewardedPlayers.iterator();
            if (iterator.hasNext()) {
                iterator.next();
                iterator.remove();
            }
        }

        this.markChanged();
        return removed; // Paper - Vault API
    }

    // Paper start - Vault API
    @Override
    public boolean cardboard$removeFromRewardedPlayers(final UUID uuid) {
        if (this.rewardedPlayers.remove(uuid)) {
            this.markChanged();
            return true;
        }

        return false;
    }
    // Paper end - Vault API
}
