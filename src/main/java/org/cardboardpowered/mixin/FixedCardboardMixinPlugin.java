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
