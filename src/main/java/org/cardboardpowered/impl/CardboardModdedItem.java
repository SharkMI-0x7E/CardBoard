/**
 * Cardboard - Spigot/Paper for Fabric
 * Copyright (C) 2020-2024 CardboardPowered.org and contributors
 * Copyright (C) 2025-2026 SharkMI and contributors
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
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