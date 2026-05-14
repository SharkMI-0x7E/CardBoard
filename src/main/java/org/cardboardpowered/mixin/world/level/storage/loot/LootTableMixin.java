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