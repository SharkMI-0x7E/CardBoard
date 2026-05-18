/**
 * Cardboard - Spigot/Paper for Fabric
 * Copyright (C) 2020-2026 CardboardPowered.org and contributors
 * Copyright (C) 2026 SharkMI and contributors
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
	 *
	 * TODO: Cannot replace with @ModifyReturnValue - this adds modded material
	 * support via isModded() check, which fundamentally extends the original logic
	 * rather than simply post-processing the return value.
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
	 *
	 * TODO: Cannot replace with @ModifyReturnValue - this @Overwrite removes the
	 * Preconditions.checkArgument(legacy, ...) guard from the original, allowing
	 * getId() to be called on modern materials.
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
