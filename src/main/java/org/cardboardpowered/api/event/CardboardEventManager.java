package org.cardboardpowered.api.event;

import org.bukkit.craftbukkit.event.CraftEventFactory;
import org.cardboardpowered.bridge.world.entity.EntityBridge;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.entity.CraftEntity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Vehicle;
import org.bukkit.event.vehicle.VehicleEnterEvent;
import org.bukkit.event.vehicle.VehicleExitEvent;

public class CardboardEventManager {

    public static CardboardEventManager INSTANCE = new CardboardEventManager();

    public void callCardboardEvents() {
        this.callCardboardFireworkExplodeEvent();
        this.callCardboardEntityMountEvent();
    }

    private void callCardboardFireworkExplodeEvent() {
        CardboardFireworkExplodeEvent.EVENT.register((firework) -> {
            if (CraftEventFactory.callFireworkExplodeEvent(firework).isCancelled()) {
                return InteractionResult.FAIL;
            }else {
                return InteractionResult.PASS;
            }
        });
    }

    private void callCardboardEntityMountEvent() {
        CardboardEntityMountEvent.EVENT.register((vehicle, entity) -> {
            if (vehicle.getPassengers().isEmpty()) {
                CraftEntity craft = (CraftEntity) ((EntityBridge) vehicle).getBukkitEntity().getVehicle();
                Entity orig = craft == null ? null : craft.getHandle();
                if (((EntityBridge) vehicle).getBukkitEntity() instanceof Vehicle && ((EntityBridge) vehicle).getBukkitEntity() instanceof org.bukkit.entity.LivingEntity) {
                    VehicleExitEvent CBevent = new VehicleExitEvent(
                            (Vehicle) ((EntityBridge) vehicle).getBukkitEntity(),
                            (LivingEntity) ((EntityBridge) entity).getBukkitEntity()
                    );
                    if (((EntityBridge) entity).isValidBF()) {
                        Bukkit.getPluginManager().callEvent(CBevent);
                    }
                    CraftEntity craftn = (CraftEntity) ((EntityBridge) vehicle).getBukkitEntity().getVehicle();
                    Entity n = craftn == null ? null : craftn.getHandle();
                    if (CBevent.isCancelled() || n != orig) {
                        return InteractionResult.FAIL;
                    }
                }

                org.bukkit.event.entity.EntityDismountEvent SPevent = new org.bukkit.event.entity.EntityDismountEvent(((EntityBridge) vehicle).getBukkitEntity(), ((EntityBridge) entity).getBukkitEntity());
                if (((EntityBridge) vehicle).isValidBF()) {
                    Bukkit.getPluginManager().callEvent(SPevent);
                }
                if (SPevent.isCancelled()) {
                    return InteractionResult.FAIL;
                }
            }
            if (!vehicle.getPassengers().isEmpty()) {
                com.google.common.base.Preconditions.checkState(!vehicle.getPassengers().contains(vehicle), "Circular entity riding! %s %s", this, entity);

                CraftEntity craft = (CraftEntity) ((EntityBridge) vehicle).getBukkitEntity().getVehicle();
                Entity orig = craft == null ? null : craft.getHandle();
                if (((EntityBridge) entity).getBukkitEntity() instanceof Vehicle && ((EntityBridge) vehicle).getBukkitEntity() instanceof org.bukkit.entity.LivingEntity) {
                    VehicleEnterEvent CBevent = new VehicleEnterEvent(
                            (Vehicle) ((EntityBridge) entity).getBukkitEntity(),
                            ((EntityBridge) vehicle).getBukkitEntity()
                            );
                    if (((EntityBridge) entity).isValidBF()) {
                        Bukkit.getPluginManager().callEvent(CBevent);
                    }
                    CraftEntity craftn = (CraftEntity) ((EntityBridge) vehicle).getBukkitEntity().getVehicle();
                    Entity n = craftn == null ? null : craftn.getHandle();
                    if (CBevent.isCancelled() || n != orig) {
                        return InteractionResult.FAIL;
                    }
                }

                org.bukkit.event.entity.EntityMountEvent SPevent = new org.bukkit.event.entity.EntityMountEvent(((EntityBridge) vehicle).getBukkitEntity(), ((EntityBridge) entity).getBukkitEntity());
                if (((EntityBridge) entity).isValidBF()) {
                    Bukkit.getPluginManager().callEvent(SPevent);
                    if (SPevent.isCancelled()) {
                        return InteractionResult.FAIL;
                    }
                }
            }
            return InteractionResult.PASS;
        });
    }
}
