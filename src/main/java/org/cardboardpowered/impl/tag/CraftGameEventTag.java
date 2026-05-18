/**
 * Cardboard - Spigot/Paper for Fabric
 * Copyright (C) 2020-2024 CardboardPowered.org and contributors
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
package org.cardboardpowered.impl.tag;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import org.bukkit.GameEvent;
import org.bukkit.NamespacedKey;
import org.bukkit.craftbukkit.util.CraftNamespacedKey;
import org.jetbrains.annotations.NotNull;

public class CraftGameEventTag extends CraftTag<net.minecraft.world.level.gameevent.GameEvent, GameEvent> {
	
	public CraftGameEventTag(Registry<net.minecraft.world.level.gameevent.GameEvent> registry,
			TagKey<net.minecraft.world.level.gameevent.GameEvent> tag) {
		super(registry, tag);
	}

	private static final Map<GameEvent, ResourceKey<net.minecraft.world.level.gameevent.GameEvent>> KEY_CACHE = Collections.synchronizedMap(new IdentityHashMap<>());
	
	@Override
	public boolean isTagged(@NotNull GameEvent gameEvent) {
	    return registry.getOrThrow(KEY_CACHE.computeIfAbsent(gameEvent, event -> ResourceKey.create(Registries.GAME_EVENT, CraftNamespacedKey.toMinecraft(event.getKey())))).is(tag);
	}

	@Override
    public Set<GameEvent> getValues() {
        return getHandle().stream().map(nms -> {
        	NamespacedKey key = CraftNamespacedKey.fromMinecraft(BuiltInRegistries.GAME_EVENT.getKey(nms.value()));
        	return GameEvent.getByKey(key);
        }).collect(Collectors.toUnmodifiableSet());
	}

}