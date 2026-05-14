package org.cardboardpowered.mixin.world.level.block;

import org.cardboardpowered.util.MixinInfo;
import org.spongepowered.asm.mixin.Mixin;

import net.minecraft.world.level.block.LeavesBlock;

@MixinInfo(events = {"LeavesDecayEvent"})
@Mixin(LeavesBlock.class)
public class LeavesBlockMixin {
    
	// Replaced by LeavesDecayEvent in iCommonLib
	
    /*@Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/block/LeavesBlock;dropStacks(Lnet/minecraft/block/BlockState;Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;)V"),
            method = "randomTick", cancellable = true)
    public void cardboard_doLeavesDecayEvent(BlockState state, ServerWorld world, BlockPos pos, Random ra, CallbackInfo ci) {        
        ActionResult result = LeavesDecayCallback.EVENT.invoker().interact(state, world, pos);
        
        if(result == ActionResult.FAIL) {
            ci.cancel();
        }
    }*/

}
