/**
 * Copyright (C) 2026 SharkMI and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 */
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