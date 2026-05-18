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
package org.cardboardpowered.impl.tag;

import java.util.Set;
import java.util.stream.Collectors;
import net.minecraft.core.Registry;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import org.bukkit.craftbukkit.entity.CraftEntityType;
//import org.bukkit.Registry;
import org.bukkit.craftbukkit.util.CraftNamespacedKey;
import java.util.Objects;

public class CraftEntityTag extends CraftTag<EntityType<?>, org.bukkit.entity.EntityType> {
    /*public EntityTagImpl(TagGroup<EntityType<?>> registry, Identifier tag) {
        super(registry, tag);
    }

    @Override
    public boolean isTagged(org.bukkit.entity.EntityType entity) {
        return this.getHandle().contains(net.minecraft.util.registry.Registry.ENTITY_TYPE.get(CraftNamespacedKey.toMinecraft(entity.getKey())));
    }

    @Override
    public Set<org.bukkit.entity.EntityType> getValues() {
        return Collections.unmodifiableSet(this.getHandle().values().stream().map(nms -> Registry.ENTITY_TYPE.get(CraftNamespacedKey.fromMinecraft(EntityType.getId(nms)))).collect(Collectors.toSet()));
    }*/
    
    public CraftEntityTag(Registry<EntityType<?>> registry, TagKey<EntityType<?>> tag) {
        super(registry, tag);
    }
    
    public boolean isTagged(org.bukkit.entity.EntityType entity) {
        return CraftEntityType.bukkitToMinecraft(entity).is(this.tag);
    }

    /*
    public boolean isTagged(org.bukkit.entity.EntityType entity) {
        return this.registry.entryOf(RegistryKey.of(RegistryKeys.ENTITY_TYPE, CraftNamespacedKey.toMinecraft(entity.getKey()))).isIn(this.tag);
    }
    */

    public Set<org.bukkit.entity.EntityType> getValues() {
        return this.getHandle().stream().map(nms -> (org.bukkit.entity.EntityType)org.bukkit.Registry.ENTITY_TYPE.get(CraftNamespacedKey.fromMinecraft(EntityType.getKey((EntityType)nms.value())))).filter(Objects::nonNull).collect(Collectors.toUnmodifiableSet());
    }
}

