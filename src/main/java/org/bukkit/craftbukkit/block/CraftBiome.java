package org.bukkit.craftbukkit.block;

import com.google.common.base.Preconditions;

import io.papermc.paper.util.OldEnumHolderable;
import java.util.Locale;
import java.util.Objects;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.block.Biome;
import org.bukkit.craftbukkit.CraftRegistry;
import org.bukkit.craftbukkit.util.CraftNamespacedKey;
import org.bukkit.craftbukkit.util.Handleable;
import org.jspecify.annotations.Nullable;

public class CraftBiome extends OldEnumHolderable<Biome, net.minecraft.world.level.biome.Biome> implements Biome {

	private static int count = 0;

    public static Biome minecraftToBukkit(net.minecraft.world.level.biome.Biome minecraft) {
        return (Biome)CraftRegistry.minecraftToBukkit(minecraft, Registries.BIOME);
    }

    public static Biome minecraftHolderToBukkit(Holder<net.minecraft.world.level.biome.Biome> minecraft) {
        return (Biome)CraftRegistry.minecraftHolderToBukkit(minecraft, Registries.BIOME);
    }

    public static net.minecraft.world.level.biome.@Nullable Biome bukkitToMinecraft(Biome bukkit) {
        if (bukkit == Biome.CUSTOM) {
            return null;
        }
        return (net.minecraft.world.level.biome.Biome)CraftRegistry.bukkitToMinecraft(bukkit);
    }

    public static @Nullable Holder<net.minecraft.world.level.biome.Biome> bukkitToMinecraftHolder(Biome bukkit) {
        if (bukkit == Biome.CUSTOM) {
            return null;
        }
        return CraftRegistry.bukkitToMinecraftHolder(bukkit);
    }

    public CraftBiome(Holder<net.minecraft.world.level.biome.Biome> holder) {
        super(holder, count++);
    }
    
    @Deprecated(forRemoval=true, since="1.21.5")
    // @ApiStatus.ScheduledForRemoval(inVersion="1.22")
    public static class LegacyCustomBiomeImpl
    implements Biome {
        private static final NamespacedKey LEGACY_CUSTOM_KEY = new NamespacedKey("minecraft", "custom");
        private final int ordinal = count++;

        public NamespacedKey getKey() {
            return LEGACY_CUSTOM_KEY;
        }

        public int compareTo(Biome other) {
            return this.ordinal - other.ordinal();
        }

        public String name() {
            return "CUSTOM";
        }

        public int ordinal() {
            return this.ordinal;
        }

        public boolean equals(Object object) {
            if (object == null || this.getClass() != object.getClass()) {
                return false;
            }
            LegacyCustomBiomeImpl that = (LegacyCustomBiomeImpl)object;
            return this.ordinal == that.ordinal;
        }

        public int hashCode() {
            return Objects.hashCode(this.ordinal);
        }

        public String toString() {
            return "CUSTOM";
        }
    }
	
	/*
	 private static int count = 0;
	
    public static Biome minecraftToBukkit(net.minecraft.world.biome.Biome minecraft) {
        Preconditions.checkArgument(minecraft != null);

        net.minecraft.registry.Registry<net.minecraft.world.biome.Biome> registry = CraftRegistry.getMinecraftRegistry(RegistryKeys.BIOME);
        Biome bukkit = Registry.BIOME.get(CraftNamespacedKey.fromMinecraft(registry.getKey(minecraft).orElseThrow().getValue()));

        if (bukkit == null) {
            return Biome.CUSTOM;
        }

        return bukkit;
    }

    public static Biome minecraftHolderToBukkit(RegistryEntry<net.minecraft.world.biome.Biome> minecraft) {
        return CraftBiome.minecraftToBukkit(minecraft.value());
    }

    public static net.minecraft.world.biome.Biome bukkitToMinecraft(Biome bukkit) {
        if (bukkit == null || bukkit == Biome.CUSTOM) {
            return null;
        }

        return CraftRegistry.getMinecraftRegistry(RegistryKeys.BIOME)
                .getOptionalValue(CraftNamespacedKey.toMinecraft(bukkit.getKey())).orElseThrow();
    }

    public static RegistryEntry<net.minecraft.world.biome.Biome> bukkitToMinecraftHolder(Biome bukkit) {
        if (bukkit == null || bukkit == Biome.CUSTOM) {
            return null;
        }

        net.minecraft.registry.Registry<net.minecraft.world.biome.Biome> registry = CraftRegistry.getMinecraftRegistry(RegistryKeys.BIOME);

        if (registry.getEntry(CraftBiome.bukkitToMinecraft(bukkit)) instanceof RegistryEntry.Reference<net.minecraft.world.biome.Biome> holder) {
            return holder;
        }

        throw new IllegalArgumentException("No Reference holder found for " + bukkit
                + ", this can happen if a plugin creates its own biome base with out properly registering it.");
    }
    
    private final NamespacedKey key;
    private final net.minecraft.world.biome.Biome biomeBase;
    private final String name;
    private final int ordinal;

    public CraftBiome(NamespacedKey key, net.minecraft.world.biome.Biome biomeBase) {
        this.key = key;
        this.biomeBase = biomeBase;
        // For backwards compatibility, minecraft values will stile return the uppercase name without the namespace,
        // in case plugins use for example the name as key in a config file to receive biome specific values.
        // Custom biomes will return the key with namespace. For a plugin this should look than like a new biome
        // (which can always be added in new minecraft versions and the plugin should therefore handle it accordingly).
        if (NamespacedKey.MINECRAFT.equals(key.getNamespace())) {
            this.name = key.getKey().toUpperCase(Locale.ROOT);
        } else {
            this.name = key.toString();
        }
        this.ordinal = CraftBiome.count++;
    }

    @Override
    public net.minecraft.world.biome.Biome getHandle() {
        return this.biomeBase;
    }

    @Override
    public NamespacedKey getKey() {
        return this.key;
    }

    @Override
    public int compareTo(Biome biome) {
        return this.ordinal - biome.ordinal();
    }

    @Override
    public String name() {
        return this.name;
    }

    @Override
    public int ordinal() {
        return this.ordinal;
    }

    @Override
    public String toString() {
        // For backwards compatibility
        return this.name();
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }

        if (!(other instanceof CraftBiome otherBiome)) {
            return false;
        }

        return this.getKey().equals(otherBiome.getKey());
    }

    @Override
    public int hashCode() {
        return this.getKey().hashCode();
    }*/

}