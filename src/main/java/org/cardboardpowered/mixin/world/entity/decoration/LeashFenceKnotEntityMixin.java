package org.cardboardpowered.mixin.world.entity.decoration;

import org.bukkit.craftbukkit.event.CraftEventFactory;
import org.cardboardpowered.util.MixinInfo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import java.util.Iterator;
import java.util.List;
import net.minecraft.network.protocol.game.ClientboundSetEntityLinkPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity.RemovalReason;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.decoration.LeashFenceKnotEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;

@MixinInfo(events = {"PlayerLeashEntityEvent", "PlayerUnleashEntityEvent"})
@Mixin(value = LeashFenceKnotEntity.class, priority = 900)
public class LeashFenceKnotEntityMixin {

    private LeashFenceKnotEntity getBF() {
        return (LeashFenceKnotEntity)(Object)this;
    }

    /**
     * @author Cardboard mod
     * @reason PlayerLeashEntityEvent
     */
    @Overwrite
    public InteractionResult interact(Player entityhuman, InteractionHand enumhand) {
        if (getBF().level().isClientSide()) return InteractionResult.SUCCESS;

        boolean flag = false;
        List<Mob> list = getBF().level().getEntitiesOfClass(Mob.class, new AABB(getBF().getX() - 7.0D, getBF().getY() - 7.0D, getBF().getZ() - 7.0D, getBF().getX() + 7.0D, getBF().getY() + 7.0D, getBF().getZ() + 7.0D));
        Iterator<Mob> iterator = list.iterator();
        Mob entityinsentient;
        while (iterator.hasNext()) {
            entityinsentient = (Mob) iterator.next();
            if (entityinsentient.getLeashHolder() == entityhuman) {
                if (CraftEventFactory.callPlayerLeashEntityEvent(entityinsentient, ((LeashFenceKnotEntity)(Object)this), entityhuman).isCancelled()) {
                    ((ServerPlayer) entityhuman).connection.send(new ClientboundSetEntityLinkPacket(entityinsentient, entityinsentient.getLeashHolder()));
                    continue;
                }
                entityinsentient.setLeashedTo((LeashFenceKnotEntity)(Object)this, true);
                flag = true;
            }
        }
        if (flag) return InteractionResult.CONSUME;
        boolean die = true;
        iterator = list.iterator();
        while (iterator.hasNext()) {
            entityinsentient = (Mob) iterator.next();
            if (entityinsentient.isLeashed() && entityinsentient.getLeashHolder() == getBF()) {
                if (CraftEventFactory.callPlayerUnleashEntityEvent(entityinsentient, entityhuman).isCancelled()) {
                    die = false;
                    continue;
                }
                // entityinsentient.detachLeash(true, !entityhuman.getAbilities().creativeMode);
                entityinsentient.dropLeash();
            }
        }
        if (die) getBF().remove(RemovalReason.KILLED);
        return InteractionResult.CONSUME;
    }

}
