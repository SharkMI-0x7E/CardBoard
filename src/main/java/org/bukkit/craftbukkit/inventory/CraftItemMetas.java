package org.bukkit.craftbukkit.inventory;

import com.destroystokyo.paper.inventory.meta.ArmorStandMeta;
// import com.github.bsideup.jabel.Desugar;

import java.util.Set;
import java.util.function.BiFunction;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.world.item.BannerItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SignItem;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ShulkerBoxBlock;
import org.bukkit.inventory.ItemType;
import org.bukkit.inventory.meta.ArmorMeta;
import org.bukkit.inventory.meta.BannerMeta;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.ColorableArmorMeta;
import org.bukkit.inventory.meta.CrossbowMeta;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.FireworkEffectMeta;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.KnowledgeBookMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.MapMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.inventory.meta.SpawnEggMeta;
import org.bukkit.inventory.meta.SuspiciousStewMeta;

public final class CraftItemMetas {
    private static final ItemMetaData<ItemMeta> EMPTY_META_DATA = new ItemMetaData<ItemMeta>(ItemMeta.class, (item, extras) -> null, (type, meta) -> null);
    private static final ItemMetaData<ItemMeta> ITEM_META_DATA = new ItemMetaData<ItemMeta>(ItemMeta.class, (item, extras) -> new CraftMetaItem(item.getComponentsPatch(), (Set<DataComponentType<?>>)extras), (type, meta) -> new CraftMetaItem((CraftMetaItem)meta));
    private static final ItemMetaData<BookMeta> SIGNED_BOOK_META_DATA = new ItemMetaData<BookMeta>(BookMeta.class, (item, extras) -> new CraftMetaBookSigned(item.getComponentsPatch(), (Set<DataComponentType<?>>)extras), (type, meta) -> {
        CraftMetaBookSigned signed;
        return meta instanceof CraftMetaBookSigned ? (signed = (CraftMetaBookSigned)meta) : new CraftMetaBookSigned((CraftMetaItem)meta);
    });
    private static final ItemMetaData<BookMeta> WRITABLE_BOOK_META_DATA = new ItemMetaData<BookMeta>(BookMeta.class, (item, extras) -> new CraftMetaBook(item.getComponentsPatch(), (Set<DataComponentType<?>>)extras), (type, meta) -> meta != null && meta.getClass().equals(CraftMetaBook.class) ? (BookMeta)meta : new CraftMetaBook((CraftMetaItem)meta));
    private static final ItemMetaData<SkullMeta> SKULL_META_DATA = new ItemMetaData<SkullMeta>(SkullMeta.class, (item, extras) -> new CraftMetaSkull(item.getComponentsPatch(), (Set<DataComponentType<?>>)extras), (type, meta) -> {
        CraftMetaSkull skull;
        return meta instanceof CraftMetaSkull ? (skull = (CraftMetaSkull)meta) : new CraftMetaSkull((CraftMetaItem)meta);
    });
    private static final ItemMetaData<ArmorMeta> ARMOR_META_DATA = new ItemMetaData<ArmorMeta>(ArmorMeta.class, (item, extras) -> new CraftMetaArmor(item.getComponentsPatch(), (Set<DataComponentType<?>>)extras), (type, meta) -> meta != null && meta.getClass().equals(CraftMetaArmor.class) ? (ArmorMeta)meta : new CraftMetaArmor((CraftMetaItem)meta));
    private static final ItemMetaData<ColorableArmorMeta> COLORABLE_ARMOR_META_DATA = new ItemMetaData<ColorableArmorMeta>(ColorableArmorMeta.class, (item, extras) -> new CraftMetaColorableArmor(item.getComponentsPatch(), (Set<DataComponentType<?>>)extras), (type, meta) -> {
        CraftMetaColorableArmor craftMetaColorableArmor;
        if (meta instanceof ColorableArmorMeta) {
            ColorableArmorMeta colorable = (ColorableArmorMeta)meta;
            craftMetaColorableArmor = (CraftMetaColorableArmor) colorable;
        } else {
            craftMetaColorableArmor = new CraftMetaColorableArmor((CraftMetaItem)meta);
        }
        return craftMetaColorableArmor;
    });
    private static final ItemMetaData<LeatherArmorMeta> LEATHER_ARMOR_META_DATA = new ItemMetaData<LeatherArmorMeta>(LeatherArmorMeta.class, (item, extras) -> new CraftMetaLeatherArmor(item.getComponentsPatch(), (Set<DataComponentType<?>>)extras), (type, meta) -> {
        CraftMetaLeatherArmor leather;
        return meta instanceof CraftMetaLeatherArmor ? (leather = (CraftMetaLeatherArmor)meta) : new CraftMetaLeatherArmor((CraftMetaItem)meta);
    });
    private static final ItemMetaData<PotionMeta> POTION_META_DATA = new ItemMetaData<PotionMeta>(PotionMeta.class, (item, extras) -> new CraftMetaPotion(item.getComponentsPatch(), (Set<DataComponentType<?>>)extras), (type, meta) -> {
        CraftMetaPotion potion;
        return meta instanceof CraftMetaPotion ? (potion = (CraftMetaPotion)meta) : new CraftMetaPotion((CraftMetaItem)meta);
    });
    private static final ItemMetaData<MapMeta> MAP_META_DATA = new ItemMetaData<MapMeta>(MapMeta.class, (item, extras) -> new CraftMetaMap(item.getComponentsPatch(), (Set<DataComponentType<?>>)extras), (type, meta) -> {
        CraftMetaMap map;
        return meta instanceof CraftMetaMap ? (map = (CraftMetaMap)meta) : new CraftMetaMap((CraftMetaItem)meta);
    });
    private static final ItemMetaData<FireworkMeta> FIREWORK_META_DATA = new ItemMetaData<FireworkMeta>(FireworkMeta.class, (item, extras) -> new CraftMetaFirework(item.getComponentsPatch(), (Set<DataComponentType<?>>)extras), (type, meta) -> {
        CraftMetaFirework firework;
        return meta instanceof CraftMetaFirework ? (firework = (CraftMetaFirework)meta) : new CraftMetaFirework((CraftMetaItem)meta);
    });
    private static final ItemMetaData<FireworkEffectMeta> CHARGE_META_DATA = new ItemMetaData<FireworkEffectMeta>(FireworkEffectMeta.class, (item, extras) -> new CraftMetaCharge(item.getComponentsPatch(), (Set<DataComponentType<?>>)extras), (type, meta) -> {
        CraftMetaCharge charge;
        return meta instanceof CraftMetaCharge ? (charge = (CraftMetaCharge)meta) : new CraftMetaCharge((CraftMetaItem)meta);
    });
    private static final ItemMetaData<EnchantmentStorageMeta> ENCHANTED_BOOK_META_DATA = new ItemMetaData<EnchantmentStorageMeta>(EnchantmentStorageMeta.class, (item, extras) -> new CraftMetaEnchantedBook(item.getComponentsPatch(), (Set<DataComponentType<?>>)extras), (type, meta) -> {
        CraftMetaEnchantedBook enchantedBook;
        return meta instanceof CraftMetaEnchantedBook ? (enchantedBook = (CraftMetaEnchantedBook)meta) : new CraftMetaEnchantedBook((CraftMetaItem)meta);
    });
    private static final ItemMetaData<BannerMeta> BANNER_META_DATA = new ItemMetaData<BannerMeta>(BannerMeta.class, (item, extras) -> new CraftMetaBanner(item.getComponentsPatch(), (Set<DataComponentType<?>>)extras), (type, meta) -> {
        CraftMetaBanner banner;
        return meta instanceof CraftMetaBanner ? (banner = (CraftMetaBanner)meta) : new CraftMetaBanner((CraftMetaItem)meta);
    });
    private static final ItemMetaData<SpawnEggMeta> SPAWN_EGG_META_DATA = new ItemMetaData<SpawnEggMeta>(SpawnEggMeta.class, (item, extras) -> new CraftMetaSpawnEgg(item.getComponentsPatch(), (Set<DataComponentType<?>>)extras), (type, meta) -> {
        CraftMetaSpawnEgg spawnEgg;
        return meta instanceof CraftMetaSpawnEgg ? (spawnEgg = (CraftMetaSpawnEgg)meta) : new CraftMetaSpawnEgg((CraftMetaItem)meta);
    });
    private static final ItemMetaData<ArmorStandMeta> ARMOR_STAND_META_DATA = new ItemMetaData<ArmorStandMeta>(ArmorStandMeta.class, (item, extras) -> new CraftMetaArmorStand(item.getComponentsPatch(), (Set<DataComponentType<?>>)extras), (type, meta) -> {
        CraftMetaArmorStand armorStand;
        return meta instanceof CraftMetaArmorStand ? (armorStand = (CraftMetaArmorStand)meta) : new CraftMetaArmorStand((CraftMetaItem)meta);
    });
    private static final ItemMetaData<KnowledgeBookMeta> KNOWLEDGE_BOOK_META_DATA = new ItemMetaData<KnowledgeBookMeta>(KnowledgeBookMeta.class, (item, extras) -> new CraftMetaKnowledgeBook(item.getComponentsPatch(), (Set<DataComponentType<?>>)extras), (type, meta) -> {
        CraftMetaKnowledgeBook knowledgeBook;
        return meta instanceof CraftMetaKnowledgeBook ? (knowledgeBook = (CraftMetaKnowledgeBook)meta) : new CraftMetaKnowledgeBook((CraftMetaItem)meta);
    });
    private static final ItemMetaData<BlockStateMeta> BLOCK_STATE_META_DATA = new ItemMetaData<BlockStateMeta>(BlockStateMeta.class, (item, extras) -> new CraftMetaBlockState(item.getComponentsPatch(), CraftItemType.minecraftToBukkit(item.getItem()), (Set<DataComponentType<?>>)extras), (type, meta) -> new CraftMetaBlockState((CraftMetaItem)meta, type.asMaterial()));
    
