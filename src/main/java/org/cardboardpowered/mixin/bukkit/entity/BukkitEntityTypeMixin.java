/**
 * Cardboard - Spigot/Paper for Fabric
 * Copyright (C) 2020-2024 CardboardPowered.org and contributors
 * Copyright (C) 2025-2026 SharkMI and contributors
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
package org.cardboardpowered.mixin.bukkit.entity;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.bukkit.NamespacedKey;
import org.bukkit.entity.EntityType;
import org.cardboardpowered.bridge.bukkit.entity.BukkitEntityTypeBridge;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(value = EntityType.class, remap = false)
public class BukkitEntityTypeMixin implements BukkitEntityTypeBridge {

	@Shadow
	private static final Map<String, EntityType> NAME_MAP = new HashMap<String, EntityType>();
	
	@Shadow
    private static final Map<Short, EntityType> ID_MAP = new HashMap<Short, EntityType>();

	@Shadow
	@Final
	@Mutable
	private NamespacedKey key;
	
	@Override
	public void cardboard$setKey(NamespacedKey newKey) {
		this.key = newKey;
	}
	
	@Override
	public void cardboard$addToMaps(String key1, int key2) {
		EntityType type = (EntityType) (Object) this;
		NAME_MAP.put(key1.toLowerCase(), type);
		ID_MAP.put((short) key2, type);
	}
	
	@Overwrite(remap = false)
	public static EntityType fromName(String name) {
        if (name == null) {
            return null;
        }
        return NAME_MAP.get(name.toLowerCase(Locale.ROOT));
    }
	
	@Overwrite(remap = false)
	public static EntityType fromId(int id) {
        if (id > Short.MAX_VALUE) {
            return null;
        }
        return ID_MAP.get((short) id);
    }


}