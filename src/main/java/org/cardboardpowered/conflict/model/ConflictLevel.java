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
