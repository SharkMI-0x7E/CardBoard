/**
 * Cardboard - Spigot/Paper for Fabric
 * Copyright (C) 2020-2026 CardboardPowered.org and contributors
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
package org.cardboardpowered.mixin.world.level.block.entity;

import net.minecraft.util.Mth;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SignBlock;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.entity.SignText;
import net.minecraft.world.phys.Vec3;
import org.cardboardpowered.bridge.world.level.block.entity.SignBlockEntityBridge;
import org.spongepowered.asm.mixin.Mixin;

/**
 * Bridge to expose SignBlockEntity internals to Cardboard's Bukkit layer.
 *
 * Previously this class exposed {@code SignText.messages} (a final internal array)
 * via {@code getTextBF()} and let callers mutate it with {@code System.arraycopy}.
 * That broke the immutability contract of {@link SignText} in 1.21.11 and caused
 * the sign text to "vanish when looked at" bug (see .trae/specs/fix-sign-text-disappearing).
 *
 * The bridge now exposes the SignText object itself; mutators must go through
 * {@link SignText#setMessage(int, net.minecraft.network.chat.Component)} which
 * returns a new immutable SignText.
 *
 * @implSpec https://github.com/PaperMC/Paper/blob/main/paper-server/patches/sources/net/minecraft/world/level/block/entity/SignBlockEntity.java.patch
 */
@Mixin(SignBlockEntity.class)
public abstract class SignBlockEntityMixin implements SignBlockEntityBridge {

    @Override
    public SignText cardboard$getFrontText() {
        return ((SignBlockEntity)(Object)this).getFrontText();
    }

    @Override
    public SignText cardboard$getBackText() {
        return ((SignBlockEntity)(Object)this).getBackText();
    }

    @Override
    public boolean cardboard$isFacingFrontText(double x, double z) {
        SignBlockEntity thiz = (SignBlockEntity) (Object) this;

        Block block = thiz.getBlockState().getBlock();
        if (block instanceof SignBlock) {
            SignBlock blocksign = (SignBlock) block;
            Vec3 vec3d = blocksign.getSignHitboxCenterPosition(thiz.getBlockState());
            double d0 = x - ((double) thiz.getBlockPos().getX() + vec3d.x);
            double d1 = z - ((double) thiz.getBlockPos().getZ() + vec3d.z);
            float f2 = blocksign.getYRotationDegrees(thiz.getBlockState());
            return Mth.degreesDifferenceAbs(f2,
                    (float) (Mth.atan2(d1, d0) * 57.2957763671875) - 90.0f) <= 90.0f;
        }
        return false;
    }

}
