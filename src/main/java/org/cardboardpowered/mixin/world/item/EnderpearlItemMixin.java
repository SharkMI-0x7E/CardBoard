package org.cardboardpowered.mixin.world.item;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrownEnderpearl;
import net.minecraft.world.item.EnderpearlItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import org.cardboardpowered.bridge.server.level.ServerPlayerBridge;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.cardboardpowered.bridge.world.entity.EntityBridge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(value = EnderpearlItem.class, priority = 900)
public class EnderpearlItemMixin extends Item {

    public EnderpearlItemMixin(net.minecraft.world.item.Item.Properties settings) {
        super(settings);
    }

    /**
     * @author
     * @reason
     */
    @Overwrite
    public InteractionResult use(Level world, Player user, InteractionHand hand) {
        ItemStack itemStack = user.getItemInHand(hand);
        if (!world.isClientSide()) {
            ThrownEnderpearl enderPearlEntity = new ThrownEnderpearl(world, user, new ItemStack(Items.ENDER_PEARL));
            enderPearlEntity.setItem(itemStack);
            enderPearlEntity.shootFromRotation(user, user.getXRot(), user.getYRot(), 0.0F, 1.5F, 1.0F);
            if (!world.addFreshEntity(enderPearlEntity)) {
                if (user instanceof ServerPlayerBridge) {
                    ((CraftPlayer)((EntityBridge) user).getBukkitEntity()).updateInventory();
                }
                return InteractionResult.FAIL;
                // return TypedActionResult.fail(itemStack);
            }
        }

        world.playSound(null, user.getX(), user.getY(), user.getZ(), SoundEvents.ENDER_PEARL_THROW, SoundSource.NEUTRAL, 0.5F, 0.4F / (world.getRandom().nextFloat() * 0.4F + 0.8F));
        // user.getItemCooldownManager().set((EnderPearlItem)(Object) this, 20);

        user.awardStat(Stats.ITEM_USED.get(this));
        if (!user.getAbilities().instabuild) {
            itemStack.shrink(1);
        }

        // return TypedActionResult.success(itemStack, world.isClient());
        return InteractionResult.SUCCESS;
    }
}
