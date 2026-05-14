package org.cardboardpowered.mixin.world.level.block.entity;

import org.spongepowered.asm.mixin.Mixin;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SignBlock;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.phys.Vec3;
import org.cardboardpowered.bridge.world.level.block.entity.SignBlockEntityBridge;

/**
 * @implSpec https://github.com/PaperMC/Paper/blob/main/paper-server/patches/sources/net/minecraft/world/level/block/entity/SignBlockEntity.java.patch
 */
@Mixin(SignBlockEntity.class)
public class SignBlockEntityMixin implements SignBlockEntityBridge {

   // @Shadow
   // public Text[] texts;

    @Override
    public Component[] getTextBF() {
    	SignBlockEntity e = (SignBlockEntity)(Object)this;
    	return e.getFrontText().getMessages(false);
    	
        //return texts;
    }
    
    // public boolean isPlayerFacingFront(PlayerEntity player) {
    //    return this.isFacingFrontText(player.getX(), player.getZ());
    //}

    @Override
    public boolean cardboard$isFacingFrontText(double x, double z) {
    	SignBlockEntity thiz = (SignBlockEntity) (Object) this;
    	
    	
        Block block = thiz.getBlockState().getBlock();
        if (block instanceof SignBlock) {
            SignBlock blocksign = (SignBlock)block;
            Vec3 vec3d = blocksign.getSignHitboxCenterPosition(thiz.getBlockState());
            double d0 = x - ((double)thiz.getBlockPos().getX() + vec3d.x);
            double d1 = z - ((double)thiz.getBlockPos().getZ() + vec3d.z);
            float f2 = blocksign.getYRotationDegrees(thiz.getBlockState());
            return Mth.degreesDifferenceAbs(f2, (float)(Mth.atan2(d1, d0) * 57.2957763671875) - 90.0f) <= 90.0f;
        }
        return false;
    }

}