package org.cardboardpowered.bridge.world.entity.raid;

import net.minecraft.world.entity.raid.Raider;

public interface RaidBridge {
    boolean isInProgress();

    java.util.Collection<Raider> getRaiders();
}
