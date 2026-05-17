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
package org.cardboardpowered.compat;

import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import org.cardboardpowered.compat.ModCompatibilityRule.Status;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class ModCompatibilityDatabase {

    private static ModCompatibilityDatabase instance;

    private final Map<String, ModCompatibilityRule> rulesByModId;
    private final boolean autoConflictResolution;

    private ModCompatibilityDatabase(Map<String, ModCompatibilityRule> rules, boolean autoConflictResolution) {
        this.rulesByModId = Collections.unmodifiableMap(rules);
        this.autoConflictResolution = autoConflictResolution;
    }

    public static ModCompatibilityDatabase load() {
        if (instance != null) {
            return instance;
        }

        Path configDir = FabricLoader.getInstance().getConfigDir().resolve("cardboard");
        Path configFile = configDir.resolve("mod-compatibility.yml");

        try {
            Files.createDirectories(configDir);
            copyDefaultConfigIfMissing(configFile);

            Map<String, Object> yamlData = parseYaml(configFile);
            boolean autoResolution = getBoolean(yamlData, "auto-conflict-resolution", true);
            Map<String, ModCompatibilityRule> rules = parseRules(yamlData);

            instance = new ModCompatibilityDatabase(rules, autoResolution);
            return instance;
        } catch (Exception e) {
            System.err.println("[Cardboard] Failed to load mod compatibility database: " + e.getMessage());
            instance = new ModCompatibilityDatabase(Collections.emptyMap(), true);
            return instance;
        }
    }

    private static void copyDefaultConfigIfMissing(Path configFile) throws IOException {
        if (!Files.exists(configFile)) {
            try (InputStream input = ModCompatibilityDatabase.class.getClassLoader().getResourceAsStream("cardboard/mod-compatibility.yml")) {
                if (input != null) {
                    Files.copy(input, configFile, StandardCopyOption.REPLACE_EXISTING);
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> parseYaml(Path configFile) throws IOException {
        Yaml yaml = new Yaml();
        try (InputStream input = Files.newInputStream(configFile)) {
            Map<String, Object> result = yaml.load(input);
            return result != null ? result : new HashMap<>();
        }
    }

    @SuppressWarnings("unchecked")
    private static Map<String, ModCompatibilityRule> parseRules(Map<String, Object> yamlData) {
        Map<String, ModCompatibilityRule> rules = new HashMap<>();
        Object conflictsObj = yamlData.get("known-conflicts");

        if (!(conflictsObj instanceof List)) {
            return rules;
        }

        List<Map<String, Object>> conflictList = (List<Map<String, Object>>) conflictsObj;
        for (Map<String, Object> conflict : conflictList) {
            String modId = (String) conflict.get("mod-id");
            if (modId == null || modId.isEmpty()) {
                continue;
            }

            String modName = (String) conflict.getOrDefault("mod-name", modId);
            String statusStr = (String) conflict.getOrDefault("status", "NEEDS_INVESTIGATION");
            Status status = parseStatus(statusStr);
            String notes = (String) conflict.getOrDefault("notes", "");

            Set<String> disabledMixins = new HashSet<>();
            Object disabledObj = conflict.get("disabled-mixins");
            if (disabledObj instanceof List) {
                for (Object item : (List<?>) disabledObj) {
                    if (item != null) {
                        disabledMixins.add(item.toString());
                    }
                }
            }

            Map<String, Integer> priorityOverrides = new HashMap<>();
            Object priorityObj = conflict.get("priority-overrides");
            if (priorityObj instanceof Map) {
                for (Map.Entry<?, ?> entry : ((Map<?, ?>) priorityObj).entrySet()) {
                    if (entry.getKey() instanceof String && entry.getValue() instanceof Number) {
                        priorityOverrides.put((String) entry.getKey(), ((Number) entry.getValue()).intValue());
                    }
                }
            }

            ModCompatibilityRule rule = new ModCompatibilityRule(
                    modId, modName, disabledMixins, priorityOverrides, notes, status
            );
            rules.put(modId, rule);
        }

        return rules;
    }

    private static Status parseStatus(String statusStr) {
        try {
            return Status.valueOf(statusStr.toUpperCase());
        } catch (IllegalArgumentException | NullPointerException e) {
            return Status.NEEDS_INVESTIGATION;
        }
    }

    private static boolean getBoolean(Map<String, Object> map, String key, boolean defaultValue) {
        Object value = map.get(key);
        if (value instanceof Boolean) {
            return (Boolean) value;
        }
        if (value instanceof String) {
            return Boolean.parseBoolean((String) value);
        }
        return defaultValue;
    }

    public Optional<ModCompatibilityRule> getRuleForMod(String modId) {
        return Optional.ofNullable(rulesByModId.get(modId));
    }

    public Map<String, ModCompatibilityRule> getAllRules() {
        return rulesByModId;
    }

    public boolean isModLoaded(String modId) {
        return FabricLoader.getInstance().isModLoaded(modId);
    }

    public Set<String> getLoadedKnownMods() {
        Set<String> loaded = new HashSet<>();
        for (String modId : rulesByModId.keySet()) {
            if (isModLoaded(modId)) {
                loaded.add(modId);
            }
        }
        return Collections.unmodifiableSet(loaded);
    }

    public boolean isAutoConflictResolutionEnabled() {
        return autoConflictResolution;
    }

    public void generateStartupReport() {
        Set<String> loadedKnownMods = getLoadedKnownMods();

        if (loadedKnownMods.isEmpty()) {
            System.out.println("[Cardboard] Mod compatibility database loaded. No known mods detected.");
            return;
        }

        System.out.println("[Cardboard] Mod compatibility database loaded. " + loadedKnownMods.size() + " known mod(s) detected:");
        for (String modId : loadedKnownMods) {
            ModCompatibilityRule rule = rulesByModId.get(modId);
            String modName = rule.getModName();
            Status status = rule.getStatus();

            Optional<ModContainer> container = FabricLoader.getInstance().getModContainer(modId);
            String version = container.map(c -> c.getMetadata().getVersion().getFriendlyString()).orElse("unknown");

            String statusIcon = switch (status) {
                case COMPATIBLE -> "[OK]";
                case CONFLICT_RESOLVED -> "[FIXED]";
                case NEEDS_INVESTIGATION -> "[?]";
            };

            System.out.println("  " + statusIcon + " " + modName + " (v" + version + ") - " + rule.getNotes());

            if (!rule.getDisabledMixins().isEmpty()) {
                System.out.println("      Disabled mixins: " + String.join(", ", rule.getDisabledMixins()));
            }
        }
    }
}
