/**
 * Cardboard - Spigot/Paper for Fabric
 * Copyright (C) 2020-2026 CardboardPowered.org and contributors
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.cardboardpowered.mixin;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cardboardpowered.CardboardConfig;

import net.fabricmc.loader.api.FabricLoader;

public class FixedCardboardMixinPlugin extends CardboardMixinPlugin {
    private static final Logger LOGGER = LogManager.getLogger("Cardboard");
    private static final String MIXIN_PACKAGE_ROOT = "org.cardboardpowered.mixin.";

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        String mixin = mixinClassName.substring(MIXIN_PACKAGE_ROOT.length());

        if (CardboardConfig.disabledMixins.contains(mixinClassName)) {
            LOGGER.info("Disabling mixin '" + mixin + "', was forced disabled in config.");
            return false;
        }

        if (mixin.equals("world.item.consume_effects.TeleportRandomlyConsumeEffectMixin") && FabricLoader.getInstance().isModLoaded("porting_lib")) {
            return false;
        }

        if (mixin.equals("server.network.ServerGamePacketListenerImplMixin_ChatEvent") && should_force_alternate_chat()) {
            LOGGER.info("Architectury Mod detected! Disabling async chat from NetworkHandler.");
            return false;
        }

        return true;
    }
}
