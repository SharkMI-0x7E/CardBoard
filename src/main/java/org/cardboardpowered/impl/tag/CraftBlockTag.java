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
package org.cardboardpowered.impl.tag;

import java.util.HashMap;
import java.util.Set;
import java.util.stream.Collectors;
import net.minecraft.core.Registry;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import org.bukkit.Material;
import org.bukkit.craftbukkit.util.CraftMagicNumbers;

public class CraftBlockTag extends CraftTag<Block, Material> {

    /*public BlockTagImpl(TagGroup<Block> registry, Identifier tag) {
        super(registry, tag);
    }

    @Override
    public boolean isTagged(Material item) {
        try {
            return getHandle().contains(CraftMagicNumbers.getBlock(item));
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public Set<Material> getValues() {
        HashMap<Material, Block> map = new HashMap<>();
        for (Block block : getHandle().values()) {
            try {
                map.put(CraftMagicNumbers.getMaterial(block), block);
            } catch (Exception e) {
            }
        }
        return map.keySet();
    }*/
    
    public CraftBlockTag(Registry<Block> registry, TagKey<Block> tag) {
        super(registry, tag);
    }

    public boolean isTagged(Material item) {
        Block block = CraftMagicNumbers.getBlock(item);
        if (block == null) {
            return false;
        }
        return block.builtInRegistryHolder().is(this.tag);
    }

    public Set<Material> getValues() {
        return this.getHandle().stream().map(block -> CraftMagicNumbers.getMaterial((Block)block.value())).collect(Collectors.toUnmodifiableSet());
    }

}