/**
 * Cardboard - Spigot/Paper for Fabric
 * Copyright (C) 2020-2024 CardboardPowered.org and contributors
 * Copyright (C) 2025-2026 SharkMI and contributors
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
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
