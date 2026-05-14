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
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.bukkit.craftbukkit.event.CraftEventFactory;
import org.cardboardpowered.bridge.server.level.ServerPlayerBridge;
import org.cardboardpowered.bridge.world.entity.EntityBridge;

@MixinInfo(events = {"EntityShootBowEvent"})
@Mixin(value = ProjectileWeaponItem.class, priority = 900)
public class ProjectileWeaponItemMixin {

    @Shadow
    public Projectile createProjectile(Level world, LivingEntity shooter, ItemStack weaponStack, ItemStack projectileStack, boolean critical) {
        return null;
    }
    
    @Shadow
    public void shootProjectile(LivingEntity shooter, Projectile projectile, int index, float speed, float divergence, float yaw, LivingEntity target) {
    }
    
    @Shadow
    public int getDurabilityUse(ItemStack projectile) {
        return 0;
    }
    
    @Inject(at = @At("HEAD"), method = "shoot(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/InteractionHand;Lnet/minecraft/world/item/ItemStack;Ljava/util/List;FFZLnet/minecraft/world/entity/LivingEntity;)V", cancellable = true)
    private void cardboard$onShoot(ServerLevel world, LivingEntity shooter, InteractionHand hand, ItemStack stack, List<ItemStack> projectiles, float speed, float divergence, boolean critical, LivingEntity target, CallbackInfo ci) {
        float f = 10.0f;
        float g = projectiles.size() == 1 ? 0.0f : 20.0f / (float)(projectiles.size() - 1);
        float h = (float)((projectiles.size() - 1) % 2) * g / 2.0f;
        float i = 1.0f;
        
        for (int j = 0; j < projectiles.size(); ++j) {
            ItemStack itemStack = projectiles.get(j);
            if (itemStack.isEmpty()) {
                continue;
            }
            
            float k = h + i * (float)((j + 1) / 2) * g;
            i = -i;
            
            Projectile projectileEntity = this.createProjectile(world, shooter, stack, itemStack, critical);
            this.shootProjectile(shooter, projectileEntity, j, speed, divergence, k, target);
            
            EntityShootBowEvent event = CraftEventFactory.callEntityShootBowEvent(shooter, stack, itemStack, projectileEntity, hand, speed, true);
            if (event.isCancelled()) {
                event.getProjectile().remove();
                continue;
            }
            
            stack.hurtAndBreak(this.getDurabilityUse(itemStack), shooter, hand.asEquipmentSlot());
            
            if (event.getProjectile() != ((EntityBridge)projectileEntity).getBukkitEntity() || world.addFreshEntity(projectileEntity)) {
                continue;
            }
            
            if (shooter instanceof ServerPlayer) {
                ((Player) ((ServerPlayerBridge) ((ServerPlayer)shooter)).getBukkitEntity()).updateInventory();
            }
            return;
        }
        
        ci.cancel();
    }

}
