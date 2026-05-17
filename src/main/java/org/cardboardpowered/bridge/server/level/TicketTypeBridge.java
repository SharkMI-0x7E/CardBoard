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