/**
 * Cardboard - Spigot/Paper for Fabric
 * Copyright (C) 2020-2024 CardboardPowered.org and contributors
 * Copyright (C) 2025-2026 SharkMI and contributors
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
package org.cardboardpowered.util.nms;

import java.io.*;
import java.util.*;

import org.cardboardpowered.CardboardConfig;

import com.google.common.collect.BiMap;

import net.md_5.specialsource.InheritanceMap;
import net.md_5.specialsource.JarRemapper;

public class CbInheritanceMap extends InheritanceMap {

	public void load(BufferedReader reader, BiMap<String, String> classMap) throws IOException {
        String line;

        while ((line = reader.readLine()) != null) {
            int commentIndex = line.indexOf('#');
            if (commentIndex != -1) {
                line = line.substring(0, commentIndex);
            }
            if (line.isEmpty()) {
                continue;
            }
            String[] tokens = line.split(" ");

            if (tokens.length < 2) {
                throw new IOException("Invalid inheritance map file line: " + line);
            }

            String className = tokens[0];
            List<String> parents = Arrays.asList(tokens).subList(1, tokens.length);

            if (classMap == null) {
                setParents(className, new ArrayList<String>(parents));
            } else {
                String remappedClassName = JarRemapper.mapTypeName(className, /*packageMap*/ null, classMap, /*defaultIfUnmapped*/ null);
                if (remappedClassName == null) {
                    // throw new IOException("Inheritance map input class not remapped: " + className);
                	if (CardboardConfig.DEBUG_VERBOSE_CALLS)
                    System.out.println("Inheritance map input class not remapped: " + className);
                } else {

	                ArrayList<String> remappedParents = new ArrayList<String>();
	                for (String parent : parents) {
	                    String remappedParent = JarRemapper.mapTypeName(parent, /*packageMap*/ null, classMap, /*defaultIfUnmapped*/ null);
	                    if (remappedParent == null) {
	                        // throw new IOException("Inheritance map parent class not remapped: " + parent);
	                    	if (CardboardConfig.DEBUG_VERBOSE_CALLS)
	                    	System.out.println("Inheritance map parent class not remapped: " + parent);
	                    } else {
	
	                    	remappedParents.add(remappedParent);
	                    }
	                }
	
	                setParents(remappedClassName, remappedParents);
                }
            }
        }
    }
	
}
