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
