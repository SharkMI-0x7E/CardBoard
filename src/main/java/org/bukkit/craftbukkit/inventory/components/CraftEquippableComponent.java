package org.bukkit.craftbukkit.inventory.components;

import com.google.common.base.Preconditions;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.equipment.EquipmentAssets;
import net.minecraft.world.item.equipment.Equippable;
import org.bukkit.Keyed;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.Sound;
import org.bukkit.Tag;
import org.bukkit.configuration.serialization.SerializableAs;
import org.bukkit.craftbukkit.CraftEquipmentSlot;
import org.bukkit.craftbukkit.CraftSound;
import org.bukkit.craftbukkit.configuration.ConfigSerializationUtil;
import org.bukkit.craftbukkit.entity.CraftEntityType;
import org.bukkit.craftbukkit.inventory.SerializableMeta;
// import org.bukkit.craftbukkit.tag.CraftEntityTag;
import org.cardboardpowered.impl.tag.CraftEntityTag;
import org.bukkit.craftbukkit.util.CraftNamespacedKey;
import org.bukkit.entity.EntityType;

@SerializableAs(value="Equippable")
public final class CraftEquippableComponent
implements org.bukkit.inventory.meta.components.EquippableComponent {
    private Equippable handle;

    public CraftEquippableComponent(Equippable handle) {
        this.handle = handle;
    }

    public CraftEquippableComponent(CraftEquippableComponent craft) {
        this.handle = craft.handle;
    }

    public CraftEquippableComponent(Map<String, Object> map) {
        EquipmentSlot slot = CraftEquipmentSlot.getNMS(org.bukkit.inventory.EquipmentSlot.valueOf((String)SerializableMeta.getString(map, "slot", false)));
        Sound equipSound = null;
        String equipSoundKey = SerializableMeta.getString(map, "equip-sound", true);
        if (equipSoundKey != null) {
            equipSound = (Sound)Registry.SOUNDS.get(NamespacedKey.fromString((String)equipSoundKey));
        }
        String model = SerializableMeta.getString(map, "model", true);
        String cameraOverlay = SerializableMeta.getString(map, "camera-overlay", true);
        HolderSet allowedEntities = null;
        Object allowed = SerializableMeta.getObject(Object.class, map, "allowed-entities", true);
        if (allowed != null) {
            allowedEntities = ConfigSerializationUtil.getHolderSet(allowed, Registries.ENTITY_TYPE);
        }
        Boolean dispensable = SerializableMeta.getObject(Boolean.class, map, "dispensable", true);
        Boolean swappable = SerializableMeta.getObject(Boolean.class, map, "swappable", true);
        Boolean damageOnHurt = SerializableMeta.getObject(Boolean.class, map, "damage-on-hurt", true);
        Boolean equipOnInteract = SerializableMeta.getObject(Boolean.class, map, "equip-on-interact", true);
        this.handle = new Equippable(slot, equipSound != null ? CraftSound.bukkitToMinecraftHolder(equipSound) : SoundEvents.ARMOR_EQUIP_GENERIC, Optional.ofNullable(model).map(Identifier::parse).map(k -> ResourceKey.create(EquipmentAssets.ROOT_ID, k)), Optional.ofNullable(cameraOverlay).map(Identifier::parse), Optional.ofNullable(allowedEntities), dispensable != null ? dispensable : true, swappable != null ? swappable : true, damageOnHurt != null ? damageOnHurt : true, equipOnInteract != null ? equipOnInteract : false, false, BuiltInRegistries.SOUND_EVENT.wrapAsHolder(SoundEvents.SHEARS_SNIP));
    }

    public Map<String, Object> serialize() {
        NamespacedKey cameraOverlay;
        LinkedHashMap<String, Object> result = new LinkedHashMap<String, Object>();
        result.put("slot", this.getSlot().name());
        result.put("equip-sound", Registry.SOUND_EVENT.getKeyOrThrow(this.getEquipSound()).toString());
        NamespacedKey model = this.getModel();
        if (model != null) {
            result.put("model", model.toString());
        }
        if ((cameraOverlay = this.getCameraOverlay()) != null) {
            result.put("camera-overlay", cameraOverlay.toString());
        }
        this.handle.allowedEntities().ifPresent(holders -> ConfigSerializationUtil.setHolderSet(result, "allowed-entities", holders));
        result.put("dispensable", this.isDispensable());
        result.put("swappable", this.isSwappable());
        result.put("damage-on-hurt", this.isDamageOnHurt());
        result.put("equip-on-interact", this.isEquipOnInteract());
        return result;
    }

    public Equippable getHandle() {
        return this.handle;
    }

    public org.bukkit.inventory.EquipmentSlot getSlot() {
        return CraftEquipmentSlot.getSlot(this.handle.slot());
    }

    public void setSlot(org.bukkit.inventory.EquipmentSlot slot) {
        this.handle = new Equippable(CraftEquipmentSlot.getNMS(slot), this.handle.equipSound(), this.handle.assetId(), this.handle.cameraOverlay(), this.handle.allowedEntities(), this.handle.dispensable(), this.handle.swappable(), this.handle.damageOnHurt(), this.handle.equipOnInteract(), this.handle.canBeSheared(), this.handle.shearingSound());
    }

    public Sound getEquipSound() {
        return CraftSound.minecraftToBukkit(this.handle.equipSound().value());
    }

    public void setEquipSound(Sound sound) {
        this.handle = new Equippable(this.handle.slot(), sound != null ? CraftSound.bukkitToMinecraftHolder(sound) : SoundEvents.ARMOR_EQUIP_GENERIC, this.handle.assetId(), this.handle.cameraOverlay(), this.handle.allowedEntities(), this.handle.dispensable(), this.handle.swappable(), this.handle.damageOnHurt(), this.handle.equipOnInteract(), this.handle.canBeSheared(), this.handle.shearingSound());
    }

    public NamespacedKey getModel() {
        return this.handle.assetId().map(a2 -> CraftNamespacedKey.fromMinecraft(a2.identifier())).orElse(null);
    }

    public void setModel(NamespacedKey key) {
        this.handle = new Equippable(this.handle.slot(), this.handle.equipSound(), Optional.ofNullable(key).map(CraftNamespacedKey::toMinecraft).map(k -> ResourceKey.create(EquipmentAssets.ROOT_ID, k)), this.handle.cameraOverlay(), this.handle.allowedEntities(), this.handle.dispensable(), this.handle.swappable(), this.handle.damageOnHurt(), this.handle.equipOnInteract(), this.handle.canBeSheared(), this.handle.shearingSound());
    }

    public NamespacedKey getCameraOverlay() {
        return this.handle.cameraOverlay().map(CraftNamespacedKey::fromMinecraft).orElse(null);
    }

    public void setCameraOverlay(NamespacedKey key) {
        this.handle = new Equippable(this.handle.slot(), this.handle.equipSound(), this.handle.assetId(), Optional.ofNullable(key).map(CraftNamespacedKey::toMinecraft), this.handle.allowedEntities(), this.handle.dispensable(), this.handle.swappable(), this.handle.damageOnHurt(), this.handle.equipOnInteract(), this.handle.canBeSheared(), this.handle.shearingSound());
    }

    public Collection<EntityType> getAllowedEntities() {
        return this.handle.allowedEntities().map(HolderSet::stream).map(stream -> stream.map(Holder::value).map(CraftEntityType::minecraftToBukkit).collect(Collectors.toList())).orElse(null);
    }

    public void setAllowedEntities(EntityType entities) {
        this.handle = new Equippable(this.handle.slot(), this.handle.equipSound(), this.handle.assetId(), this.handle.cameraOverlay(), entities != null ? Optional.of(HolderSet.direct(CraftEntityType.bukkitToMinecraftHolder(entities))) : Optional.empty(), this.handle.dispensable(), this.handle.swappable(), this.handle.damageOnHurt(), this.handle.equipOnInteract(), this.handle.canBeSheared(), this.handle.shearingSound());
    }

    public void setAllowedEntities(Collection<EntityType> entities) {
        this.handle = new Equippable(this.handle.slot(), this.handle.equipSound(), this.handle.assetId(), this.handle.cameraOverlay(), entities != null ? Optional.of(HolderSet.direct(entities.stream().map(CraftEntityType::bukkitToMinecraftHolder).collect(Collectors.toList()))) : Optional.empty(), this.handle.dispensable(), this.handle.swappable(), this.handle.damageOnHurt(), this.handle.equipOnInteract(), this.handle.canBeSheared(), this.handle.shearingSound());
    }

    public void setAllowedEntities(Tag<EntityType> tag) {
        Preconditions.checkArgument((tag == null || tag instanceof CraftEntityTag ? 1 : 0) != 0, "tag must be an entity tag");
        this.handle = new Equippable(this.handle.slot(), this.handle.equipSound(), this.handle.assetId(), this.handle.cameraOverlay(), tag != null ? Optional.of(((CraftEntityTag)tag).getHandle()) : Optional.empty(), this.handle.dispensable(), this.handle.swappable(), this.handle.damageOnHurt(), this.handle.equipOnInteract(), this.handle.canBeSheared(), this.handle.shearingSound());
    }

    public boolean isDispensable() {
        return this.handle.dispensable();
    }

    public void setDispensable(boolean dispensable) {
        this.handle = new Equippable(this.handle.slot(), this.handle.equipSound(), this.handle.assetId(), this.handle.cameraOverlay(), this.handle.allowedEntities(), dispensable, this.handle.swappable(), this.handle.damageOnHurt(), this.handle.equipOnInteract(), this.handle.canBeSheared(), this.handle.shearingSound());
    }

    public boolean isSwappable() {
        return this.handle.swappable();
    }

    public void setSwappable(boolean swappable) {
        this.handle = new Equippable(this.handle.slot(), this.handle.equipSound(), this.handle.assetId(), this.handle.cameraOverlay(), this.handle.allowedEntities(), this.handle.dispensable(), swappable, this.handle.damageOnHurt(), this.handle.equipOnInteract(), this.handle.canBeSheared(), this.handle.shearingSound());
    }

    public boolean isDamageOnHurt() {
        return this.handle.damageOnHurt();
    }

    public void setDamageOnHurt(boolean damage) {
        this.handle = new Equippable(this.handle.slot(), this.handle.equipSound(), this.handle.assetId(), this.handle.cameraOverlay(), this.handle.allowedEntities(), this.handle.dispensable(), this.handle.swappable(), damage, this.handle.equipOnInteract(), this.handle.canBeSheared(), this.handle.shearingSound());
    }

    public boolean isEquipOnInteract() {
        return this.handle.equipOnInteract();
    }

    public void setEquipOnInteract(boolean equip) {
        this.handle = new Equippable(this.handle.slot(), this.handle.equipSound(), this.handle.assetId(), this.handle.cameraOverlay(), this.handle.allowedEntities(), this.handle.dispensable(), this.handle.swappable(), this.handle.damageOnHurt(), equip, this.handle.canBeSheared(), this.handle.shearingSound());
    }

    public int hashCode() {
        int hash = 7;
        hash = 19 * hash + this.handle.hashCode();
        return hash;
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || this.getClass() != obj.getClass()) {
            return false;
        }
        CraftEquippableComponent other = (CraftEquippableComponent)obj;
        return this.handle.equals(other.handle);
    }

    public String toString() {
        return "CraftEquippableComponent{component" + String.valueOf(this.handle) + "}";
    }
}

