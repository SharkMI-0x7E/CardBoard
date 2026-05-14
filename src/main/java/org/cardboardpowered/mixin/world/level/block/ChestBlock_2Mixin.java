package org.cardboardpowered.mixin.world.level.block;

import org.cardboardpowered.extras.BukkitChestDoubleInventory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.Optional;
import net.minecraft.world.Container;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.level.block.entity.ChestBlockEntity;

@Mixin(targets = "net.minecraft.world.level.block.ChestBlock$2")
public class ChestBlock_2Mixin {
    @Inject(method = "acceptDouble(Lnet/minecraft/world/level/block/entity/ChestBlockEntity;Lnet/minecraft/world/level/block/entity/ChestBlockEntity;)Ljava/util/Optional;", at = @At("RETURN"), cancellable = true, locals = LocalCapture.CAPTURE_FAILHARD)
    private void bukkitCustomInventory(ChestBlockEntity chestBlockEntity, ChestBlockEntity chestBlockEntity2, CallbackInfoReturnable<Optional<MenuProvider>> cir, Container inventory) {
        cir.setReturnValue(Optional
                .of(new BukkitChestDoubleInventory(chestBlockEntity, chestBlockEntity2,
                        (net.minecraft.world.CompoundContainer) inventory)));
    }
}
