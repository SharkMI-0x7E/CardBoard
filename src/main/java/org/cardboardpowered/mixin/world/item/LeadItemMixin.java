package org.cardboardpowered.mixin.world.item;

import org.bukkit.craftbukkit.event.CraftEventFactory;
import org.cardboardpowered.bridge.world.entity.EntityBridge;
import org.cardboardpowered.bridge.server.level.ServerPlayerBridge;
import org.bukkit.Bukkit;
import org.bukkit.block.BlockFace;
import org.bukkit.craftbukkit.block.CraftBlock;
import org.bukkit.entity.Hanging;
import org.bukkit.entity.Player;
import org.bukkit.event.hanging.HangingPlaceEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.cardboardpowered.util.MixinInfo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import java.util.Iterator;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.decoration.LeashFenceKnotEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.LeadItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

@MixinInfo(events = {"HangingPlaceEvent"})
@Mixin(value = LeadItem.class, priority = 900)
public class LeadItemMixin extends Item {

    public LeadItemMixin(net.minecraft.world.item.Item.Properties settings) {
        super(settings);
    }

    /**
     * @author
     * @reason
     */
    @Overwrite
    public static InteractionResult bindPlayerMobs(net.minecraft.world.entity.player.Player player, Level world, BlockPos pos) {
        LeashFenceKnotEntity leashKnotEntity = null;
        boolean bl = false;
        double d = 7.0;
        int i = pos.getX();
        int j = pos.getY();
        int k = pos.getZ();
        List<Mob> list = world.getEntitiesOfClass(Mob.class, new AABB((double)i - 7.0, (double)j - 7.0, (double)k - 7.0, (double)i + 7.0, (double)j + 7.0, (double)k + 7.0));
        Iterator var11 = list.iterator();

        while(var11.hasNext()) {
            Mob mobEntity = (Mob)var11.next();
            if (mobEntity.getLeashHolder() == player) {
                if (leashKnotEntity == null) {
                    leashKnotEntity = LeashFenceKnotEntity.getOrCreateKnot(world, pos);

                    HangingPlaceEvent event = new HangingPlaceEvent((Hanging) ((EntityBridge) leashKnotEntity).getBukkitEntity(), player != null ? (Player) ((ServerPlayerBridge) player).getBukkitEntity() : null, CraftBlock.at((ServerLevel) world, pos), BlockFace.SELF, EquipmentSlot.HAND);
                    Bukkit.getPluginManager().callEvent(event);

                    if (event.isCancelled()) {
                        leashKnotEntity.discard();
                        return InteractionResult.PASS;
                    }
                    leashKnotEntity.playPlacementSound();
                }
                if (CraftEventFactory.callPlayerLeashEntityEvent(mobEntity, leashKnotEntity, player).isCancelled()) {
                    continue;
                }
                mobEntity.setLeashedTo(leashKnotEntity, true);
                bl = true;
            }
        }

        return bl ? InteractionResult.SUCCESS : InteractionResult.PASS;
    }
}
