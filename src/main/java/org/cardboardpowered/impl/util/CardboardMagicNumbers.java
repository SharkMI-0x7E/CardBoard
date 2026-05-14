package org.cardboardpowered.impl.util;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import io.izzel.arclight.api.EnumHelper;
import io.izzel.arclight.api.Unsafe;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.craftbukkit.legacy.CraftLegacy;
import org.bukkit.craftbukkit.util.CraftMagicNumbers;
import org.bukkit.craftbukkit.util.CraftNamespacedKey;
import org.cardboardpowered.CardboardMod;
import org.cardboardpowered.bridge.bukkit.BukkitMaterialBridge;
import org.cardboardpowered.impl.CardboardModdedBlock;
import org.cardboardpowered.impl.CardboardModdedItem;

import java.util.*;

public class CardboardMagicNumbers {
    public static final Map<String, Material> BY_NAME = Unsafe.getStatic(Material.class, "BY_NAME");
    private static final List<Class<?>> MAT_CTOR = ImmutableList.of(int.class);
    public static final HashMap<String, Material> MODDED_MATERIALS = new HashMap<>();

    public static final HashMap<Item, Material> MODDED_ITEM_MATERIAL = new HashMap<>();
    public static final HashMap<Material, Item> MODDED_MATERIAL_ITEM = new HashMap<>();

    @Deprecated
    public static void setupUnknownModdedMaterials() {
        for (Material material : Material.values()) {
            if (material.isLegacy()) continue;
            Identifier key = key(material);
            BuiltInRegistries.ITEM.getOptional(key).ifPresent((item) -> CraftMagicNumbers.MATERIAL_ITEM.put(material, item));
            BuiltInRegistries.BLOCK.getOptional(key).ifPresent((block) -> CraftMagicNumbers.MATERIAL_BLOCK.put(material, block));
            //BuiltInRegistries.FLUID.getOptional(key).ifPresent((fluid) -> CraftMagicNumbers.MATERIAL_FLUID.put(material, fluid));
        }
    }

    public static boolean has_mixin_interface(Material m) {
        // Make sure mixin has applied
        if ( (Object) m instanceof BukkitMaterialBridge) {
            return true;
        }
        return false;
    }

    public static void test() {
        // TODO: This needs to be kept updated when Spigot updates
        // It is the value of Material.values().length
        CardboardMod.LOGGER.info("DEB: " + Material.values().length);
        int MATERIAL_LENGTH = 2121; // 1837; //1525;
        int i = MATERIAL_LENGTH - 1;

        List<String> names = new ArrayList<>();
        List<Material> list = new ArrayList<>();

        String lastMod = "";
        for (Block block : BuiltInRegistries.BLOCK) {
            Identifier id = BuiltInRegistries.BLOCK.getKey(block);
            String name = standardize(id);
            String nam = id.getNamespace().toUpperCase(Locale.ROOT) + "_" + id.getPath().toUpperCase(Locale.ROOT);
            if (id.getNamespace().startsWith("minecraft")) {
                boolean has = false;
                try {
                    Material.valueOf(id.getPath().toUpperCase());
                    has = true;
                } catch (IllegalArgumentException e) {
                    // Snapshot or API not updated.
                    has = false;
                    nam = id.getPath().toUpperCase(Locale.ROOT);
                }
                if (has) {
                    continue;
                }
            }

            Material material = BY_NAME.get(name);
            if (null == material && !names.contains(name)) {
                material = EnumHelper.makeEnum(Material.class, name, i, MAT_CTOR, ImmutableList.of(i));
                if (!has_mixin_interface(material)) {
                    CardboardMod.LOGGER.warning("Material not instanceof IMixinMaterial");
                    return;
                }

                ((BukkitMaterialBridge)(Object)material).setModdedData(new CardboardModdedBlock(id.toString()));
                CraftMagicNumbers.MATERIAL_BLOCK.put(material, block);
                BY_NAME.put(name, material);
                list.add(material);
                MODDED_MATERIALS.put(name, material);

                CardboardMod.LOGGER.info("Registering modded block '" + id + "'..");

                if (!(lastMod.equalsIgnoreCase(id.namespace))) {
                    CardboardMod.LOGGER.info("Registering modded blocks from mod '" + (lastMod = id.namespace) + "'..");
                }
            }
            Material m = Material.getMaterial(nam);
            CraftMagicNumbers.BLOCK_MATERIAL.put(block, m);
            CraftMagicNumbers.MATERIAL_BLOCK.put(m, block);
        }

        for (Item item : BuiltInRegistries.ITEM) {
            Identifier id = BuiltInRegistries.ITEM.getKey(item);
            String name = standardize(id);
            String nam = id.getNamespace().toUpperCase(Locale.ROOT) + "_" + id.getPath().toUpperCase(Locale.ROOT);
            if (id.getNamespace().startsWith("minecraft")) {
                boolean has = false;
                try {
                    Material.valueOf(id.getPath().toUpperCase());
                    has = true;
                } catch (IllegalArgumentException e) {
                    // Snapshot or API not updated.
                    nam = id.getPath().toUpperCase(Locale.ROOT);
                    has = false;
                }
                if (has) {
                    continue;
                }
            }

            Material material = BY_NAME.get(name);
            if (null == material && !names.contains(name)) {
                material = EnumHelper.makeEnum(Material.class, name, i, MAT_CTOR, ImmutableList.of(i));
                if (!has_mixin_interface(material)) {
                    CardboardMod.LOGGER.warning("Material not instanceof IMixinMaterial");
                    return;
                }

                ((BukkitMaterialBridge)(Object)material).setModdedData(new CardboardModdedItem(id.toString()));
                CraftMagicNumbers.MATERIAL_ITEM.put(material, item);
                BY_NAME.put(name, material);
                list.add(material);
                MODDED_MATERIALS.put(name, material);

                if (!(lastMod.equalsIgnoreCase(id.namespace)))
                    CardboardMod.LOGGER.info("Registering modded items from mod '" + (lastMod = id.namespace) + "'..");
            }
            Material m = Material.getMaterial(nam);
            CraftMagicNumbers.ITEM_MATERIAL.put(item, m);
            CraftMagicNumbers.MATERIAL_ITEM.put(m, item);
        }

        //for (net.minecraft.fluid.Fluid fluid : Registries.FLUID)
        //    FLUID_MATERIAL.put(fluid, org.bukkit.Registries.FLUID.get(CraftNamespacedKey.fromMinecraft(Registries.FLUID.getId(fluid))));

        EnumHelper.addEnums(Material.class, list);

        for (Material material : list) {
            Identifier key = key(material);
            BuiltInRegistries.ITEM.getOptional(key).ifPresent((item) -> CraftMagicNumbers.MATERIAL_ITEM.put(material, item));
            BuiltInRegistries.BLOCK.getOptional(key).ifPresent((block) -> CraftMagicNumbers.MATERIAL_BLOCK.put(material, block));
            //BuiltInRegistries.FLUID.getOptional(key).ifPresent((fluid) -> MATERIAL_FLUID.put(material, fluid));
        }
    }

