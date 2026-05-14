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