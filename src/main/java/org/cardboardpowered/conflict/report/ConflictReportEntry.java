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