    public static HashMap<String, Material> getModdedMaterials() {
        HashMap<String, Material> map = new HashMap<>();
        for (Block block : BuiltInRegistries.BLOCK) {
            Identifier id = BuiltInRegistries.BLOCK.getKey(block);
            String name = standardize(id);
            if (id.getNamespace().startsWith("minecraft")) continue;

            map.put(name, Material.getMaterial(id.getNamespace().toUpperCase(Locale.ROOT) + "_" + id.getPath().toUpperCase(Locale.ROOT)));
        }

        for (Item item : BuiltInRegistries.ITEM) {
            Identifier id = BuiltInRegistries.ITEM.getKey(item);
            String name = standardize(id);
            if (id.getNamespace().startsWith("minecraft")) continue;

            map.put(name, Material.getMaterial(id.getNamespace().toUpperCase(Locale.ROOT) + "_" + id.getPath().toUpperCase(Locale.ROOT)));
        }
        return map;
    }

    public static String standardize(Identifier location) {
        Preconditions.checkNotNull(location, "location");
        return (location.getNamespace().equals(NamespacedKey.MINECRAFT) ? location.getPath() : location.toString())
                .replace(':', '_')
                .replaceAll("\\s+", "_")
                .replaceAll("\\W", "")
                .toUpperCase(Locale.ENGLISH);
    }

    public static String standardizeLower(Identifier location) {
        return (location.getNamespace().equals(NamespacedKey.MINECRAFT) ? location.getPath() : location.toString())
                .replace(':', '_')
                .replaceAll("\\s+", "_")
                .replaceAll("\\W", "")
                .toLowerCase(Locale.ENGLISH);
    }

    public static Material getMaterial(Block block) {
        Identifier id = BuiltInRegistries.BLOCK.getKey(block);
        Material m = CraftMagicNumbers.BLOCK_MATERIAL.getOrDefault(block, Material.getMaterial(id.getNamespace().toUpperCase(Locale.ROOT) + "_" + id.getPath().toUpperCase(Locale.ROOT)));
        CraftMagicNumbers.BLOCK_MATERIAL.put(block, m);
        CraftMagicNumbers.MATERIAL_BLOCK.put(m, block);
        return m;
    }

    public static Material getMaterial(Item item) {
        return CraftMagicNumbers.ITEM_MATERIAL.getOrDefault(item, Material.AIR);
    }

    public static Item getItem(Material material) {
        if (material != null && material.isLegacy()) material = CraftLegacy.fromLegacy(material);
        return CraftMagicNumbers.MATERIAL_ITEM.getOrDefault(material, getModdedItem(material));
    }

    public static Block getBlock(Material material) {
        if (material != null && material.isLegacy()) material = CraftLegacy.fromLegacy(material);
        return CraftMagicNumbers.MATERIAL_BLOCK.getOrDefault(material, getModdedBlock(material));
    }

    public static Item getModdedItem(Material mat) {
        if (!((Object)mat instanceof BukkitMaterialBridge)) {
            // Dev env
            return null;
        }
        BukkitMaterialBridge mm = (BukkitMaterialBridge)(Object) mat;
        if (!mm.isModded()) return null;

        Identifier id = Identifier.parse(mm.getModdedData().getId());
        Item item = BuiltInRegistries.ITEM.getValue(id);
        CraftMagicNumbers.MATERIAL_ITEM.put(mat, item);
        return item;
    }

    public static Block getModdedBlock(Material mat) {
        if (null == mat) return Blocks.STONE;
        if (!((Object)mat instanceof BukkitMaterialBridge)) {
            // Dev env
            System.out.println("Could not locate BukkitMaterialBridge.");
            return Blocks.STONE;
        }
        BukkitMaterialBridge mm = (BukkitMaterialBridge)(Object) mat;
        if (!mm.isModded()) return null;

        Identifier id = Identifier.parse(mm.getModdedData().getId());
        Block block = BuiltInRegistries.BLOCK.getValue(id);
        CraftMagicNumbers.MATERIAL_BLOCK.put(mat, block);
        return block;
    }


    public static Identifier key(Material mat) {
        return CraftNamespacedKey.toMinecraft(mat.getKey());
    }
}
