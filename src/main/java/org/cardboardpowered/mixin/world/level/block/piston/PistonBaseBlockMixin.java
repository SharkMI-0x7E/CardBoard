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
package org.cardboardpowered.mixin.world.level.block.piston;

import org.cardboardpowered.bridge.world.level.LevelBridge;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.block.CraftBlock;
import org.bukkit.event.block.BlockPistonEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.cardboardpowered.extras.DualBlockList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.piston.PistonBaseBlock;
import net.minecraft.world.level.block.piston.PistonStructureResolver;

//@MixinInfo(events = {"BlockPistonExtendEvent","BlockPistonRetractEvent","BlockPistonEvent"})
@Mixin(PistonBaseBlock.class)
public class PistonBaseBlockMixin {

	/*
    private PistonHandler cardboard_ph;

    @Redirect(at = @At(value = "NEW", target = "(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/math/Direction;Z)Lnet/minecraft/block/piston/PistonHandler;"), method = "tryMove")
    public PistonHandler cardboard_storePH(World world, BlockPos pos, Direction dir, boolean retract) {
        return (cardboard_ph = new PistonHandler(world,pos,dir,retract));
    }
    */

    @Inject(at = @At(value = "INVOKE_ASSIGN", target = "Lnet/minecraft/world/level/block/piston/PistonStructureResolver;getToDestroy()Ljava/util/List;"),
            method = "moveBlocks", cancellable = true, locals = LocalCapture.CAPTURE_FAILHARD)
    public void cardboard_doPistonEvents(Level world, BlockPos pos, Direction dir, boolean retract, CallbackInfoReturnable<Boolean> ci,
    		BlockPos blockPos, PistonStructureResolver helper) {

        final org.bukkit.block.Block bblock = ((LevelBridge)world).cardboard$getWorld().getBlockAt(pos.getX(), pos.getY(), pos.getZ());

		// TODO: Fix null
		//if (null == cardboard_ph) {
		//	System.out.println("Debug: MixinPistonBlock: null == cardboard_ph.");
		//	return;
		//}

        final List<BlockPos> moved = helper.getToPush();
        final List<BlockPos> broken = helper.getToDestroy();

        // Direction enumdirection1 = retract ? helper.pistonDirection : helper.pistonDirection ;
        
        Direction enumdirection1 = retract ? dir : dir.getOpposite();

        List<org.bukkit.block.Block> blocks = new DualBlockList(moved, broken, bblock.getWorld()) {

            @Override
            public int size() {
                return moved.size() + broken.size();
            }

            @Override
            public org.bukkit.block.Block get(int index) {
                if (index >= size() || index < 0)
                    throw new ArrayIndexOutOfBoundsException(index);
                BlockPos pos = (BlockPos) (index < moved.size() ? moved.get(index) : broken.get(index - moved.size()));
                return bblock.getWorld().getBlockAt(pos.getX(), pos.getY(), pos.getZ());
            }
        };
        BlockPistonEvent event = retract ? new BlockPistonExtendEvent(bblock, blocks, CraftBlock.notchToBlockFace(enumdirection1)) 
                        : new BlockPistonRetractEvent(bblock, blocks, CraftBlock.notchToBlockFace(enumdirection1));
        CraftServer.INSTANCE.getPluginManager().callEvent(event);

        if (event.isCancelled()) {
            for (BlockPos b : broken)
                world.sendBlockUpdated(b, Blocks.AIR.defaultBlockState(), world.getBlockState(b), 3);
            for (BlockPos b : moved) {
                world.sendBlockUpdated(b, Blocks.AIR.defaultBlockState(), world.getBlockState(b), 3);
                b = b.relative(enumdirection1);
                world.sendBlockUpdated(b, Blocks.AIR.defaultBlockState(), world.getBlockState(b), 3);
            }
            ci.setReturnValue(false);
            return;
        }
    }

}
