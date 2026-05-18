/**
 * Cardboard - Spigot/Paper for Fabric
 * Copyright (C) 2020-2024 CardboardPowered.org and contributors
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
package org.cardboardpowered.mixin.world.level.storage.loot;

import net.minecraft.world.level.storage.loot.LootTable;
import org.cardboardpowered.util.MixinInfo;
import org.spongepowered.asm.mixin.Mixin;

@MixinInfo(events = {"LootGenerateEvent"})
@Mixin(LootTable.class)
public class LootTableMixin {

	// TODO: 1.19
	
    /*public void supplyInventory(Inventory iinventory, LootContext loottableinfo) {
        // CraftBukkit start
        this.fillInventory(iinventory, loottableinfo, false);
    }

    public void fillInventory(Inventory iinventory, LootContext loottableinfo, boolean plugin) {
        List<ItemStack> list = this.generateLoot(loottableinfo);
        Random random = loottableinfo.getRandom();
        LootGenerateEvent event = CraftEventFactory.callLootGenerateEvent(iinventory, (LootTable)(Object)this, loottableinfo, list, plugin);
        if (event.isCancelled()) return;
        list = event.getLoot().stream().map(CraftItemStack::asNMSCopy).collect(Collectors.toList());

        List<Integer> list1 = this.getFreeSlots(iinventory, random);

        this.shuffle(list, list1.size(), random);
        Iterator iterator = list.iterator();

        while (iterator.hasNext()) {
            ItemStack itemstack = (ItemStack) iterator.next();

            if (list1.isEmpty())
                return;
            iinventory.setStack((Integer) list1.remove(list1.size() - 1), itemstack.isEmpty() ? ItemStack.EMPTY : itemstack);
        }

    }

    @Shadow
    public void shuffle(List<ItemStack> list, int i, Random random) {
    }

    @Shadow
    public List<Integer> getFreeSlots(Inventory iinventory, Random random) {
        return null;
    }

    @Shadow
    public List<ItemStack> generateLoot(LootContext loottableinfo) {
        return null;
    }*/

}