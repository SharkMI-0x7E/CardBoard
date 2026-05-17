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
package org.cardboardpowered;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CardboardLogger {

	private static Logger LOGGER = LoggerFactory.getLogger("Cardboard");

	public static CardboardLogger get(String prefix) {
		return new CardboardLogger(prefix);
	}

	public static Logger getSLF4J() {
		return LOGGER;
	}

	private String prefix;
	
	public CardboardLogger() {
		this.prefix = "";
	}
	
	public CardboardLogger(String prefix) {
		this.prefix = prefix + " ";
	}

	public void info(String message) {
		LOGGER.info(prefix + message);
    }
	
	public void error(String message) {
		LOGGER.error(prefix + message);
    }

	public void debug(String message) {
		if (CardboardConfig.DEBUG_OTHER || CardboardConfig.DEBUG_VERBOSE_CALLS) {
			LOGGER.info(prefix + " (DEBUG): " + message);
		}
    }

}