    /*
    private static final ItemMetaData<ShieldMeta> SHIELD_META_DATA = new ItemMetaData<ShieldMeta>(ShieldMeta.class, (item, extras) -> new CraftMetaShield(item.getComponentChanges(), (Set<ComponentType<?>>)extras), (type, meta) -> new CraftMetaShield((CraftMetaItem)meta));
    private static final ItemMetaData<TropicalFishBucketMeta> TROPICAL_FISH_BUCKET_META_DATA = new ItemMetaData<TropicalFishBucketMeta>(TropicalFishBucketMeta.class, (item, extras) -> new CraftMetaTropicalFishBucket(item.getComponentChanges(), (Set<ComponentType<?>>)extras), (type, meta) -> {
        CraftMetaTropicalFishBucket tropicalFishBucket;
        return meta instanceof CraftMetaTropicalFishBucket ? (tropicalFishBucket = (CraftMetaTropicalFishBucket)meta) : new CraftMetaTropicalFishBucket((CraftMetaItem)meta);
    });
    private static final ItemMetaData<AxolotlBucketMeta> AXOLOTL_BUCKET_META_DATA = new ItemMetaData<AxolotlBucketMeta>(AxolotlBucketMeta.class, (item, extras) -> new CraftMetaAxolotlBucket(item.getComponentChanges(), (Set<ComponentType<?>>)extras), (type, meta) -> {
        CraftMetaAxolotlBucket axolotlBucket;
        return meta instanceof CraftMetaAxolotlBucket ? (axolotlBucket = (CraftMetaAxolotlBucket)meta) : new CraftMetaAxolotlBucket((CraftMetaItem)meta);
    });
    */
    
