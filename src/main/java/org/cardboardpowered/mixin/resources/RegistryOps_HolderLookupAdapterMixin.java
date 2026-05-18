/**
 * Cardboard - Spigot/Paper for Fabric
 * Copyright (C) 2020-2026 CardboardPowered.org and contributors
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
package org.cardboardpowered.mixin.resources;

import net.minecraft.core.HolderLookup;
import net.minecraft.resources.RegistryOps;
import org.cardboardpowered.bridge.resources.RegistryInfoLookupBridge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(RegistryOps.HolderLookupAdapter.class)
public class RegistryOps_HolderLookupAdapterMixin implements RegistryInfoLookupBridge {

	@Shadow
	private HolderLookup.Provider lookupProvider;
	
	@Override
    public HolderLookup.Provider lookupForValueCopyViaBuilders() {
        return this.lookupProvider;
    }
	
}
