package org.cardboardpowered.mixin.world.level.block.entity;

import net.minecraft.world.level.block.entity.TheEndGatewayBlockEntity;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(TheEndGatewayBlockEntity.class)
public class TheEndGatewayBlockEntityMixin {

	/*
	@Shadow
	private static void startTeleportCooldown(World world, BlockPos pos, BlockState state, EndGatewayBlockEntity be) {
	}


	@Redirect(method = "tryTeleportingEntity", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;resetPortalCooldown()V"))
	private static void onResetPortalCooldown(Entity instance) {
		// ignore, called somewhere else here
	}

	@Unique private static boolean wasCancelled;

	@Redirect(method = "tryTeleportingEntity", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/entity/EndGatewayBlockEntity;startTeleportCooldown(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;Lnet/minecraft/block/entity/EndGatewayBlockEntity;)V"))
	private static void onStartTeleportCooldown(World world, BlockPos pos, BlockState state, EndGatewayBlockEntity blockEntity) {
		if(wasCancelled) {
			wasCancelled = false;
		} else {
			startTeleportCooldown(world, pos, state, blockEntity);
		}
	}

	@Redirect(method = "tryTeleportingEntity", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;teleport(DDD)V"))
	private static void bukkitize(Entity target, double x, double y, double z) {
		if(!(target instanceof ServerPlayerEntity player)) {
			target.resetPortalCooldown();
			target.teleport(x, y, z);
			return;
		}

		Location loc = callEvent((IMixinWorld) player.getWorld(), (IMixinEntity) player, x, y, z);

		if(loc == null) {
			wasCancelled = true;
			return;
		}

		target.resetPortalCooldown();
		((IMixinPlayNetworkHandler) player.networkHandler).teleport(loc);
	}

	@Unique
	private static Location callEvent(IMixinWorld world, IMixinEntity teleported, double x, double y, double z) {
		CraftPlayer player = (CraftPlayer) teleported.getBukkitEntity();
		Location location = new Location(world.getCraftWorld(), x, y, z);
		location.setPitch(player.getLocation().getPitch());
		location.setYaw(player.getLocation().getYaw());

		PlayerTeleportEvent teleEvent = new PlayerTeleportEvent(player, player.getLocation(), location, PlayerTeleportEvent.TeleportCause.END_GATEWAY);
		return teleEvent.isCancelled() ? null : teleEvent.getTo();
	}
	*/

}
