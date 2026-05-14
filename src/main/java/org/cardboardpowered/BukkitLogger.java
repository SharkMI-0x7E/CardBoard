/**
 * CardboardPowered - Bukkit/Spigot for Fabric
 * Copyright (C) CardboardPowered.org and contributors
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation; either 
 * version 3 of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.cardboardpowered;

import java.util.logging.LogRecord;
import java.util.logging.Logger;

import org.apache.logging.log4j.Level;
import org.slf4j.LoggerFactory;

public class BukkitLogger extends Logger {

	public static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger("Bukkit");
	
    public org.slf4j.Logger log4j;
    private static BukkitLogger inst;
    private boolean doPrefix;

    public static BukkitLogger getLogger() {
        return (inst == null) ? (inst = new BukkitLogger()) : inst;
    }
    
    public static java.util.logging.Logger getLogger(String name) {
    	return new BukkitLogger(name, null);
    }
    
    public static java.util.logging.Logger getLogger(String name, String res) {
        return new BukkitLogger(name, res);
    }

    public static BukkitLogger getPluginLogger(String pluginName) {
        return new BukkitLogger(pluginName, null);
    }

    public BukkitLogger() {
        this("Bukkit", null);
        this.doPrefix = false;
    }

    public BukkitLogger(String name, String str) {
        super(name, str);
        this.log4j = LoggerFactory.getLogger(name);
        this.doPrefix = CardboardConfig.shouldAddPrefixToLoggers();
        if (inst == null) {
            inst = this;
        }
    }
    
    @Override
    public void setParent(final Logger parent) {
        if (this.getParent() != null) {
            this.warning("Ignoring attempt to change parent of plugin logger");
        } else {
            this.info("Setting plugin logger parent to {0}" + parent.getName());
            super.setParent(parent);
        }
    }
    
    @Override
    public void log(LogRecord lr) {
    	org.slf4j.event.Level level = convertLevel1(lr.getLevel());
    	
        if (lr.getThrown() == null) {
        	if (this.doPrefix) {
        		log4j.atLevel(level).log("[" + this.getName() + "] " + lr.getMessage());
        	} else {
        		log4j.atLevel(level).log(lr.getMessage());
        	}
            // log4j.log(level, lr.getMessage());
        } else {
        	log4j.atLevel(level).log(lr.getMessage(), lr.getThrown());
        	// log4j.error(lr.getMessage(), lr.getThrown());;
        	// log4j.log(level, lr.getMessage(), lr.getThrown());
        }
    }
    
    private Level convertLevel(java.util.logging.Level l, LogRecord lr) {
    	String m = lr.getMessage();

        if (l == java.util.logging.Level.ALL)     return Level.ALL;
        if (l == java.util.logging.Level.CONFIG)  return Level.TRACE;
        if (l == java.util.logging.Level.WARNING) return Level.WARN;
        if (l == java.util.logging.Level.INFO)    return Level.INFO;
        if (l == java.util.logging.Level.OFF)     return Level.OFF;
        if (l == java.util.logging.Level.SEVERE)  return Level.FATAL;

        if (l == java.util.logging.Level.FINE || l == java.util.logging.Level.FINER || l == java.util.logging.Level.FINEST)
            return Level.WARN;
        return Level.ALL;
    }
    
    private org.slf4j.event.Level convertLevel1(java.util.logging.Level l) {
        if (l == java.util.logging.Level.ALL)     return org.slf4j.event.Level.INFO;
        if (l == java.util.logging.Level.CONFIG)  return org.slf4j.event.Level.TRACE;
        if (l == java.util.logging.Level.WARNING) return org.slf4j.event.Level.WARN;
        if (l == java.util.logging.Level.INFO)    return org.slf4j.event.Level.INFO;
        if (l == java.util.logging.Level.OFF)     return org.slf4j.event.Level.TRACE;
        if (l == java.util.logging.Level.SEVERE)  return org.slf4j.event.Level.ERROR;

        if (l == java.util.logging.Level.FINE || l == java.util.logging.Level.FINER || l == java.util.logging.Level.FINEST)
        	return org.slf4j.event.Level.WARN;
        return org.slf4j.event.Level.INFO;
    }

    private Level convertLevel(java.util.logging.Level l) {
        if (l == java.util.logging.Level.ALL)     return Level.ALL;
        if (l == java.util.logging.Level.CONFIG)  return Level.TRACE;
        if (l == java.util.logging.Level.WARNING) return Level.WARN;
        if (l == java.util.logging.Level.INFO)    return Level.INFO;
        if (l == java.util.logging.Level.OFF)     return Level.OFF;
        if (l == java.util.logging.Level.SEVERE)  return Level.FATAL;

        if (l == java.util.logging.Level.FINE || l == java.util.logging.Level.FINER || l == java.util.logging.Level.FINEST)
            return Level.WARN;
        return Level.ALL;
    }

}