package org.bukkit.craftbukkit.packs;

import com.google.common.base.Preconditions;
import java.util.Collection;
import java.util.Objects;
import java.util.stream.Collectors;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.storage.PrimaryLevelData;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.block.BlockType;
import org.bukkit.craftbukkit.block.CraftBlockType;
import org.bukkit.craftbukkit.inventory.CraftItemType;
import org.bukkit.craftbukkit.packs.CraftDataPack;
import org.bukkit.craftbukkit.util.CraftMagicNumbers;
import org.bukkit.inventory.ItemType;
// import org.bukkit.packs.DataPack;
// import org.bukkit.packs.DataPackManager;
import org.cardboardpowered.impl.world.CraftWorld;
import org.jetbrains.annotations.NotNull;

/**
 * Removed in Paper 1.21.9+
 */
@Deprecated
public class CraftDataPackManager /* implements DataPackManager */ {

    private final PackRepository handle;

    public CraftDataPackManager(PackRepository resourcePackRepository) {
        this.handle = resourcePackRepository;
    }

    public PackRepository getHandle() {
        return this.handle;
    }

    public Collection<CraftDataPack> getDataPacks() {
        this.getHandle().reload();
        Collection<Pack> availablePacks = this.getHandle().getAvailablePacks();
        return availablePacks.stream().map(CraftDataPack::new).collect(Collectors.toUnmodifiableList());
    }

    public CraftDataPack getDataPack(NamespacedKey namespacedKey) {
        Preconditions.checkArgument((namespacedKey != null ? 1 : 0) != 0, (Object)"namespacedKey cannot be null");
        return new CraftDataPack(this.getHandle().getPack(namespacedKey.getKey()));
    }

    public Collection<CraftDataPack> getEnabledDataPacks(World world) {
        Preconditions.checkArgument((world != null ? 1 : 0) != 0, (Object)"world cannot be null");
        CraftWorld craftWorld = (CraftWorld)world;
        return ((PrimaryLevelData)craftWorld.getHandle().serverLevelData).getDataConfiguration().dataPacks().getEnabled().stream().map(packName -> {
            Pack resourcePackLoader = this.getHandle().getPack((String)packName);
            if (resourcePackLoader != null) {
                return new CraftDataPack(resourcePackLoader);
            }
            return null;
        }).filter(Objects::nonNull).collect(Collectors.toUnmodifiableList());
    }

    public Collection<CraftDataPack> getDisabledDataPacks(World world) {
        Preconditions.checkArgument((world != null ? 1 : 0) != 0, (Object)"world cannot be null");
        CraftWorld craftWorld = (CraftWorld)world;

        return ((PrimaryLevelData)craftWorld.getHandle().serverLevelData).getDataConfiguration().dataPacks().getDisabled().stream().map(packName -> {
            Pack resourcePackLoader = this.getHandle().getPack((String)packName);
            if (resourcePackLoader != null) {
                return new CraftDataPack(resourcePackLoader);
            }
            return null;
        }).filter(Objects::nonNull).collect(Collectors.toUnmodifiableList());
    }

    public boolean isEnabledByFeature(Material material, World world) {
        Preconditions.checkArgument((material != null ? 1 : 0) != 0, (Object)"material cannot be null");
        Preconditions.checkArgument((material.isItem() || material.isBlock() ? 1 : 0) != 0, (Object)"material need to be a item or block");
        Preconditions.checkArgument((world != null ? 1 : 0) != 0, (Object)"world cannot be null");
        CraftWorld craftWorld = (CraftWorld)world;
        if (material.isItem()) {
            return CraftMagicNumbers.getItem(material).isEnabled(craftWorld.getHandle().enabledFeatures());
        }
        if (material.isBlock()) {
            return CraftMagicNumbers.getBlock(material).isEnabled(craftWorld.getHandle().enabledFeatures());
        }
        return false;
    }

    public boolean isEnabledByFeature(org.bukkit.entity.EntityType entityType, World world) {
        Preconditions.checkArgument((entityType != null ? 1 : 0) != 0, (Object)"entityType cannot be null");
        Preconditions.checkArgument((world != null ? 1 : 0) != 0, (Object)"world cannot be null");
        Preconditions.checkArgument((entityType != org.bukkit.entity.EntityType.UNKNOWN ? 1 : 0) != 0, (Object)"EntityType.UNKNOWN its not allowed here");
        CraftWorld craftWorld = (CraftWorld)world;
        EntityType<?> nmsEntity = BuiltInRegistries.ENTITY_TYPE.getValue(Identifier.parse(entityType.getKey().getKey()));
        return nmsEntity.isEnabled(craftWorld.getHandle().enabledFeatures());
    }

    // 1.20.6 API
    
	// @Override
	public boolean isEnabledByFeature(@NotNull ItemType itemType, @NotNull World world) {
		// TODO Auto-generated method stub
        return CraftItemType.bukkitToMinecraftNew((ItemType)itemType.typed()).isEnabled( ((CraftWorld)world) .getHandle().enabledFeatures());
	}

	// @Override
	public boolean isEnabledByFeature(@NotNull BlockType blockType, @NotNull World world) {
		CraftWorld craftWorld = (CraftWorld)world;
		return CraftBlockType.bukkitToMinecraftNew((BlockType)blockType.typed()).isEnabled(craftWorld.getHandle().enabledFeatures());
	}
}

