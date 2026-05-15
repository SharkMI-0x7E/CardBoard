package org.cardboardpowered.conflict.model;

public class MixinConflict {

    public ConflictLevel level;
    public String conflictType;
    public String targetClass;
    public String targetMethod;
    public String cardboardMixinClass;
    public MixinMethod cardboardMethod;
    public String otherModId;
    public String otherMixinClass;
    public MixinMethod otherMethod;
    public String suggestion;
    public boolean isResolved;
    public String resolutionNote;

    public static MixinConflict ofFatal(MixinMethod cardboardMethod, MixinMethod otherMethod, String suggestion) {
        MixinConflict conflict = new MixinConflict();
        conflict.level = ConflictLevel.FATAL;
        conflict.conflictType = "OVERWRITE_OVERWRITE";
        conflict.cardboardMethod = cardboardMethod;
        conflict.otherMethod = otherMethod;
        conflict.suggestion = suggestion;
        return conflict;
    }

    public static MixinConflict ofHigh(MixinMethod cardboardMethod, MixinMethod otherMethod, String suggestion) {
        MixinConflict conflict = new MixinConflict();
        conflict.level = ConflictLevel.HIGH;
        conflict.cardboardMethod = cardboardMethod;
        conflict.otherMethod = otherMethod;
        conflict.suggestion = suggestion;
        return conflict;
    }

    public static MixinConflict ofMedium(MixinMethod cardboardMethod, MixinMethod otherMethod, String suggestion) {
        MixinConflict conflict = new MixinConflict();
        conflict.level = ConflictLevel.MEDIUM;
        conflict.cardboardMethod = cardboardMethod;
        conflict.otherMethod = otherMethod;
        conflict.suggestion = suggestion;
        return conflict;
    }

    public static MixinConflict ofLow(MixinMethod cardboardMethod, MixinMethod otherMethod, String suggestion) {
        MixinConflict conflict = new MixinConflict();
        conflict.level = ConflictLevel.LOW;
        conflict.cardboardMethod = cardboardMethod;
        conflict.otherMethod = otherMethod;
        conflict.suggestion = suggestion;
        return conflict;
    }

    public boolean isFatal() {
        return level == ConflictLevel.FATAL;
    }

    public String getConflictDescription() {
        return level.name() + ": " + conflictType + " on " + targetClass + "." + targetMethod;
    }

    public String getShortId() {
        return level.name() + "_" + targetClass + "_" + targetMethod + "_" + cardboardMixinClass + "_vs_" + otherModId;
    }

    @Override
    public String toString() {
        return "MixinConflict{" +
                "level=" + level +
                ", conflictType='" + conflictType + '\'' +
                ", targetClass='" + targetClass + '\'' +
                ", targetMethod='" + targetMethod + '\'' +
                ", cardboardMixin='" + cardboardMixinClass + '\'' +
                ", otherMod='" + otherModId + '\'' +
                ", otherMixin='" + otherMixinClass + '\'' +
                ", suggestion='" + suggestion + '\'' +
                ", resolved=" + isResolved +
                '}';
    }
}
