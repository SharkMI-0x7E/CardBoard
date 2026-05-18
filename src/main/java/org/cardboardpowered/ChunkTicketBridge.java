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
package org.cardboardpowered;

import net.minecraft.server.level.TicketType;

/**
 * Paper's added ChunkTicketType values
 */
public class ChunkTicketBridge {

	// private static final int PAPER_FLAG = 6;
	
	// Paper - start
    /*
	public static final ChunkTicketType POST_TELEPORT = ChunkTicketType.register("post_teleport", 5L, false, PAPER_FLAG);
    public static final ChunkTicketType PLUGIN_TICKET = ChunkTicketType.register("plugin_ticket", 0L, false, PAPER_FLAG);
    public static final ChunkTicketType FUTURE_AWAIT = ChunkTicketType.register("future_await", 0L, false, PAPER_FLAG;
    public static final ChunkTicketType CHUNK_LOAD = ChunkTicketType.register("chunk_load", 0L, false, Use.LOADING);
    */
    
    public static final TicketType POST_TELEPORT = TicketType.register("post_teleport", 5L, 6);
    public static final TicketType PLUGIN_TICKET = TicketType.register("plugin_ticket", 0L, 6);
    public static final TicketType FUTURE_AWAIT = TicketType.register("future_await", 0L, 6);
    public static final TicketType CHUNK_LOAD = TicketType.register("chunk_load", 0L, 2);
    
    // Paper - end
    
    // public static final ChunkTicketType PLUGIN = ChunkTicketType.register("plugin", 0L, 6);

}