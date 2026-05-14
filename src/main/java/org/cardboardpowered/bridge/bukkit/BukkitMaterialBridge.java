package org.cardboardpowered.bridge.bukkit;

import org.cardboardpowered.impl.CardboardModdedMaterial;

public interface BukkitMaterialBridge {

    boolean isModded();

    CardboardModdedMaterial getModdedData();

    void setModdedData(CardboardModdedMaterial data);

}