    private static final ItemMetaData<CrossbowMeta> CROSSBOW_META_DATA = new ItemMetaData<CrossbowMeta>(CrossbowMeta.class, (item, extras) -> new CraftMetaCrossbow(item.getComponentsPatch(), (Set<DataComponentType<?>>)extras), (type, meta) -> {
        CraftMetaCrossbow crossbow;
        return meta instanceof CraftMetaCrossbow ? (crossbow = (CraftMetaCrossbow)meta) : new CraftMetaCrossbow((CraftMetaItem)meta);
    });
    private static final ItemMetaData<SuspiciousStewMeta> SUSPICIOUS_STEW_META_DATA = new ItemMetaData<SuspiciousStewMeta>(SuspiciousStewMeta.class, (item, extras) -> new CraftMetaSuspiciousStew(item.getComponentsPatch(), (Set<DataComponentType<?>>)extras), (type, meta) -> {
        CraftMetaSuspiciousStew suspiciousStew;
        return meta instanceof CraftMetaSuspiciousStew ? (suspiciousStew = (CraftMetaSuspiciousStew)meta) : new CraftMetaSuspiciousStew((CraftMetaItem)meta);
    });
    private static final ItemMetaData<ItemMeta> ENTITY_TAG_META_DATA = new ItemMetaData<ItemMeta>(ItemMeta.class, (item, extras) -> new CraftMetaEntityTag(item.getComponentsPatch(), (Set<DataComponentType<?>>)extras), (type, meta) -> {
        CraftMetaEntityTag entityTag;
        return meta instanceof CraftMetaEntityTag ? (entityTag = (CraftMetaEntityTag)meta) : new CraftMetaEntityTag((CraftMetaItem)meta);
    });
    
