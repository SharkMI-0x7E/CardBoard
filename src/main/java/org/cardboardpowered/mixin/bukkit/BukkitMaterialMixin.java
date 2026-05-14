package org.cardboardpowered.mixin.bukkit;

import org.bukkit.Material;
import org.bukkit.inventory.ItemType;
import org.cardboardpowered.impl.CardboardModdedMaterial;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import org.cardboardpowered.bridge.bukkit.BukkitMaterialBridge;

@Mixin(value = Material.class, remap = false)
public class BukkitMaterialMixin implements BukkitMaterialBridge {

	
	
	
	/**
	 * @reason We need API update
	 * @see https://hub.spigotmc.org/stash/projects/SPIGOT/repos/bukkit/diff/src/main/java/org/bukkit/Material.java?until=ad2fd61c8784c7bac6542e39fca7e506c7966865
	 */
	@Inject(at = @At("HEAD"), method = "isBlock", cancellable = true, remap = false)
	public void fix_material_block(CallbackInfoReturnable<Boolean> ci) {
		if ( ((Material)(Object)this).name().equalsIgnoreCase("GRASS") ) {
			ci.setReturnValue(false);
		}
	}
	
	
    //public static final String LEGACY_PREFIX = "LEGACY_";
    
	@Shadow
	private int id;
    //private final Constructor<? extends MaterialData> ctor;
    //private static final Map<String, Material> BY_NAME;
    //private final int maxStack;
    
	// @Shadow
	// private short durability;
    //public final Class<?> data;
    //private final boolean legacy;
    //private final NamespacedKey key;
    //private boolean isBlock;
	
	@Shadow
	public ItemType asItemType() {
        return null; // Shadowed
    }
	
	private org.cardboardpowered.impl.CardboardModdedMaterial moddedData;

	@Override
	public boolean isModded() {
		return null != moddedData;
	}

	@Override
	public CardboardModdedMaterial getModdedData() {
		return moddedData;
	}

	@Override
	public void setModdedData(CardboardModdedMaterial data) {
		this.moddedData = data;
	}

	/*private Material(final int id, org.cardboardpowered.impl.CardboardModdedMaterial data) {
		this(id, 64);
		setModdedData(data);
	}*/
	
	/**
	 * @author Cardboard
	 * @reason Support Modded Materials
	 */
	@Overwrite
    public short getMaxDurability() {
		if (isModded()) return moddedData.getDamage(); // CARDBOARD
        // return this.durability;
        
        ItemType type = asItemType();
        return type == null ? 0 : type.getMaxDurability();
    }
	
	/**
	 * @author Cardboard
	 * @reason Support Modded Materials
	 */
    @Overwrite
    public int getId() {
    	// CARDBOARD REMOVED: Preconditions.checkArgument(this.legacy, "Cannot get ID of Modern Material");
        return this.id;
    }
	
    /*
	@Inject(at = @At("HEAD"), method = "isBlock0", cancellable = true, remap = false)
	public void mod_is_block(CallbackInfoReturnable<Boolean> ci) {
		if (isModded()) {
			ci.setReturnValue(moddedData.isBlock());
		}
	}
	*/
	
	@Inject(at = @At("HEAD"), method = "isItem", cancellable = true, remap = false)
	public void mod_is_item(CallbackInfoReturnable<Boolean> ci) {
		if (isModded()) {
			ci.setReturnValue(moddedData.isItem());
		}
	}

}
