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
        entry.targetClass = conflict.getTargetClass();
        entry.targetMethod = conflict.getTargetMethod();
        entry.conflictType = conflict.getConflictType();
        entry.level = conflict.getLevel() != null ? conflict.getLevel().name() : "UNKNOWN";
        entry.cardboardMixin = conflict.getCardboardMixinClass();
        entry.otherModId = conflict.getOtherModId();
        entry.otherMixin = conflict.getOtherMixinClass();
        entry.suggestion = conflict.getSuggestion();
        entry.isResolved = conflict.isResolved();
        entry.resolutionNote = conflict.getResolutionNote();

        MixinMethod cbMethod = conflict.getCardboardMethod();
        if (cbMethod != null) {
            entry.cardboardAnnotation = cbMethod.getAnnotationType();
            entry.cardboardPriority = cbMethod.getPriority();
        }

        MixinMethod otherMethod = conflict.getOtherMethod();
        if (otherMethod != null) {
            entry.otherAnnotation = otherMethod.getAnnotationType();
            entry.otherPriority = otherMethod.getPriority();
        }

        return entry;
    }
}
