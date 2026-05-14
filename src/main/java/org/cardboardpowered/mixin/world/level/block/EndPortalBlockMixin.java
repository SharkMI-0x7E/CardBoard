package org.cardboardpowered.mixin.world.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EndPortalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import org.bukkit.Bukkit;
import org.bukkit.event.entity.EntityPortalEnterEvent;
import org.cardboardpowered.util.MixinInfo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import org.cardboardpowered.bridge.world.entity.EntityBridge;
import org.cardboardpowered.bridge.world.level.LevelBridge;

@MixinInfo(events = {"EntityPortalEnterEvent"})
@Mixin(EndPortalBlock.class)
public class EndPortalBlockMixin {

    @Inject(at = @At("HEAD"), method = "entityInside")
    public void callBukkitEvent_EntityPortalEnterEvent(BlockState state, Level world, BlockPos pos, Entity entity, InsideBlockEffectApplier ech, boolean b, CallbackInfo ci) {
        if (world instanceof ServerLevel && !entity.isPassenger() && !entity.isVehicle() && entity.canUsePortal(true) && Shapes.joinIsNotEmpty(Shapes.create(entity.getBoundingBox().move(-pos.getX(), -pos.getY(), -pos.getZ())), state.getShape(world, pos), BooleanOp.AND)) {
            EntityPortalEnterEvent event = new EntityPortalEnterEvent(((EntityBridge)entity).getBukkitEntity(), new org.bukkit.Location(((LevelBridge)world).cardboard$getWorld(), pos.getX(), pos.getY(), pos.getZ()));
            Bukkit.getPluginManager().callEvent(event);
        }
    }

}