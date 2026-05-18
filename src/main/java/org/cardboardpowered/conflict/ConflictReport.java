/**
 * Cardboard - Spigot/Paper for Fabric
 * Copyright (C) 2020-2024 CardboardPowered.org and contributors
 * Copyright (C) 2025-2026 SharkMI and contributors
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.cardboardpowered.conflict;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cardboardpowered.conflict.model.ConflictLevel;
import org.cardboardpowered.conflict.model.MixinConflict;
import org.cardboardpowered.conflict.report.ConflictReportEntry;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Generates formatted conflict reports for console and JSON output.
 */
public class ConflictReport {

    private static final Logger LOGGER = LogManager.getLogger("Cardboard-ConflictReport");
    private static final String PREFIX = "[Cardboard] ";
    private static final int MAX_LINE_LENGTH = 75;
    private static final String INDENT = "      ";

    private final List<MixinConflict> allConflicts;
    private final int cardboardMixinCount;
    private final int otherModCount;
    private final long scanDurationMs;

    private List<MixinConflict> fatalConflicts = new ArrayList<>();
    private List<MixinConflict> highConflicts = new ArrayList<>();
    private List<MixinConflict> mediumConflicts = new ArrayList<>();
    private List<MixinConflict> lowConflicts = new ArrayList<>();
    private List<MixinConflict> resolvedConflicts = new ArrayList<>();

    public ConflictReport(List<MixinConflict> conflicts, int cardboardMixinCount,
                          int otherModCount, long scanDurationMs) {
        this.allConflicts = conflicts;
        this.cardboardMixinCount = cardboardMixinCount;
        this.otherModCount = otherModCount;
        this.scanDurationMs = scanDurationMs;
        groupConflicts();
    }

    private void groupConflicts() {
        for (MixinConflict c : allConflicts) {
            if (c.isResolved()) {
                resolvedConflicts.add(c);
            } else {
                if (c.getLevel() == ConflictLevel.FATAL) {
                    fatalConflicts.add(c);
                } else if (c.getLevel() == ConflictLevel.HIGH) {
                    highConflicts.add(c);
                } else if (c.getLevel() == ConflictLevel.MEDIUM) {
                    mediumConflicts.add(c);
                } else if (c.getLevel() == ConflictLevel.LOW) {
                    lowConflicts.add(c);
                }
            }
        }
    }

    /**
     * Print formatted report to console via SLF4J Logger.
     */
    public void printConsole() {
        LOGGER.info("{}", buildConsoleReport());
    }

    /**
     * Build the full console report string.
     */
    String buildConsoleReport() {
        StringBuilder sb = new StringBuilder();

        // Header
        sb.append("Mixin Conflict Detection Report\n");
        sb.append("Scanned ").append(cardboardMixinCount)
                .append(" Cardboard mixins, ")
                .append(otherModCount).append(" other mods with mixins\n");

        int unresolvedCount = allConflicts.size() - resolvedConflicts.size();
        if (unresolvedCount == 0 && resolvedConflicts.isEmpty()) {
            sb.append("No conflicts detected\n");
            sb.append("Scan completed in ").append(formatDuration(scanDurationMs));
            return sb.toString();
        }

        sb.append("\n");

        // Unresolved conflicts by level
        appendLevelSection(sb, "FATAL", fatalConflicts);
        appendLevelSection(sb, "HIGH", highConflicts);
        appendLevelSection(sb, "MEDIUM", mediumConflicts);
        appendLevelSection(sb, "LOW", lowConflicts);

        // Resolved conflicts
        if (!resolvedConflicts.isEmpty()) {
            sb.append("Resolved conflicts (").append(resolvedConflicts.size()).append("):\n");
            for (int i = 0; i < resolvedConflicts.size(); i++) {
                appendConflict(sb, resolvedConflicts.get(i), i + 1, true);
            }
            sb.append("\n");
        }

        // Summary
        sb.append("Summary: ")
                .append(fatalConflicts.size()).append(" FATAL, ")
                .append(highConflicts.size()).append(" HIGH, ")
                .append(mediumConflicts.size()).append(" MEDIUM, ")
                .append(lowConflicts.size()).append(" LOW");
        if (!resolvedConflicts.isEmpty()) {
            sb.append(", ").append(resolvedConflicts.size()).append(" resolved");
        }
        sb.append("\n");
        sb.append("Scan completed in ").append(formatDuration(scanDurationMs));

        return sb.toString();
    }

