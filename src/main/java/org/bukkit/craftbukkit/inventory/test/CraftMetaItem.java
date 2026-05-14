package org.bukkit.craftbukkit.inventory.test;

class CraftMetaItem {}

/*import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.ImmutableSortedMap;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import com.mojang.authlib.GameProfile;
import io.papermc.paper.adventure.PaperAdventure;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.kyori.adventure.text.Component;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.chat.ComponentSerializer;
import net.minecraft.block.BlockState;
import net.minecraft.component.ComponentChanges;
import net.minecraft.component.DataComponentType;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.AttributeModifierSlot;
import net.minecraft.component.type.AttributeModifiersComponent;
import net.minecraft.component.type.BlockStateComponent;
import net.minecraft.component.type.CustomModelDataComponent;
import net.minecraft.component.type.FoodComponent;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.component.type.UnbreakableComponent;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.item.BlockPredicatesChecker;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.NbtSizeTracker;
import net.minecraft.predicate.BlockPredicate;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.MinecraftServer;
import net.minecraft.text.Text;
import net.minecraft.util.Rarity;
import net.minecraft.util.Unit;
import org.apache.commons.lang3.EnumUtils;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.block.data.BlockData;
import org.bukkit.configuration.serialization.DelegateDeserialization;
import org.bukkit.craftbukkit.CraftEquipmentSlot;
import org.bukkit.craftbukkit.Overridden;
import org.bukkit.craftbukkit.attribute.CraftAttribute;
import org.bukkit.craftbukkit.attribute.CraftAttributeInstance;
import org.bukkit.craftbukkit.attribute.CraftAttributeMap;
import org.bukkit.craftbukkit.block.CraftBlockType;
import org.bukkit.craftbukkit.block.data.CraftBlockData;
import org.bukkit.craftbukkit.enchantments.CraftEnchantment;
import org.bukkit.craftbukkit.inventory.CraftItemFactory;
import org.bukkit.craftbukkit.inventory.CraftMetaArmor;
import org.bukkit.craftbukkit.inventory.CraftMetaArmorStand;
import org.bukkit.craftbukkit.inventory.CraftMetaAxolotlBucket;
import org.bukkit.craftbukkit.inventory.CraftMetaBanner;
import org.bukkit.craftbukkit.inventory.CraftMetaBlockState;
import org.bukkit.craftbukkit.inventory.CraftMetaBook;
import org.bukkit.craftbukkit.inventory.CraftMetaBookSigned;
import org.bukkit.craftbukkit.inventory.CraftMetaBundle;
import org.bukkit.craftbukkit.inventory.CraftMetaCharge;
import org.bukkit.craftbukkit.inventory.CraftMetaColorableArmor;
import org.bukkit.craftbukkit.inventory.CraftMetaCompass;
import org.bukkit.craftbukkit.inventory.CraftMetaCrossbow;
import org.bukkit.craftbukkit.inventory.CraftMetaEnchantedBook;
import org.bukkit.craftbukkit.inventory.CraftMetaEntityTag;
import org.bukkit.craftbukkit.inventory.CraftMetaFirework;
import org.bukkit.craftbukkit.inventory.CraftMetaKnowledgeBook;
import org.bukkit.craftbukkit.inventory.CraftMetaLeatherArmor;
import org.bukkit.craftbukkit.inventory.CraftMetaMap;
import org.bukkit.craftbukkit.inventory.CraftMetaMusicInstrument;
import org.bukkit.craftbukkit.inventory.CraftMetaOminousBottle;
import org.bukkit.craftbukkit.inventory.CraftMetaPotion;
import org.bukkit.craftbukkit.inventory.CraftMetaSkull;
import org.bukkit.craftbukkit.inventory.CraftMetaSpawnEgg;
import org.bukkit.craftbukkit.inventory.CraftMetaSuspiciousStew;
import org.bukkit.craftbukkit.inventory.CraftMetaTropicalFishBucket;
import org.bukkit.craftbukkit.inventory.SerializableMeta;
import org.bukkit.craftbukkit.inventory.components.CraftFoodComponent;
import org.bukkit.craftbukkit.inventory.tags.DeprecatedCustomTagContainer;
import org.bukkit.craftbukkit.legacy.FieldRename;
import org.bukkit.craftbukkit.persistence.CraftPersistentDataContainer;
import org.bukkit.craftbukkit.persistence.CraftPersistentDataTypeRegistry;
import org.bukkit.craftbukkit.util.ApiVersion;
import org.bukkit.craftbukkit.util.CraftChatMessage;
import org.bukkit.craftbukkit.util.CraftMagicNumbers;
import org.bukkit.craftbukkit.util.CraftNBTTagConfigSerializer;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemRarity;
import org.bukkit.inventory.meta.BlockDataMeta;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.Repairable;
import org.bukkit.inventory.meta.tags.CustomItemTagContainer;
import org.bukkit.persistence.PersistentDataContainer;
import org.jetbrains.annotations.VisibleForTesting;

@DelegateDeserialization(value=SerializableMeta.class)
class CraftMetaItem
implements ItemMeta,
Damageable,
Repairable,
BlockDataMeta {
    /*static final ItemMetaKeyType<Text> NAME = new ItemMetaKeyType<Text>(DataComponentTypes.CUSTOM_NAME, "display-name");
    static final ItemMetaKeyType<Text> ITEM_NAME = new ItemMetaKeyType<Text>(DataComponentTypes.ITEM_NAME, "item-name");
    static final ItemMetaKeyType<LoreComponent> LORE = new ItemMetaKeyType<LoreComponent>(DataComponentTypes.LORE, "lore");
    static final ItemMetaKeyType<CustomModelDataComponent> CUSTOM_MODEL_DATA = new ItemMetaKeyType<CustomModelDataComponent>(DataComponentTypes.CUSTOM_MODEL_DATA, "custom-model-data");
    static final ItemMetaKeyType<ItemEnchantmentsComponent> ENCHANTMENTS = new ItemMetaKeyType<ItemEnchantmentsComponent>(DataComponentTypes.ENCHANTMENTS, "enchants");
    static final ItemMetaKeyType<Integer> REPAIR = new ItemMetaKeyType<Integer>(DataComponentTypes.REPAIR_COST, "repair-cost");
    static final ItemMetaKeyType<AttributeModifiersComponent> ATTRIBUTES = new ItemMetaKeyType<AttributeModifiersComponent>(DataComponentTypes.ATTRIBUTE_MODIFIERS, "attribute-modifiers");
    static final ItemMetaKey ATTRIBUTES_IDENTIFIER = new ItemMetaKey("AttributeName");
    static final ItemMetaKey ATTRIBUTES_SLOT = new ItemMetaKey("Slot");
    static final ItemMetaKey HIDEFLAGS = new ItemMetaKey("ItemFlags");
    static final ItemMetaKeyType<Unit> HIDE_TOOLTIP = new ItemMetaKeyType<Unit>(DataComponentTypes.HIDE_TOOLTIP, "hide-tool-tip");
    static final ItemMetaKeyType<UnbreakableComponent> UNBREAKABLE = new ItemMetaKeyType<UnbreakableComponent>(DataComponentTypes.UNBREAKABLE, "Unbreakable");
    static final ItemMetaKeyType<Boolean> ENCHANTMENT_GLINT_OVERRIDE = new ItemMetaKeyType<Boolean>(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, "enchantment-glint-override");
    static final ItemMetaKeyType<Unit> FIRE_RESISTANT = new ItemMetaKeyType<Unit>(DataComponentTypes.FIRE_RESISTANT, "fire-resistant");
    static final ItemMetaKeyType<Integer> MAX_STACK_SIZE = new ItemMetaKeyType<Integer>(DataComponentTypes.MAX_STACK_SIZE, "max-stack-size");
    static final ItemMetaKeyType<Rarity> RARITY = new ItemMetaKeyType<Rarity>(DataComponentTypes.RARITY, "rarity");
    static final ItemMetaKeyType<FoodComponent> FOOD = new ItemMetaKeyType<FoodComponent>(DataComponentTypes.FOOD, "food");
    static final ItemMetaKeyType<Integer> DAMAGE = new ItemMetaKeyType<Integer>(DataComponentTypes.DAMAGE, "Damage");
    static final ItemMetaKeyType<Integer> MAX_DAMAGE = new ItemMetaKeyType<Integer>(DataComponentTypes.MAX_DAMAGE, "max-damage");
    static final ItemMetaKeyType<BlockStateComponent> BLOCK_DATA = new ItemMetaKeyType<BlockStateComponent>(DataComponentTypes.BLOCK_STATE, "BlockStateTag");
    static final ItemMetaKey BUKKIT_CUSTOM_TAG = new ItemMetaKey("PublicBukkitValues");
    static final ItemMetaKeyType<Unit> HIDE_ADDITIONAL_TOOLTIP = new ItemMetaKeyType<Unit>(DataComponentTypes.HIDE_ADDITIONAL_TOOLTIP);
    static final ItemMetaKeyType<NbtComponent> CUSTOM_DATA = new ItemMetaKeyType<NbtComponent>(DataComponentTypes.CUSTOM_DATA);
    static final ItemMetaKeyType<BlockPredicatesChecker> CAN_PLACE_ON = new ItemMetaKeyType<BlockPredicatesChecker>(DataComponentTypes.CAN_PLACE_ON);
    static final ItemMetaKeyType<BlockPredicatesChecker> CAN_BREAK = new ItemMetaKeyType<BlockPredicatesChecker>(DataComponentTypes.CAN_BREAK);
    private List<BlockPredicate> canPlaceOnPredicates;
    private List<BlockPredicate> canBreakPredicates;
    private Text displayName;
    private Text itemName;
    private List<Text> lore;
    private Integer customModelData;
    private Map<String, String> blockData;
    private EnchantmentMap enchantments;
    private Multimap<Attribute, AttributeModifier> attributeModifiers;
    private int repairCost;
    private int hideFlag;
    private boolean hideTooltip;
    private boolean unbreakable;
    private Boolean enchantmentGlintOverride;
    private boolean fireResistant;
    private Integer maxStackSize;
    private ItemRarity rarity;
    private CraftFoodComponent food;
    private int damage;
    private Integer maxDamage;
    private static final Set<DataComponentType> HANDLED_TAGS = Sets.newHashSet();
    private static final CraftPersistentDataTypeRegistry DATA_TYPE_REGISTRY = new CraftPersistentDataTypeRegistry();
    private NbtCompound customTag;
    protected ComponentChanges.Builder unhandledTags = ComponentChanges.builder();
    private CraftPersistentDataContainer persistentDataContainer = new CraftPersistentDataContainer(DATA_TYPE_REGISTRY);
    private int version = CraftMagicNumbers.INSTANCE.getDataVersion();
    @VisibleForTesting
    public static final Map<Class<? extends CraftMetaItem>, Set<DataComponentType<?>>> HANDLED_DCTS_PER_TYPE = new HashMap();
    private static final Set<DataComponentType<?>> DEFAULT_HANDLED_DCTS = Set.of(CraftMetaItem.NAME.TYPE, CraftMetaItem.ITEM_NAME.TYPE, CraftMetaItem.LORE.TYPE, CraftMetaItem.CUSTOM_MODEL_DATA.TYPE, CraftMetaItem.BLOCK_DATA.TYPE, CraftMetaItem.REPAIR.TYPE, CraftMetaItem.ENCHANTMENTS.TYPE, CraftMetaItem.HIDE_ADDITIONAL_TOOLTIP.TYPE, CraftMetaItem.HIDE_TOOLTIP.TYPE, CraftMetaItem.UNBREAKABLE.TYPE, CraftMetaItem.ENCHANTMENT_GLINT_OVERRIDE.TYPE, CraftMetaItem.FIRE_RESISTANT.TYPE, CraftMetaItem.MAX_STACK_SIZE.TYPE, CraftMetaItem.RARITY.TYPE, CraftMetaItem.FOOD.TYPE, CraftMetaItem.DAMAGE.TYPE, CraftMetaItem.MAX_DAMAGE.TYPE, CraftMetaItem.CUSTOM_DATA.TYPE, CraftMetaItem.ATTRIBUTES.TYPE, CraftMetaItem.CAN_PLACE_ON.TYPE, CraftMetaItem.CAN_BREAK.TYPE);

    CraftMetaItem(CraftMetaItem meta) {
        if (meta == null) {
            return;
        }
        this.displayName = meta.displayName;
        this.itemName = meta.itemName;
        if (meta.lore != null) {
            this.lore = new ArrayList<Text>(meta.lore);
        }
        this.customModelData = meta.customModelData;
        this.blockData = meta.blockData;
        if (meta.enchantments != null) {
            this.enchantments = new EnchantmentMap(meta.enchantments);
        }
        if (meta.hasAttributeModifiers()) {
            this.attributeModifiers = LinkedHashMultimap.create(meta.attributeModifiers);
        }
        this.repairCost = meta.repairCost;
        this.hideFlag = meta.hideFlag;
        this.hideTooltip = meta.hideTooltip;
        this.unbreakable = meta.unbreakable;
        this.enchantmentGlintOverride = meta.enchantmentGlintOverride;
        this.fireResistant = meta.fireResistant;
        this.maxStackSize = meta.maxStackSize;
        this.rarity = meta.rarity;
        if (meta.hasFood()) {
            this.food = new CraftFoodComponent(meta.food);
        }
        this.damage = meta.damage;
        this.maxDamage = meta.maxDamage;
        this.unhandledTags = meta.unhandledTags;
        this.persistentDataContainer.putAll(meta.persistentDataContainer.getTagsCloned());
        this.customTag = meta.customTag;
        this.version = meta.version;
        this.canPlaceOnPredicates = meta.canPlaceOnPredicates;
        this.canBreakPredicates = meta.canBreakPredicates;
    }

    CraftMetaItem(ComponentChanges tag, Set<DataComponentType<?>> extraHandledTags) {
        CraftMetaItem.getOrEmpty(tag, NAME).ifPresent(component -> {
            this.displayName = component;
        });
        CraftMetaItem.getOrEmpty(tag, ITEM_NAME).ifPresent(component -> {
            this.itemName = component;
        });
        CraftMetaItem.getOrEmpty(tag, LORE).ifPresent(l -> {
            List<Text> list = l.lines();
            this.lore = new ArrayList<Text>(list.size());
            for (int index = 0; index < list.size(); ++index) {
                Text line = list.get(index);
                this.lore.add(line);
            }
        });
        CraftMetaItem.getOrEmpty(tag, CUSTOM_MODEL_DATA).ifPresent(i2 -> {
            this.customModelData = i2.value();
        });
        CraftMetaItem.getOrEmpty(tag, BLOCK_DATA).ifPresent(t -> {
            this.blockData = t.properties();
        });
        CraftMetaItem.getOrEmpty(tag, ENCHANTMENTS).ifPresent(en -> {
            this.enchantments = CraftMetaItem.buildEnchantments(en);
            if (!en.showInTooltip) {
                this.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            }
        });
        CraftMetaItem.getOrEmpty(tag, ATTRIBUTES).ifPresent(en -> {
            this.attributeModifiers = CraftMetaItem.buildModifiers(en);
            if (!en.showInTooltip()) {
                this.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            }
        });
        CraftMetaItem.getOrEmpty(tag, REPAIR).ifPresent(i2 -> {
            this.repairCost = i2;
        });
        CraftMetaItem.getOrEmpty(tag, HIDE_ADDITIONAL_TOOLTIP).ifPresent(h2 -> this.addItemFlags(ItemFlag.HIDE_ADDITIONAL_TOOLTIP));
        CraftMetaItem.getOrEmpty(tag, HIDE_TOOLTIP).ifPresent(u -> {
            this.hideTooltip = true;
        });
        CraftMetaItem.getOrEmpty(tag, UNBREAKABLE).ifPresent(u -> {
            this.unbreakable = true;
            if (!u.showInTooltip()) {
                this.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
            }
        });
        CraftMetaItem.getOrEmpty(tag, ENCHANTMENT_GLINT_OVERRIDE).ifPresent(override -> {
            this.enchantmentGlintOverride = override;
        });
        CraftMetaItem.getOrEmpty(tag, FIRE_RESISTANT).ifPresent(u -> {
            this.fireResistant = true;
        });
        CraftMetaItem.getOrEmpty(tag, MAX_STACK_SIZE).ifPresent(i2 -> {
            this.maxStackSize = i2;
        });
        CraftMetaItem.getOrEmpty(tag, RARITY).ifPresent(enumItemRarity -> {
            this.rarity = ItemRarity.valueOf((String)enumItemRarity.name());
        });
        CraftMetaItem.getOrEmpty(tag, FOOD).ifPresent(foodInfo -> {
            this.food = new CraftFoodComponent((FoodComponent)foodInfo);
        });
        CraftMetaItem.getOrEmpty(tag, DAMAGE).ifPresent(i2 -> {
            this.damage = i2;
        });
        CraftMetaItem.getOrEmpty(tag, MAX_DAMAGE).ifPresent(i2 -> {
            this.maxDamage = i2;
        });
        CraftMetaItem.getOrEmpty(tag, CUSTOM_DATA).ifPresent(customData -> {
            this.customTag = customData.copyNbt();
            if (this.customTag.contains(CraftMetaItem.BUKKIT_CUSTOM_TAG.NBT)) {
                NbtCompound compound = this.customTag.getCompound(CraftMetaItem.BUKKIT_CUSTOM_TAG.NBT);
                Set<String> keys = compound.getKeys();
                for (String key : keys) {
                    this.persistentDataContainer.put(key, compound.get(key).copy());
                }
                this.customTag.remove(CraftMetaItem.BUKKIT_CUSTOM_TAG.NBT);
            }
            if (this.customTag.isEmpty()) {
                this.customTag = null;
            }
        });
        CraftMetaItem.getOrEmpty(tag, CAN_PLACE_ON).ifPresent(data -> {
            this.canPlaceOnPredicates = List.copyOf(data.predicates);
            if (!data.showInTooltip()) {
                this.addItemFlags(ItemFlag.HIDE_PLACED_ON);
            }
        });
        CraftMetaItem.getOrEmpty(tag, CAN_BREAK).ifPresent(data -> {
            this.canBreakPredicates = List.copyOf(data.predicates);
            if (!data.showInTooltip()) {
                this.addItemFlags(ItemFlag.HIDE_DESTROYS);
            }
        });
        Set<DataComponentType<?>> handledTags = CraftMetaItem.getTopLevelHandledDcts(this.getClass());
        if (extraHandledTags != null) {
            extraHandledTags.addAll(handledTags);
            handledTags = extraHandledTags;
        }
        Set<Map.Entry<DataComponentType<?>, Optional<?>>> keys = tag.entrySet();
        for (Map.Entry<DataComponentType<?>, Optional<?>> key : keys) {
            if (key.getValue().isEmpty()) {
                this.unhandledTags.remove(key.getKey());
                continue;
            }
            if (handledTags.contains(key.getKey())) continue;
            key.getValue().ifPresentOrElse(value -> this.unhandledTags.add((DataComponentType)key.getKey(), value), () -> this.unhandledTags.remove((DataComponentType)key.getKey()));
        }
    }

    static EnchantmentMap buildEnchantments(ItemEnchantmentsComponent tag) {
        EnchantmentMap enchantments = new EnchantmentMap();
        tag.getEnchantmentsMap().forEach(entry -> {
            RegistryEntry id = (RegistryEntry)entry.getKey();
            int level = entry.getIntValue();
            Enchantment enchant = CraftEnchantment.minecraftHolderToBukkit(id);
            if (enchant != null) {
                enchantments.put(enchant, level);
            }
        });
        return enchantments;
    }

    static Multimap<Attribute, AttributeModifier> buildModifiers(AttributeModifiersComponent tag) {
        LinkedHashMultimap modifiers = LinkedHashMultimap.create();
        List<AttributeModifiersComponent.Entry> mods = tag.modifiers();
        int size = mods.size();
        for (int i2 = 0; i2 < size; ++i2) {
            AttributeModifiersComponent.Entry entry = mods.get(i2);
            EntityAttributeModifier nmsModifier = entry.modifier();
            if (nmsModifier == null) continue;
            AttributeModifier attribMod = CraftAttributeInstance.convert(nmsModifier);
            Attribute attribute = CraftAttribute.minecraftHolderToBukkit(entry.attribute());
            if (attribute == null) continue;
            if (entry.slot() != null) {
                AttributeModifierSlot slotName = entry.slot();
                if (slotName == null) {
                    modifiers.put((Object)attribute, (Object)attribMod);
                    continue;
                }
                EquipmentSlotGroup slot = null;
                try {
                    slot = CraftEquipmentSlot.getSlot(slotName);
                }
                catch (IllegalArgumentException illegalArgumentException) {
                    // empty catch block
                }
                if (slot == null) {
                    modifiers.put((Object)attribute, (Object)attribMod);
                    continue;
                }
                attribMod = new AttributeModifier(attribMod.getUniqueId(), attribMod.getName(), attribMod.getAmount(), attribMod.getOperation(), slot);
            }
            modifiers.put((Object)attribute, (Object)attribMod);
        }
        return modifiers;
    }

    CraftMetaItem(Map<String, Object> map) {
        String custom;
        Object nbtMap;
        String unhandled;
        String internal;
        Integer maxDamage;
        Integer damage;
        CraftFoodComponent food;
        String rarity;
        Integer maxStackSize;
        Boolean fireResistant;
        Boolean enchantmentGlintOverride;
        Boolean unbreakable;
        Boolean hideTooltip;
        Iterable hideFlags;
        Object blockData;
        Integer customModelData;
        this.displayName = CraftChatMessage.fromJSONOrString(SerializableMeta.getString(map, CraftMetaItem.NAME.BUKKIT, true), true, false);
        this.itemName = CraftChatMessage.fromJSONOrNull(SerializableMeta.getString(map, CraftMetaItem.ITEM_NAME.BUKKIT, true));
        Iterable lore = SerializableMeta.getObject(Iterable.class, map, CraftMetaItem.LORE.BUKKIT, true);
        if (lore != null) {
            this.lore = new ArrayList<Text>();
            CraftMetaItem.safelyAdd(lore, this.lore, true);
        }
        if ((customModelData = SerializableMeta.getObject(Integer.class, map, CraftMetaItem.CUSTOM_MODEL_DATA.BUKKIT, true)) != null) {
            this.setCustomModelData(customModelData);
        }
        if ((blockData = SerializableMeta.getObject(Object.class, map, CraftMetaItem.BLOCK_DATA.BUKKIT, true)) != null) {
            HashMap<String, String> mapBlockData = new HashMap<String, String>();
            NbtCompound nbtBlockData = (NbtCompound)CraftNBTTagConfigSerializer.deserialize(blockData);
            for (String key : nbtBlockData.getKeys()) {
                mapBlockData.put(key, nbtBlockData.getString(key));
            }
            this.blockData = mapBlockData;
        }
        this.enchantments = CraftMetaItem.buildEnchantments(map, ENCHANTMENTS);
        this.attributeModifiers = CraftMetaItem.buildModifiers(map, ATTRIBUTES);
        Integer repairCost = SerializableMeta.getObject(Integer.class, map, CraftMetaItem.REPAIR.BUKKIT, true);
        if (repairCost != null) {
            this.setRepairCost(repairCost);
        }
        if ((hideFlags = SerializableMeta.getObject(Iterable.class, map, CraftMetaItem.HIDEFLAGS.BUKKIT, true)) != null) {
            Iterator<String> iterator = hideFlags.iterator();
            while (iterator.hasNext()) {
                String hideFlagObject;
                String hideFlagString = hideFlagObject = iterator.next();
                try {
                    ItemFlag hideFlatEnum = ItemFlag.valueOf((String)FieldRename.convertItemFlagName(ApiVersion.CURRENT, hideFlagString));
                    this.addItemFlags(hideFlatEnum);
                }
                catch (IllegalArgumentException hideFlatEnum) {}
            }
        }
        if ((hideTooltip = SerializableMeta.getObject(Boolean.class, map, CraftMetaItem.HIDE_TOOLTIP.BUKKIT, true)) != null) {
            this.setHideTooltip(hideTooltip);
        }
        if ((unbreakable = SerializableMeta.getObject(Boolean.class, map, CraftMetaItem.UNBREAKABLE.BUKKIT, true)) != null) {
            this.setUnbreakable(unbreakable);
        }
        if ((enchantmentGlintOverride = SerializableMeta.getObject(Boolean.class, map, CraftMetaItem.ENCHANTMENT_GLINT_OVERRIDE.BUKKIT, true)) != null) {
            this.setEnchantmentGlintOverride(enchantmentGlintOverride);
        }
        if ((fireResistant = SerializableMeta.getObject(Boolean.class, map, CraftMetaItem.FIRE_RESISTANT.BUKKIT, true)) != null) {
            this.setFireResistant(fireResistant);
        }
        if ((maxStackSize = SerializableMeta.getObject(Integer.class, map, CraftMetaItem.MAX_STACK_SIZE.BUKKIT, true)) != null) {
            this.setMaxStackSize(maxStackSize);
        }
        if ((rarity = SerializableMeta.getString(map, CraftMetaItem.RARITY.BUKKIT, true)) != null) {
            this.setRarity(ItemRarity.valueOf((String)rarity));
        }
        if ((food = SerializableMeta.getObject(CraftFoodComponent.class, map, CraftMetaItem.FOOD.BUKKIT, true)) != null) {
            this.setFood(food);
        }
        if ((damage = SerializableMeta.getObject(Integer.class, map, CraftMetaItem.DAMAGE.BUKKIT, true)) != null) {
            this.setDamage(damage);
        }
        if ((maxDamage = SerializableMeta.getObject(Integer.class, map, CraftMetaItem.MAX_DAMAGE.BUKKIT, true)) != null) {
            this.setMaxDamage(maxDamage);
        }
        if ((internal = SerializableMeta.getString(map, "internal", true)) != null) {
            ByteArrayInputStream buf = new ByteArrayInputStream(Base64.getDecoder().decode(internal));
            try {
                NbtCompound internalTag = NbtIo.readCompressed(buf, NbtSizeTracker.ofUnlimitedBytes());
                this.deserializeInternal(internalTag, map);
            }
            catch (IOException ex) {
                Logger.getLogger(CraftMetaItem.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        if ((unhandled = SerializableMeta.getString(map, "unhandled", true)) != null) {
            ByteArrayInputStream buf = new ByteArrayInputStream(Base64.getDecoder().decode(unhandled));
            try {
                NbtCompound unhandledTag = NbtIo.readCompressed(buf, NbtSizeTracker.ofUnlimitedBytes());
                ComponentChanges patch = (ComponentChanges)ComponentChanges.CODEC.parse(MinecraftServer.getDefaultRegistryAccess().getOps(NbtOps.INSTANCE), (Object)unhandledTag).result().get();
                CraftMetaItem.getOrEmpty(patch, CAN_PLACE_ON).ifPresent(data -> {
                    this.canPlaceOnPredicates = List.copyOf(data.predicates);
                });
                CraftMetaItem.getOrEmpty(patch, CAN_BREAK).ifPresent(data -> {
                    this.canBreakPredicates = List.copyOf(data.predicates);
                });
                this.unhandledTags.copy(patch.withRemovedIf(type -> type == CraftMetaItem.CAN_PLACE_ON.TYPE || type == CraftMetaItem.CAN_BREAK.TYPE));
            }
            catch (IOException ex) {
                Logger.getLogger(CraftMetaItem.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        if ((nbtMap = SerializableMeta.getObject(Object.class, map, CraftMetaItem.BUKKIT_CUSTOM_TAG.BUKKIT, true)) != null) {
            this.persistentDataContainer.putAll((NbtCompound)CraftNBTTagConfigSerializer.deserialize(nbtMap));
        }
        if ((custom = SerializableMeta.getString(map, "custom", true)) != null) {
            ByteArrayInputStream buf = new ByteArrayInputStream(Base64.getDecoder().decode(custom));
            try {
                this.customTag = NbtIo.readCompressed(buf, NbtSizeTracker.ofUnlimitedBytes());
            }
            catch (IOException ex) {
                Logger.getLogger(CraftMetaItem.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    void deserializeInternal(NbtCompound tag, Object context) {
        if (tag.contains(CraftMetaItem.ATTRIBUTES.NBT, 9)) {
            this.attributeModifiers = CraftMetaItem.buildModifiersLegacy(tag, ATTRIBUTES);
        }
    }

    private static Multimap<Attribute, AttributeModifier> buildModifiersLegacy(NbtCompound tag, ItemMetaKey key) {
        LinkedHashMultimap modifiers = LinkedHashMultimap.create();
        if (!tag.contains(key.NBT, 9)) {
            return modifiers;
        }
        NbtList mods = tag.getList(key.NBT, 10);
        int size = mods.size();
        for (int i2 = 0; i2 < size; ++i2) {
            Attribute attribute;
            EntityAttributeModifier nmsModifier;
            NbtCompound entry = mods.getCompound(i2);
            if (entry.isEmpty() || (nmsModifier = EntityAttributeModifier.fromNbt(entry)) == null) continue;
            AttributeModifier attribMod = CraftAttributeInstance.convert(nmsModifier);
            String attributeName = CraftAttributeMap.convertIfNeeded(entry.getString(CraftMetaItem.ATTRIBUTES_IDENTIFIER.NBT));
            if (attributeName == null || attributeName.isEmpty() || (attribute = CraftAttribute.stringToBukkit(attributeName)) == null) continue;
            if (entry.contains(CraftMetaItem.ATTRIBUTES_SLOT.NBT, 8)) {
                String slotName = entry.getString(CraftMetaItem.ATTRIBUTES_SLOT.NBT);
                if (slotName == null || slotName.isEmpty()) {
                    modifiers.put((Object)attribute, (Object)attribMod);
                    continue;
                }
                EquipmentSlot slot = null;
                try {
                    slot = CraftEquipmentSlot.getSlot(net.minecraft.entity.EquipmentSlot.byName(slotName.toLowerCase(Locale.ROOT)));
                }
                catch (IllegalArgumentException illegalArgumentException) {
                    // empty catch block
                }
                if (slot == null) {
                    modifiers.put((Object)attribute, (Object)attribMod);
                    continue;
                }
                attribMod = new AttributeModifier(attribMod.getUniqueId(), attribMod.getName(), attribMod.getAmount(), attribMod.getOperation(), slot);
            }
            modifiers.put((Object)attribute, (Object)attribMod);
        }
        return modifiers;
    }

    static EnchantmentMap buildEnchantments(Map<String, Object> map, ItemMetaKey key) {
        Map ench = SerializableMeta.getObject(Map.class, map, key.BUKKIT, true);
        if (ench == null) {
            return null;
        }
        EnchantmentMap enchantments = new EnchantmentMap();
        for (Map.Entry entry : ench.entrySet()) {
            Enchantment enchantment;
            String enchantKey = entry.getKey().toString();
            if (enchantKey.equals("SWEEPING")) {
                enchantKey = "SWEEPING_EDGE";
            }
            if ((enchantment = Enchantment.getByName((String)FieldRename.convertEnchantmentName(ApiVersion.CURRENT, enchantKey))) == null || !(entry.getValue() instanceof Integer)) continue;
            enchantments.put(enchantment, (Integer)entry.getValue());
        }
        return enchantments;
    }

    static Multimap<Attribute, AttributeModifier> buildModifiers(Map<String, Object> map, ItemMetaKey key) {
        Map mods = SerializableMeta.getObject(Map.class, map, key.BUKKIT, true);
        LinkedHashMultimap result = LinkedHashMultimap.create();
        if (mods == null) {
            return result;
        }
        for (Object obj : mods.keySet()) {
            String attributeName;
            if (!(obj instanceof String) || Strings.isNullOrEmpty((String)(attributeName = (String)obj))) continue;
            List list = SerializableMeta.getObject(List.class, mods, attributeName, true);
            if (list == null || list.isEmpty()) {
                return result;
            }
            for (Object o : list) {
                if (!(o instanceof AttributeModifier)) continue;
                AttributeModifier modifier = (AttributeModifier)o;
                Attribute attribute = (Attribute)EnumUtils.getEnum(Attribute.class, (String)FieldRename.convertAttributeName(ApiVersion.CURRENT, attributeName.toUpperCase(Locale.ROOT)));
                if (attribute == null) continue;
                result.put((Object)attribute, (Object)modifier);
            }
        }
        return result;
    }

    @Overridden
    void applyToItem(Applicator itemTag) {
        NbtCompound customTag;
        if (this.hasDisplayName()) {
            itemTag.put(NAME, this.displayName);
        }
        if (this.hasItemName()) {
            itemTag.put(ITEM_NAME, this.itemName);
        }
        if (this.lore != null) {
            itemTag.put(LORE, new LoreComponent(this.lore));
        }
        if (this.hasCustomModelData()) {
            itemTag.put(CUSTOM_MODEL_DATA, new CustomModelDataComponent(this.customModelData));
        }
        if (this.hasBlockData()) {
            itemTag.put(BLOCK_DATA, new BlockStateComponent(this.blockData));
        }
        if (this.hideFlag != 0 && this.hasItemFlag(ItemFlag.HIDE_ADDITIONAL_TOOLTIP)) {
            itemTag.put(HIDE_ADDITIONAL_TOOLTIP, Unit.INSTANCE);
        }
        this.applyEnchantments(this.enchantments, itemTag, ENCHANTMENTS, ItemFlag.HIDE_ENCHANTS);
        this.applyModifiers(this.attributeModifiers, itemTag);
        if (this.hasRepairCost()) {
            itemTag.put(REPAIR, this.repairCost);
        }
        if (this.isHideTooltip()) {
            itemTag.put(HIDE_TOOLTIP, Unit.INSTANCE);
        }
        if (this.isUnbreakable()) {
            itemTag.put(UNBREAKABLE, new UnbreakableComponent(!this.hasItemFlag(ItemFlag.HIDE_UNBREAKABLE)));
        }
        if (this.hasEnchantmentGlintOverride()) {
            itemTag.put(ENCHANTMENT_GLINT_OVERRIDE, this.getEnchantmentGlintOverride());
        }
        if (this.isFireResistant()) {
            itemTag.put(FIRE_RESISTANT, Unit.INSTANCE);
        }
        if (this.hasMaxStackSize()) {
            itemTag.put(MAX_STACK_SIZE, this.maxStackSize);
        }
        if (this.hasRarity()) {
            itemTag.put(RARITY, Rarity.valueOf(this.rarity.name()));
        }
        if (this.hasFood()) {
            itemTag.put(FOOD, this.food.getHandle());
        }
        if (this.hasDamage()) {
            itemTag.put(DAMAGE, this.damage);
        }
        if (this.hasMaxDamage()) {
            itemTag.put(MAX_DAMAGE, this.maxDamage);
        }
        if (this.canPlaceOnPredicates != null && !this.canPlaceOnPredicates.isEmpty()) {
            itemTag.put(CAN_PLACE_ON, new BlockPredicatesChecker(this.canPlaceOnPredicates, !this.hasItemFlag(ItemFlag.HIDE_PLACED_ON)));
        }
        if (this.canBreakPredicates != null && !this.canBreakPredicates.isEmpty()) {
            itemTag.put(CAN_BREAK, new BlockPredicatesChecker(this.canBreakPredicates, !this.hasItemFlag(ItemFlag.HIDE_DESTROYS)));
        }
        for (Map.Entry<DataComponentType<?>, Optional<?>> e2 : this.unhandledTags.build().entrySet()) {
            e2.getValue().ifPresentOrElse(value -> itemTag.builder.add((DataComponentType)e2.getKey(), value), () -> itemTag.remove((DataComponentType)e2.getKey()));
        }
        NbtCompound nbtCompound = customTag = this.customTag != null ? this.customTag.copy() : null;
        if (!this.persistentDataContainer.isEmpty()) {
            NbtCompound bukkitCustomCompound = new NbtCompound();
            Map<String, NbtElement> rawPublicMap = this.persistentDataContainer.getRaw();
            for (Map.Entry<String, NbtElement> nbtBaseEntry : rawPublicMap.entrySet()) {
                bukkitCustomCompound.put(nbtBaseEntry.getKey(), nbtBaseEntry.getValue());
            }
            if (customTag == null) {
                customTag = new NbtCompound();
            }
            customTag.put(CraftMetaItem.BUKKIT_CUSTOM_TAG.BUKKIT, bukkitCustomCompound);
        }
        if (customTag != null) {
            itemTag.put(CUSTOM_DATA, NbtComponent.of(customTag));
        }
    }

    void applyEnchantments(Map<Enchantment, Integer> enchantments, Applicator tag, ItemMetaKeyType<ItemEnchantmentsComponent> key, ItemFlag itemFlag) {
        if (enchantments == null) {
            return;
        }
        ItemEnchantmentsComponent.Builder list = new ItemEnchantmentsComponent.Builder(ItemEnchantmentsComponent.DEFAULT);
        for (Map.Entry<Enchantment, Integer> entry : enchantments.entrySet()) {
            list.set(CraftEnchantment.bukkitToMinecraft(entry.getKey()), entry.getValue());
        }
        list.showInTooltip = !this.hasItemFlag(itemFlag);
        tag.put(key, list.build());
    }

    void applyModifiers(Multimap<Attribute, AttributeModifier> modifiers, Applicator tag) {
        if (modifiers == null || modifiers.isEmpty()) {
            return;
        }
        AttributeModifiersComponent.Builder list = AttributeModifiersComponent.builder();
        for (Map.Entry entry : modifiers.entries()) {
            if (entry.getKey() == null || entry.getValue() == null) continue;
            EntityAttributeModifier nmsModifier = CraftAttributeInstance.convert((AttributeModifier)entry.getValue());
            RegistryEntry<EntityAttribute> name = CraftAttribute.bukkitToMinecraftHolder((Attribute)entry.getKey());
            if (name == null) continue;
            AttributeModifierSlot group = CraftEquipmentSlot.getNMSGroup(((AttributeModifier)entry.getValue()).getSlotGroup());
            list.add(name, nmsModifier, group);
        }
        tag.put(ATTRIBUTES, list.build().withShowInTooltip(!this.hasItemFlag(ItemFlag.HIDE_ATTRIBUTES)));
    }

    @Overridden
    boolean applicableTo(Material type) {
        return type != Material.AIR;
    }

    @Overridden
    boolean isEmpty() {
        return !this.hasDisplayName() && !this.hasItemName() && !this.hasLocalizedName() && !this.hasEnchants() && this.lore == null && !this.hasCustomModelData() && !this.hasBlockData() && !this.hasRepairCost() && this.unhandledTags.build().isEmpty() && this.persistentDataContainer.isEmpty() && this.hideFlag == 0 && !this.isHideTooltip() && !this.isUnbreakable() && !this.hasEnchantmentGlintOverride() && !this.isFireResistant() && !this.hasMaxStackSize() && !this.hasRarity() && !this.hasFood() && !this.hasDamage() && !this.hasMaxDamage() && !this.hasAttributeModifiers() && this.customTag == null && this.canPlaceOnPredicates == null && this.canBreakPredicates == null;
    }

    public Component displayName() {
        return this.displayName == null ? null : PaperAdventure.asAdventure(this.displayName);
    }

    public void displayName(Component displayName) {
        this.displayName = displayName == null ? null : PaperAdventure.asVanilla(displayName);
    }

    public String getDisplayName() {
        return CraftChatMessage.fromComponent(this.displayName);
    }

    public BaseComponent[] getDisplayNameComponent() {
        return this.displayName == null ? new BaseComponent[]{} : ComponentSerializer.parse((String)CraftChatMessage.toJSON(this.displayName));
    }

    public final void setDisplayName(String name) {
        this.displayName = CraftChatMessage.fromStringOrNull(name);
    }

    public void setDisplayNameComponent(BaseComponent[] component) {
        this.displayName = CraftChatMessage.fromJSON(ComponentSerializer.toString((BaseComponent[])component));
    }

    public boolean hasDisplayName() {
        return this.displayName != null;
    }

    public String getItemName() {
        return CraftChatMessage.fromComponent(this.itemName);
    }

    public final void setItemName(String name) {
        this.itemName = CraftChatMessage.fromStringOrNull(name);
    }

    public boolean hasItemName() {
        return this.itemName != null;
    }

    public Component itemName() {
        return PaperAdventure.asAdventure(this.itemName);
    }

    public void itemName(Component name) {
        this.itemName = PaperAdventure.asVanilla(name);
    }

    public String getLocalizedName() {
        return this.getDisplayName();
    }

    public void setLocalizedName(String name) {
    }

    public boolean hasLocalizedName() {
        return false;
    }

    public boolean hasLore() {
        return this.lore != null && !this.lore.isEmpty();
    }

    public List<Component> lore() {
        return this.lore != null ? PaperAdventure.asAdventure(this.lore) : null;
    }

    public void lore(List<? extends Component> lore) {
        Preconditions.checkArgument((lore == null || lore.size() <= 256 ? 1 : 0) != 0, (String)"lore cannot have more than %s lines", (int)256);
        this.lore = lore != null ? PaperAdventure.asVanilla(lore) : null;
    }

    public boolean hasRepairCost() {
        return this.repairCost > 0;
    }

    public boolean hasEnchant(Enchantment ench) {
        Preconditions.checkArgument((ench != null ? 1 : 0) != 0, (Object)"Enchantment cannot be null");
        return this.hasEnchants() && this.enchantments.containsKey(ench);
    }

    public int getEnchantLevel(Enchantment ench) {
        Integer level;
        Preconditions.checkArgument((ench != null ? 1 : 0) != 0, (Object)"Enchantment cannot be null");
        Integer n = level = this.hasEnchants() ? (Integer)this.enchantments.get(ench) : null;
        if (level == null) {
            return 0;
        }
        return level;
    }

    public Map<Enchantment, Integer> getEnchants() {
        return this.hasEnchants() ? ImmutableSortedMap.copyOfSorted((SortedMap)this.enchantments) : ImmutableMap.of();
    }

    public boolean addEnchant(Enchantment ench, int level, boolean ignoreRestrictions) {
        Preconditions.checkArgument((ench != null ? 1 : 0) != 0, (Object)"Enchantment cannot be null");
        if (this.enchantments == null) {
            this.enchantments = new EnchantmentMap();
        }
        if (ignoreRestrictions || level >= ench.getStartLevel() && level <= ench.getMaxLevel()) {
            Integer old = this.enchantments.put(ench, level);
            return old == null || old != level;
        }
        return false;
    }

    public boolean removeEnchant(Enchantment ench) {
        boolean enchantmentRemoved;
        Preconditions.checkArgument((ench != null ? 1 : 0) != 0, (Object)"Enchantment cannot be null");
        boolean bl = enchantmentRemoved = this.hasEnchants() && this.enchantments.remove(ench) != null;
        if (enchantmentRemoved && this.enchantments.isEmpty()) {
            this.enchantments = null;
        }
        return enchantmentRemoved;
    }

    public void removeEnchantments() {
        if (this.hasEnchants()) {
            this.enchantments.clear();
        }
    }

    public boolean hasEnchants() {
        return this.enchantments != null && !this.enchantments.isEmpty();
    }

    public boolean hasConflictingEnchant(Enchantment ench) {
        return CraftMetaItem.checkConflictingEnchants(this.enchantments, ench);
    }

    public void addItemFlags(ItemFlag ... hideFlags) {
        for (ItemFlag f2 : hideFlags) {
            this.hideFlag |= this.getBitModifier(f2);
        }
    }

    public void removeItemFlags(ItemFlag ... hideFlags) {
        for (ItemFlag f2 : hideFlags) {
            this.hideFlag &= ~this.getBitModifier(f2);
        }
    }

    public Set<ItemFlag> getItemFlags() {
        EnumSet<ItemFlag> currentFlags = EnumSet.noneOf(ItemFlag.class);
        for (ItemFlag f2 : ItemFlag.values()) {
            if (!this.hasItemFlag(f2)) continue;
            currentFlags.add(f2);
        }
        return currentFlags;
    }

    public boolean hasItemFlag(ItemFlag flag) {
        int bitModifier = this.getBitModifier(flag);
        return (this.hideFlag & bitModifier) == bitModifier;
    }

    private int getBitModifier(ItemFlag hideFlag) {
        return 1 << hideFlag.ordinal();
    }

    public List<String> getLore() {
        return this.lore == null ? null : new ArrayList(Lists.transform(this.lore, CraftChatMessage::fromComponent));
    }

    public List<BaseComponent[]> getLoreComponents() {
        return this.lore == null ? null : new ArrayList(this.lore.stream().map(entry -> ComponentSerializer.parse((String)CraftChatMessage.toJSON(entry))).collect(Collectors.toList()));
    }

    public void setLore(List<String> lore) {
        Preconditions.checkArgument((lore == null || lore.size() <= 256 ? 1 : 0) != 0, (String)"lore cannot have more than %s lines", (int)256);
        if (lore == null || lore.isEmpty()) {
            this.lore = null;
        } else {
            if (this.lore == null) {
                this.lore = new ArrayList<Text>(lore.size());
            } else {
                this.lore.clear();
            }
            CraftMetaItem.safelyAdd(lore, this.lore, false);
        }
    }

    public void setLoreComponents(List<BaseComponent[]> lore) {
        Preconditions.checkArgument((lore == null || lore.size() <= 256 ? 1 : 0) != 0, (String)"lore cannot have more than %s lines", (int)256);
        if (lore == null) {
            this.lore = null;
        } else if (this.lore == null) {
            this.lore = new ArrayList<Text>(lore.size());
            CraftMetaItem.safelyAdd(lore, this.lore, false);
        } else {
            this.lore.clear();
            CraftMetaItem.safelyAdd(lore, this.lore, false);
        }
    }

    public boolean hasCustomModelData() {
        return this.customModelData != null;
    }

    public int getCustomModelData() {
        Preconditions.checkState((boolean)this.hasCustomModelData(), (Object)"We don't have CustomModelData! Check hasCustomModelData first!");
        return this.customModelData;
    }

    public void setCustomModelData(Integer data) {
        this.customModelData = data;
    }

    public boolean hasBlockData() {
        return this.blockData != null;
    }

    public BlockData getBlockData(Material material) {
        BlockState defaultData = CraftBlockType.bukkitToMinecraft(material).getDefaultState();
        return CraftBlockData.fromData(this.hasBlockData() ? new BlockStateComponent(this.blockData).applyToState(defaultData) : defaultData);
    }

    public void setBlockData(BlockData blockData) {
        this.blockData = blockData == null ? null : ((CraftBlockData)blockData).toStates();
    }

    public int getRepairCost() {
        return this.repairCost;
    }

    public void setRepairCost(int cost) {
        this.repairCost = cost;
    }

    public boolean isHideTooltip() {
        return this.hideTooltip;
    }

    public void setHideTooltip(boolean hideTooltip) {
        this.hideTooltip = hideTooltip;
    }

    public boolean isUnbreakable() {
        return this.unbreakable;
    }

    public void setUnbreakable(boolean unbreakable) {
        this.unbreakable = unbreakable;
    }

    public boolean hasEnchantmentGlintOverride() {
        return this.enchantmentGlintOverride != null;
    }

    public Boolean getEnchantmentGlintOverride() {
        Preconditions.checkState((boolean)this.hasEnchantmentGlintOverride(), (Object)"We don't have enchantment_glint_override! Check hasEnchantmentGlintOverride first!");
        return this.enchantmentGlintOverride;
    }

    public void setEnchantmentGlintOverride(Boolean override) {
        this.enchantmentGlintOverride = override;
    }

    public boolean isFireResistant() {
        return this.fireResistant;
    }

    public void setFireResistant(boolean fireResistant) {
        this.fireResistant = fireResistant;
    }

    public boolean hasMaxStackSize() {
        return this.maxStackSize != null;
    }

    public int getMaxStackSize() {
        Preconditions.checkState((boolean)this.hasMaxStackSize(), (Object)"We don't have max_stack_size! Check hasMaxStackSize first!");
        return this.maxStackSize;
    }

    public void setMaxStackSize(Integer max) {
        Preconditions.checkArgument((max == null || max > 0 ? 1 : 0) != 0, (Object)"max_stack_size must be > 0");
        Preconditions.checkArgument((max == null || max <= 99 ? 1 : 0) != 0, (Object)"max_stack_size must be <= 99");
        this.maxStackSize = max;
    }

    public boolean hasRarity() {
        return this.rarity != null;
    }

    public ItemRarity getRarity() {
        Preconditions.checkState((boolean)this.hasRarity(), (Object)"We don't have rarity! Check hasRarity first!");
        return this.rarity;
    }

    public void setRarity(ItemRarity rarity) {
        this.rarity = rarity;
    }

    public boolean hasFood() {
        return this.food != null;
    }

    public org.bukkit.inventory.meta.components.FoodComponent getFood() {
        return this.hasFood() ? new CraftFoodComponent(this.food) : new CraftFoodComponent(new FoodComponent(0, 0.0f, false, 0.0f, Collections.emptyList()));
    }

    public void setFood(org.bukkit.inventory.meta.components.FoodComponent food) {
        this.food = food == null ? null : new CraftFoodComponent((CraftFoodComponent)food);
    }

    public boolean hasAttributeModifiers() {
        return this.attributeModifiers != null && !this.attributeModifiers.isEmpty();
    }

    public Multimap<Attribute, AttributeModifier> getAttributeModifiers() {
        return this.hasAttributeModifiers() ? ImmutableMultimap.copyOf(this.attributeModifiers) : null;
    }

    private void checkAttributeList() {
        if (this.attributeModifiers == null) {
            this.attributeModifiers = LinkedHashMultimap.create();
        }
    }

    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(@Nullable EquipmentSlot slot) {
        this.checkAttributeList();
        LinkedHashMultimap result = LinkedHashMultimap.create();
        for (Map.Entry entry : this.attributeModifiers.entries()) {
            if (((AttributeModifier)entry.getValue()).getSlot() != null && ((AttributeModifier)entry.getValue()).getSlot() != slot) continue;
            result.put((Object)((Attribute)entry.getKey()), (Object)((AttributeModifier)entry.getValue()));
        }
        return result;
    }

    public Collection<AttributeModifier> getAttributeModifiers(@Nonnull Attribute attribute) {
        Preconditions.checkNotNull((Object)attribute, (Object)"Attribute cannot be null");
        return this.attributeModifiers.containsKey((Object)attribute) ? ImmutableList.copyOf((Collection)this.attributeModifiers.get((Object)attribute)) : null;
    }

    public boolean addAttributeModifier(@Nonnull Attribute attribute, @Nonnull AttributeModifier modifier) {
        Preconditions.checkNotNull((Object)attribute, (Object)"Attribute cannot be null");
        Preconditions.checkNotNull((Object)modifier, (Object)"AttributeModifier cannot be null");
        this.checkAttributeList();
        for (Map.Entry entry : this.attributeModifiers.entries()) {
            Preconditions.checkArgument((!((AttributeModifier)entry.getValue()).getUniqueId().equals(modifier.getUniqueId()) || entry.getKey() != attribute ? 1 : 0) != 0, (String)"Cannot register AttributeModifier. Modifier is already applied! %s", (Object)modifier);
        }
        return this.attributeModifiers.put((Object)attribute, (Object)modifier);
    }

    public void setAttributeModifiers(@Nullable Multimap<Attribute, AttributeModifier> attributeModifiers) {
        if (attributeModifiers == null || attributeModifiers.isEmpty()) {
            this.attributeModifiers = LinkedHashMultimap.create();
            return;
        }
        this.checkAttributeList();
        this.attributeModifiers.clear();
        Iterator iterator = attributeModifiers.entries().iterator();
        while (iterator.hasNext()) {
            Map.Entry next = (Map.Entry)iterator.next();
            if (next.getKey() == null || next.getValue() == null) {
                iterator.remove();
                continue;
            }
            this.attributeModifiers.put((Object)((Attribute)next.getKey()), (Object)((AttributeModifier)next.getValue()));
        }
    }

    public boolean removeAttributeModifier(@Nonnull Attribute attribute) {
        Preconditions.checkNotNull((Object)attribute, (Object)"Attribute cannot be null");
        this.checkAttributeList();
        return !this.attributeModifiers.removeAll((Object)attribute).isEmpty();
    }

    public boolean removeAttributeModifier(@Nullable EquipmentSlot slot) {
        this.checkAttributeList();
        int removed = 0;
        Iterator iter = this.attributeModifiers.entries().iterator();
        while (iter.hasNext()) {
            Map.Entry entry = (Map.Entry)iter.next();
            if (((AttributeModifier)entry.getValue()).getSlot() != null && ((AttributeModifier)entry.getValue()).getSlot() != slot) continue;
            iter.remove();
            ++removed;
        }
        return removed > 0;
    }

    public boolean removeAttributeModifier(@Nonnull Attribute attribute, @Nonnull AttributeModifier modifier) {
        Preconditions.checkNotNull((Object)attribute, (Object)"Attribute cannot be null");
        Preconditions.checkNotNull((Object)modifier, (Object)"AttributeModifier cannot be null");
        this.checkAttributeList();
        int removed = 0;
        Iterator iter = this.attributeModifiers.entries().iterator();
        while (iter.hasNext()) {
            Map.Entry entry = (Map.Entry)iter.next();
            if (entry.getKey() == null || entry.getValue() == null) {
                iter.remove();
                ++removed;
                continue;
            }
            if (entry.getKey() != attribute || !((AttributeModifier)entry.getValue()).getUniqueId().equals(modifier.getUniqueId())) continue;
            iter.remove();
            ++removed;
        }
        return removed > 0;
    }

    public String getAsString() {
        Applicator tag = new Applicator(this){};
        this.applyToItem(tag);
        ComponentChanges patch = tag.build();
        NbtElement nbt = (NbtElement)ComponentChanges.CODEC.encodeStart(MinecraftServer.getDefaultRegistryAccess().getOps(NbtOps.INSTANCE), (Object)patch).getOrThrow();
        return nbt.toString();
    }

    public CustomItemTagContainer getCustomTagContainer() {
        return new DeprecatedCustomTagContainer(this.getPersistentDataContainer());
    }

    public PersistentDataContainer getPersistentDataContainer() {
        return this.persistentDataContainer;
    }

    private static boolean compareModifiers(Multimap<Attribute, AttributeModifier> first, Multimap<Attribute, AttributeModifier> second) {
        if (first == null || second == null) {
            return false;
        }
        for (Map.Entry entry : first.entries()) {
            if (second.containsEntry(entry.getKey(), entry.getValue())) continue;
            return false;
        }
        for (Map.Entry entry : second.entries()) {
            if (first.containsEntry(entry.getKey(), entry.getValue())) continue;
            return false;
        }
        return true;
    }

    public boolean hasDamage() {
        return this.damage > 0;
    }

    public int getDamage() {
        return this.damage;
    }

    public void setDamage(int damage) {
        this.damage = damage;
    }

    public boolean hasMaxDamage() {
        return this.maxDamage != null;
    }

    public int getMaxDamage() {
        Preconditions.checkState((boolean)this.hasMaxDamage(), (Object)"We don't have max_damage! Check hasMaxDamage first!");
        return this.maxDamage;
    }

    public void setMaxDamage(Integer maxDamage) {
        this.maxDamage = maxDamage;
    }

    public final boolean equals(Object object) {
        if (object == null) {
            return false;
        }
        if (object == this) {
            return true;
        }
        if (!(object instanceof CraftMetaItem)) {
            return false;
        }
        return CraftItemFactory.instance().equals((ItemMeta)this, (ItemMeta)object);
    }

    @Overridden
    boolean equalsCommon(CraftMetaItem that) {
        return (this.hasDisplayName() ? that.hasDisplayName() && this.displayName.equals(that.displayName) : !that.hasDisplayName()) && (this.hasItemName() ? that.hasItemName() && this.itemName.equals(that.itemName) : !that.hasItemName()) && (this.hasEnchants() ? that.hasEnchants() && this.enchantments.equals(that.enchantments) : !that.hasEnchants()) && Objects.equals(this.lore, that.lore) && (this.hasCustomModelData() ? that.hasCustomModelData() && this.customModelData.equals(that.customModelData) : !that.hasCustomModelData()) && (this.hasBlockData() ? that.hasBlockData() && this.blockData.equals(that.blockData) : !that.hasBlockData()) && (this.hasRepairCost() ? that.hasRepairCost() && this.repairCost == that.repairCost : !that.hasRepairCost()) && (this.hasAttributeModifiers() ? that.hasAttributeModifiers() && CraftMetaItem.compareModifiers(this.attributeModifiers, that.attributeModifiers) : !that.hasAttributeModifiers()) && this.unhandledTags.equals(that.unhandledTags) && Objects.equals(this.customTag, that.customTag) && this.persistentDataContainer.equals(that.persistentDataContainer) && this.hideFlag == that.hideFlag && this.isHideTooltip() == that.isHideTooltip() && this.isUnbreakable() == that.isUnbreakable() && (this.hasEnchantmentGlintOverride() ? that.hasEnchantmentGlintOverride() && this.enchantmentGlintOverride.equals(that.enchantmentGlintOverride) : !that.hasEnchantmentGlintOverride()) && this.fireResistant == that.fireResistant && (this.hasMaxStackSize() ? that.hasMaxStackSize() && this.maxStackSize.equals(that.maxStackSize) : !that.hasMaxStackSize()) && this.rarity == that.rarity && (this.hasFood() ? that.hasFood() && this.food.equals(that.food) : !that.hasFood()) && (this.hasDamage() ? that.hasDamage() && this.damage == that.damage : !that.hasDamage()) && (this.hasMaxDamage() ? that.hasMaxDamage() && this.maxDamage.equals(that.maxDamage) : !that.hasMaxDamage()) && (this.canPlaceOnPredicates != null ? that.canPlaceOnPredicates != null && this.canPlaceOnPredicates.equals(that.canPlaceOnPredicates) : that.canPlaceOnPredicates == null) && (this.canBreakPredicates != null ? that.canBreakPredicates != null && this.canBreakPredicates.equals(that.canBreakPredicates) : that.canBreakPredicates == null) && this.version == that.version;
    }

    @Overridden
    boolean notUncommon(CraftMetaItem meta) {
        return true;
    }

    public final int hashCode() {
        return this.applyHash();
    }

    @Overridden
    int applyHash() {
        int hash = 3;
        hash = 61 * hash + (this.hasDisplayName() ? this.displayName.hashCode() : 0);
        hash = 61 * hash + (this.hasItemName() ? this.itemName.hashCode() : 0);
        hash = 61 * hash + (this.lore != null ? this.lore.hashCode() : 0);
        hash = 61 * hash + (this.hasCustomModelData() ? this.customModelData.hashCode() : 0);
        hash = 61 * hash + (this.hasBlockData() ? this.blockData.hashCode() : 0);
        hash = 61 * hash + (this.hasEnchants() ? this.enchantments.hashCode() : 0);
        hash = 61 * hash + (this.hasRepairCost() ? this.repairCost : 0);
        hash = 61 * hash + this.unhandledTags.hashCode();
        hash = 61 * hash + (this.customTag != null ? this.customTag.hashCode() : 0);
        hash = 61 * hash + (!this.persistentDataContainer.isEmpty() ? this.persistentDataContainer.hashCode() : 0);
        hash = 61 * hash + this.hideFlag;
        hash = 61 * hash + (this.isHideTooltip() ? 1231 : 1237);
        hash = 61 * hash + (this.isUnbreakable() ? 1231 : 1237);
        hash = 61 * hash + (this.hasEnchantmentGlintOverride() ? this.enchantmentGlintOverride.hashCode() : 0);
        hash = 61 * hash + (this.isFireResistant() ? 1231 : 1237);
        hash = 61 * hash + (this.hasMaxStackSize() ? this.maxStackSize.hashCode() : 0);
        hash = 61 * hash + (this.hasRarity() ? this.rarity.hashCode() : 0);
        hash = 61 * hash + (this.hasFood() ? this.food.hashCode() : 0);
        hash = 61 * hash + (this.hasDamage() ? this.damage : 0);
        hash = 61 * hash + (this.hasMaxDamage() ? 1231 : 1237);
        hash = 61 * hash + (this.hasAttributeModifiers() ? this.attributeModifiers.hashCode() : 0);
        hash = 61 * hash + (this.canPlaceOnPredicates != null ? this.canPlaceOnPredicates.hashCode() : 0);
        hash = 61 * hash + (this.canBreakPredicates != null ? this.canBreakPredicates.hashCode() : 0);
        hash = 61 * hash + this.version;
        return hash;
    }

    @Overridden
    public CraftMetaItem clone() {
        try {
            CraftMetaItem clone = (CraftMetaItem)super.clone();
            if (this.lore != null) {
                clone.lore = new ArrayList<Text>(this.lore);
            }
            clone.customModelData = this.customModelData;
            clone.blockData = this.blockData;
            if (this.enchantments != null) {
                clone.enchantments = new EnchantmentMap(this.enchantments);
            }
            if (this.hasAttributeModifiers()) {
                clone.attributeModifiers = LinkedHashMultimap.create(this.attributeModifiers);
            }
            if (this.customTag != null) {
                clone.customTag = this.customTag.copy();
            }
            clone.persistentDataContainer = new CraftPersistentDataContainer(this.persistentDataContainer.getTagsCloned(), DATA_TYPE_REGISTRY);
            clone.hideFlag = this.hideFlag;
            clone.hideTooltip = this.hideTooltip;
            clone.unbreakable = this.unbreakable;
            clone.enchantmentGlintOverride = this.enchantmentGlintOverride;
            clone.fireResistant = this.fireResistant;
            clone.maxStackSize = this.maxStackSize;
            clone.rarity = this.rarity;
            if (this.hasFood()) {
                clone.food = new CraftFoodComponent(this.food);
            }
            clone.damage = this.damage;
            clone.maxDamage = this.maxDamage;
            clone.version = this.version;
            if (this.canPlaceOnPredicates != null) {
                clone.canPlaceOnPredicates = List.copyOf(this.canPlaceOnPredicates);
            }
            if (this.canBreakPredicates != null) {
                clone.canBreakPredicates = List.copyOf(this.canBreakPredicates);
            }
            return clone;
        }
        catch (CloneNotSupportedException e2) {
            throw new Error(e2);
        }
    }

    public final Map<String, Object> serialize() {
        ImmutableMap.Builder map = ImmutableMap.builder();
        map.put((Object)"meta-type", SerializableMeta.classMap.get(this.getClass()));
        this.serialize((ImmutableMap.Builder<String, Object>)map);
        return map.build();
    }

    @Overridden
    ImmutableMap.Builder<String, Object> serialize(ImmutableMap.Builder<String, Object> builder) {
        boolean canPlaceOnAddToUnhandled;
        boolean canBreakAddToUnhandled;
        if (this.hasDisplayName()) {
            builder.put((Object)CraftMetaItem.NAME.BUKKIT, (Object)CraftChatMessage.toJSON(this.displayName));
        }
        if (this.hasItemName()) {
            builder.put((Object)CraftMetaItem.ITEM_NAME.BUKKIT, (Object)CraftChatMessage.toJSON(this.itemName));
        }
        if (this.hasLore()) {
            ArrayList<String> jsonLore = new ArrayList<String>();
            for (Text component : this.lore) {
                jsonLore.add(CraftChatMessage.toJSON(component));
            }
            builder.put((Object)CraftMetaItem.LORE.BUKKIT, jsonLore);
        }
        if (this.hasCustomModelData()) {
            builder.put((Object)CraftMetaItem.CUSTOM_MODEL_DATA.BUKKIT, (Object)this.customModelData);
        }
        if (this.hasBlockData()) {
            builder.put((Object)CraftMetaItem.BLOCK_DATA.BUKKIT, this.blockData);
        }
        CraftMetaItem.serializeEnchantments(this.enchantments, builder, ENCHANTMENTS);
        CraftMetaItem.serializeModifiers(this.attributeModifiers, builder, ATTRIBUTES);
        if (this.hasRepairCost()) {
            builder.put((Object)CraftMetaItem.REPAIR.BUKKIT, (Object)this.repairCost);
        }
        ArrayList<String> hideFlags = new ArrayList<String>();
        for (ItemFlag hideFlagEnum : this.getItemFlags()) {
            hideFlags.add(hideFlagEnum.name());
        }
        if (!hideFlags.isEmpty()) {
            builder.put((Object)CraftMetaItem.HIDEFLAGS.BUKKIT, hideFlags);
        }
        if (this.isHideTooltip()) {
            builder.put((Object)CraftMetaItem.HIDE_TOOLTIP.BUKKIT, (Object)this.hideTooltip);
        }
        if (this.isUnbreakable()) {
            builder.put((Object)CraftMetaItem.UNBREAKABLE.BUKKIT, (Object)this.unbreakable);
        }
        if (this.hasEnchantmentGlintOverride()) {
            builder.put((Object)CraftMetaItem.ENCHANTMENT_GLINT_OVERRIDE.BUKKIT, (Object)this.enchantmentGlintOverride);
        }
        if (this.isFireResistant()) {
            builder.put((Object)CraftMetaItem.FIRE_RESISTANT.BUKKIT, (Object)this.fireResistant);
        }
        if (this.hasMaxStackSize()) {
            builder.put((Object)CraftMetaItem.MAX_STACK_SIZE.BUKKIT, (Object)this.maxStackSize);
        }
        if (this.hasRarity()) {
            builder.put((Object)CraftMetaItem.RARITY.BUKKIT, (Object)this.rarity.name());
        }
        if (this.hasFood()) {
            builder.put((Object)CraftMetaItem.FOOD.BUKKIT, (Object)this.food);
        }
        if (this.hasDamage()) {
            builder.put((Object)CraftMetaItem.DAMAGE.BUKKIT, (Object)this.damage);
        }
        if (this.hasMaxDamage()) {
            builder.put((Object)CraftMetaItem.MAX_DAMAGE.BUKKIT, (Object)this.maxDamage);
        }
        HashMap<String, NbtElement> internalTags = new HashMap<String, NbtElement>();
        this.serializeInternal(internalTags);
        if (!internalTags.isEmpty()) {
            NbtCompound internal = new NbtCompound();
            for (Map.Entry e2 : internalTags.entrySet()) {
                internal.put((String)e2.getKey(), (NbtElement)e2.getValue());
            }
            try {
                ByteArrayOutputStream buf = new ByteArrayOutputStream();
                NbtIo.writeCompressed(internal, buf);
                builder.put((Object)"internal", (Object)Base64.getEncoder().encodeToString(buf.toByteArray()));
            }
            catch (IOException ex) {
                Logger.getLogger(CraftMetaItem.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        boolean bl = canBreakAddToUnhandled = this.canBreakPredicates != null && !this.canBreakPredicates.isEmpty();
        if (canBreakAddToUnhandled) {
            this.unhandledTags.add(DataComponentTypes.CAN_BREAK, new BlockPredicatesChecker(this.canBreakPredicates, !this.hasItemFlag(ItemFlag.HIDE_DESTROYS)));
        }
        boolean bl2 = canPlaceOnAddToUnhandled = this.canPlaceOnPredicates != null && !this.canPlaceOnPredicates.isEmpty();
        if (canPlaceOnAddToUnhandled) {
            this.unhandledTags.add(DataComponentTypes.CAN_PLACE_ON, new BlockPredicatesChecker(this.canPlaceOnPredicates, !this.hasItemFlag(ItemFlag.HIDE_PLACED_ON)));
        }
        if (!this.unhandledTags.isEmpty()) {
            NbtElement unhandled = (NbtElement)ComponentChanges.CODEC.encodeStart(MinecraftServer.getDefaultRegistryAccess().getOps(NbtOps.INSTANCE), (Object)this.unhandledTags.build()).getOrThrow(IllegalStateException::new);
            try {
                ByteArrayOutputStream buf = new ByteArrayOutputStream();
                NbtIo.writeCompressed((NbtCompound)unhandled, buf);
                builder.put((Object)"unhandled", (Object)Base64.getEncoder().encodeToString(buf.toByteArray()));
            }
            catch (IOException ex) {
                Logger.getLogger(CraftMetaItem.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        if (canBreakAddToUnhandled) {
            this.unhandledTags.clear(DataComponentTypes.CAN_BREAK);
        }
        if (canPlaceOnAddToUnhandled) {
            this.unhandledTags.clear(DataComponentTypes.CAN_PLACE_ON);
        }
        if (!this.persistentDataContainer.isEmpty()) {
            builder.put((Object)CraftMetaItem.BUKKIT_CUSTOM_TAG.BUKKIT, (Object)this.persistentDataContainer.serialize());
        }
        if (this.customTag != null) {
            try {
                ByteArrayOutputStream buf = new ByteArrayOutputStream();
                NbtIo.writeCompressed(this.customTag, buf);
                builder.put((Object)"custom", (Object)Base64.getEncoder().encodeToString(buf.toByteArray()));
            }
            catch (IOException ex) {
                Logger.getLogger(CraftMetaItem.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return builder;
    }

    void serializeInternal(Map<String, NbtElement> unhandledTags) {
    }

    Material updateMaterial(Material material) {
        return material;
    }

    static void serializeEnchantments(Map<Enchantment, Integer> enchantments, ImmutableMap.Builder<String, Object> builder, ItemMetaKey key) {
        if (enchantments == null || enchantments.isEmpty()) {
            return;
        }
        ImmutableMap.Builder enchants = ImmutableMap.builder();
        for (Map.Entry<Enchantment, Integer> enchant : enchantments.entrySet()) {
            enchants.put((Object)enchant.getKey().getName(), (Object)enchant.getValue());
        }
        builder.put((Object)key.BUKKIT, (Object)enchants.build());
    }

    static void serializeModifiers(Multimap<Attribute, AttributeModifier> modifiers, ImmutableMap.Builder<String, Object> builder, ItemMetaKey key) {
        if (modifiers == null || modifiers.isEmpty()) {
            return;
        }
        LinkedHashMap mods = new LinkedHashMap();
        for (Map.Entry entry : modifiers.entries()) {
            Collection modCollection;
            if (entry.getKey() == null || (modCollection = modifiers.get((Object)((Attribute)entry.getKey()))) == null || modCollection.isEmpty()) continue;
            mods.put(((Attribute)entry.getKey()).name(), new ArrayList(modCollection));
        }
        builder.put((Object)key.BUKKIT, mods);
    }

    static void safelyAdd(Iterable<?> addFrom, Collection<Text> addTo, boolean possiblyJsonInput) {
        if (addFrom == null) {
            return;
        }
        for (Object object : addFrom) {
            Text component;
            if (object instanceof BaseComponent[]) {
                BaseComponent[] baseComponentArr = (BaseComponent[])object;
                addTo.add(CraftChatMessage.fromJSON(ComponentSerializer.toString((BaseComponent[])baseComponentArr)));
                continue;
            }
            if (!(object instanceof String)) {
                if (object != null) {
                    throw new IllegalArgumentException(String.valueOf(addFrom) + " cannot contain non-string " + object.getClass().getName());
                }
                addTo.add(Text.empty());
                continue;
            }
            String entry = object.toString();
            Text text = component = possiblyJsonInput ? CraftChatMessage.fromJSONOrString(entry) : CraftChatMessage.fromStringOrNull(entry);
            if (component != null) {
                addTo.add(component);
                continue;
            }
            addTo.add(Text.empty());
        }
    }

    static boolean checkConflictingEnchants(Map<Enchantment, Integer> enchantments, Enchantment ench) {
        if (enchantments == null || enchantments.isEmpty()) {
            return false;
        }
        for (Enchantment enchant : enchantments.keySet()) {
            if (!enchant.conflictsWith(ench)) continue;
            return true;
        }
        return false;
    }

    public final String toString() {
        return (String)SerializableMeta.classMap.get(this.getClass()) + "_META:" + String.valueOf(this.serialize());
    }

    public int getVersion() {
        return this.version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public static Set<DataComponentType<?>> getTopLevelHandledDcts(Class<? extends CraftMetaItem> clazz) {
        Map<Class<? extends CraftMetaItem>, Set<DataComponentType<?>>> map = HANDLED_DCTS_PER_TYPE;
        synchronized (map) {
            if (HANDLED_DCTS_PER_TYPE.isEmpty()) {
                HashMap map2 = new HashMap();
                map2.put(CraftMetaArmor.class, Set.of(CraftMetaArmor.TRIM.TYPE));
                map2.put(CraftMetaArmorStand.class, Set.of(CraftMetaArmorStand.ENTITY_TAG.TYPE));
                map2.put(CraftMetaAxolotlBucket.class, Set.of(CraftMetaAxolotlBucket.ENTITY_TAG.TYPE));
                map2.put(CraftMetaBanner.class, Set.of(CraftMetaBanner.PATTERNS.TYPE));
                map2.put(CraftMetaBlockState.class, Set.of(CraftMetaBlockState.BLOCK_ENTITY_TAG.TYPE));
                map2.put(CraftMetaBook.class, Set.of(CraftMetaBook.BOOK_CONTENT.TYPE));
                map2.put(CraftMetaBookSigned.class, Set.of(CraftMetaBookSigned.BOOK_CONTENT.TYPE));
                map2.put(CraftMetaBundle.class, Set.of(CraftMetaBundle.ITEMS.TYPE));
                map2.put(CraftMetaCharge.class, Set.of(CraftMetaCharge.EXPLOSION.TYPE));
                map2.put(CraftMetaColorableArmor.class, Set.of(CraftMetaArmor.TRIM.TYPE, CraftMetaLeatherArmor.COLOR.TYPE));
                map2.put(CraftMetaCompass.class, Set.of(CraftMetaCompass.LODESTONE_TARGET.TYPE));
                map2.put(CraftMetaCrossbow.class, Set.of(CraftMetaCrossbow.CHARGED_PROJECTILES.TYPE));
                map2.put(CraftMetaEnchantedBook.class, Set.of(CraftMetaEnchantedBook.STORED_ENCHANTMENTS.TYPE));
                map2.put(CraftMetaEntityTag.class, Set.of(CraftMetaEntityTag.ENTITY_TAG.TYPE));
                map2.put(CraftMetaFirework.class, Set.of(CraftMetaFirework.FIREWORKS.TYPE));
                map2.put(CraftMetaKnowledgeBook.class, Set.of(CraftMetaKnowledgeBook.BOOK_RECIPES.TYPE));
                map2.put(CraftMetaLeatherArmor.class, Set.of(CraftMetaLeatherArmor.COLOR.TYPE));
                map2.put(CraftMetaMap.class, Set.of(CraftMetaMap.MAP_COLOR.TYPE, CraftMetaMap.MAP_POST_PROCESSING.TYPE, CraftMetaMap.MAP_ID.TYPE));
                map2.put(CraftMetaMusicInstrument.class, Set.of(CraftMetaMusicInstrument.GOAT_HORN_INSTRUMENT.TYPE));
                map2.put(CraftMetaOminousBottle.class, Set.of(CraftMetaOminousBottle.OMINOUS_BOTTLE_AMPLIFIER.TYPE));
                map2.put(CraftMetaPotion.class, Set.of(CraftMetaPotion.POTION_CONTENTS.TYPE));
                map2.put(CraftMetaSkull.class, Set.of(CraftMetaSkull.SKULL_PROFILE.TYPE, CraftMetaSkull.NOTE_BLOCK_SOUND.TYPE));
                map2.put(CraftMetaSpawnEgg.class, Set.of(CraftMetaSpawnEgg.ENTITY_TAG.TYPE));
                map2.put(CraftMetaSuspiciousStew.class, Set.of(CraftMetaSuspiciousStew.EFFECTS.TYPE));
                map2.put(CraftMetaTropicalFishBucket.class, Set.of(CraftMetaTropicalFishBucket.ENTITY_TAG.TYPE));
                for (Map.Entry entry : map2.entrySet()) {
                    ArrayList topLevelTags = new ArrayList((Collection)entry.getValue());
                    topLevelTags.addAll(DEFAULT_HANDLED_DCTS);
                    HANDLED_DCTS_PER_TYPE.put((Class)entry.getKey(), Set.copyOf(topLevelTags));
                }
            }
            return HANDLED_DCTS_PER_TYPE.getOrDefault(clazz, DEFAULT_HANDLED_DCTS);
        }
    }

    protected static <T> Optional<? extends T> getOrEmpty(ComponentChanges tag, ItemMetaKeyType<T> type) {
        Optional result = tag.get(type.TYPE);
        return result != null ? result : Optional.empty();
    }

    private static class EnchantmentMap
    extends TreeMap<Enchantment, Integer> {
        private EnchantmentMap(Map<Enchantment, Integer> enchantments) {
            this();
            this.putAll(enchantments);
        }

        private EnchantmentMap() {
            super(Comparator.comparing(o -> o.getKey().toString()));
        }

        @Override
        public EnchantmentMap clone() {
            return (EnchantmentMap)super.clone();
        }
    }

    static final class ItemMetaKeyType<T>
    extends ItemMetaKey {
        final DataComponentType<T> TYPE;

        ItemMetaKeyType(DataComponentType<T> type) {
            this(type, null, null);
        }

        ItemMetaKeyType(DataComponentType<T> type, String both) {
            this(type, both, both);
        }

        ItemMetaKeyType(DataComponentType<T> type, String nbt, String bukkit) {
            super(nbt, bukkit);
            this.TYPE = type;
        }
    }

    static class ItemMetaKey {
        final String BUKKIT;
        final String NBT;

        ItemMetaKey(String both) {
            this(both, both);
        }

        ItemMetaKey(String nbt, String bukkit) {
            this.NBT = nbt;
            this.BUKKIT = bukkit;
        }

        @Retention(value=RetentionPolicy.SOURCE)
        @Target(value={ElementType.FIELD})
        static @interface Specific {
            public To value();

            public static enum To {
                BUKKIT,
                NBT;

            }
        }
    }

    static abstract class Applicator {
        final ComponentChanges.Builder builder = ComponentChanges.builder();

        Applicator() {
        }

        void skullCallback(GameProfile gameProfile) {
        }

        <T> Applicator put(ItemMetaKeyType<T> key, T value) {
            this.builder.add(key.TYPE, value);
            return this;
        }

        <T> Applicator remove(DataComponentType<T> type) {
            this.builder.remove(type);
            return this;
        }

        ComponentChanges build() {
            return this.builder.build();
        }
    }
}*/

