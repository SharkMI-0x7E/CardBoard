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

import java.util.Objects;
import java.util.Optional;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.tags.TagKey;
import org.bukkit.Keyed;
import org.bukkit.NamespacedKey;
import org.bukkit.Tag;
import org.bukkit.craftbukkit.util.CraftNamespacedKey;

/**
 * Cardboard Implementation of {@link org.bukkit.Tag}
 */
public abstract class CraftTag<N, B extends Keyed> implements Tag<B> {

	protected final net.minecraft.core.Registry<N> registry;
	protected final TagKey<N> tag;
	private HolderSet.Named<N> handle;

	public CraftTag(Registry<N> registry, TagKey<N> tag) {
		this.registry = registry;
		this.tag = tag;
		
		Optional< HolderSet.Named<N> > handleOptional = registry.get(this.tag);
		this.handle = handleOptional.orElseThrow();
	}

	public HolderSet.Named<N> getHandle() {
		return handle;
	}

	@Override
	public NamespacedKey getKey() {
		return CraftNamespacedKey.fromMinecraft(tag.location());
	}

	@Override
	public int hashCode() {
		int hash = 3;
		hash = 59 * hash + Objects.hashCode(this.registry);
		return 59 * hash + Objects.hashCode(this.tag);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		} else {
			return !(obj instanceof CraftTag<?, ?> other) ? false : Objects.equals(this.registry, other.registry) && Objects.equals(this.tag, other.tag);
		}
	}

	@Override
	public String toString() {
		return "CraftTag{" + this.tag + "}";
	}

}