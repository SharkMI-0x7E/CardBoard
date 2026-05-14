package org.cardboardpowered.mixin.world.entity.ambient;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import org.bukkit.Bukkit;
import org.bukkit.entity.Bat;
import org.bukkit.event.entity.BatToggleSleepEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import org.cardboardpowered.bridge.world.entity.EntityBridge;

@Mixin(net.minecraft.world.entity.ambient.Bat.class)
public class BatMixin {

    
    @Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/ambient/Bat;setResting(Z)V"),
            method = "customServerAiStep")
    public void mobTick_doBatSleepEvent(net.minecraft.world.entity.ambient.Bat bat, boolean sleep) {
        if (handleBatToggleSleepEvent((net.minecraft.world.entity.ambient.Bat)(Object)this, !sleep)) {
            this.setResting(sleep);
        }
    }

    @Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/ambient/Bat;setResting(Z)V"),
            method = "hurtServer")
    public void damage_doBatSleepEvent(net.minecraft.world.entity.ambient.Bat bat, boolean sleep, ServerLevel world, DamageSource source, float amount) {
        if (handleBatToggleSleepEvent((net.minecraft.world.entity.ambient.Bat)(Object)this, true)) {
            this.setResting(false);
        }
    }

    @Shadow
    public void setResting(boolean b) {}

    // note: 1.21.4: awake is always == true.
    private static boolean handleBatToggleSleepEvent(Entity bat, boolean awake) {
        BatToggleSleepEvent event = new BatToggleSleepEvent((Bat) ((EntityBridge)bat).getBukkitEntity(), awake);
        Bukkit.getPluginManager().callEvent(event);
        return !event.isCancelled();
    }

}