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

            conflicts.addAll(detectOverwriteOverwrite(targetClass, classInfos));
            conflicts.addAll(detectOverwriteInject(targetClass, classInfos));
            conflicts.addAll(detectOverwriteRedirect(targetClass, classInfos));
            conflicts.addAll(detectRedirectRedirect(targetClass, classInfos));
            conflicts.addAll(detectModifyArgModifyArg(targetClass, classInfos));
            conflicts.addAll(detectInjectInject(targetClass, classInfos));
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
            if (!info.isMixin || info.targetClasses == null || info.targetClasses.isEmpty()) {
                continue;
            }
            for (String rawTargetClass : info.targetClasses) {
                String normalizedTarget = normalizeClassName(rawTargetClass);
                result.computeIfAbsent(normalizedTarget, k -> new ArrayList<>()).add(info);
            }
        }
        return result;
    }

    /**
     * R1: Detect double @Overwrite on the same method (FATAL).
     */
    private List<MixinConflict> detectOverwriteOverwrite(String targetClass, List<MixinClassInfo> classInfos) {
        List<MixinConflict> conflicts = new ArrayList<>();

        // Collect all overwrites: method name -> list of (classInfo, method)
        Map<String, List<MixinMethodEntry>> overwriteMap = new HashMap<>();
        for (MixinClassInfo info : classInfos) {
            if (info.overwrites == null) continue;
            for (MixinMethod method : info.overwrites) {
                List<String> expandedTargets = expandWildcardMethods(method.targetMethods, info);
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
    private List<MixinConflict> detectOverwriteInject(String targetClass, List<MixinClassInfo> classInfos) {
        List<MixinConflict> conflicts = new ArrayList<>();

        Map<String, List<MixinMethodEntry>> overwriteMap = collectMethodsByTarget(classInfos, "Overwrite");
        Map<String, List<MixinMethodEntry>> injectMap = collectMethodsByTarget(classInfos, "Inject");

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
    private List<MixinConflict> detectOverwriteRedirect(String targetClass, List<MixinClassInfo> classInfos) {
        List<MixinConflict> conflicts = new ArrayList<>();

        Map<String, List<MixinMethodEntry>> overwriteMap = collectMethodsByTarget(classInfos, "Overwrite");
        Map<String, List<MixinMethodEntry>> redirectMap = collectMethodsByTarget(classInfos, "Redirect");

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
    private List<MixinConflict> detectRedirectRedirect(String targetClass, List<MixinClassInfo> classInfos) {
        List<MixinConflict> conflicts = new ArrayList<>();

        // Group by method + @At target key
        Map<String, List<MixinMethodEntry>> redirectGroups = new HashMap<>();
        for (MixinClassInfo info : classInfos) {
            if (info.redirects == null) continue;
            for (MixinMethod method : info.redirects) {
                String atTarget = method.getAtTargetKey();
                for (String targetMethod : method.targetMethods) {
                    String key = targetMethod + "|" + atTarget;
                    redirectGroups.computeIfAbsent(key, k -> new ArrayList<>())
                            .add(new MixinMethodEntry(info, method));
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
    private List<MixinConflict> detectModifyArgModifyArg(String targetClass, List<MixinClassInfo> classInfos) {
        List<MixinConflict> conflicts = new ArrayList<>();

        Map<String, List<MixinMethodEntry>> modifyArgGroups = new HashMap<>();
        for (MixinClassInfo info : classInfos) {
            if (info.modifyArgs == null) continue;
            for (MixinMethod method : info.modifyArgs) {
                String atTarget = method.getAtTargetKey();
                for (String targetMethod : method.targetMethods) {
                    String key = targetMethod + "|" + atTarget;
                    modifyArgGroups.computeIfAbsent(key, k -> new ArrayList<>())
                            .add(new MixinMethodEntry(info, method));
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
    private List<MixinConflict> detectInjectInject(String targetClass, List<MixinClassInfo> classInfos) {
        List<MixinConflict> conflicts = new ArrayList<>();

        Map<String, List<MixinMethodEntry>> injectMap = collectMethodsByTarget(classInfos, "Inject");

        for (Map.Entry<String, List<MixinMethodEntry>> entry : injectMap.entrySet()) {
            String methodName = entry.getKey();
            List<MixinMethodEntry> injects = entry.getValue();

            // Count unique mods
            Set<String> uniqueMods = injects.stream()
                    .map(e -> e.classInfo.sourceModId)
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
            List<MixinClassInfo> classInfos, String annotationType) {
        Map<String, List<MixinMethodEntry>> result = new HashMap<>();

        for (MixinClassInfo info : classInfos) {
            List<MixinMethod> methods = getMethodsByType(info, annotationType);
            if (methods == null || methods.isEmpty()) continue;

            for (MixinMethod method : methods) {
                List<String> expandedTargets = expandWildcardMethods(method.targetMethods, info);
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
                return info.overwrites;
            case "Inject":
                return info.injections;
            case "Redirect":
                return info.redirects;
            case "ModifyArg":
                return info.modifyArgs;
            case "ModifyVariable":
                return info.modifyVariables;
            case "ModifyReturnValue":
                return info.modifyReturnValues;
            case "WrapWithCondition":
                return info.wrapWithConditions;
            default:
                return Collections.emptyList();
        }
    }

    /**
     * Expand wildcard method names to exact matches.
     * Supports patterns like "method_*" or "*" or exact names.
     */
    private List<String> expandWildcardMethods(List<String> methodPatterns, MixinClassInfo classInfo) {
        List<String> result = new ArrayList<>();
        if (methodPatterns == null || methodPatterns.isEmpty()) {
            return result;
        }

        for (String pattern : methodPatterns) {
            if (pattern == null || pattern.isEmpty()) {
                continue;
            }

            if (pattern.equals("*")) {
                result.addAll(getAllMethodNames(classInfo));
            } else if (pattern.contains("*")) {
                result.addAll(matchWildcard(pattern, getAllMethodNames(classInfo)));
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
        if (info.overwrites != null)
            info.overwrites.stream().map(m -> m.targetMethods).filter(Objects::nonNull).forEach(names::addAll);
        if (info.injections != null)
            info.injections.stream().map(m -> m.targetMethods).filter(Objects::nonNull).forEach(names::addAll);
        if (info.redirects != null)
            info.redirects.stream().map(m -> m.targetMethods).filter(Objects::nonNull).forEach(names::addAll);
        if (info.modifyArgs != null)
            info.modifyArgs.stream().map(m -> m.targetMethods).filter(Objects::nonNull).forEach(names::addAll);
        if (info.modifyVariables != null)
            info.modifyVariables.stream().map(m -> m.targetMethods).filter(Objects::nonNull).forEach(names::addAll);
        if (info.modifyReturnValues != null)
            info.modifyReturnValues.stream().map(m -> m.targetMethods).filter(Objects::nonNull).forEach(names::addAll);
        if (info.wrapWithConditions != null)
            info.wrapWithConditions.stream().map(m -> m.targetMethods).filter(Objects::nonNull).forEach(names::addAll);
        return new ArrayList<>(names);
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
        if (a.sourceModId == null || b.sourceModId == null) {
            return false;
        }
        return !a.sourceModId.equals(b.sourceModId);
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
        if (conflict.cardboardMethod == null || conflict.otherMethod == null) {
            return true;
        }
        // Already filtered during detection, but double-check
        return true;
    }

    /**
     * Mark conflicts that are already known in the compatibility database.
     */
    private List<MixinConflict> markKnownConflicts(List<MixinConflict> conflicts) {
        if (compatDatabase == null) {
            return conflicts;
        }

        for (MixinConflict conflict : conflicts) {
            if (conflict.otherModId == null) continue;

            Optional<ModCompatibilityRule> rule = compatDatabase.getRuleForMod(conflict.otherModId);
            if (rule.isPresent()) {
                ModCompatibilityRule r = rule.get();
                if (r.getStatus() == ModCompatibilityRule.Status.CONFLICT_RESOLVED) {
                    conflict.isResolved = true;
                    conflict.resolutionNote = "Known rule: " + r.getNotes();
                } else if (r.getStatus() == ModCompatibilityRule.Status.COMPATIBLE) {
                    if (conflict.level == ConflictLevel.LOW) {
                        conflict.isResolved = true;
                        conflict.resolutionNote = "Known compatible: " + r.getNotes();
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
            if (conflict.level == ConflictLevel.FATAL) {
                if (conflict.cardboardMixinClass != null) {
                    fatalMixinSet.add(conflict.cardboardMixinClass);
                }
            }

            String key = conflict.targetClass + "#" + conflict.targetMethod;
            conflictMethodMap.computeIfAbsent(key, k -> new HashSet<>())
                    .add(conflict.cardboardMixinClass != null ? conflict.cardboardMixinClass : "");
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
        conflict.conflictType = conflictType;
        conflict.level = level;
        conflict.targetClass = targetClass;
        conflict.targetMethod = targetMethod;
        conflict.suggestion = suggestion;

        // Determine which is Cardboard and which is other mod
        if (isCardboardMod(a.classInfo.sourceModId)) {
            conflict.cardboardMixinClass = a.classInfo.className;
            conflict.cardboardMethod = a.method;
            conflict.otherModId = b.classInfo.sourceModId;
            conflict.otherMixinClass = b.classInfo.className;
            conflict.otherMethod = b.method;
        } else if (isCardboardMod(b.classInfo.sourceModId)) {
            conflict.cardboardMixinClass = b.classInfo.className;
            conflict.cardboardMethod = b.method;
            conflict.otherModId = a.classInfo.sourceModId;
            conflict.otherMixinClass = a.classInfo.className;
            conflict.otherMethod = a.method;
        } else {
            // Neither is Cardboard, put first as cardboard side
            conflict.cardboardMixinClass = a.classInfo.className;
            conflict.cardboardMethod = a.method;
            conflict.otherModId = b.classInfo.sourceModId;
            conflict.otherMixinClass = b.classInfo.className;
            conflict.otherMethod = b.method;
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
