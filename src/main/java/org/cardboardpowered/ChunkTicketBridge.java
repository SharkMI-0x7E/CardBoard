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