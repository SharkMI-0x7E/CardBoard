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