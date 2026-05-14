package org.cardboardpowered.mixin.world.entity.npc.villager;

import net.minecraft.world.entity.npc.villager.AbstractVillager;
import org.bukkit.craftbukkit.inventory.CraftMerchant;
import org.spongepowered.asm.mixin.Mixin;
import org.bukkit.craftbukkit.entity.CraftAbstractVillager;
import org.cardboardpowered.bridge.world.entity.npc.villager.AbstractVillagerBridge;

@Mixin(AbstractVillager.class)
public class AbstractVillagerMixin implements AbstractVillagerBridge {

    // private CraftMerchant craftMerchant;

    @Override
    public CraftMerchant getCraftMerchant() {
    	
    	return (CraftAbstractVillager) ((AbstractVillager)(Object)this).getBukkitEntity();
    	
        // return (craftMerchant == null) ? craftMerchant = new CraftMerchant((MerchantEntity)(Object) this) : craftMerchant;
    }

}
