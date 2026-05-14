package org.cardboardpowered.mixin.world.level.block;

import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.VegetationBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import org.cardboardpowered.util.MixinInfo;
import org.spongepowered.asm.mixin.Mixin;

@MixinInfo(events = {"BlockGrowEvent, EntityChangeBlockEvent"})
@Mixin(CropBlock.class)
public abstract class CropBlockMixin extends VegetationBlock {

    protected CropBlockMixin(BlockBehaviour.Properties settings) {
        super(settings);
    }
    
    // TODO: 1.21.4

    /*
    @Redirect (method = "randomTick", at = @At (value = "INVOKE",
            target = "Lnet/minecraft/server/world/ServerWorld;setBlockState" +
                    "(Lnet/minecraft/util/math/BlockPos;" +
                    "Lnet/minecraft/block/BlockState;I)Z"))
    private boolean bukkit_growEvent0(ServerWorld world, BlockPos pos, BlockState state, int flags) {
        return CraftEventFactory.handleBlockGrowEvent(world, pos, state, flags);
    }

    @Redirect (method = "applyGrowth", at = @At (value = "INVOKE",
            target = "Lnet/minecraft/world/World;setBlockState" +
                    "(Lnet/minecraft/util/math/BlockPos;" +
                    "Lnet/minecraft/block/BlockState;I)Z"))
    private boolean bukkit_growEvent1(World world, BlockPos pos, BlockState state, int flags) {
        return CraftEventFactory.handleBlockGrowEvent(world, pos, state, flags);
    }

    @Redirect (method = "onEntityCollision", at = @At (value = "INVOKE",
            target = "Lnet/minecraft/world/GameRules;getBoolean(Lnet/minecraft/world/GameRules$Key;)Z"))
    private boolean bukkit_ifHack(GameRules instance, GameRules.Key<GameRules.BooleanRule> rule) {
        return true;
        // mumfrey pls allow us to add conditions to ifs
    }

    @Inject (method = "onEntityCollision", at = @At (value = "INVOKE",
            target = "Lnet/minecraft/world/World;breakBlock" +
                    "(Lnet/minecraft/util/math/BlockPos;" +
                    "ZLnet/minecraft/entity/Entity;)Z"))
    private void bukkit_entityChangeBlockEvent(BlockState state, World world, BlockPos pos, Entity entity,
                                               CallbackInfo ci) {
    	if (world instanceof ServerWorld) {
            ServerWorld sworld = (ServerWorld)world;
	    	if (CraftEventFactory
	                .callEntityChangeBlockEvent(entity, pos, Blocks.AIR.getDefaultState(), !sworld.getGameRules()
	                        .getBoolean(GameRules.DO_MOB_GRIEFING))
	                .isCancelled()) {
	            super.onEntityCollision(state, world, pos, entity);
	            ci.cancel();
	        }
    	}
    }*/

}
