/**
 * Cardboard - Spigot/Paper for Fabric
 * Copyright (C) 2020-2024 CardboardPowered.org and contributors
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
package org.cardboardpowered.util;

import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;

public class PermissionHook {
    public static boolean hasPermission(Entity e, String permission) {
    	if (Permissions.check(e, permission)) {
    	    // Woo!
    		return true;
    	}

        return false;
    }
}