    /*
    private static final ItemMetaData<CompassMeta> COMPASS_META_DATA = new ItemMetaData<CompassMeta>(CompassMeta.class, (item, extras) -> new CraftMetaCompass(item.getComponentChanges(), (Set<ComponentType<?>>)extras), (type, meta) -> {
        CraftMetaCompass compass;
        return meta instanceof CraftMetaCompass ? (compass = (CraftMetaCompass)meta) : new CraftMetaCompass((CraftMetaItem)meta);
    });
    private static final ItemMetaData<BundleMeta> BUNDLE_META_DATA = new ItemMetaData<BundleMeta>(BundleMeta.class, (item, extras) -> new CraftMetaBundle(item.getComponentChanges(), (Set<ComponentType<?>>)extras), (type, meta) -> {
        CraftMetaBundle bundle;
        return meta instanceof CraftMetaBundle ? (bundle = (CraftMetaBundle)meta) : new CraftMetaBundle((CraftMetaItem)meta);
    });
    private static final ItemMetaData<MusicInstrumentMeta> MUSIC_INSTRUMENT_META_DATA = new ItemMetaData<MusicInstrumentMeta>(MusicInstrumentMeta.class, (item, extras) -> new CraftMetaMusicInstrument(item.getComponentChanges(), (Set<ComponentType<?>>)extras), (type, meta) -> {
        CraftMetaMusicInstrument musicInstrument;
        return meta instanceof CraftMetaMusicInstrument ? (musicInstrument = (CraftMetaMusicInstrument)meta) : new CraftMetaMusicInstrument((CraftMetaItem)meta);
    });
    private static final ItemMetaData<OminousBottleMeta> OMINOUS_BOTTLE_META_DATA = new ItemMetaData<OminousBottleMeta>(OminousBottleMeta.class, (item, extras) -> new CraftMetaOminousBottle(item.getComponentChanges(), (Set<ComponentType<?>>)extras), (type, meta) -> {
        CraftMetaOminousBottle musicInstrument;
        return meta instanceof CraftMetaOminousBottle ? (musicInstrument = (CraftMetaOminousBottle)meta) : new CraftMetaOminousBottle((CraftMetaItem)meta);
    });
    */

