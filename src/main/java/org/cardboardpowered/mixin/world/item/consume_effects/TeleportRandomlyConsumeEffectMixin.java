package org.cardboardpowered.mixin.world.item.consume_effects;

import net.minecraft.world.item.consume_effects.TeleportRandomlyConsumeEffect;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(value = TeleportRandomlyConsumeEffect.class, priority = 900)
public class TeleportRandomlyConsumeEffectMixin { // extends Item {

	// TODO
	
	/*
    public MixinChorusFruitItem(net.minecraft.item.Item.Settings settings) {
        super(settings);
    }

    /**
     * @reason .
     * @author .
     *
    @Overwrite
    public ItemStack finishUsing(ItemStack itemstack, World world, LivingEntity entity) {
        ItemStack itemstack1 = super.finishUsing(itemstack, world, entity);
        if (world.isClient) return itemstack1;

        for (int i = 0; i < 16; ++i) {
            double d3 = entity.getX() + (entity.getRandom().nextDouble() - 0.5D) * 16.0D;
            double d4 = MathHelper.clamp(entity.getY() + (double) (entity.getRandom().nextInt(16) - 8), 0.0D, (double) (world.getHeight() - 1));
            double d5 = entity.getZ() + (entity.getRandom().nextDouble() - 0.5D) * 16.0D;

            if (entity instanceof ServerPlayerEntity) {
                Player player = (Player)((IMixinServerEntityPlayer)((ServerPlayerEntity) entity)).getBukkitEntity();
                PlayerTeleportEvent teleEvent = new PlayerTeleportEvent(player, player.getLocation(), new Location(player.getWorld(), d3, d4, d5), PlayerTeleportEvent.TeleportCause.CHORUS_FRUIT);
                Bukkit.getServer().getPluginManager().callEvent(teleEvent);
                if (teleEvent.isCancelled()) break;
                d3 = teleEvent.getTo().getX();
                d4 = teleEvent.getTo().getY();
                d5 = teleEvent.getTo().getZ();
            }

            if (entity.hasVehicle()) entity.stopRiding();

            if (entity.teleport(d3, d4, d5, true)) {
                world.playSound((PlayerEntity) null, entity.getX(), entity.getY(), entity.getZ(), SoundEvents.ITEM_CHORUS_FRUIT_TELEPORT, SoundCategory.MASTER, 1.0F, 1.0F);
                entity.playSound(SoundEvents.ITEM_CHORUS_FRUIT_TELEPORT, 1.0F, 1.0F);
                break;
            }
        }
        if (entity instanceof PlayerEntity) ((PlayerEntity) entity).getItemCooldownManager().set(itemstack, 20);

        return itemstack1;
    }*/

}