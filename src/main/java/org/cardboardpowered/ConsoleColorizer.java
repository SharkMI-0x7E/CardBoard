/**
 * Cardboard - Spigot/Paper for Fabric
 * Copyright (C) 2020-2026 CardboardPowered.org and contributors
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
package org.cardboardpowered;

import java.util.Map;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.ConsoleAppender;
import org.apache.logging.log4j.core.config.AppenderRef;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.apache.logging.log4j.core.layout.PatternLayout;

/**
 * Gives the {@code Cardboard} logger its own colored console line so Cardboard's
 * output stands out from other mods.
 *
 * <p>The color is applied to a dedicated console appender only. The log file
 * appender keeps the plain Minecraft pattern, so {@code logs/latest.log} stays
 * free of ANSI escape codes. Cardboard's logger is made non-additive so its
 * lines are not printed twice, and the file appender(s) are re-attached
 * explicitly so file logging keeps working.
 *
 * <p>If better-fabric-console is present, this does nothing - that mod owns the
 * console look and Cardboard steps aside.
 */
public final class ConsoleColorizer {

    /**
     * Multi-color console pattern: dim-gray brackets, green timestamp, cyan
     * thread, bright-yellow level, and the message in bright white.
     */
    private static final String DEFAULT_PATTERN =
            "\u001B[90m[\u001B[32m%d{HH:mm:ss}\u001B[90m] "
          + "[\u001B[36m%t\u001B[90m/\u001B[93m%level\u001B[90m]: "
          + "\u001B[97m%msg{nolookups}\u001B[0m%n";

    private ConsoleColorizer() {
    }

    /**
     * Resolve the console pattern: the built-in multi-color layout unless the
     * user set {@code console-color-pattern} in cardboard-config.yml to a custom
     * log4j2 pattern (log4j2's own {@code %highlight}/{@code %style} work too).
     */
    private static String pattern() {
        String configured = CardboardConfig.consoleColorPattern;
        if (configured == null || configured.isBlank() || "default".equalsIgnoreCase(configured)) {
            return DEFAULT_PATTERN;
        }
        return configured;
    }

    public static void installCardboardLoggerColor() {
        if (CardboardConfig.isBetterConsole() || !CardboardConfig.coloredConsole) {
            return;
        }
        try {
            LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
            Configuration cfg = ctx.getConfiguration();

            PatternLayout layout = PatternLayout.newBuilder()
                    .withConfiguration(cfg)
                    .withPattern(pattern())
                    .build();

            ConsoleAppender colored = ConsoleAppender.newBuilder()
                    .setName("CardboardConsole")
                    .setConfiguration(cfg)
                    .setTarget(ConsoleAppender.Target.SYSTEM_OUT)
                    .setLayout(layout)
                    .build();
            colored.start();
            cfg.addAppender(colored);

            LoggerConfig logger = LoggerConfig.createLogger(
                    false, Level.ALL, "Cardboard", null,
                    new AppenderRef[0], null, cfg, null);
            logger.addAppender(colored, Level.ALL, null);

            // additivity=false cuts the logger off from root's appenders, so
            // re-attach every non-console appender (file / rolling / async ...)
            // to keep log files working. Console is handled by our colored appender.
            for (Map.Entry<String, Appender> entry : cfg.getAppenders().entrySet()) {
                Appender appender = entry.getValue();
                if (appender instanceof ConsoleAppender) {
                    continue;
                }
                logger.addAppender(appender, Level.ALL, null);
            }

            cfg.addLogger("Cardboard", logger);
            ctx.updateLoggers(cfg);
        } catch (Throwable t) {
            // Console cosmetics must never break server startup.
            System.err.println("[Cardboard] console colorizer skipped: " + t);
        }
    }

}
