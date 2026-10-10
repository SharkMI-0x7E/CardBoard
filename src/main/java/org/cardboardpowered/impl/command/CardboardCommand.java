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
package org.cardboardpowered.impl.command;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.StringUtil;
import org.cardboardpowered.CardboardConfig;
import org.cardboardpowered.compat.ModCompatibilityDatabase;
import org.cardboardpowered.compat.ModCompatibilityRule;
import org.cardboardpowered.mixin.CardboardMixinPlugin;
import org.cardboardpowered.util.Messages;
import org.cardboardpowered.util.Messages.Lang;

import com.google.common.collect.ImmutableList;

import net.fabricmc.loader.api.FabricLoader;

/**
 * The {@code /cardboard} command (alias {@code /cb}): the single entry point for
 * Cardboard's own subcommands.
 *
 * <p>Subcommands that already have a standalone command ({@code version},
 * {@code mods}, {@code plugins}) are delegated to that command object, so there
 * is exactly one implementation shared with {@code /version} and {@code /plugins}.
 * The rest are handled here. All user-facing text goes through {@link Messages}.
 */
public class CardboardCommand extends Command {

    private static final List<String> SUBS = ImmutableList.of(
            "help", "version", "about", "tps", "worlds", "mods", "plugins",
            "debug", "log", "doctor", "dump", "compat", "reload", "lang");

    /** Levels the {@code /cardboard log} subcommand accepts. */
    private static final List<String> LOG_LEVELS = ImmutableList.of(
            "trace", "debug", "info", "warn", "error", "fatal", "all", "off");

    /** {@code --add-opens} flags Cardboard's docs recommend (README § Java 21 Flags). */
    private static final List<String> RECOMMENDED_ADD_OPENS = ImmutableList.of(
            "java.base/java.lang", "java.base/java.lang.invoke", "java.base/java.lang.reflect",
            "java.base/java.io", "java.base/java.net", "java.base/java.nio",
            "java.base/java.util", "java.base/java.util.concurrent", "java.base/java.util.jar",
            "java.base/java.util.zip", "java.base/java.text", "java.base/sun.nio.ch",
            "java.base/sun.security.x509", "java.rmi/sun.rmi.transport");

    private final VersionCommand version = new VersionCommand("version");
    private final ModsCommand mods = new ModsCommand("fabricmods");
    private final CardboardPluginsCommand plugins = new CardboardPluginsCommand("plugins");

    /** Runtime debug toggles, mapped to their config keys and backing fields. */
    private enum DebugFlag {
        VERBOSE("verbose", "debug_print_all_calls"),
        EVENTS("events", "debug_print_event_call"),
        PLAYER("player", "debug_player"),
        OTHER("other", "debug_other"),
        REMAP("remap", "debug_print_remaputil"),
        WORLDEDIT("worldedit", "debug_print_remap_for_worldedit");

        final String name;
        final String configKey;

        DebugFlag(String name, String configKey) {
            this.name = name;
            this.configKey = configKey;
        }

        boolean get() {
            switch (this) {
                case VERBOSE: return CardboardConfig.DEBUG_VERBOSE_CALLS;
                case EVENTS: return CardboardConfig.DEBUG_EVENT_CALL;
                case PLAYER: return CardboardConfig.DEBUG_PLAYER;
                case OTHER: return CardboardConfig.DEBUG_OTHER;
                case REMAP: return CardboardConfig.DEBUG_LOG_REMAP;
                default: return CardboardConfig.DEBUG_REMAP_WE;
            }
        }

        void set(boolean value) {
            switch (this) {
                case VERBOSE: CardboardConfig.DEBUG_VERBOSE_CALLS = value; break;
                case EVENTS: CardboardConfig.DEBUG_EVENT_CALL = value; break;
                case PLAYER: CardboardConfig.DEBUG_PLAYER = value; break;
                case OTHER: CardboardConfig.DEBUG_OTHER = value; break;
                case REMAP: CardboardConfig.DEBUG_LOG_REMAP = value; break;
                default: CardboardConfig.DEBUG_REMAP_WE = value; break;
            }
        }

        static DebugFlag byName(String name) {
            for (DebugFlag flag : values()) {
                if (flag.name.equalsIgnoreCase(name)) {
                    return flag;
                }
            }
            return null;
        }
    }

    public CardboardCommand() {
        super("cardboard");

        this.description = "Cardboard main command";
        this.usageMessage = "/cardboard <" + String.join("|", SUBS) + ">";
        this.setAliases(Arrays.asList("cb"));
        this.setPermission("cardboard.command.admin");
    }

