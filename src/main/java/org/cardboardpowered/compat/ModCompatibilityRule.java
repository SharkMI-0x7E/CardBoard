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
