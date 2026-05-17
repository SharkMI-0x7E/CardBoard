package org.cardboardpowered.conflict.model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MixinClassInfo {

    private String className;
    private boolean isMixin;
    private List<String> targetClasses = new ArrayList<>();
    private int priority = 1000;
    private String sourceModId;
    private String sourceJarPath;

    private List<MixinMethod> overwrites = new ArrayList<>();
    private List<MixinMethod> injections = new ArrayList<>();
    private List<MixinMethod> redirects = new ArrayList<>();
    private List<MixinMethod> modifyArgs = new ArrayList<>();
    private List<MixinMethod> modifyVariables = new ArrayList<>();
    private List<MixinMethod> modifyReturnValues = new ArrayList<>();
    private List<MixinMethod> wrapWithConditions = new ArrayList<>();

    public String getClassName() { return className; }
    public void setClassName(String className) { this.className = className; }

    public boolean isMixin() { return isMixin; }
    public void setMixin(boolean isMixin) { this.isMixin = isMixin; }

    public List<String> getTargetClasses() { return targetClasses; }
    public void setTargetClasses(List<String> targetClasses) { this.targetClasses = targetClasses; }

    public int getPriority() { return priority; }
    public void setPriority(int priority) { this.priority = priority; }

    public String getSourceModId() { return sourceModId; }
    public void setSourceModId(String sourceModId) { this.sourceModId = sourceModId; }

    public String getSourceJarPath() { return sourceJarPath; }
    public void setSourceJarPath(String sourceJarPath) { this.sourceJarPath = sourceJarPath; }

    public List<MixinMethod> getOverwrites() { return overwrites; }
    public void setOverwrites(List<MixinMethod> overwrites) { this.overwrites = overwrites; }

    public List<MixinMethod> getInjections() { return injections; }
    public void setInjections(List<MixinMethod> injections) { this.injections = injections; }

    public List<MixinMethod> getRedirects() { return redirects; }
    public void setRedirects(List<MixinMethod> redirects) { this.redirects = redirects; }

    public List<MixinMethod> getModifyArgs() { return modifyArgs; }
    public void setModifyArgs(List<MixinMethod> modifyArgs) { this.modifyArgs = modifyArgs; }

    public List<MixinMethod> getModifyVariables() { return modifyVariables; }
    public void setModifyVariables(List<MixinMethod> modifyVariables) { this.modifyVariables = modifyVariables; }

    public List<MixinMethod> getModifyReturnValues() { return modifyReturnValues; }
    public void setModifyReturnValues(List<MixinMethod> modifyReturnValues) { this.modifyReturnValues = modifyReturnValues; }

    public List<MixinMethod> getWrapWithConditions() { return wrapWithConditions; }
    public void setWrapWithConditions(List<MixinMethod> wrapWithConditions) { this.wrapWithConditions = wrapWithConditions; }

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
