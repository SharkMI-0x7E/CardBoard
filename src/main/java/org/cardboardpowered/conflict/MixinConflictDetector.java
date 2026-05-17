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
package org.cardboardpowered.conflict;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cardboardpowered.compat.ModCompatibilityDatabase;
import org.cardboardpowered.compat.ModCompatibilityRule;
import org.cardboardpowered.conflict.model.ConflictLevel;
import org.cardboardpowered.conflict.model.MixinClassInfo;
import org.cardboardpowered.conflict.model.MixinConflict;
import org.cardboardpowered.conflict.model.MixinMethod;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Core conflict detection algorithm for Mixin annotations.
 * Implements R1-R6 rules to detect FATAL/HIGH/MEDIUM/LOW conflicts across mods.
 */
public class MixinConflictDetector {

    private static final Logger LOGGER = LogManager.getLogger("Cardboard-ConflictDetector");

    private static final String CARDBOARD_MOD_ID = "cardboard";
    private static final String CARDBOARD_MOD_ID_ALT = "cardboardmod";

    private static final int INJECT_LOW_THRESHOLD = 5;

    private static final String CONFLICT_OVERWRITE_OVERWRITE = "OVERWRITE_OVERWRITE";
    private static final String CONFLICT_OVERWRITE_INJECT = "OVERWRITE_INJECT";
    private static final String CONFLICT_OVERWRITE_REDIRECT = "OVERWRITE_REDIRECT";
    private static final String CONFLICT_REDIRECT_REDIRECT = "REDIRECT_REDIRECT";
    private static final String CONFLICT_MODIFYARG_MODIFYARG = "MODIFYARG_MODIFYARG";
    private static final String CONFLICT_INJECT_INJECT = "INJECT_INJECT";

    private final MappingBridge mappingBridge;
    private final ModCompatibilityDatabase compatDatabase;

    // Pre-built lookup structures
    private Set<String> fatalMixinSet = new HashSet<>();
    private Map<String, Set<String>> conflictMethodMap = new HashMap<>();

    public MixinConflictDetector() {
        this(null, null);
    }

    public MixinConflictDetector(MappingBridge mappingBridge, ModCompatibilityDatabase compatDatabase) {
        this.mappingBridge = mappingBridge != null ? mappingBridge : new MappingBridge();
        this.compatDatabase = compatDatabase;
    }

    /**
     * Run conflict detection on all parsed Mixin class info.
     */
    public List<MixinConflict> detect(List<MixinClassInfo> allClassInfos) {
        long startTime = System.currentTimeMillis();
        List<MixinConflict> conflicts = new ArrayList<>();

        // Step 1: Group by target class
        Map<String, List<MixinClassInfo>> byTargetClass = groupByTargetClass(allClassInfos);
        LOGGER.info("Grouped {} mixin classes into {} target classes", allClassInfos.size(), byTargetClass.size());

        // Step 2: For each target class, detect conflicts
        for (Map.Entry<String, List<MixinClassInfo>> entry : byTargetClass.entrySet()) {
            String targetClass = entry.getKey();
            List<MixinClassInfo> classInfos = entry.getValue();

            // Pre-collect all method names mentioned by any mixin in this group for wildcard expansion
            Set<String> allMethodNames = collectAllMethodNames(classInfos);
            LOGGER.debug("Target class {} has {} candidate method names for wildcard expansion", targetClass, allMethodNames.size());

            conflicts.addAll(detectOverwriteOverwrite(targetClass, classInfos, allMethodNames));
            conflicts.addAll(detectOverwriteInject(targetClass, classInfos, allMethodNames));
            conflicts.addAll(detectOverwriteRedirect(targetClass, classInfos, allMethodNames));
            conflicts.addAll(detectRedirectRedirect(targetClass, classInfos, allMethodNames));
            conflicts.addAll(detectModifyArgModifyArg(targetClass, classInfos, allMethodNames));
            conflicts.addAll(detectInjectInject(targetClass, classInfos, allMethodNames));
        }

        // Step 3: Filter self-conflicts (same mod)
        conflicts = filterSelfConflicts(conflicts);
        LOGGER.info("After self-conflict filter: {} conflicts remaining", conflicts.size());

        // Step 4: Mark known conflicts from compatibility database
        conflicts = markKnownConflicts(conflicts);

        // Step 5: Pre-build lookup structures
        buildLookupStructures(conflicts);

        long elapsed = System.currentTimeMillis() - startTime;
        LOGGER.info("Conflict detection completed in {}ms. Found {} conflicts", elapsed, conflicts.size());

        return conflicts;
    }

