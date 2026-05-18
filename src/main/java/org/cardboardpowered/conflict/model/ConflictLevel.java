/**
 * Cardboard - Spigot/Paper for Fabric
 * Copyright (C) 2020-2026 CardboardPowered.org and contributors*
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
package org.cardboardpowered.conflict.model;

public enum ConflictLevel {

    FATAL("Fatal conflict: Multiple mods use @Overwrite on the same method, server will crash", 4),
    HIGH("High conflict: @Overwrite coexists with @Inject/@Redirect, injection may be ineffective", 3),
    MEDIUM("Medium conflict: Multiple @Redirect/ModifyArg compete, behavior is unpredictable", 2),
    LOW("Low conflict: Multiple @Inject coexist, usually compatible but order may affect behavior", 1);

    private final String description;
    private final int severity;

    ConflictLevel(String description, int severity) {
        this.description = description;
        this.severity = severity;
    }

    public String getDescription() {
        return description;
    }

    public int getSeverity() {
        return severity;
    }
}
