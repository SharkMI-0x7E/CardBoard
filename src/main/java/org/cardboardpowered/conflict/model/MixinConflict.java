package org.cardboardpowered.conflict.model;

public class MixinConflict {

    private ConflictLevel level;
    private String conflictType;
    private String targetClass;
    private String targetMethod;
    private String cardboardMixinClass;
    private MixinMethod cardboardMethod;
    private String cardboardModId;
    private String otherModId;
    private String otherMixinClass;
    private MixinMethod otherMethod;
    private String suggestion;
    private boolean isResolved;
    private String resolutionNote;

    public static MixinConflict ofFatal(MixinMethod cardboardMethod, MixinMethod otherMethod, String targetClass, String targetMethod, String cardboardMixinClass, String otherMixinClass, String cardboardModId, String otherModId, String suggestion) {
        MixinConflict conflict = new MixinConflict();
        conflict.level = ConflictLevel.FATAL;
        conflict.conflictType = "OVERWRITE_OVERWRITE";
        conflict.cardboardMethod = cardboardMethod;
        conflict.otherMethod = otherMethod;
        conflict.targetClass = targetClass;
        conflict.targetMethod = targetMethod;
        conflict.cardboardMixinClass = cardboardMixinClass;
        conflict.otherMixinClass = otherMixinClass;
        conflict.cardboardModId = cardboardModId;
        conflict.otherModId = otherModId;
        conflict.suggestion = suggestion;
        return conflict;
    }

    public static MixinConflict ofHigh(MixinMethod cardboardMethod, MixinMethod otherMethod, String targetClass, String targetMethod, String cardboardMixinClass, String otherMixinClass, String cardboardModId, String otherModId, String suggestion) {
        MixinConflict conflict = new MixinConflict();
        conflict.level = ConflictLevel.HIGH;
        conflict.cardboardMethod = cardboardMethod;
        conflict.otherMethod = otherMethod;
        conflict.targetClass = targetClass;
        conflict.targetMethod = targetMethod;
        conflict.cardboardMixinClass = cardboardMixinClass;
        conflict.otherMixinClass = otherMixinClass;
        conflict.cardboardModId = cardboardModId;
        conflict.otherModId = otherModId;
        conflict.suggestion = suggestion;
        return conflict;
    }

    public static MixinConflict ofMedium(MixinMethod cardboardMethod, MixinMethod otherMethod, String targetClass, String targetMethod, String cardboardMixinClass, String otherMixinClass, String cardboardModId, String otherModId, String suggestion) {
        MixinConflict conflict = new MixinConflict();
        conflict.level = ConflictLevel.MEDIUM;
        conflict.cardboardMethod = cardboardMethod;
        conflict.otherMethod = otherMethod;
        conflict.targetClass = targetClass;
        conflict.targetMethod = targetMethod;
        conflict.cardboardMixinClass = cardboardMixinClass;
        conflict.otherMixinClass = otherMixinClass;
        conflict.cardboardModId = cardboardModId;
        conflict.otherModId = otherModId;
        conflict.suggestion = suggestion;
        return conflict;
    }

    public static MixinConflict ofLow(MixinMethod cardboardMethod, MixinMethod otherMethod, String targetClass, String targetMethod, String cardboardMixinClass, String otherMixinClass, String cardboardModId, String otherModId, String suggestion) {
        MixinConflict conflict = new MixinConflict();
        conflict.level = ConflictLevel.LOW;
        conflict.cardboardMethod = cardboardMethod;
        conflict.otherMethod = otherMethod;
        conflict.targetClass = targetClass;
        conflict.targetMethod = targetMethod;
        conflict.cardboardMixinClass = cardboardMixinClass;
        conflict.otherMixinClass = otherMixinClass;
        conflict.cardboardModId = cardboardModId;
        conflict.otherModId = otherModId;
        conflict.suggestion = suggestion;
        return conflict;
    }

    public ConflictLevel getLevel() { return level; }
    public void setLevel(ConflictLevel level) { this.level = level; }

    public String getConflictType() { return conflictType; }
    public void setConflictType(String conflictType) { this.conflictType = conflictType; }

    public String getTargetClass() { return targetClass; }
    public void setTargetClass(String targetClass) { this.targetClass = targetClass; }

    public String getTargetMethod() { return targetMethod; }
    public void setTargetMethod(String targetMethod) { this.targetMethod = targetMethod; }

    public String getCardboardMixinClass() { return cardboardMixinClass; }
    public void setCardboardMixinClass(String cardboardMixinClass) { this.cardboardMixinClass = cardboardMixinClass; }

    public MixinMethod getCardboardMethod() { return cardboardMethod; }
    public void setCardboardMethod(MixinMethod cardboardMethod) { this.cardboardMethod = cardboardMethod; }

    public String getCardboardModId() { return cardboardModId; }
    public void setCardboardModId(String cardboardModId) { this.cardboardModId = cardboardModId; }

    public String getOtherModId() { return otherModId; }
    public void setOtherModId(String otherModId) { this.otherModId = otherModId; }

    public String getOtherMixinClass() { return otherMixinClass; }
    public void setOtherMixinClass(String otherMixinClass) { this.otherMixinClass = otherMixinClass; }

    public MixinMethod getOtherMethod() { return otherMethod; }
    public void setOtherMethod(MixinMethod otherMethod) { this.otherMethod = otherMethod; }

    public String getSuggestion() { return suggestion; }
    public void setSuggestion(String suggestion) { this.suggestion = suggestion; }

    public boolean isResolved() { return isResolved; }
    public void setResolved(boolean resolved) { isResolved = resolved; }

    public String getResolutionNote() { return resolutionNote; }
    public void setResolutionNote(String resolutionNote) { this.resolutionNote = resolutionNote; }

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
