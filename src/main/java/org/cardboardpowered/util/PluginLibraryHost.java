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
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.cardboardpowered.util;

import java.net.URL;

/**
 * Legal bridge entry point for runtime plugin library loaders (the Libby family,
 * in any relocated package). Such loaders historically append downloaded
 * dependency jars to the plugin classloader by reflectively invoking
 * {@code URLClassLoader#addURL}. On Java 9+ that reflection requires
 * {@code --add-opens java.base/java.net=ALL-UNNAMED}, and their fallback ladder
 * (Unsafe IMPL_LOOKUP theft, dynamic byte-buddy self-attach) fails on locked-down
 * and Android/Termux runtimes.
 *
 * <p>{@code PluginClassLoader} already extends {@code URLClassLoader}, so appending
 * a URL needs no escalation at all when the loader exposes a public method.
 * {@link LibbyClasspathFixer} rewrites family-matched helper classes to call this
 * interface instead of performing any reflective module opening.
 */
public interface PluginLibraryHost {

    /**
     * Appends a jar URL to this plugin classloader's classpath.
     *
     * @param url the jar URL to add; must not be null
     */
    void cardboard$addPluginLibrary(URL url);
}
