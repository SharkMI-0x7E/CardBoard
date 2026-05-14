package org.cardboardpowered.bridge.world.level.storage.loot.parameters;

import net.minecraft.resources.Identifier;
import net.minecraft.util.context.ContextKey;

public interface LootContextParamsBridge {

	ContextKey<Integer> LOOTING_MOD = new ContextKey<>(Identifier.parse("bukkit:looting_mod"));

}