    /**
     * Group MixinClassInfo by normalized target class name.
     */
    private Map<String, List<MixinClassInfo>> groupByTargetClass(List<MixinClassInfo> allClassInfos) {
        Map<String, List<MixinClassInfo>> result = new HashMap<>();
        for (MixinClassInfo info : allClassInfos) {
            if (!info.isMixin() || info.getTargetClasses() == null || info.getTargetClasses().isEmpty()) {
                continue;
            }
            for (String rawTargetClass : info.getTargetClasses()) {
                String normalizedTarget = normalizeClassName(rawTargetClass);
                result.computeIfAbsent(normalizedTarget, k -> new ArrayList<>()).add(info);
            }
        }
        return result;
    }

    /**
     * Collect all method names mentioned by any mixin in the group.
     * This provides the candidate set for wildcard expansion.
     */
    private Set<String> collectAllMethodNames(List<MixinClassInfo> classInfos) {
        Set<String> names = new HashSet<>();
        for (MixinClassInfo info : classInfos) {
            names.addAll(getAllMethodNames(info));
        }
        return names;
    }

    /**
     * R1: Detect double @Overwrite on the same method (FATAL).
     */
    private List<MixinConflict> detectOverwriteOverwrite(String targetClass, List<MixinClassInfo> classInfos, Set<String> allMethodNames) {
        List<MixinConflict> conflicts = new ArrayList<>();

        // Collect all overwrites: method name -> list of (classInfo, method)
        Map<String, List<MixinMethodEntry>> overwriteMap = new HashMap<>();
        for (MixinClassInfo info : classInfos) {
            if (info.getOverwrites() == null) continue;
            for (MixinMethod method : info.getOverwrites()) {
                List<String> expandedTargets = expandWildcardMethods(method.getTargetMethods(), allMethodNames);
                for (String methodName : expandedTargets) {
                    String key = methodName;
                    overwriteMap.computeIfAbsent(key, k -> new ArrayList<>())
                            .add(new MixinMethodEntry(info, method));
                }
            }
        }

        // Check for cross-mod pairs
        for (Map.Entry<String, List<MixinMethodEntry>> entry : overwriteMap.entrySet()) {
            String methodName = entry.getKey();
            List<MixinMethodEntry> entries = entry.getValue();

            for (int i = 0; i < entries.size(); i++) {
                for (int j = i + 1; j < entries.size(); j++) {
                    MixinMethodEntry a = entries.get(i);
                    MixinMethodEntry b = entries.get(j);

                    if (!isCrossModConflict(a.classInfo, b.classInfo)) {
                        continue;
                    }

                    // Determine which is Cardboard and which is other mod
                    MixinConflict conflict = createConflict(CONFLICT_OVERWRITE_OVERWRITE, ConflictLevel.FATAL,
                            targetClass, methodName, a, b,
                            "Double @Overwrite on the same method. One of the conflicting mixins must be disabled.");
                    conflicts.add(conflict);
                }
            }
        }

        return conflicts;
    }

    /**
     * R2: Detect @Overwrite vs @Inject on the same method (HIGH).
     */
    private List<MixinConflict> detectOverwriteInject(String targetClass, List<MixinClassInfo> classInfos, Set<String> allMethodNames) {
        List<MixinConflict> conflicts = new ArrayList<>();

        Map<String, List<MixinMethodEntry>> overwriteMap = collectMethodsByTarget(classInfos, "Overwrite", allMethodNames);
        Map<String, List<MixinMethodEntry>> injectMap = collectMethodsByTarget(classInfos, "Inject", allMethodNames);

        for (Map.Entry<String, List<MixinMethodEntry>> owEntry : overwriteMap.entrySet()) {
            String methodName = owEntry.getKey();
            List<MixinMethodEntry> injects = injectMap.get(methodName);
            if (injects == null) continue;

            for (MixinMethodEntry overwrite : owEntry.getValue()) {
                for (MixinMethodEntry inject : injects) {
                    if (!isCrossModConflict(overwrite.classInfo, inject.classInfo)) {
                        continue;
                    }

                    MixinConflict conflict = createConflict(CONFLICT_OVERWRITE_INJECT, ConflictLevel.HIGH,
                            targetClass, methodName, overwrite, inject,
                            "@Overwrite replaces entire method body, @Inject may be ineffective.");
                    conflicts.add(conflict);
                }
            }
        }

        return conflicts;
    }

