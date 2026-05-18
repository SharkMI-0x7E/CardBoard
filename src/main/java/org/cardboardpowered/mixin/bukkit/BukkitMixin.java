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

import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.CraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.metadata.ModMetadata;

@Mixin(value = Bukkit.class, remap = false)
public class BukkitMixin {

	/**
	 * Use Fabric's ModMetadata for version
	 * info instead of grabbing from META-INF
	 * 
	 * @author cardboard
	 * @reason META-INF
	 *
	 * TODO: Cannot replace with @ModifyReturnValue - this method completely replaces
	 * the original Bukkit logic to use Fabric ModMetadata instead of META-INF.
	 */
	@Overwrite(remap = false)
    public static String getVersionMessage() {
		ModMetadata metadata = FabricLoader.getInstance().getModContainer("cardboard").get().getMetadata();
		
		String ver = metadata.getVersion().getFriendlyString();
        if (ver.contains("version")) ver = CraftServer.INSTANCE.getShortVersion(); // Dev ENV
		
		return "This server is running " + Bukkit.getName() + " version " + ver + " (Implementing API version " + Bukkit.getBukkitVersion() + ")";
    }
	
}
