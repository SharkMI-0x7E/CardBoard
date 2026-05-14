package org.cardboardpowered.bridge.network.protocol.game;

import net.minecraft.world.level.block.state.BlockState;

public interface ClientboundSectionBlocksUpdatePacketBridge {

	void cardboard$set_block_states(BlockState[] states);

}
