package org.cardboardpowered.mixin.bukkit;

import org.bukkit.Keyed;
import org.bukkit.Registry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import io.papermc.paper.registry.RegistryAccess;

import java.util.Objects;

/**
 * Test
 */
@Mixin(value = Registry.class, remap = false)
public interface BukkitRegistryMixin {
	
	@Overwrite(remap = false)
    private static <A extends Keyed> Registry<A> legacyRegistryFor(final Class<A> clazz) {
        return Objects.requireNonNull(RegistryAccess.registryAccess().getRegistry(clazz),
        		() -> "No registry present for class name: " + clazz.getName() + ". This is a bug.");
    }
	
}

/*
@Mixin(value = net.minecraft.server.dedicated.MinecraftDedicatedServer.class, priority = 900)
@Deprecated
public class MixinArmorItem {
	
}
*/

/*
@MixinInfo(events = {"BlockDispenseArmorEvent"})
@Mixin(value = ArmorItem.class, priority = 900)
public class MixinArmorItem {

	// TODO 1.21.4
	
    /**
     * @reason .
     * @author .
     */
	/*
    @Overwrite
    public static boolean dispenseArmor(BlockPointer isourceblock, ItemStack itemstack) {
        BlockPos blockposition = isourceblock.pos().offset((Direction) isourceblock.state().get(DispenserBlock.FACING));
        List<LivingEntity> list = isourceblock.world().getEntitiesByClass(LivingEntity.class, new Box(blockposition), EntityPredicates.EXCEPT_SPECTATOR.and(new EntityPredicates.Equipable(itemstack)));

        if (list.isEmpty()) {
            return false;
        } else {
            LivingEntity entityliving = (LivingEntity) list.get(0);
            EquipmentSlot enumitemslot = entityliving.getPreferredEquipmentSlot(itemstack);
            ItemStack itemstack1 = itemstack.split(1);

            World world = isourceblock.world();
            org.bukkit.block.Block block = ((IMixinWorld)world).getCraftWorld().getBlockAt(isourceblock.pos().getX(), isourceblock.pos().getY(), isourceblock.pos().getZ());
            CraftItemStack craftItem = CraftItemStack.asCraftMirror(itemstack1);

            BlockDispenseArmorEvent event = new BlockDispenseArmorEvent(block, craftItem.clone(), (LivingEntityImpl) ((IMixinEntity)entityliving).getBukkitEntity());
            if (!DispenserBlockHelper.eventFired)
                Bukkit.getPluginManager().callEvent(event);

            if (event.isCancelled()) {
                itemstack.increment(1);
                return false;
            }

            if (!event.getItem().equals(craftItem)) {
                itemstack.increment(1);
                // Chain to handler for new item
                ItemStack eventStack = CraftItemStack.asNMSCopy(event.getItem());
                DispenserBehavior idispensebehavior = (DispenserBehavior) DispenserBlock.BEHAVIORS.get(eventStack.getItem());
                if (idispensebehavior != DispenserBehavior.NOOP && idispensebehavior != ArmorItem.DISPENSER_BEHAVIOR) {
                    idispensebehavior.dispense(isourceblock, eventStack);
                    return true;
                }
            }

            entityliving.equipStack(enumitemslot, itemstack1);
            if (entityliving instanceof MobEntity) {
                ((MobEntity) entityliving).setEquipmentDropChance(enumitemslot, 2.0F);
                ((MobEntity) entityliving).setPersistent();
            }

            return true;
        }
    }
    

}*/
