package org.cardboardpowered.mixin.resources;

import net.minecraft.core.HolderLookup;
import net.minecraft.resources.RegistryOps.RegistryInfoLookup;
import org.cardboardpowered.bridge.resources.RegistryInfoLookupBridge;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(RegistryInfoLookup.class)
public interface RegistryInfoLookupMixin extends RegistryInfoLookupBridge {

	/**
	 */
	@Override
	public HolderLookup.Provider lookupForValueCopyViaBuilders();
	
}