    private void appendLevelSection(StringBuilder sb, String level, List<MixinConflict> conflicts) {
        sb.append(level).append(" conflicts (").append(conflicts.size()).append("):\n");
        if (conflicts.isEmpty()) {
            sb.append(INDENT).append("None\n");
        } else {
            for (int i = 0; i < conflicts.size(); i++) {
                appendConflict(sb, conflicts.get(i), i + 1, false);
            }
        }
        sb.append("\n");
    }

    private void appendConflict(StringBuilder sb, MixinConflict conflict, int index, boolean isResolved) {
        String target = conflict.getTargetClass() + "#" + conflict.getTargetMethod();
        sb.append("  [").append(index).append("] Target: ").append(target).append("\n");

        String cbInfo = formatMixinInfo(conflict.getCardboardMixinClass(), conflict.getCardboardMethod());
        appendWrappedLine(sb, INDENT + "Cardboard: " + cbInfo);

        String otherInfo = formatMixinInfo(conflict.getOtherMixinClass(), conflict.getOtherMethod());
        String otherPrefix = INDENT + "Other";
        if (conflict.getOtherModId() != null) {
            otherPrefix += " (" + conflict.getOtherModId() + ")";
        }
        appendWrappedLine(sb, otherPrefix + ": " + otherInfo);

        String note = isResolved ? "Resolved: " + conflict.getResolutionNote() : conflict.getSuggestion();
        String label = isResolved ? INDENT + "Note:" : INDENT + "Suggestion:";
        appendWrappedLine(sb, label + " " + note);

        sb.append("\n");
    }

    private String formatMixinInfo(String mixinClass, org.cardboardpowered.conflict.model.MixinMethod method) {
        if (mixinClass == null) {
            return "unknown";
        }
        String shortName = mixinClass;
        int lastDot = mixinClass.lastIndexOf('.');
        if (lastDot > 0) {
            shortName = mixinClass.substring(lastDot + 1);
        }

        StringBuilder sb = new StringBuilder(shortName);
        if (method != null) {
            sb.append(" @").append(method.getAnnotationType());
            sb.append(" [priority=").append(method.getPriority()).append("]");
        }
        return sb.toString();
    }

    /**
     * Append a line with wrapping if it exceeds MAX_LINE_LENGTH.
     */
    private void appendWrappedLine(StringBuilder sb, String line) {
        if (line.length() <= MAX_LINE_LENGTH) {
            sb.append(line).append("\n");
            return;
        }

        // Find a good break point near MAX_LINE_LENGTH
        int breakPoint = MAX_LINE_LENGTH;
        while (breakPoint > MAX_LINE_LENGTH - 20 && breakPoint > 0) {
            if (line.charAt(breakPoint - 1) == ' ') {
                break;
            }
            breakPoint--;
        }
        if (breakPoint <= MAX_LINE_LENGTH - 20) {
            breakPoint = MAX_LINE_LENGTH;
        }

        sb.append(line.substring(0, breakPoint).trim()).append("\n");
        String remaining = line.substring(breakPoint).trim();
        while (remaining.length() > MAX_LINE_LENGTH) {
            int nextBreak = MAX_LINE_LENGTH - INDENT.length();
            while (nextBreak > nextBreak - 20 && nextBreak > 0) {
                if (remaining.charAt(nextBreak - 1) == ' ') {
                    break;
                }
                nextBreak--;
            }
            if (nextBreak <= MAX_LINE_LENGTH - INDENT.length() - 20) {
                nextBreak = MAX_LINE_LENGTH - INDENT.length();
            }
            sb.append(INDENT).append(remaining.substring(0, nextBreak).trim()).append("\n");
            remaining = remaining.substring(nextBreak).trim();
        }
        if (!remaining.isEmpty()) {
            sb.append(INDENT).append(remaining).append("\n");
        }
    }

