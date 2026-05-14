/**
 * Cardboard Mod - Copyright (c) 2020-2025
 */
package org.cardboardpowered.mixin.server.level;

import net.minecraft.server.level.TicketType;
import org.cardboardpowered.ChunkTicketBridge;
import org.cardboardpowered.bridge.server.level.TicketTypeBridge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

/**
 * Mixin for ChunkTicketType
 * 
 * @author Cardboard Mod
 * @implNote ChunkTicketType (Yarn)
 * @implNote TicketType (Paper/Moj)
 * @see {@link https://github.com/PaperMC/Paper/blob/main/paper-server/patches/sources/net/minecraft/server/level/TicketType.java.patch}
 */
@Mixin(TicketType.class)
public class TicketTypeMixin implements TicketTypeBridge {

    // Bukkit
	private static final TicketType PLUGIN = TicketType.register("plugin", 0L, 6);
			// old 1.21.8: register("plugin", 0L, false, Use.LOADING_AND_SIMULATION);

	// Paper - start
    private static final TicketType POST_TELEPORT = ChunkTicketBridge.POST_TELEPORT;
    private static final TicketType PLUGIN_TICKET = ChunkTicketBridge.PLUGIN_TICKET;
    private static final TicketType FUTURE_AWAIT = ChunkTicketBridge.FUTURE_AWAIT;
    private static final TicketType CHUNK_LOAD = ChunkTicketBridge.CHUNK_LOAD;
    // Paper - end

    @Override
    public TicketType getBukkitPluginTicketType() {
        return PLUGIN;
    }
    
    @Shadow
    public static TicketType register(String id, long expiryTicks, int flags) {
    	return null; // Shadowed
    }
    
    /*
    @Shadow
    public static ChunkTicketType register(String id, long expiryTicks, boolean persist, Use use) {
    	return null; // Shadowed
    }
    */

}