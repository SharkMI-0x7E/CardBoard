package org.cardboardpowered.bridge.world.entity;

import net.minecraft.world.entity.LivingEntity;
import org.bukkit.event.entity.EntityTargetEvent;
import org.jspecify.annotations.Nullable;

public interface MobBridge {
    boolean cardboard$setTarget(@Nullable LivingEntity target, EntityTargetEvent.@Nullable TargetReason reason);
}