    /**
     * R3: Detect @Overwrite vs @Redirect on the same method (HIGH).
     */
    private List<MixinConflict> detectOverwriteRedirect(String targetClass, List<MixinClassInfo> classInfos, Set<String> allMethodNames) {
        List<MixinConflict> conflicts = new ArrayList<>();

        Map<String, List<MixinMethodEntry>> overwriteMap = collectMethodsByTarget(classInfos, "Overwrite", allMethodNames);
        Map<String, List<MixinMethodEntry>> redirectMap = collectMethodsByTarget(classInfos, "Redirect", allMethodNames);

        for (Map.Entry<String, List<MixinMethodEntry>> owEntry : overwriteMap.entrySet()) {
            String methodName = owEntry.getKey();
            List<MixinMethodEntry> redirects = redirectMap.get(methodName);
            if (redirects == null) continue;

            for (MixinMethodEntry overwrite : owEntry.getValue()) {
                for (MixinMethodEntry redirect : redirects) {
                    if (!isCrossModConflict(overwrite.classInfo, redirect.classInfo)) {
                        continue;
                    }

                    MixinConflict conflict = createConflict(CONFLICT_OVERWRITE_REDIRECT, ConflictLevel.HIGH,
                            targetClass, methodName, overwrite, redirect,
                            "@Overwrite covers method body, @Redirect target may be unreachable.");
                    conflicts.add(conflict);
                }
            }
        }

        return conflicts;
    }

    /**
     * R4: Detect double @Redirect competing on the same INVOKE target (MEDIUM).
     * Uses method + @At target exact matching.
     */
    private List<MixinConflict> detectRedirectRedirect(String targetClass, List<MixinClassInfo> classInfos, Set<String> allMethodNames) {
        List<MixinConflict> conflicts = new ArrayList<>();

        // Group by method + @At target key
        Map<String, List<MixinMethodEntry>> redirectGroups = new HashMap<>();
        for (MixinClassInfo info : classInfos) {
            if (info.getRedirects() == null) continue;
            for (MixinMethod method : info.getRedirects()) {
                String atTarget = normalizeAtTarget(method.getAtTargetKey());
                for (String targetMethod : method.getTargetMethods()) {
                    List<String> expanded = expandWildcardMethods(List.of(targetMethod), allMethodNames);
                    for (String expandedMethod : expanded) {
                        String key = expandedMethod + "|" + atTarget;
                        redirectGroups.computeIfAbsent(key, k -> new ArrayList<>())
                                .add(new MixinMethodEntry(info, method));
                    }
                }
            }
        }

        for (Map.Entry<String, List<MixinMethodEntry>> entry : redirectGroups.entrySet()) {
            String key = entry.getKey();
            List<MixinMethodEntry> entries = entry.getValue();

            String[] parts = key.split("\\|", 2);
            String methodName = parts[0];
            String atTarget = parts.length > 1 ? parts[1] : "";

            for (int i = 0; i < entries.size(); i++) {
                for (int j = i + 1; j < entries.size(); j++) {
                    MixinMethodEntry a = entries.get(i);
                    MixinMethodEntry b = entries.get(j);

                    if (!isCrossModConflict(a.classInfo, b.classInfo)) {
                        continue;
                    }

                    String suggestion = "Both @Redirect target the same INVOKE. Only one will take effect. " +
                            "Check if both are needed, or adjust priority.";
                    if (!atTarget.isEmpty()) {
                        suggestion += " INVOKE target: " + atTarget;
                    }

                    MixinConflict conflict = createConflict(CONFLICT_REDIRECT_REDIRECT, ConflictLevel.MEDIUM,
                            targetClass, methodName, a, b, suggestion);
                    conflicts.add(conflict);
                }
            }
        }

        return conflicts;
    }

