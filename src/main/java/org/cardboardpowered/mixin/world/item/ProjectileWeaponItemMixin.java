package org.cardboardpowered.mixin.world.item;

import java.util.List;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ProjectileWeaponItem;
import net.minecraft.world.level.Level;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.cardboardpowered.util.MixinInfo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.bukkit.craftbukkit.event.CraftEventFactory;
import org.cardboardpowered.bridge.server.level.ServerPlayerBridge;
import org.cardboardpowered.bridge.world.entity.EntityBridge;

@MixinInfo(events = {"EntityShootBowEvent"})
// @Mixin(BowItem.class)
@Mixin(value = ProjectileWeaponItem.class, priority = 900)
/**
 * TODO: Rename class, 1.20.6 moved
 * functionality to RangedWeaponItem
 */
public class ProjectileWeaponItemMixin {

    public boolean cancel_BF = false;

    
    @Shadow
    public Projectile createProjectile( Level world, LivingEntity shooter, ItemStack weaponStack, ItemStack projectileStack, boolean critical) {
    	return null; // Shadowed
    }
    
    @Shadow
    public void shootProjectile( LivingEntity shooter, Projectile projectile, int index, float speed, float divergence, float yaw, LivingEntity target) {
    }
    
    @Shadow
    public int getDurabilityUse( ItemStack projectile) {
    	return 0; // Shadowed
    }
    
    /**
     * @author cardboard mod
     * @reason callEntityShootBowEvent
     * 
     * TODO: use inject
     */
    @Overwrite
    public void shoot(ServerLevel world, LivingEntity shooter, InteractionHand hand, ItemStack stack, List<ItemStack> projectiles, float speed, float divergence, boolean critical, LivingEntity target) {
        float f = 10.0f;
        float g = projectiles.size() == 1 ? 0.0f : 20.0f / (float)(projectiles.size() - 1);
        float h = (float)((projectiles.size() - 1) % 2) * g / 2.0f;
        float i = 1.0f;
        for (int j = 0; j < projectiles.size(); ++j) {
            ItemStack itemStack = projectiles.get(j);
            if (itemStack.isEmpty()) continue;
            float k = h + i * (float)((j + 1) / 2) * g;
            i = -i;
            Projectile projectileEntity = this.createProjectile(world, shooter, stack, itemStack, critical);
            this.shootProjectile(shooter, projectileEntity, j, speed, divergence, k, target);
            EntityShootBowEvent event = CraftEventFactory.callEntityShootBowEvent(shooter, stack, itemStack, projectileEntity, hand, speed, true);
            if (event.isCancelled()) {
                event.getProjectile().remove();
                return;
            }
            stack.hurtAndBreak(this.getDurabilityUse(itemStack), shooter, hand.asEquipmentSlot());
            if (event.getProjectile() != ((EntityBridge)projectileEntity).getBukkitEntity() || world.addFreshEntity(projectileEntity)) continue;
            if (shooter instanceof ServerPlayer) {
            	((Player) ((ServerPlayerBridge)  ((ServerPlayer)shooter) ).getBukkitEntity()).updateInventory();
            }
            return;
        }
    }
    
    /*
    @Inject(at = @At(value = "INVOKE", shift = At.Shift.AFTER, target = ""))
    public void cardboard$do_EntityShootBowEvent(
    		World world, LivingEntity shooter, Hand hand, ItemStack stack,
    		List<ItemStack> projectiles, float speed, float divergence, boolean critical, LivingEntity target) {
    	
    	EntityShootBowEvent event = CraftEventFactory.callEntityShootBowEvent(shooter, stack, itemStack, projectileEntity, hand, speed, true);
        if (event.isCancelled()) {
            event.getProjectile().remove();
            return;
        }
        stack.damage(this.getWeaponStackDamage(itemStack), shooter, LivingEntity.getSlotForHand(hand));
        if (event.getProjectile() != projectileEntity.getBukkitEntity() || world.spawnEntity(projectileEntity)) continue;
        if (shooter instanceof ServerPlayerEntity) {
            ((ServerPlayerEntity)shooter).getBukkitEntity().updateInventory();
        }
        return;
    	
    }
    
    @Redirect(
    		at = @At(value = "INVOKE", target="Lnet/minecraft/world/ModifiableWorld;spawnEntity(Lnet/minecraft/entity/Entity;)Z"),
    		method = "shootAll"
    	)
    public boolean cardboard$redirect_bowitem_spawnEntity() {
    	return false;
    }
    
    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;damage(ILnet/minecraft/entity/LivingEntity;Ljava/util/function/Consumer;)V"), method = "onStoppedUsing", cancellable = true)
    public void bukkitize2(ItemStack a, World b, LivingEntity c, int d, CallbackInfo ci) {
        if (cancel_BF) {
            cancel_BF = false;
            if (c instanceof PlayerEntity) {
                Player plr = (Player) ((IMixinServerEntityPlayer)((PlayerEntity) c)).getBukkitEntity();
                plr.updateInventory();
            }
            ci.cancel();
            return;
        }
    }

    @Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ArrowItem;createArrow(Lnet/minecraft/world/World;Lnet/minecraft/item/ItemStack;Lnet/minecraft/entity/LivingEntity;)Lnet/minecraft/entity/projectile/PersistentProjectileEntity;"),
            method = "onStoppedUsing")
    public PersistentProjectileEntity bukkitize(ArrowItem itemarrow, World world, ItemStack itemstack, LivingEntity entityliving) {
        PlayerEntity entityhuman = (PlayerEntity) entityliving;
        ItemStack itemstack1 = entityhuman.getProjectileType(itemstack); //.getArrowType(itemstack);

        PersistentProjectileEntity entityarrow = itemarrow.createArrow(world, itemstack1, (LivingEntity) entityhuman);
        cancel_BF = false;

        boolean flag = entityhuman.abilities.creativeMode || EnchantmentHelper.getLevel(Enchantments.INFINITY, itemstack) > 0;
        boolean flag1 = flag && itemstack1.getItem() == Items.ARROW;

        entityarrow.setVelocity(entityhuman, entityhuman.pitch, entityhuman.yaw, 0.0F, 3.0F, 1.0F);
        int k = EnchantmentHelper.getLevel(Enchantments.POWER, itemstack);
        if (k > 0) entityarrow.setDamage(entityarrow.getDamage() + (double) k * 0.5D + 0.5D);

        int l = EnchantmentHelper.getLevel(Enchantments.PUNCH, itemstack);
        if (l > 0) entityarrow.setPunch(l);
        if (EnchantmentHelper.getLevel(Enchantments.FLAME, itemstack) > 0) entityarrow.setOnFireFor(100);

        org.bukkit.event.entity.EntityShootBowEvent event = CraftEventFactory.callEntityShootBowEvent(entityhuman, itemstack, itemstack1, entityarrow, entityhuman.getActiveHand(), 0f, !flag1);
        if (event.isCancelled()) 
            cancel_BF = true;
        return entityarrow;
    }
    */

}