/**
 * Cardboard - Spigot/Paper for Fabric
 * Copyright (C) 2020-2024 CardboardPowered.org and contributors
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
package org.cardboardpowered.mixin.world.item;

import net.minecraft.world.item.EnderEyeItem;
import net.minecraft.world.item.Item;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(EnderEyeItem.class)
public class EnderEyeItemMixin extends Item {

    public EnderEyeItemMixin(net.minecraft.world.item.Item.Properties settings) {
        super(settings);
    }

    /**
     * @reason .
     * @author .
     */
    /*@Overwrite
    public TypedActionResult<ItemStack> use(World world, PlayerEntity entityhuman, Hand enumhand) {
        ItemStack itemstack = entityhuman.getStackInHand(enumhand);
        BlockHitResult movingobjectpositionblock = raycast(world, entityhuman, RaycastContext.FluidHandling.NONE);

        if (movingobjectpositionblock.getType() == HitResult.Type.BLOCK && world.getBlockState(((BlockHitResult) movingobjectpositionblock).getBlockPos()).isOf(Blocks.END_PORTAL_FRAME)) {
            return TypedActionResult.pass(itemstack);
        } else {
            entityhuman.setCurrentHand(enumhand);
            if (world instanceof ServerWorld) {
                BlockPos blockposition = ((ServerWorld) world).getChunkManager().getChunkGenerator().locateStructure((ServerWorld) world, StructureFeature.STRONGHOLD, entityhuman.getBlockPos(), 100, false);

                if (blockposition != null) {
                    EyeOfEnderEntity entityendersignal = new EyeOfEnderEntity(world, entityhuman.getX(), entityhuman.getBodyY(0.5D), entityhuman.getZ());

                    entityendersignal.setItem(itemstack);
                    entityendersignal.initTargetPos(blockposition);

                    if (!world.spawnEntity(entityendersignal)) return new TypedActionResult<ItemStack>(ActionResult.FAIL, itemstack); // Bukkit

                    if (entityhuman instanceof ServerPlayerEntity)
                        Criteria.USED_ENDER_EYE.trigger((ServerPlayerEntity) entityhuman, blockposition);

                    world.playSound((PlayerEntity) null, entityhuman.getX(), entityhuman.getY(), entityhuman.getZ(), SoundEvents.ENTITY_ENDER_EYE_LAUNCH, SoundCategory.NEUTRAL, 0.5F, 0.4F / (EnderEyeItem.RANDOM.nextFloat() * 0.4F + 0.8F));
                    world.syncWorldEvent((PlayerEntity) null, 1003, entityhuman.getBlockPos(), 0);
                    if (!entityhuman.abilities.creativeMode) itemstack.decrement(1);

                    entityhuman.incrementStat(Stats.USED.getOrCreateStat((EnderEyeItem)(Object)this));
                    entityhuman.swingHand(enumhand, true);
                    return TypedActionResult.success(itemstack);
                }
            }
            return TypedActionResult.consume(itemstack);
        }
    }*/

}