    /**
     * R5: Detect double @ModifyArg competing on the same INVOKE target (MEDIUM).
     */
    private List<MixinConflict> detectModifyArgModifyArg(String targetClass, List<MixinClassInfo> classInfos, Set<String> allMethodNames) {
        List<MixinConflict> conflicts = new ArrayList<>();

        Map<String, List<MixinMethodEntry>> modifyArgGroups = new HashMap<>();
        for (MixinClassInfo info : classInfos) {
            if (info.getModifyArgs() == null) continue;
            for (MixinMethod method : info.getModifyArgs()) {
                String atTarget = normalizeAtTarget(method.getAtTargetKey());
                for (String targetMethod : method.getTargetMethods()) {
                    List<String> expanded = expandWildcardMethods(List.of(targetMethod), allMethodNames);
                    for (String expandedMethod : expanded) {
                        String key = expandedMethod + "|" + atTarget;
                        modifyArgGroups.computeIfAbsent(key, k -> new ArrayList<>())
                                .add(new MixinMethodEntry(info, method));
                    }
                }
            }
        }

        for (Map.Entry<String, List<MixinMethodEntry>> entry : modifyArgGroups.entrySet()) {
            String key = entry.getKey();
            List<MixinMethodEntry> entries = entry.getValue();

            String[] parts = key.split("\\|", 2);
            String methodName = parts[0];
            String atTarget = parts.length > 1 ? parts[1] : "";

            for (int i = 0; i < entries.size(); i++) {
                for (int j = i + 1; j < entries.size(); j++) {
                    MixinMethodEntry a = entries.get(i);
                    MixinMethodEntry b = entries.get(j);

                    if (!isCrossModConflict(a.classInfo, b.classInfo)) {
                        continue;
                    }

                    String suggestion = "Both @ModifyArg target the same INVOKE. Order is undefined. " +
                            "Adjust priority if necessary.";
                    if (!atTarget.isEmpty()) {
                        suggestion += " INVOKE target: " + atTarget;
                    }

                    MixinConflict conflict = createConflict(CONFLICT_MODIFYARG_MODIFYARG, ConflictLevel.MEDIUM,
                            targetClass, methodName, a, b, suggestion);
                    conflicts.add(conflict);
                }
            }
        }

        return conflicts;
    }

    /**
     * R6: Detect multiple @Inject coexistence (LOW when >5 different mods).
     */
    private List<MixinConflict> detectInjectInject(String targetClass, List<MixinClassInfo> classInfos, Set<String> allMethodNames) {
        List<MixinConflict> conflicts = new ArrayList<>();

        Map<String, List<MixinMethodEntry>> injectMap = collectMethodsByTarget(classInfos, "Inject", allMethodNames);

        for (Map.Entry<String, List<MixinMethodEntry>> entry : injectMap.entrySet()) {
            String methodName = entry.getKey();
            List<MixinMethodEntry> injects = entry.getValue();

            // Count unique mods
            Set<String> uniqueMods = injects.stream()
                    .map(e -> e.classInfo.getSourceModId())
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());

            if (uniqueMods.size() <= INJECT_LOW_THRESHOLD) {
                continue;
            }

            // Generate conflict pairs for mods exceeding threshold
            List<MixinMethodEntry> entries = entry.getValue();
            for (int i = 0; i < entries.size(); i++) {
                for (int j = i + 1; j < entries.size(); j++) {
                    MixinMethodEntry a = entries.get(i);
                    MixinMethodEntry b = entries.get(j);

                    if (!isCrossModConflict(a.classInfo, b.classInfo)) {
                        continue;
                    }

                    MixinConflict conflict = createConflict(CONFLICT_INJECT_INJECT, ConflictLevel.LOW,
                            targetClass, methodName, a, b,
                            "Multiple @Inject coexist on the same method. " +
                                    uniqueMods.size() + " mods total. Execution order depends on priority.");
                    conflicts.add(conflict);
                }
            }
        }

