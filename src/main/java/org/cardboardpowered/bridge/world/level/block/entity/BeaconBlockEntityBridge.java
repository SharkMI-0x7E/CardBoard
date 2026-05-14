package org.cardboardpowered.bridge.world.level.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BeaconBlockEntity;
import net.minecraft.world.phys.AABB;
import org.cardboardpowered.bridge.world.entity.EntityBridge;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Objects;

public interface BeaconBlockEntityBridge {
    // Paper start - diff out applyEffects logic components
    // Generally smarter than spigot trying to split the logic up, as that diff is giant.
    static int computeEffectDuration(final int beaconLevel) {
        return (9 + beaconLevel * 2) * 20; // Diff from applyEffects
    }

    static int computeEffectAmplifier(final int beaconLevel, @Nullable Holder<MobEffect> primaryEffect, @Nullable Holder<MobEffect> secondaryEffect) {
        int i = 0;
        if (beaconLevel >= 4 && Objects.equals(primaryEffect, secondaryEffect)) {
            i = 1;
        }
        return i;
    }

    private static double computeBeaconRange(final int beaconLevel) {
        return beaconLevel * 10 + 10; // Diff from applyEffects
    }

    public static List<Player> getHumansInRange(final Level level, final BlockPos pos, final int beaconLevel, final @Nullable BeaconBlockEntity blockEntity) {
        final double d = blockEntity != null ? ((BeaconBlockEntityBridge)blockEntity).cardboard$getEffectRange() : computeBeaconRange(beaconLevel);
        AABB aabb = new AABB(pos).inflate(d).expandTowards(0.0, level.getHeight(), 0.0); // Diff from applyEffects
        // Improve performance of human lookup by switching to a global player iteration when searching over 128 blocks
        List<Player> list;
        if (d <= 128.0) {
            list = level.getEntitiesOfClass(Player.class, aabb); // Diff from applyEffect
        } else {
            list = new java.util.ArrayList<>();
            for (final Player player : level.players()) {
                if (!net.minecraft.world.entity.EntitySelector.NO_SPECTATORS.test(player)) continue;
                if (player.getBoundingBox().intersects(aabb)) {
                    list.add(player);
                }
            }
        }
        return list;
    }

    static boolean hasSecondaryEffect(final int beaconLevel, final Holder<MobEffect> primaryEffect, final @Nullable Holder<MobEffect> secondaryEffect) {
        return beaconLevel >= 4 && !Objects.equals(primaryEffect, secondaryEffect) && secondaryEffect != null;
    }
    // Paper end - diff out applyEffects logic components

    // Paper start - BeaconEffectEvent
    private static void applyEffectsAndCallEvent(final Level level, final BlockPos position, final List<Player> players, final MobEffectInstance mobEffectInstance, final boolean isPrimary) {
        final org.bukkit.potion.PotionEffect apiEffect = org.bukkit.craftbukkit.potion.CraftPotionUtil.toBukkit(mobEffectInstance);
        final org.bukkit.craftbukkit.block.CraftBlock apiBlock = org.bukkit.craftbukkit.block.CraftBlock.at(level, position);
        for (final Player player : players) {
            final com.destroystokyo.paper.event.block.BeaconEffectEvent event = new com.destroystokyo.paper.event.block.BeaconEffectEvent(
                    apiBlock, apiEffect, (org.bukkit.entity.Player) ((EntityBridge)player).getBukkitEntity(), isPrimary
            );
            if (!event.callEvent()) continue;
            player.addEffect(org.bukkit.craftbukkit.potion.CraftPotionUtil.fromBukkit(event.getEffect()));
        }
    }

    org.bukkit.potion.@Nullable PotionEffect cardboard$getPrimaryEffect();

    org.bukkit.potion.@Nullable PotionEffect cardboard$getSecondaryEffect();

    double cardboard$getEffectRange();

    void cardboard$setEffectRange(double range);

    void cardboard$resetEffectRange();
    // Paper end - BeaconEffectEvent
}
