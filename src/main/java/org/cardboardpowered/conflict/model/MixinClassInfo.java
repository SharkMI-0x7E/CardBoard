package org.cardboardpowered.conflict.model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MixinClassInfo {

    public String className;
    public boolean isMixin;
    public List<String> targetClasses = new ArrayList<>();
    public int priority = 1000;
    public String sourceModId;
    public String sourceJarPath;

    public List<MixinMethod> overwrites = new ArrayList<>();
    public List<MixinMethod> injections = new ArrayList<>();
    public List<MixinMethod> redirects = new ArrayList<>();
    public List<MixinMethod> modifyArgs = new ArrayList<>();
    public List<MixinMethod> modifyVariables = new ArrayList<>();
    public List<MixinMethod> modifyReturnValues = new ArrayList<>();
    public List<MixinMethod> wrapWithConditions = new ArrayList<>();

    public boolean hasOverwrite() {
        return overwrites != null && !overwrites.isEmpty();
    }

    public boolean hasInject() {
        return injections != null && !injections.isEmpty();
    }

    public boolean hasRedirect() {
        return redirects != null && !redirects.isEmpty();
    }

    public List<MixinMethod> getAllMethods() {
        Set<MixinMethod> all = new HashSet<>();
        if (overwrites != null) all.addAll(overwrites);
        if (injections != null) all.addAll(injections);
        if (redirects != null) all.addAll(redirects);
        if (modifyArgs != null) all.addAll(modifyArgs);
        if (modifyVariables != null) all.addAll(modifyVariables);
        if (modifyReturnValues != null) all.addAll(modifyReturnValues);
        if (wrapWithConditions != null) all.addAll(wrapWithConditions);
        return new ArrayList<>(all);
    }

    public String getTargetClass() {
        if (targetClasses == null || targetClasses.isEmpty()) {
            return "";
        }
        return targetClasses.get(0);
    }

    public int getMethodCount() {
        int count = 0;
        if (overwrites != null) count += overwrites.size();
        if (injections != null) count += injections.size();
        if (redirects != null) count += redirects.size();
        if (modifyArgs != null) count += modifyArgs.size();
        if (modifyVariables != null) count += modifyVariables.size();
        if (modifyReturnValues != null) count += modifyReturnValues.size();
        if (wrapWithConditions != null) count += wrapWithConditions.size();
        return count;
    }

    @Override
    public String toString() {
        return "MixinClassInfo{" +
                "className='" + className + '\'' +
                ", isMixin=" + isMixin +
                ", targetClasses=" + targetClasses +
                ", priority=" + priority +
                ", sourceModId='" + sourceModId + '\'' +
                ", methodCount=" + getMethodCount() +
                '}';
    }
}
