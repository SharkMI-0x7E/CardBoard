package org.cardboardpowered.bridge.server.level;

import net.minecraft.server.level.TicketType;
import org.cardboardpowered.mixin.server.level.TicketTypeMixin;

/**
 * Injection Interface for ChunkTicketType.
 * 
 * @see {@link TicketTypeMixin}
 */
public interface TicketTypeBridge {

	/**
	 */
    TicketType getBukkitPluginTicketType();

}