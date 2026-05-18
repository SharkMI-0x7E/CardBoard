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
package org.cardboardpowered.mixin.core;

import java.util.Optional;
import net.minecraft.core.HolderLookup.RegistryLookup;
import net.minecraft.resources.ResourceKey;
import org.cardboardpowered.bridge.core.RegistryLookupBridge;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(RegistryLookup.Delegate.class)
public interface RegistryLookup_DelegateMixin<T> extends RegistryLookupBridge<T> {

	@Override
    default public Optional<T> getValueForCopying(ResourceKey<T> resourceKey) {
        return ( (RegistryLookupBridge) ( (RegistryLookup.Delegate) (Object) this ).parent() ).getValueForCopying(resourceKey);
    }
	
}
