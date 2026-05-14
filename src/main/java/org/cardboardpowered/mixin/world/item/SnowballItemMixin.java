package org.cardboardpowered.mixin.world.item;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.projectile.throwableitemprojectile.Snowball;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SnowballItem;
import net.minecraft.world.level.Level;
import org.cardboardpowered.bridge.server.level.ServerPlayerBridge;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.cardboardpowered.bridge.world.entity.EntityBridge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(value = SnowballItem.class, priority = 900)
public class SnowballItemMixin extends Item {

    public SnowballItemMixin(net.minecraft.world.item.Item.Properties settings) {
        super(settings);
    }

    /**
     * @author
     * @reason
     */
    @Overwrite
    public InteractionResult use(Level world, net.minecraft.world.entity.player.Player user, InteractionHand hand) {
        ItemStack itemStack = user.getItemInHand(hand);
        if (!world.isClientSide()) {
            Snowball snowballEntity = new Snowball(world, user, itemStack);
            snowballEntity.setItem(itemStack);
            snowballEntity.shootFromRotation(user, user.getXRot(), user.getYRot(), 0.0F, 1.5F, 1.0F);
            if (!world.addFreshEntity(snowballEntity)) {
                if (user instanceof ServerPlayerBridge) {
                    ((CraftPlayer)((EntityBridge) user).getBukkitEntity()).updateInventory();
                }
                return InteractionResult.FAIL;
            }
        }
        world.playSound(null, user.getX(), user.getY(), user.getZ(), SoundEvents.SNOWBALL_THROW, SoundSource.NEUTRAL, 0.5F, 0.4F / (world.getRandom().nextFloat() * 0.4F + 0.8F));
        user.awardStat(Stats.ITEM_USED.get(this));
        if (!user.getAbilities().instabuild) {
            itemStack.shrink(1);
        }
        return InteractionResult.SUCCESS;
    }
    
    /*
    private static float cb$POWER = 1.5f;
    
    // @Override
    public ActionResult use_new(World world, PlayerEntity user, Hand hand) {
        ItemStack itemstack = user.getStackInHand(hand);
        if (world instanceof ServerWorld) {
        	
            ServerWorld worldserver = (ServerWorld)world;
            Projectile.Delayed<SnowballEntity> snowball = ProjectileEntity.spawnProjectileFromRotationDelayed(SnowballEntity::new, worldserver, itemstack, user, 0.0f, cb$POWER, 1.0f);
            PlayerLaunchProjectileEvent event = new PlayerLaunchProjectileEvent((Player)user.getBukkitEntity(), (org.bukkit.inventory.ItemStack)CraftItemStack.asCraftMirror(itemstack), (Projectile)snowball.projectile().getBukkitEntity());
            if (event.callEvent() && snowball.attemptSpawn()) {
                user.incrementStat(Stats.USED.getOrCreateStat(this));
                if (event.shouldConsume()) {
                    itemstack.decrementUnlessCreative(1, user);
                } else if (user instanceof ServerPlayerEntity) {
                	((IMixinServerEntityPlayer) user).getBukkit().updateInventory();
                }
                world.playSound((PlayerEntity)null, user.getX(), user.getY(), user.getZ(), SoundEvents.ENTITY_SNOWBALL_THROW, SoundCategory.NEUTRAL, 0.5f, 0.4f / (world.getRandom().nextFloat() * 0.4f + 0.8f));
            } else {
                if (user instanceof ServerPlayerEntity) {
                	((IMixinServerEntityPlayer) user).getBukkit().updateInventory();
                }
                return ActionResult.FAIL;
            }
        }
        return ActionResult.SUCCESS;
    }
    */
    
}
