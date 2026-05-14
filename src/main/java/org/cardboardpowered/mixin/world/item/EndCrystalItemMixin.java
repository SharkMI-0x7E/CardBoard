package org.cardboardpowered.mixin.world.item;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.item.EndCrystalItem;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.dimension.end.EndDragonFight;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import org.bukkit.craftbukkit.event.CraftEventFactory;

@Mixin(value = EndCrystalItem.class, priority = 900)
public class EndCrystalItemMixin {

    /**
     * @reason .
     * @author .
     */
    @Overwrite
    public InteractionResult useOn(UseOnContext itemactioncontext) {
        Level world = itemactioncontext.getLevel();
        BlockPos blockpos = itemactioncontext.getClickedPos();
        BlockState iblockdata = world.getBlockState(blockpos);

        if (!iblockdata.is(Blocks.BEDROCK) && !iblockdata.is(Blocks.OBSIDIAN)) return InteractionResult.FAIL;

        BlockPos blockpos1 = blockpos.above();
        if (!world.isEmptyBlock(blockpos1)) return InteractionResult.FAIL;
        double x = (double) blockpos1.getX();
        double y = (double) blockpos1.getY();
        double z = (double) blockpos1.getZ();
        List<Entity> list = world.getEntities((Entity) null, new AABB(x, y, z, x + 1.0D, y + 2.0D, z + 1.0D));

        if (!list.isEmpty())
        return InteractionResult.FAIL;

        if (world instanceof ServerLevel) {
            EndCrystal entityendercrystal = new EndCrystal(world, x + 0.5D, y, z + 0.5D);

            entityendercrystal.setShowBottom(false);
            if (CraftEventFactory.callEntityPlaceEvent(itemactioncontext, entityendercrystal).isCancelled()) return InteractionResult.FAIL;
            world.addFreshEntity(entityendercrystal);
            EndDragonFight enderdragonbattle = ((ServerLevel) world).getDragonFight();
            if (enderdragonbattle != null) enderdragonbattle.tryRespawn();
        }
        itemactioncontext.getItemInHand().shrink(1);
        return InteractionResult.SUCCESS;
        //return ActionResult.success(world.isClient);
    }

}