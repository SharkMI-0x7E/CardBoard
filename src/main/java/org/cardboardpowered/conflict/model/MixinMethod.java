package org.cardboardpowered.conflict.model;

import java.util.ArrayList;
import java.util.List;

public class MixinMethod {

    private String name;
    private String descriptor;
    private String annotationType;
    private List<String> targetMethods = new ArrayList<>();
    private List<String> atValues = new ArrayList<>();
    private List<String> atTargets = new ArrayList<>();
    private boolean cancellable;
    private int priority = 1000;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescriptor() { return descriptor; }
    public void setDescriptor(String descriptor) { this.descriptor = descriptor; }

    public String getAnnotationType() { return annotationType; }
    public void setAnnotationType(String annotationType) { this.annotationType = annotationType; }

    public List<String> getTargetMethods() { return targetMethods; }
    public void setTargetMethods(List<String> targetMethods) { this.targetMethods = targetMethods; }

    public List<String> getAtValues() { return atValues; }
    public void setAtValues(List<String> atValues) { this.atValues = atValues; }

    public List<String> getAtTargets() { return atTargets; }
    public void setAtTargets(List<String> atTargets) { this.atTargets = atTargets; }

    public boolean isCancellable() { return cancellable; }
    public void setCancellable(boolean cancellable) { this.cancellable = cancellable; }

    public int getPriority() { return priority; }
    public void setPriority(int priority) { this.priority = priority; }

    public boolean isOverwrite() {
        return "Overwrite".equals(annotationType);
    }

    public boolean isInject() {
        return "Inject".equals(annotationType);
    }

    public boolean isRedirect() {
        return "Redirect".equals(annotationType);
    }

    public String getAtTargetKey() {
        if (atTargets == null || atTargets.isEmpty()) {
            return "";
        }
        return atTargets.get(0);
    }

    @Override
    public String toString() {
        return "MixinMethod{" +
                "name='" + name + '\'' +
                ", descriptor='" + descriptor + '\'' +
                ", annotationType='" + annotationType + '\'' +
                ", targetMethods=" + targetMethods +
                ", atValues=" + atValues +
                ", atTargets=" + atTargets +
                ", cancellable=" + cancellable +
                ", priority=" + priority +
                '}';
    }
}
