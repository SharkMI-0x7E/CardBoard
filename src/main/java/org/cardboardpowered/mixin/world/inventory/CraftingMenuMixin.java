package org.cardboardpowered.mixin.world.inventory;

import net.minecraft.world.entity.player.Player;
import org.bukkit.craftbukkit.event.CraftEventFactory;
import org.cardboardpowered.bridge.world.entity.EntityBridge;
import org.cardboardpowered.bridge.world.inventory.AbstractContainerMenuBridge;
import org.bukkit.craftbukkit.inventory.CraftInventoryCrafting;
import org.bukkit.craftbukkit.inventory.CraftInventoryView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Optional;
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.inventory.CraftingMenu;
import net.minecraft.world.inventory.ResultContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(CraftingMenu.class)
public class CraftingMenuMixin extends AbstractContainerMenuMixin {

	// Lnet/minecraft/screen/CraftingScreenHandler;input:Lnet/minecraft/inventory/RecipeInputInventory;
	
	// Lnet/minecraft/screen/AbstractCraftingScreenHandler;craftingInventory:Lnet/minecraft/inventory/RecipeInputInventory;
	
    //@Shadow public RecipeInputInventory input;
    // @Shadow public CraftingResultInventory result;
    @Shadow public ContainerLevelAccess access;
    @Shadow public net.minecraft.world.entity.player.Player player;

    private CraftInventoryView bukkitEntity = null;
    private Inventory playerInv;

    @Inject(method = "<init>(ILnet/minecraft/world/entity/player/Inventory;Lnet/minecraft/world/inventory/ContainerLevelAccess;)V", at = @At("TAIL"))
    public void setPlayerInv(int i, Inventory playerinventory, ContainerLevelAccess containeraccess, CallbackInfo ci) {
    	this.playerInv = playerinventory;
    }

    @Override
    public CraftInventoryView getBukkitView() {
        if (bukkitEntity != null)
            return bukkitEntity;

        CraftingMenu thiz = (CraftingMenu) (Object) this;

        CraftInventoryCrafting inventory = new CraftInventoryCrafting(thiz.craftSlots, thiz.resultSlots);
        bukkitEntity = new CraftInventoryView((org.bukkit.entity.Player)((EntityBridge)this.playerInv.player).getBukkitEntity(), inventory, (CraftingMenu)(Object)this);
        return bukkitEntity;
    }

    private static void aBF(int i, Level world, net.minecraft.world.entity.player.Player entityhuman, CraftingContainer inventorycrafting, ResultContainer inventorycraftresult, AbstractContainerMenu container) {
        if (!world.isClientSide()) {
        	CraftingInput craftinginput = inventorycrafting.asCraftInput();
            ServerPlayer entityplayer = (ServerPlayer) entityhuman;
            ItemStack itemstack = ItemStack.EMPTY;
            Optional<RecipeHolder<CraftingRecipe>> optional = world.getServer().getRecipeManager().getRecipeFor(
                    RecipeType.CRAFTING, craftinginput, world);

            if (optional.isPresent()) {
                RecipeHolder<CraftingRecipe> recipecrafting = optional.get();
                if (inventorycraftresult.setRecipeUsed(entityplayer, recipecrafting))
                    itemstack = recipecrafting.value().assemble(craftinginput, world.registryAccess());
            }
            itemstack = CraftEventFactory.callPreCraftEvent(inventorycrafting, inventorycraftresult, itemstack, ((AbstractContainerMenuBridge)container).getBukkitView(), false);
            inventorycraftresult.setItem(0, itemstack);
            entityplayer.connection.send(new ClientboundContainerSetSlotPacket(i, container.incrementStateId(), 0, itemstack));
        }
    }

    /**
     * @reason Call PreCraftEvent
     * @author cardboard
     */
    @Overwrite
    public void slotsChanged(Container iinventory) {
        this.access.execute((world, blockposition) -> {
        	CraftingMenu thiz = (CraftingMenu)(Object)this;
            aBF(((CraftingMenu)(Object)this).containerId, world, this.player, thiz.craftSlots, thiz.resultSlots, thiz);
        });
    }

    @Inject(method = "stillValid", at = @At("HEAD"), cancellable = true)
    public void stillValidCraftBukkit(Player player, CallbackInfoReturnable<Boolean> cir) {
        if (!this.checkReachable) cir.setReturnValue(true); // CraftBukkit
    }
}
