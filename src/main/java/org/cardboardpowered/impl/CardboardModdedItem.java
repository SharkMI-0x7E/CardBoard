package org.cardboardpowered.impl;

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.Item;

public class CardboardModdedItem implements CardboardModdedMaterial {

    private Item item;
    private String id;

    public CardboardModdedItem(String id) {
        this.id = id;
        this.item = net.minecraft.core.registries.BuiltInRegistries.ITEM.getValue(net.minecraft.resources.Identifier.parse(id));
    }

    public CardboardModdedItem(Item item) {
        this.item = item;
    }

    @Override
    public short getDamage() {
    	
    	return item.components().get(DataComponents.MAX_DAMAGE).shortValue();
    	
        // return (short) item.getMaxDamage();
    }

    @Override
    public boolean isBlock() {
        return false;
    }

    @Override
    public boolean isItem() {
        return true;
    }

    @Override
    public boolean isEdible() {
        return false;
    }

    @Override
    public String getId() {
        return id;
    }

}