        return conflicts;
    }

    /**
     * Collect methods of a given annotation type, grouped by target method name.
     */
    private Map<String, List<MixinMethodEntry>> collectMethodsByTarget(
            List<MixinClassInfo> classInfos, String annotationType, Set<String> allMethodNames) {
        Map<String, List<MixinMethodEntry>> result = new HashMap<>();

        for (MixinClassInfo info : classInfos) {
            List<MixinMethod> methods = getMethodsByType(info, annotationType);
            if (methods == null || methods.isEmpty()) continue;

            for (MixinMethod method : methods) {
                List<String> expandedTargets = expandWildcardMethods(method.getTargetMethods(), allMethodNames);
                for (String methodName : expandedTargets) {
                    result.computeIfAbsent(methodName, k -> new ArrayList<>())
                            .add(new MixinMethodEntry(info, method));
                }
            }
        }

        return result;
    }

    /**
     * Get method list from MixinClassInfo by annotation type.
     */
    private List<MixinMethod> getMethodsByType(MixinClassInfo info, String annotationType) {
        switch (annotationType) {
            case "Overwrite":
                return info.getOverwrites();
            case "Inject":
                return info.getInjections();
            case "Redirect":
                return info.getRedirects();
            case "ModifyArg":
                return info.getModifyArgs();
            case "ModifyVariable":
                return info.getModifyVariables();
            case "ModifyReturnValue":
                return info.getModifyReturnValues();
            case "WrapWithCondition":
                return info.getWrapWithConditions();
            default:
                return Collections.emptyList();
        }
    }

    /**
     * Expand wildcard method names to exact matches.
     * Supports patterns like "method_*" or "*" or exact names.
     */
    private List<String> expandWildcardMethods(List<String> methodPatterns, Set<String> candidateMethods) {
        List<String> result = new ArrayList<>();
        if (methodPatterns == null || methodPatterns.isEmpty()) {
            return result;
        }

        for (String pattern : methodPatterns) {
            if (pattern == null || pattern.isEmpty()) {
                continue;
            }

            if (pattern.equals("*")) {
                result.addAll(candidateMethods);
            } else if (pattern.contains("*")) {
                result.addAll(matchWildcard(pattern, new ArrayList<>(candidateMethods)));
            } else {
                result.add(pattern);
            }
        }

        return result.stream().distinct().collect(Collectors.toList());
    }

    /**
     * Get all method names from a MixinClassInfo (all annotation types).
     */
    private List<String> getAllMethodNames(MixinClassInfo info) {
        Set<String> names = new HashSet<>();
        if (info.getOverwrites() != null)
            info.getOverwrites().stream().map(m -> m.getTargetMethods()).filter(Objects::nonNull).forEach(names::addAll);
        if (info.getInjections() != null)
            info.getInjections().stream().map(m -> m.getTargetMethods()).filter(Objects::nonNull).forEach(names::addAll);
        if (info.getRedirects() != null)
            info.getRedirects().stream().map(m -> m.getTargetMethods()).filter(Objects::nonNull).forEach(names::addAll);
        if (info.getModifyArgs() != null)
            info.getModifyArgs().stream().map(m -> m.getTargetMethods()).filter(Objects::nonNull).forEach(names::addAll);
        if (info.getModifyVariables() != null)
            info.getModifyVariables().stream().map(m -> m.getTargetMethods()).filter(Objects::nonNull).forEach(names::addAll);
        if (info.getModifyReturnValues() != null)
            info.getModifyReturnValues().stream().map(m -> m.getTargetMethods()).filter(Objects::nonNull).forEach(names::addAll);
        if (info.getWrapWithConditions() != null)
            info.getWrapWithConditions().stream().map(m -> m.getTargetMethods()).filter(Objects::nonNull).forEach(names::addAll);
        return new ArrayList<>(names);
    }

    /**
     * Normalize an @At target descriptor for consistent comparison.
     * Strips class name prefix and normalizes slash/dot format.
     * e.g. "Lnet/minecraft/Class;method()V" -> "method()V"
     * e.g. "Lnet.minecraft.Class;method()V" -> "method()V"
     */
    private String normalizeAtTarget(String target) {
        if (target == null || target.isEmpty()) {
            return "";
        }
        String normalized = target;
        // If it starts with L and contains ;, it's a full descriptor
        if (normalized.startsWith("L") && normalized.contains(";")) {
            int semiIndex = normalized.indexOf(';');
            if (semiIndex >= 0 && semiIndex + 1 < normalized.length()) {
                normalized = normalized.substring(semiIndex + 1);
            }
        }
        // Also handle cases where it's "ClassName.method()V" format
        int lastDot = normalized.lastIndexOf('.');
        int lastSlash = normalized.lastIndexOf('/');
        int lastSep = Math.max(lastDot, lastSlash);
        if (lastSep >= 0 && lastSep + 1 < normalized.length()) {
            char nextChar = normalized.charAt(lastSep + 1);
            // Only strip if next char looks like a method name (not a descriptor like V)
            if (Character.isJavaIdentifierStart(nextChar)) {
                normalized = normalized.substring(lastSep + 1);
            }
        }
        return normalized.replace('/', '.').replace('$', '.');
    }

    /**
     * Match a wildcard pattern against a list of method names.
     */
    private List<String> matchWildcard(String pattern, List<String> candidates) {
        // Convert wildcard pattern to regex
        String regex = pattern
                .replace(".", "\\.")
                .replace("*", ".*")
                .replace("?", ".");
        Pattern p = Pattern.compile("^" + regex + "$");

        List<String> matches = new ArrayList<>();
        for (String candidate : candidates) {
            if (p.matcher(candidate).matches()) {
                matches.add(candidate);
            }
        }
        return matches;
    }

    /**
     * Check if two MixinClassInfo are from different mods (cross-mod conflict).
     */
    private boolean isCrossModConflict(MixinClassInfo a, MixinClassInfo b) {
        if (a.getSourceModId() == null || b.getSourceModId() == null) {
            return false;
        }
        return !a.getSourceModId().equals(b.getSourceModId());
    }

    /**
     * Filter out self-conflicts (same mod conflicts).
     */
    private List<MixinConflict> filterSelfConflicts(List<MixinConflict> conflicts) {
        return conflicts.stream()
                .filter(c -> isCrossModConflictByConflict(c))
                .collect(Collectors.toList());
    }

    /**
     * Check if a conflict is a cross-mod conflict.
     */
    private boolean isCrossModConflictByConflict(MixinConflict conflict) {
        if (conflict.getCardboardMethod() == null || conflict.getOtherMethod() == null) {
            return true;
        }
        // Compare mod IDs to ensure it's a cross-mod conflict
        String cardboardModId = conflict.getCardboardModId();
        String otherModId = conflict.getOtherModId();
        if (cardboardModId == null || otherModId == null) {
            return true;
        }
        return !cardboardModId.equals(otherModId);
    }

    /**
     * Mark conflicts that are already known in the compatibility database.
     */
    private List<MixinConflict> markKnownConflicts(List<MixinConflict> conflicts) {
        if (compatDatabase == null) {
            return conflicts;
        }

        for (MixinConflict conflict : conflicts) {
            if (conflict.getOtherModId() == null) continue;

            Optional<ModCompatibilityRule> rule = compatDatabase.getRuleForMod(conflict.getOtherModId());
            if (rule.isPresent()) {
                ModCompatibilityRule r = rule.get();
                if (r.getStatus() == ModCompatibilityRule.Status.CONFLICT_RESOLVED) {
                    conflict.setResolved(true);
                    conflict.setResolutionNote("Known rule: " + r.getNotes());
                } else if (r.getStatus() == ModCompatibilityRule.Status.COMPATIBLE) {
                    if (conflict.getLevel() == ConflictLevel.LOW) {
                        conflict.setResolved(true);
                        conflict.setResolutionNote("Known compatible: " + r.getNotes());
                    }
                }
            }
        }

        return conflicts;
    }

    /**
     * Build pre-built lookup structures for O(1) shouldApplyMixin queries.
     */
    private void buildLookupStructures(List<MixinConflict> conflicts) {
        fatalMixinSet = new HashSet<>();
        conflictMethodMap = new HashMap<>();

        for (MixinConflict conflict : conflicts) {
            if (conflict.getLevel() == ConflictLevel.FATAL) {
                if (conflict.getCardboardMixinClass() != null) {
                    fatalMixinSet.add(conflict.getCardboardMixinClass());
                }
            }

            String key = conflict.getTargetClass() + "#" + conflict.getTargetMethod();
            conflictMethodMap.computeIfAbsent(key, k -> new HashSet<>())
                    .add(conflict.getCardboardMixinClass() != null ? conflict.getCardboardMixinClass() : "");
        }
    }

    /**
     * Get the set of FATAL conflict mixin class names.
     * Used by shouldApplyMixin for O(1) lookup.
     */
    public Set<String> getFatalMixinSet() {
        return Collections.unmodifiableSet(fatalMixinSet);
    }

    /**
     * Get the conflict method map.
     * Key: "targetClass#targetMethod", Value: set of conflicting mixin class names.
     */
    public Map<String, Set<String>> getConflictMethodMap() {
        return Collections.unmodifiableMap(conflictMethodMap);
    }

    /**
     * Normalize a class name using the MappingBridge.
     */
    private String normalizeClassName(String rawClassName) {
        if (rawClassName == null || rawClassName.isEmpty()) {
            return "";
        }
        // Convert from internal JVM format (net/minecraft/class_1234) to dot format
        String dotFormat = rawClassName.replace('/', '.');
        return mappingBridge.normalizeClassName(dotFormat);
    }

    /**
     * Create a MixinConflict with all fields populated.
     */
    private MixinConflict createConflict(String conflictType, ConflictLevel level,
                                         String targetClass, String targetMethod,
                                         MixinMethodEntry a, MixinMethodEntry b,
                                         String suggestion) {
        MixinConflict conflict = new MixinConflict();
        conflict.setConflictType(conflictType);
        conflict.setLevel(level);
        conflict.setTargetClass(targetClass);
        conflict.setTargetMethod(targetMethod);
        conflict.setSuggestion(suggestion);

        // Determine which is Cardboard and which is other mod
        if (isCardboardMod(a.classInfo.getSourceModId())) {
            conflict.setCardboardMixinClass(a.classInfo.getClassName());
            conflict.setCardboardMethod(a.method);
            conflict.setOtherModId(b.classInfo.getSourceModId());
            conflict.setOtherMixinClass(b.classInfo.getClassName());
            conflict.setOtherMethod(b.method);
            conflict.setCardboardModId(a.classInfo.getSourceModId());
        } else if (isCardboardMod(b.classInfo.getSourceModId())) {
            conflict.setCardboardMixinClass(b.classInfo.getClassName());
            conflict.setCardboardMethod(b.method);
            conflict.setOtherModId(a.classInfo.getSourceModId());
            conflict.setOtherMixinClass(a.classInfo.getClassName());
            conflict.setOtherMethod(a.method);
            conflict.setCardboardModId(b.classInfo.getSourceModId());
        } else {
            // Neither is Cardboard, put first as cardboard side
            conflict.setCardboardMixinClass(a.classInfo.getClassName());
            conflict.setCardboardMethod(a.method);
            conflict.setOtherModId(b.classInfo.getSourceModId());
            conflict.setOtherMixinClass(b.classInfo.getClassName());
            conflict.setOtherMethod(b.method);
            conflict.setCardboardModId(a.classInfo.getSourceModId());
        }

        return conflict;
    }

    /**
     * Check if a mod ID is Cardboard.
     */
    private boolean isCardboardMod(String modId) {
        if (modId == null) return false;
        String lower = modId.toLowerCase();
        return lower.equals(CARDBOARD_MOD_ID) || lower.equals(CARDBOARD_MOD_ID_ALT);
    }

    /**
     * Internal helper class to pair MixinClassInfo with MixinMethod.
     */
    private static class MixinMethodEntry {
        final MixinClassInfo classInfo;
        final MixinMethod method;

        MixinMethodEntry(MixinClassInfo classInfo, MixinMethod method) {
            this.classInfo = classInfo;
            this.method = method;
        }
    }
}