    /**
     * Write JSON report to config/cardboard/conflict-report.json.
     */
    public void writeJson() {
        try {
            String json = buildJsonReport();
            Path configDir = FabricLoader.getInstance().getConfigDir().resolve("cardboard");
            Files.createDirectories(configDir);
            Path outputFile = configDir.resolve("conflict-report.json");
            Files.writeString(outputFile, json, StandardCharsets.UTF_8);
            LOGGER.info("Conflict report written to {}", outputFile.toAbsolutePath());
            LOGGER.debug("Report size: {} bytes, {} conflicts", json.length(), allConflicts.size());
        } catch (IOException e) {
            LOGGER.error("Failed to write conflict report JSON to config/cardboard/conflict-report.json: {}", e.getMessage(), e);
            LOGGER.warn("Conflict report JSON generation failed, but server can continue. Check disk space and permissions.");
        }
    }

    /**
     * Build JSON report string using Gson.
     */
    String buildJsonReport() {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        JsonReport report = new JsonReport();
        report.timestamp = Instant.now().toString();
        report.scanDurationMs = scanDurationMs;
        report.cardboardMixinCount = cardboardMixinCount;
        report.otherModCount = otherModCount;

        report.conflicts = new JsonConflicts();
        report.conflicts.fatal = allConflicts.stream()
                .filter(c -> c.getLevel() == ConflictLevel.FATAL)
                .map(ConflictReportEntry::fromConflict)
                .collect(Collectors.toList());
        report.conflicts.high = allConflicts.stream()
                .filter(c -> c.getLevel() == ConflictLevel.HIGH)
                .map(ConflictReportEntry::fromConflict)
                .collect(Collectors.toList());
        report.conflicts.medium = allConflicts.stream()
                .filter(c -> c.getLevel() == ConflictLevel.MEDIUM)
                .map(ConflictReportEntry::fromConflict)
                .collect(Collectors.toList());
        report.conflicts.low = allConflicts.stream()
                .filter(c -> c.getLevel() == ConflictLevel.LOW)
                .map(ConflictReportEntry::fromConflict)
                .collect(Collectors.toList());
        report.conflicts.resolved = allConflicts.stream()
                .filter(c -> c.isResolved())
                .map(ConflictReportEntry::fromConflict)
                .collect(Collectors.toList());

        return gson.toJson(report);
    }

    /**
     * Get the count of unresolved conflicts.
     */
    public int getUnresolvedCount() {
        return allConflicts.size() - resolvedConflicts.size();
    }

    /**
     * Get all conflicts (resolved and unresolved).
     */
    public List<MixinConflict> getAllConflicts() {
        return allConflicts;
    }

    /**
     * Get only unresolved conflicts.
     */
    public List<MixinConflict> getUnresolvedConflicts() {
        return allConflicts.stream()
                .filter(c -> !c.isResolved())
                .collect(Collectors.toList());
    }

    private String formatDuration(long ms) {
        if (ms < 1000) {
            return ms + "ms";
        }
        return String.format("%.1fs", ms / 1000.0);
    }

    /**
     * Top-level JSON report structure.
     */
    private static class JsonReport {
        String timestamp;
        long scanDurationMs;
        int cardboardMixinCount;
        int otherModCount;
        JsonConflicts conflicts;
    }

    /**
     * Grouped conflicts by level.
     */
    private static class JsonConflicts {
        List<ConflictReportEntry> fatal = new ArrayList<>();
        List<ConflictReportEntry> high = new ArrayList<>();
        List<ConflictReportEntry> medium = new ArrayList<>();
        List<ConflictReportEntry> low = new ArrayList<>();
        List<ConflictReportEntry> resolved = new ArrayList<>();
    }
}