    @Override
    public boolean execute(CommandSender sender, String alias, String[] args) {
        if (!sender.hasPermission("cardboard.command.admin")) {
            sender.sendMessage(Messages.get(sender, "cmd.no-permission"));
            return true;
        }
        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }

        String sub = args[0].toLowerCase(Locale.ROOT);
        String[] rest = Arrays.copyOfRange(args, 1, args.length);
        switch (sub) {
            case "help":
                sendHelp(sender);
                return true;
            case "version":
                return version.execute(sender, "version", rest);
            case "about":
                return about(sender);
            case "tps":
                return tps(sender);
            case "worlds":
                return worlds(sender);
            case "mods":
                return mods.execute(sender, "mods", rest);
            case "plugins":
                return plugins.execute(sender, "plugins", rest);
            case "debug":
                return debug(sender, rest);
            case "log":
                return log(sender, rest);
            case "doctor":
                return doctor(sender);
            case "dump":
                return dump(sender);
            case "compat":
                return compat(sender);
            case "reload":
                return reload(sender);
            case "lang":
                return lang(sender, rest);
            default:
                sender.sendMessage(Messages.get(sender, "cmd.unknown", args[0]));
                return true;
        }
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage(Messages.get(sender, "cmd.help.title"));
        sender.sendMessage(Messages.get(sender, "cmd.help.usage", "/cardboard <" + String.join("|", SUBS) + ">"));
        for (String sub : SUBS) {
            sender.sendMessage(Messages.get(sender, "cmd.help.line", usageOf(sub), Messages.get(sender, "cmd.desc." + sub)));
        }
    }

    private static String usageOf(String sub) {
        switch (sub) {
            case "help": return "/cardboard help";
            case "version": return "/cardboard version";
            case "about": return "/cardboard about";
            case "tps": return "/cardboard tps";
            case "worlds": return "/cardboard worlds";
            case "mods": return "/cardboard mods";
            case "plugins": return "/cardboard plugins [info|enable|disable <name>]";
            case "debug": return "/cardboard debug [list|<flag>|all on|off]";
            case "log": return "/cardboard log <" + String.join("|", LOG_LEVELS) + ">";
            case "doctor": return "/cardboard doctor";
            case "dump": return "/cardboard dump";
            case "compat": return "/cardboard compat";
            case "reload": return "/cardboard reload";
            default: return "/cardboard lang <zh|en>";
        }
    }

    // ------------------------------------------------------------------ tps

    private boolean tps(CommandSender sender) {
        Runtime rt = Runtime.getRuntime();
        long used = (rt.totalMemory() - rt.freeMemory()) / 1048576L;
        long max = rt.maxMemory() / 1048576L;
        double mspt = Bukkit.getServer().getAverageTickTime();
        double[] tps = Bukkit.getServer().getTPS();
        long uptime = ManagementFactory.getRuntimeMXBean().getUptime();

        sender.sendMessage(Messages.get(sender, "tps.title"));
        sender.sendMessage(Messages.get(sender, "tps.line", fmt(tps.length > 0 ? tps[0] : 20.0), fmt(mspt)));
        sender.sendMessage(Messages.get(sender, "tps.memory", used, max));
        sender.sendMessage(Messages.get(sender, "tps.uptime", formatUptime(uptime)));
        return true;
    }

    private static String fmt(double v) {
        return String.format(Locale.ROOT, "%.2f", v);
    }

    private static String formatUptime(long millis) {
        long s = millis / 1000L;
        long d = s / 86400L;
        long h = (s % 86400L) / 3600L;
        long m = (s % 3600L) / 60L;
        if (d > 0) return d + "d " + h + "h " + m + "m";
        if (h > 0) return h + "h " + m + "m";
        return m + "m";
    }

    // ---------------------------------------------------------------- debug

    private boolean debug(CommandSender sender, String[] args) {
        if (args.length == 0 || args[0].equalsIgnoreCase("list")) {
            debugList(sender);
            return true;
        }

        String first = args[0].toLowerCase(Locale.ROOT);
        if (first.equals("all")) {
            boolean on = args.length < 2 || !args[1].equalsIgnoreCase("off");
            CardboardConfig.DEBUG_MODE = on;
            for (DebugFlag flag : DebugFlag.values()) {
                flag.set(on);
            }
            sender.sendMessage(Messages.get(sender, on ? "debug.all-on" : "debug.all-off"));
            sender.sendMessage(Messages.get(sender, "debug.reload-hint"));
            return true;
        }

        DebugFlag flag = DebugFlag.byName(first);
        if (flag == null) {
            sender.sendMessage(Messages.get(sender, "debug.usage"));
            return true;
        }

        boolean value = !flag.get();
        flag.set(value);
        sender.sendMessage(Messages.get(sender, "debug.toggled", flag.name, stateText(sender, value)));
        if (value && !CardboardConfig.DEBUG_MODE) {
            CardboardConfig.DEBUG_MODE = true;
            sender.sendMessage(Messages.get(sender, "debug.master-auto"));
        }
        sender.sendMessage(Messages.get(sender, "debug.reload-hint"));
        return true;
    }

    private void debugList(CommandSender sender) {
        sender.sendMessage(Messages.get(sender, "debug.list.title"));
        for (DebugFlag flag : DebugFlag.values()) {
            sender.sendMessage(Messages.get(sender, "debug.list.line",
                    flag.name, stateText(sender, flag.get()), flag.configKey));
        }
        sender.sendMessage(Messages.get(sender, "debug.list.master",
                stateText(sender, CardboardConfig.DEBUG_MODE)));
        sender.sendMessage(Messages.get(sender, "debug.reload-hint"));
    }

    private String stateText(CommandSender sender, boolean on) {
        return Messages.get(sender, on ? "debug.on" : "debug.off");
    }

    // ------------------------------------------------------------------ log

    private boolean log(CommandSender sender, String[] args) {
        LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
        Configuration conf = ctx.getConfiguration();
        LoggerConfig root = conf.getLoggerConfig(LogManager.ROOT_LOGGER_NAME);

        if (args.length == 0) {
            sender.sendMessage(Messages.get(sender, "log.current", String.valueOf(root.getLevel())));
            return true;
        }

        Level level = Level.getLevel(args[0].toUpperCase(Locale.ROOT));
        if (level == null) {
            sender.sendMessage(Messages.get(sender, "log.unknown", args[0]));
            return true;
        }

        root.setLevel(level);
        ctx.updateLoggers(conf);
        sender.sendMessage(Messages.get(sender, "log.set", level.name()));
        if (level == Level.TRACE || level == Level.DEBUG || level == Level.ALL) {
            sender.sendMessage(Messages.get(sender, "log.verbose-warn"));
        }
        return true;
    }

    // --------------------------------------------------------------- doctor

    private boolean doctor(CommandSender sender) {
        sender.sendMessage(Messages.get(sender, "doctor.title"));

        // Java version (MC 1.21.11 needs Java 21+).
        int feature = Runtime.version().feature();
        sender.sendMessage(check(sender, feature >= 21,
                Messages.get(sender, "doctor.java", System.getProperty("java.version"), 21)));

        // Memory.
        Runtime rt = Runtime.getRuntime();
        long used = (rt.totalMemory() - rt.freeMemory()) / 1048576L;
        long max = rt.maxMemory() / 1048576L;
        sender.sendMessage(check(sender, max <= 0 || used < max * 9 / 10,
                Messages.get(sender, "doctor.memory", used, max)));

        // --add-opens coverage.
        java.util.Set<String> opened = parseAddOpens(ManagementFactory.getRuntimeMXBean().getInputArguments());
        List<String> missing = new ArrayList<>();
        for (String required : RECOMMENDED_ADD_OPENS) {
            if (!opened.contains(required)) {
                missing.add(required);
            }
        }
        sender.sendMessage(check(sender, missing.isEmpty(),
                Messages.get(sender, "doctor.addopens", RECOMMENDED_ADD_OPENS.size() - missing.size(), RECOMMENDED_ADD_OPENS.size())));
        if (!missing.isEmpty()) {
            sender.sendMessage(Messages.get(sender, "doctor.addopens.missing", String.join(", ", missing)));
        }

        // Plugins.
        Plugin[] plugins = Bukkit.getPluginManager().getPlugins();
        int disabled = 0;
        for (Plugin plugin : plugins) {
            if (!plugin.isEnabled()) {
                disabled++;
            }
        }
        sender.sendMessage(check(sender, disabled == 0,
                Messages.get(sender, "doctor.plugins", plugins.length - disabled, plugins.length)));

        // Mixin conflicts (last scan only).
        int conflictCount = CardboardMixinPlugin.getScanResults().size();
        sender.sendMessage(check(sender, conflictCount == 0,
                conflictCount == 0
                        ? Messages.get(sender, "doctor.conflicts.none")
                        : Messages.get(sender, "doctor.conflicts.some", conflictCount)));
        sender.sendMessage(Messages.get(sender, "doctor.conflicts.hint"));
        return true;
    }

    /** Prefix a doctor line with a colored status marker. */
    private static String check(CommandSender sender, boolean ok, String message) {
        return Messages.get(sender, ok ? "doctor.ok" : "doctor.warn") + " " + message;
    }

    /**
     * Collect the {@code module/package} targets of every {@code --add-opens}
     * flag, handling both {@code --add-opens=x/y=...} and the space-separated
     * form. Package segments are compared exactly (a plain {@code contains}
     * would let {@code java.base/java.lang.invoke} masquerade as
     * {@code java.base/java.lang}).
     */
    private static java.util.Set<String> parseAddOpens(List<String> args) {
        java.util.Set<String> opened = new java.util.HashSet<>();
        for (int i = 0; i < args.size(); i++) {
            String arg = args.get(i);
            String spec = null;
            if (arg.startsWith("--add-opens=")) {
                spec = arg.substring("--add-opens=".length());
            } else if (arg.equals("--add-opens") && i + 1 < args.size()) {
                spec = args.get(i + 1);
            }
            if (spec != null) {
                int eq = spec.indexOf('=');
                if (eq > 0) {
                    opened.add(spec.substring(0, eq));
                }
            }
        }
        return opened;
    }

    // ----------------------------------------------------------------- dump

    private boolean dump(CommandSender sender) {
        String stamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss"));
        Path out = Path.of("cardboard-dump-" + stamp + ".txt");
        try {
            Files.writeString(out, buildDump(), StandardCharsets.UTF_8);
            sender.sendMessage(Messages.get(sender, "dump.done", out.toAbsolutePath().toString()));
        } catch (IOException e) {
            sender.sendMessage(Messages.get(sender, "dump.failed", e.toString()));
        }
        return true;
    }

    private String buildDump() {
        StringBuilder sb = new StringBuilder();
        sb.append("# Cardboard diagnostic dump\n");
        sb.append("# generated: ").append(LocalDateTime.now()).append("\n\n");

        Runtime rt = Runtime.getRuntime();
        sb.append("## Environment\n");
        sb.append("java: ").append(System.getProperty("java.version")).append('\n');
        sb.append("os: ").append(System.getProperty("os.name")).append(' ')
                .append(System.getProperty("os.version")).append('\n');
        sb.append("memory: ").append((rt.totalMemory() - rt.freeMemory()) / 1048576L)
                .append(" / ").append(rt.maxMemory() / 1048576L).append(" MB\n");
        sb.append("uptime: ").append(formatUptime(ManagementFactory.getRuntimeMXBean().getUptime())).append('\n');
        sb.append("jvm args: ").append(String.join(" ", ManagementFactory.getRuntimeMXBean().getInputArguments())).append("\n\n");

        sb.append("## Plugins\n");
        for (Plugin plugin : Bukkit.getPluginManager().getPlugins()) {
            sb.append(plugin.isEnabled() ? "[enabled]  " : "[disabled] ")
                    .append(plugin.getName()).append(' ').append(plugin.getDescription().getVersion()).append('\n');
        }
        sb.append('\n');

        sb.append("## Threads\n");
        Map<Thread, StackTraceElement[]> traces = Thread.getAllStackTraces();
        int nonDaemon = 0;
        for (Thread thread : traces.keySet()) {
            if (!thread.isDaemon()) {
                nonDaemon++;
            }
        }
        sb.append("total threads: ").append(traces.size())
                .append(", non-daemon (can block JVM exit): ").append(nonDaemon).append('\n');
        for (Map.Entry<Thread, StackTraceElement[]> entry : traces.entrySet()) {
            Thread thread = entry.getKey();
            sb.append("\n\"").append(thread.getName()).append("\" id=").append(thread.getId())
                    .append(thread.isDaemon() ? " daemon" : " NON-DAEMON")
                    .append(" state=").append(thread.getState()).append('\n');
            for (StackTraceElement frame : entry.getValue()) {
                sb.append("    at ").append(frame).append('\n');
            }
        }
        return sb.toString();
    }

    // --------------------------------------------------------------- compat

    private boolean compat(CommandSender sender) {
        Map<String, ModCompatibilityRule> rules = ModCompatibilityDatabase.load().getAllRules();
        sender.sendMessage(Messages.get(sender, "compat.title", rules.size()));
        if (rules.isEmpty()) {
            sender.sendMessage(Messages.get(sender, "compat.empty"));
            return true;
        }
        for (ModCompatibilityRule rule : rules.values()) {
            sender.sendMessage(Messages.get(sender, "compat.line",
                    rule.getModId(), Messages.get(sender, statusKey(rule.getStatus())), rule.getNotes()));
        }
        return true;
    }

    private static String statusKey(ModCompatibilityRule.Status status) {
        switch (status) {
            case COMPATIBLE: return "compat.status.compatible";
            case CONFLICT_RESOLVED: return "compat.status.resolved";
            default: return "compat.status.investigation";
        }
    }

    // ---------------------------------------------------------------- about

    private boolean about(CommandSender sender) {
        String cbVersion = FabricLoader.getInstance().getModContainer("cardboardmc")
                .map(c -> c.getMetadata().getVersion().getFriendlyString())
                .orElse("unknown");
        String api = Bukkit.getBukkitVersion();
        String mc = api.contains("-") ? api.substring(0, api.indexOf('-')) : api;

        sender.sendMessage(Messages.get(sender, "about.title"));
        sender.sendMessage(Messages.get(sender, "about.version", cbVersion));
        sender.sendMessage(Messages.get(sender, "about.minecraft", mc));
        sender.sendMessage(Messages.get(sender, "about.api", api));
        sender.sendMessage(Messages.get(sender, "about.fork"));
        sender.sendMessage(Messages.get(sender, "about.upstream"));
        return true;
    }

    // --------------------------------------------------------------- worlds

    private boolean worlds(CommandSender sender) {
        List<World> worlds = Bukkit.getWorlds();
        sender.sendMessage(Messages.get(sender, "worlds.title", worlds.size()));
        for (World world : worlds) {
            sender.sendMessage(Messages.get(sender, "worlds.entry", world.getName(), world.getPlayerCount()));
        }
        return true;
    }

    // --------------------------------------------------------------- reload

    private boolean reload(CommandSender sender) {
        sender.sendMessage(Messages.get(sender, "reload.starting"));
        try {
            CardboardConfig.setup();
            sender.sendMessage(Messages.get(sender, "reload.done"));
        } catch (Exception e) {
            sender.sendMessage(Messages.get(sender, "reload.failed", e.toString()));
        }
        return true;
    }

    // ----------------------------------------------------------------- lang

    private boolean lang(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(Messages.get(sender, "lang.player-only"));
            return true;
        }
        if (args.length == 0) {
            sender.sendMessage(Messages.get(sender, "lang.usage"));
            return true;
        }
        String code = args[0].toLowerCase(Locale.ROOT);
        if (code.equals("zh") || code.equals("zh_cn") || code.equals("cn")) {
            Messages.setOverride(sender, Lang.ZH_CN);
            sender.sendMessage(Messages.get(sender, "lang.set", "中文"));
            return true;
        }
        if (code.equals("en") || code.equals("en_us")) {
            Messages.setOverride(sender, Lang.EN_US);
            sender.sendMessage(Messages.get(sender, "lang.set", "English"));
            return true;
        }
        sender.sendMessage(Messages.get(sender, "lang.unknown", args[0]));
        return true;
    }

    // ----------------------------------------------------------- tabComplete

    @Override
    public List<String> tabComplete(CommandSender sender, String alias, String[] args) {
        if (args.length == 1) {
            String prefix = args[0].toLowerCase(Locale.ROOT);
            List<String> out = new ArrayList<>();
            for (String sub : SUBS) {
                if (StringUtil.startsWithIgnoreCase(sub, prefix)) {
                    out.add(sub);
                }
            }
            return out;
        }

        if (args.length >= 2) {
            String sub = args[0].toLowerCase(Locale.ROOT);
            String[] rest = Arrays.copyOfRange(args, 1, args.length);
            if (sub.equals("version")) return version.tabComplete(sender, "version", rest);
            if (sub.equals("mods")) return mods.tabComplete(sender, "mods", rest);
            if (sub.equals("plugins")) return plugins.tabComplete(sender, "plugins", rest);
            if (sub.equals("log") && args.length == 2) return prefixMatches(LOG_LEVELS, args[1]);
            if (sub.equals("debug")) return debugTabComplete(args);
            if (sub.equals("lang") && args.length == 2) return prefixMatches(Arrays.asList("zh", "en"), args[1]);
        }
        return ImmutableList.of();
    }

    private static List<String> debugTabComplete(String[] args) {
        if (args.length == 2) {
            List<String> options = new ArrayList<>();
            options.add("list");
            options.add("all");
            for (DebugFlag flag : DebugFlag.values()) {
                options.add(flag.name);
            }
            return prefixMatches(options, args[1]);
        }
        if (args.length == 3 && args[1].equalsIgnoreCase("all")) {
            return prefixMatches(Arrays.asList("on", "off"), args[2]);
        }
        return ImmutableList.of();
    }

    private static List<String> prefixMatches(List<String> options, String prefix) {
        List<String> out = new ArrayList<>();
        for (String option : options) {
            if (StringUtil.startsWithIgnoreCase(option, prefix)) {
                out.add(option);
            }
        }
        return out;
    }

}
