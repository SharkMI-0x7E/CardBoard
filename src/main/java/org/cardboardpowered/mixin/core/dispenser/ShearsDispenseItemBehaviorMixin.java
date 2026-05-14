package org.cardboardpowered.mixin.core.dispenser;

import com.google.common.collect.Lists;
import org.cardboardpowered.bridge.world.entity.EntityBridge;
import org.cardboardpowered.bridge.world.level.LevelBridge;
import java.util.Collections;
import java.util.List;
import net.minecraft.core.dispenser.BlockSource;
import net.minecraft.core.dispenser.DispenseItemBehavior;
import net.minecraft.core.dispenser.ShearsDispenseItemBehavior;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Shearable;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.DispenserBlock;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.event.block.BlockDispenseEvent;
import org.bukkit.event.block.BlockShearEntityEvent;
import org.cardboardpowered.impl.block.DispenserBlockHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ShearsDispenseItemBehavior.class)
public class ShearsDispenseItemBehaviorMixin {

    // todo: nonstatic
    private static Block cardboard_block;
    private static CraftItemStack cardboard_saved;

    @Inject(at = @At("HEAD"), method = "execute")
    protected void cardboard_dispenseSilently(BlockSource pointer, ItemStack stack, CallbackInfoReturnable<ItemStack> ci) {
        cardboard_block = ((LevelBridge)pointer.level()).cardboard$getWorld().getBlockAt(pointer.pos().getX(), pointer.pos().getY(), pointer.pos().getZ());
        cardboard_saved = CraftItemStack.asCraftMirror(stack);

        BlockDispenseEvent event = new BlockDispenseEvent(cardboard_block, cardboard_saved.clone(), new org.bukkit.util.Vector(0, 0, 0));
        if (!DispenserBlockHelper.eventFired) Bukkit.getPluginManager().callEvent(event);

        if (event.isCancelled()) {
            ci.setReturnValue(stack);
            return;
        }

        if (!event.getItem().equals(cardboard_saved)) {
            ItemStack eventStack = CraftItemStack.asNMSCopy(event.getItem());
            DispenseItemBehavior idispensebehavior = (DispenseItemBehavior) DispenserBlock.DISPENSER_REGISTRY.get(eventStack.getItem());
            if (idispensebehavior != DispenseItemBehavior.NOOP && idispensebehavior != this) {
                idispensebehavior.dispense(pointer, eventStack);
                ci.setReturnValue(stack);
                return;
            }
        }
    }

    @Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Shearable;shear(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/sounds/SoundSource;Lnet/minecraft/world/item/ItemStack;)V"),
            method = "tryShearEntity")
    private static void doEvent(Shearable s, ServerLevel sworld, SoundSource cat, ItemStack stack) {
    	BlockShearEntityEvent event = callBlockShearEntityEvent((LivingEntity)s, cardboard_block, cardboard_saved, Shearable_generateDefaultDrops());
    	if (!event.isCancelled()) {
           
    		CraftItemStack.asNMSCopy(event.getDrops());
    		s.shear(sworld, SoundSource.BLOCKS, stack);
        	// s.sheared(sworld, SoundCategory.BLOCKS, stack, CraftItemStack.asNMSCopy(event.getDrops()));
        	
        	// s.sheared(cat);
        }
    }
    
    private static List<ItemStack> Shearable_generateDefaultDrops() {
        return Collections.emptyList();
    }

    private static BlockShearEntityEvent callBlockShearEntityEvent(Entity animal, org.bukkit.block.Block dispenser, CraftItemStack is, List<net.minecraft.world.item.ItemStack> drops) {

    	BlockShearEntityEvent bse = new BlockShearEntityEvent(
    			dispenser,
    			((EntityBridge)animal).getBukkitEntity(),
    			is,
    			Lists.transform(drops, CraftItemStack::asCraftMirror)
    	);
        Bukkit.getPluginManager().callEvent(bse);
        return bse;
    }

}
