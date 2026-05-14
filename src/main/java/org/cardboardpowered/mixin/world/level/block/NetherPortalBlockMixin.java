package org.cardboardpowered.mixin.world.level.block;

import net.minecraft.world.level.block.NetherPortalBlock;
import org.cardboardpowered.util.MixinInfo;
import org.spongepowered.asm.mixin.Mixin;

/**
 * 1.21.10 Note: now uses iCommonLib API
 * I forgot about this in 1.21.9
 */
@MixinInfo(events = {"EntityPortalEnterEvent"})
@Mixin(NetherPortalBlock.class)
@Deprecated
public class NetherPortalBlockMixin {

	/*
    @Inject(at = @At("HEAD"), method = "onEntityCollision")
    public void callBukkitEvent(BlockState state, World world, BlockPos pos, Entity entity, EntityCollisionHandler ech, CallbackInfo ci) {
        if (!entity.hasVehicle() && !entity.hasPassengers() && entity.canUsePortals(true)) {
            EntityPortalEnterEvent event = new EntityPortalEnterEvent(((IMixinEntity)entity).getBukkitEntity(), new org.bukkit.Location(((IMixinWorld)world).getCraftWorld(), pos.getX(), pos.getY(), pos.getZ()));
            Bukkit.getPluginManager().callEvent(event);
        }
    }
    */

}