/**
 * Cardboard - Spigot/Paper for Fabric
 * Copyright (C) 2020-2026 CardboardPowered.org and contributors
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
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