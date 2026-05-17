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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MappingBridgeUnitTest {

    private MappingBridge bridge;

    @BeforeEach
    void setUp() {
        bridge = new MappingBridge();
    }

    @Test
    void testToFallbackWhenNoFabricRuntime() {
        String result = bridge.toNamed("net.minecraft.class_1234");

        assertNotNull(result, "Should return a fallback value");
        assertTrue(result.startsWith("intermediary(unresolved):"),
                "Should use unresolved fallback format");
        assertTrue(result.contains("net.minecraft.class_1234"),
                "Should contain the original class name");
    }

    @Test
    void testToIntermediaryFallbackWhenNoFabricRuntime() {
        String result = bridge.toIntermediary("net.minecraft.server.level.ServerLevel");

        assertNotNull(result, "Should return a fallback value");
        assertTrue(result.startsWith("intermediary(unresolved):") || !result.isEmpty(),
                "Should return a value (fallback or unchanged)");
    }

    @Test
    void testNormalizeClassName_intermediaryFormat() {
        String result = bridge.normalizeClassName("net.minecraft.class_1234");

        assertNotNull(result);
        // With no Fabric runtime, normalizeClassName should return unresolved format
        // or at least not crash
        assertFalse(result.isEmpty());
    }

    @Test
    void testNormalizeClassName_alreadyNamedFormat() {
        String result = bridge.normalizeClassName("net.minecraft.server.level.ServerLevel");

        assertNotNull(result);
        assertFalse(result.isEmpty());
    }

    @Test
    void testCacheStatsInitiallyZero() {
        String stats = bridge.getCacheStats();
        assertNotNull(stats);
        assertTrue(stats.contains("hit=0"), "Initial stats should show 0 hits");
    }

    @Test
    void testCacheStatsAfterCalls() {
        bridge.toNamed("net.minecraft.class_1111");
        bridge.toNamed("net.minecraft.class_1111");

        String stats = bridge.getCacheStats();
        assertNotNull(stats);
        assertTrue(stats.contains("hit=") && stats.contains("miss="), "Stats should show hit and miss counts");
    }

    @Test
    void testClearCache() {
        bridge.toNamed("net.minecraft.class_1111");
        bridge.clearCache();

        String stats = bridge.getCacheStats();
        assertTrue(stats.contains("hit=0") && stats.contains("miss=0"),
                "After clear, stats should show 0");
    }
}
