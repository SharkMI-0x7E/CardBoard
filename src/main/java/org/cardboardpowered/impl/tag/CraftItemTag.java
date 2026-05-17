/**
 * Copyright (C) 2026 SharkMI and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 */
package org.cardboardpowered.impl.tag;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;
import net.minecraft.core.Registry;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import org.bukkit.Material;
import org.bukkit.craftbukkit.util.CraftMagicNumbers;

public class CraftItemTag extends CraftTag<Item, Material> {

    /*public ItemTagImpl(TagGroup<Item> registry, Identifier tag) {
        super(registry, tag);
    }

    @Override
    public boolean isTagged(Material item) {
        return getHandle().contains(CraftMagicNumbers.getItem(item));
    }

    @Override
    public Set<Material> getValues() {
        return Collections.unmodifiableSet(getHandle().values().stream().map((item) -> CraftMagicNumbers.getMaterial(item)).collect(Collectors.toSet()));
    }*/
    
    public CraftItemTag(Registry<Item> registry, TagKey<Item> tag) {
        super(registry, tag);
    }

    public boolean isTagged(Material item) {
        Item minecraft = CraftMagicNumbers.getItem(item);
        if (minecraft == null) {
            return false;
        }
        return minecraft.builtInRegistryHolder().is(this.tag);
    }

    public Set<Material> getValues() {
        return this.getHandle().stream().map(item -> CraftMagicNumbers.getMaterial((Item)item.value())).collect(Collectors.toUnmodifiableSet());
    }

}