package org.cardboardpowered.mixin.world.item;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.sheep.Sheep;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.ItemStack;
import org.bukkit.Bukkit;
import org.bukkit.event.entity.SheepDyeWoolEvent;
import org.cardboardpowered.util.MixinInfo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import org.cardboardpowered.bridge.world.entity.EntityBridge;

@MixinInfo(events = {"SheepDyeWoolEvent"})
@Mixin(value = DyeItem.class, priority = 900)
public class DyeItemMixin {

    @Shadow
    public DyeColor dyeColor;

    /**
     * @reason .
     * @author .
     */
    @SuppressWarnings("deprecation")
    @Overwrite
    public InteractionResult interactLivingEntity(ItemStack itemstack, Player entityhuman, LivingEntity entityliving, InteractionHand enumhand) {
        if (!(entityliving instanceof Sheep)) return InteractionResult.PASS;

        Sheep entitysheep = (Sheep) entityliving;
        if (entitysheep.isAlive() && !entitysheep.isSheared() && entitysheep.getColor() != this.dyeColor) {
            if (!entityhuman.level().isClientSide()) {
                byte bColor = (byte) this.dyeColor.getId();
                SheepDyeWoolEvent event = new SheepDyeWoolEvent((org.bukkit.entity.Sheep) ((EntityBridge)entitysheep).getBukkitEntity(), org.bukkit.DyeColor.getByWoolData(bColor));
                Bukkit.getServer().getPluginManager().callEvent(event);
                if (event.isCancelled()) return InteractionResult.PASS;

                entitysheep.setColor(DyeColor.byId((byte) event.getColor().getWoolData()));
                itemstack.shrink(1);
            }
            return InteractionResult.SUCCESS; // ActionResult.success(entityhuman.getWorld().isClient);
        }
        return InteractionResult.PASS;
    }

}
