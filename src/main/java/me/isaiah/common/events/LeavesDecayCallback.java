package me.isaiah.common.events;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.level.block.state.BlockState;

/**
 * TODO: This is here for testing; will be moved to icommonlib
 */
public interface LeavesDecayCallback {
    
    Event<LeavesDecayCallback> EVENT = EventFactory.createArrayBacked(LeavesDecayCallback.class,
        (listeners) -> (state, world, pos) -> {
            for (LeavesDecayCallback listener : listeners) {
                InteractionResult result = listener.interact(state, world, pos);
 
                if(result != InteractionResult.PASS) {
                    return result;
                }
            }
 
        return InteractionResult.PASS;
    });
 
    InteractionResult interact(BlockState state, ServerLevel world, BlockPos pos);
}