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
package org.cardboardpowered.compat;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

public class ModCompatibilityRule {

    public enum Status {
        COMPATIBLE,
        CONFLICT_RESOLVED,
        NEEDS_INVESTIGATION
    }

    private final String modId;
    private final String modName;
    private final Set<String> disabledMixins;
    private final Map<String, Integer> priorityOverrides;
    private final String notes;
    private final Status status;

    public ModCompatibilityRule(String modId, String modName, Set<String> disabledMixins,
                                 Map<String, Integer> priorityOverrides, String notes, Status status) {
        this.modId = modId;
        this.modName = modName;
        this.disabledMixins = disabledMixins != null ? Collections.unmodifiableSet(disabledMixins) : Collections.emptySet();
        this.priorityOverrides = priorityOverrides != null ? Collections.unmodifiableMap(priorityOverrides) : Collections.emptyMap();
        this.notes = notes != null ? notes : "";
        this.status = status != null ? status : Status.NEEDS_INVESTIGATION;
    }

    public String getModId() {
        return modId;
    }

    public String getModName() {
        return modName;
    }

    public Set<String> getDisabledMixins() {
        return disabledMixins;
    }

    public Map<String, Integer> getPriorityOverrides() {
        return priorityOverrides;
    }

    public String getNotes() {
        return notes;
    }

    public Status getStatus() {
        return status;
    }

    @Override
    public String toString() {
        return "ModCompatibilityRule{modId='" + modId + "', name='" + modName + "', status=" + status + "}";
    }
}
