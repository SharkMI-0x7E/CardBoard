package org.cardboardpowered.conflict.report;

import org.cardboardpowered.conflict.model.MixinConflict;
import org.cardboardpowered.conflict.model.MixinMethod;

/**
 * DTO for JSON serialization of conflict reports.
 * Avoids serializing MixinMethod objects directly which would produce nested JSON.
 */
public class ConflictReportEntry {

    public String targetClass;
    public String targetMethod;
    public String conflictType;
    public String level;
    public String cardboardMixin;
    public String cardboardAnnotation;
    public int cardboardPriority;
    public String otherModId;
    public String otherMixin;
    public String otherAnnotation;
    public int otherPriority;
    public String suggestion;
    public boolean isResolved;
    public String resolutionNote;

    public static ConflictReportEntry fromConflict(MixinConflict conflict) {
        ConflictReportEntry entry = new ConflictReportEntry();
        entry.targetClass = conflict.targetClass;
        entry.targetMethod = conflict.targetMethod;
        entry.conflictType = conflict.conflictType;
        entry.level = conflict.level != null ? conflict.level.name() : "UNKNOWN";
        entry.cardboardMixin = conflict.cardboardMixinClass;
        entry.otherModId = conflict.otherModId;
        entry.otherMixin = conflict.otherMixinClass;
        entry.suggestion = conflict.suggestion;
        entry.isResolved = conflict.isResolved;
        entry.resolutionNote = conflict.resolutionNote;

        MixinMethod cbMethod = conflict.cardboardMethod;
        if (cbMethod != null) {
            entry.cardboardAnnotation = cbMethod.annotationType;
            entry.cardboardPriority = cbMethod.priority;
        }

        MixinMethod otherMethod = conflict.otherMethod;
        if (otherMethod != null) {
            entry.otherAnnotation = otherMethod.annotationType;
            entry.otherPriority = otherMethod.priority;
        }

        return entry;
    }
}