    public static <I extends ItemMeta> ItemMetaData<I> getItemMetaData(CraftItemType<?> itemType) {
        Block blockHandle;
        Item itemHandle = itemType.getHandle();
        if (itemHandle instanceof BlockItem) {
            BlockItem itemBlock = (BlockItem)itemHandle;
            blockHandle = itemBlock.getBlock();
        } else {
        	blockHandle = null;
        }
        if (itemType == ItemType.AIR) {
            return CraftItemMetas.asType(EMPTY_META_DATA);
        }
        if (itemType == ItemType.WRITTEN_BOOK) {
            return CraftItemMetas.asType(SIGNED_BOOK_META_DATA);
        }
        if (itemType == ItemType.WRITABLE_BOOK) {
            return CraftItemMetas.asType(WRITABLE_BOOK_META_DATA);
        }
        if (itemType == ItemType.CREEPER_HEAD || itemType == ItemType.DRAGON_HEAD || itemType == ItemType.PIGLIN_HEAD || itemType == ItemType.PLAYER_HEAD || itemType == ItemType.SKELETON_SKULL || itemType == ItemType.WITHER_SKELETON_SKULL || itemType == ItemType.ZOMBIE_HEAD) {
            return CraftItemMetas.asType(SKULL_META_DATA);
        }
        if (itemType == ItemType.CHAINMAIL_HELMET || itemType == ItemType.CHAINMAIL_CHESTPLATE || itemType == ItemType.CHAINMAIL_LEGGINGS || itemType == ItemType.CHAINMAIL_BOOTS || itemType == ItemType.DIAMOND_HELMET || itemType == ItemType.DIAMOND_CHESTPLATE || itemType == ItemType.DIAMOND_LEGGINGS || itemType == ItemType.DIAMOND_BOOTS || itemType == ItemType.GOLDEN_HELMET || itemType == ItemType.GOLDEN_CHESTPLATE || itemType == ItemType.GOLDEN_LEGGINGS || itemType == ItemType.GOLDEN_BOOTS || itemType == ItemType.IRON_HELMET || itemType == ItemType.IRON_CHESTPLATE || itemType == ItemType.IRON_LEGGINGS || itemType == ItemType.IRON_BOOTS || itemType == ItemType.NETHERITE_HELMET || itemType == ItemType.NETHERITE_CHESTPLATE || itemType == ItemType.NETHERITE_LEGGINGS || itemType == ItemType.NETHERITE_BOOTS || itemType == ItemType.TURTLE_HELMET) {
            return CraftItemMetas.asType(ARMOR_META_DATA);
        }
        if (itemType == ItemType.LEATHER_HELMET || itemType == ItemType.LEATHER_CHESTPLATE || itemType == ItemType.LEATHER_LEGGINGS || itemType == ItemType.LEATHER_BOOTS || itemType == ItemType.WOLF_ARMOR) {
            return CraftItemMetas.asType(COLORABLE_ARMOR_META_DATA);
        }
        if (itemType == ItemType.LEATHER_HORSE_ARMOR) {
            return CraftItemMetas.asType(LEATHER_ARMOR_META_DATA);
        }
        if (itemType == ItemType.POTION || itemType == ItemType.SPLASH_POTION || itemType == ItemType.LINGERING_POTION || itemType == ItemType.TIPPED_ARROW) {
            return CraftItemMetas.asType(POTION_META_DATA);
        }
        if (itemType == ItemType.FILLED_MAP) {
            return CraftItemMetas.asType(MAP_META_DATA);
        }
        if (itemType == ItemType.FIREWORK_ROCKET) {
            return CraftItemMetas.asType(FIREWORK_META_DATA);
        }
        if (itemType == ItemType.FIREWORK_STAR) {
            return CraftItemMetas.asType(CHARGE_META_DATA);
        }
        if (itemType == ItemType.ENCHANTED_BOOK) {
            return CraftItemMetas.asType(ENCHANTED_BOOK_META_DATA);
        }
        if (itemHandle instanceof BannerItem) {
            return CraftItemMetas.asType(BANNER_META_DATA);
        }
        if (itemHandle instanceof SpawnEggItem) {
            return CraftItemMetas.asType(SPAWN_EGG_META_DATA);
        }
        if (itemType == ItemType.ARMOR_STAND) {
            return CraftItemMetas.asType(ARMOR_STAND_META_DATA);
        }
        if (itemType == ItemType.KNOWLEDGE_BOOK) {
            return CraftItemMetas.asType(KNOWLEDGE_BOOK_META_DATA);
        }
        if (itemType == ItemType.FURNACE || itemType == ItemType.CHEST || itemType == ItemType.TRAPPED_CHEST || itemType == ItemType.JUKEBOX || itemType == ItemType.DISPENSER || itemType == ItemType.DROPPER || itemHandle instanceof SignItem || itemType == ItemType.SPAWNER || itemType == ItemType.BREWING_STAND || itemType == ItemType.ENCHANTING_TABLE || itemType == ItemType.COMMAND_BLOCK || itemType == ItemType.REPEATING_COMMAND_BLOCK || itemType == ItemType.CHAIN_COMMAND_BLOCK || itemType == ItemType.BEACON || itemType == ItemType.DAYLIGHT_DETECTOR || itemType == ItemType.HOPPER || itemType == ItemType.COMPARATOR || itemType == ItemType.STRUCTURE_BLOCK || blockHandle instanceof ShulkerBoxBlock || itemType == ItemType.ENDER_CHEST || itemType == ItemType.BARREL || itemType == ItemType.BELL || itemType == ItemType.BLAST_FURNACE || itemType == ItemType.CAMPFIRE || itemType == ItemType.SOUL_CAMPFIRE || itemType == ItemType.JIGSAW || itemType == ItemType.LECTERN || itemType == ItemType.SMOKER || itemType == ItemType.BEEHIVE || itemType == ItemType.BEE_NEST || itemType == ItemType.SCULK_CATALYST || itemType == ItemType.SCULK_SHRIEKER || itemType == ItemType.SCULK_SENSOR || itemType == ItemType.CALIBRATED_SCULK_SENSOR || itemType == ItemType.CHISELED_BOOKSHELF || itemType == ItemType.DECORATED_POT || itemType == ItemType.SUSPICIOUS_SAND || itemType == ItemType.SUSPICIOUS_GRAVEL || itemType == ItemType.CRAFTER || itemType == ItemType.TRIAL_SPAWNER || itemType == ItemType.VAULT) {
            return CraftItemMetas.asType(BLOCK_STATE_META_DATA);
        }
        /*
        if (itemType == ItemType.SHIELD) {
            return CraftItemMetas.asType(SHIELD_META_DATA);
        }
        if (itemType == ItemType.TROPICAL_FISH_BUCKET) {
            return CraftItemMetas.asType(TROPICAL_FISH_BUCKET_META_DATA);
        }
        if (itemType == ItemType.AXOLOTL_BUCKET) {
            return CraftItemMetas.asType(AXOLOTL_BUCKET_META_DATA);
        }
        */
        if (itemType == ItemType.CROSSBOW) {
            return CraftItemMetas.asType(CROSSBOW_META_DATA);
        }
        if (itemType == ItemType.SUSPICIOUS_STEW) {
            return CraftItemMetas.asType(SUSPICIOUS_STEW_META_DATA);
        }
        if (itemType == ItemType.COD_BUCKET || itemType == ItemType.PUFFERFISH_BUCKET || itemType == ItemType.TADPOLE_BUCKET || itemType == ItemType.SALMON_BUCKET || itemType == ItemType.ITEM_FRAME || itemType == ItemType.GLOW_ITEM_FRAME || itemType == ItemType.PAINTING) {
            return CraftItemMetas.asType(ENTITY_TAG_META_DATA);
        }
        /*
        if (itemType == ItemType.COMPASS) {
            return CraftItemMetas.asType(COMPASS_META_DATA);
        }
        if (itemType == ItemType.BUNDLE) {
            return CraftItemMetas.asType(BUNDLE_META_DATA);
        }
        if (itemType == ItemType.GOAT_HORN) {
            return CraftItemMetas.asType(MUSIC_INSTRUMENT_META_DATA);
        }
        if (itemType == ItemType.OMINOUS_BOTTLE) {
            return CraftItemMetas.asType(OMINOUS_BOTTLE_META_DATA);
        }
        */
        return CraftItemMetas.asType(ITEM_META_DATA);
    }

    private static <I extends ItemMeta> ItemMetaData<I> asType(ItemMetaData<?> metaData) {
        return (ItemMetaData<I>) metaData;
    }

    private CraftItemMetas() {
    }

    // @Desugar
    public record ItemMetaData<I extends ItemMeta>(Class<I> metaClass, BiFunction<ItemStack, Set<DataComponentType<?>>, I> fromItemStack, BiFunction<ItemType.Typed<I>, CraftMetaItem, I> fromItemMeta) {
    }
}

