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
package org.cardboardpowered.impl.command;

import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.SimpleCommandMap;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class CommandMapImpl extends SimpleCommandMap {
	private static final Class<?> CARDBOARD_VERSION_COMMAND = VersionCommand.class;

	public static HashMap<String, Command> DUMMY_MAP = new HashMap<String, Command>();

	public CommandMapImpl(Server server) {
		super(server, DUMMY_MAP);

		registerCardboardCommands();
	}

	@Override
	public boolean register(@NotNull String label, @NotNull String fallbackPrefix, @NotNull Command command) {
		if (label.equals("version") && fallbackPrefix.equals("bukkit")
				&& !CARDBOARD_VERSION_COMMAND.isInstance(command)) {
			// Let Cardboard version command take priority
			return false;
		}
		return super.register(label, fallbackPrefix, command);
	}

	@Override
	public synchronized void clearCommands() {
		super.clearCommands();
		registerCardboardCommands();
	}

	@Override
	public Map<String, Command> getKnownCommands() {
		return knownCommands;
	}

	private void registerCardboardCommands() {
		// Standard commands kept standalone for compatibility.
		register("bukkit", new VersionCommand("version"));
		register("bukkit", new CardboardPluginsCommand("plugins"));

		// Everything else hangs off the single /cardboard entry point.
		register("cardboard", new CardboardCommand());
	}

}
