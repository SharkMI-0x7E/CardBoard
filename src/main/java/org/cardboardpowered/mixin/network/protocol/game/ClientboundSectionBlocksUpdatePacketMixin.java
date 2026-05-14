package org.cardboardpowered.mixin.network.protocol.game;

import net.minecraft.network.protocol.game.ClientboundSectionBlocksUpdatePacket;
import net.minecraft.world.level.block.state.BlockState;
import org.cardboardpowered.bridge.network.protocol.game.ClientboundSectionBlocksUpdatePacketBridge;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ClientboundSectionBlocksUpdatePacket.class)
public class ClientboundSectionBlocksUpdatePacketMixin implements ClientboundSectionBlocksUpdatePacketBridge {

    @Shadow
    @Final
    @Mutable
    private BlockState[] states;

    @Override
    public void cardboard$set_block_states(BlockState[] states) {
        this.states = states;
    }
 
}
