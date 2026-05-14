package org.bukkit.craftbukkit.inventory;

import net.minecraft.world.Container;
import org.bukkit.block.Lectern;
import org.bukkit.inventory.LecternInventory;

import org.cardboardpowered.bridge.world.ContainerBridge;

public class CraftInventoryLectern extends CraftInventory implements LecternInventory {

    public CraftInventoryLectern(Container inventory) {
        super(inventory);
    }

    @Override
    public Lectern getHolder() {
        return (Lectern) ((ContainerBridge)(Object)inventory).getOwner();
    }

}