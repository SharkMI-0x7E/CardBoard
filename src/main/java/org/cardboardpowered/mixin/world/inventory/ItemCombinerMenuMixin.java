package org.cardboardpowered.mixin.world.inventory;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.ItemCombinerMenu;
import net.minecraft.world.inventory.ResultContainer;
import org.cardboardpowered.bridge.world.inventory.ItemCombinerMenuBridge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Yarn:	ForgingScreenHandler
 * Mojmap:	ItemCombinerMenu
 * 
 * @implNote We are currently missing impl
 * 
 * @see {@link ItemCombinerMenuBridge}
 * @implSpec https://github.com/PaperMC/Paper/blob/main/paper-server/patches/sources/net/minecraft/world/inventory/ItemCombinerMenu.java.patch
 */
@Mixin(ItemCombinerMenu.class)
public abstract class ItemCombinerMenuMixin extends AbstractContainerMenuMixin implements ItemCombinerMenuBridge {

    @Shadow
    public ResultContainer resultSlots = new ResultContainer();

    @Shadow
    public Container inputSlots;

    @Shadow
    public ContainerLevelAccess access;

    @Shadow
    public Player player;

    @Inject(method = "stillValid", at = @At("HEAD"), cancellable = true)
    public void stillValidCraftBukkit(Player player, CallbackInfoReturnable<Boolean> cir) {
        if (!this.checkReachable) cir.setReturnValue(true); // CraftBukkit
    }
}