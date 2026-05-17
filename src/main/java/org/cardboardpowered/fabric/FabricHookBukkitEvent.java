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
package org.cardboardpowered.fabric;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;

/**
 * @author wdog5
 * Made mod can hook Bukkit Event
 */
public interface FabricHookBukkitEvent {

    Event<FabricHookBukkitEvent> EVENT = EventFactory.createArrayBacked(FabricHookBukkitEvent.class,
            (listeners) -> (bukkitEvent) -> {
                for (FabricHookBukkitEvent listener : listeners) {
                    listener.hook(bukkitEvent);
                }
            });

    void hook(org.bukkit.event.Event bukkitEvent);
}
