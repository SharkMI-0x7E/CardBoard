package org.cardboardpowered.mixin.world.entity.raid;

import net.minecraft.world.entity.raid.Raid;
import net.minecraft.world.entity.raid.Raider;
import org.cardboardpowered.bridge.world.entity.raid.RaidBridge;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Map;
import java.util.Set;

@Mixin(Raid.class)
public class RaidMixin implements RaidBridge {
    @Shadow
    private Raid.RaidStatus status;

    @Shadow
    @Final
    private Map<Integer, Set<Raider>> groupRaiderMap;

    // CraftBukkit start
    @Override
    public boolean isInProgress() {
        return this.status == Raid.RaidStatus.ONGOING;
    }
    // CraftBukkit end

    // CraftBukkit start - a method to get all raiders
    @Override
    public java.util.Collection<Raider> getRaiders() {
        return this.groupRaiderMap.values().stream().flatMap(Set::stream).collect(java.util.stream.Collectors.toSet());
    }
    // CraftBukkit end
}