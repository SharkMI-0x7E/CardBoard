package org.cardboardpowered.bridge.world.entity;

import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import org.bukkit.craftbukkit.attribute.CraftAttributeMap;
import org.bukkit.event.entity.EntityPotionEffectEvent;
import org.jspecify.annotations.Nullable;

public interface LivingEntityBridge {

    int getExpReward();

    void pushEffectCause(EntityPotionEffectEvent.Cause cause);

    CraftAttributeMap cardboard_getAttr();

    @Nullable ItemEntity cardboard$drop(ItemStack stack, boolean randomizeMotion, boolean includeThrower);

    @Nullable ItemEntity cardboard$drop(ItemStack stack, boolean randomizeMotion, boolean includeThrower, boolean callEvent, java.util.function.@Nullable Consumer<org.bukkit.entity.Item> entityOperation);
}