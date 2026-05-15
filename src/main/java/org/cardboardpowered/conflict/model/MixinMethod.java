package org.cardboardpowered.conflict.model;

import java.util.ArrayList;
import java.util.List;

public class MixinMethod {

    public String name;
    public String descriptor;
    public String annotationType;
    public List<String> targetMethods = new ArrayList<>();
    public List<String> atValues = new ArrayList<>();
    public List<String> atTargets = new ArrayList<>();
    public boolean cancellable;
    public int priority = 1000;

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
