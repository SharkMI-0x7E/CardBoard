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
package org.cardboardpowered.util;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Tiny two-language (zh_CN / en_US) message table for Cardboard's own commands.
 *
 * <p>Language selection, in priority order: an explicit per-player override set
 * via {@code /cardboard lang}; otherwise the player's client locale (anything
 * starting with "zh" gets Chinese, everyone else English); otherwise, for the
 * console and any non-player sender, {@link #DEFAULT}.
 *
 * <p>Colours use '&amp;' codes, translated on the way out, so messages stay
 * readable in the source and render as proper {@link ChatColor} in game.
 */
public final class Messages {

    public enum Lang {
        ZH_CN, EN_US
    }

    /** Language used for the console and any non-player sender. */
    private static final Lang DEFAULT = Lang.ZH_CN;

    private static final Map<Lang, Map<String, String>> TABLES = new HashMap<>();

    /** Per-player overrides set via /cardboard lang. */
    private static final Map<UUID, Lang> OVERRIDES = new ConcurrentHashMap<>();

    private Messages() {
    }

    public static void setOverride(CommandSender sender, Lang lang) {
        if (sender instanceof Player player) {
            OVERRIDES.put(player.getUniqueId(), lang);
        }
    }

    public static Lang langFor(CommandSender sender) {
        if (sender instanceof Player player) {
            Lang override = OVERRIDES.get(player.getUniqueId());
            if (override != null) {
                return override;
            }
            String locale = player.getLocale();
            if (locale != null && !locale.isEmpty()) {
                return locale.toLowerCase(Locale.ROOT).startsWith("zh") ? Lang.ZH_CN : Lang.EN_US;
            }
        }
        return DEFAULT;
    }

    /** Resolve a message for the sender's language, then apply '&' colour codes. */
    public static String get(CommandSender sender, String key, Object... args) {
        return get(langFor(sender), key, args);
    }

    public static String get(Lang lang, String key, Object... args) {
        Map<String, String> table = TABLES.get(lang);
        String template = table != null ? table.get(key) : null;
        if (template == null) {
            template = TABLES.get(Lang.EN_US).getOrDefault(key, key);
        }
        if (args.length > 0) {
            template = String.format(template, args);
        }
        return ChatColor.translateAlternateColorCodes('&', template);
    }

    static {
        Map<String, String> zh = new HashMap<>();
        zh.put("cmd.no-permission", "&c你没有权限执行此命令。");
        zh.put("cmd.unknown", "&c未知的子命令：&e%s&c。输入 &e/cardboard help&c 查看帮助。");
        zh.put("cmd.help.usage", "&7用法：&e%s");
        zh.put("cmd.help.title", "&6Cardboard &7命令帮助");
        zh.put("cmd.help.line", "&e%s &8- &7%s");
        zh.put("cmd.desc.help", "显示本帮助");
        zh.put("cmd.desc.version", "显示服务器版本信息");
        zh.put("cmd.desc.about", "显示 Cardboard 项目信息");
        zh.put("cmd.desc.tps", "显示 TPS / MSPT / 内存 / 运行时长");
        zh.put("cmd.desc.mods", "列出已加载的 Fabric 模组");
        zh.put("cmd.desc.plugins", "查看插件列表（可 info / enable / disable）");
        zh.put("cmd.desc.debug", "调试开关：列出 / 翻转 / 全开全关");
        zh.put("cmd.desc.worlds", "列出所有世界及玩家数");
        zh.put("cmd.desc.conflicts", "手动运行一次 Mixin 冲突扫描");
        zh.put("cmd.desc.log", "运行时调整根日志级别");
        zh.put("cmd.desc.doctor", "一键健康自检并给出建议");
        zh.put("cmd.desc.dump", "导出诊断快照（含线程栈）到文件");
        zh.put("cmd.desc.compat", "列出模组兼容规则");
        zh.put("cmd.desc.reload", "重新加载 Cardboard 配置");
        zh.put("cmd.desc.lang", "切换语言：zh | en（仅影响自己）");

        zh.put("version.line", "&6%s &7版本 &f%s&7（实现 API &f%s&7）");
        zh.put("version.checking", "&7正在检查版本，请稍候……");
        zh.put("version.latest", "&a你正在使用最新版本");
        zh.put("version.behind", "&e你落后了 %s 个版本");
        zh.put("version.custom", "&7未知版本，自定义构建？");
        zh.put("version.plugin-not-found", "&c服务器没有运行名为该名称的插件。");
        zh.put("version.use-plugins", "&7用 &e/plugins&7 查看插件列表。");

        zh.put("mods.title", "&6已加载的 Fabric 模组 &7(%s)");
        zh.put("mods.empty", "&7没有已加载的模组。");

        zh.put("plugins.title", "&6插件 &7(已启用 &a%s&7 / 共 &f%s&7)");
        zh.put("plugins.legend", "&8图例：&a绿色=已启用 &c红色=未启用");
        zh.put("plugins.empty", "&7没有已加载的插件。");
        zh.put("plugins.info.header", "&6插件：&f%s");
        zh.put("plugins.info.version", "&7版本：&f%s");
        zh.put("plugins.info.authors", "&7作者：&f%s");
        zh.put("plugins.info.website", "&7网站：&f%s");
        zh.put("plugins.info.enabled", "&7状态：&a已启用");
        zh.put("plugins.info.disabled", "&7状态：&c未启用");
        zh.put("plugins.info.not-found", "&c找不到插件：&e%s");
        zh.put("plugins.info.compat", "&7兼容状态：%s &8%s");
        zh.put("plugins.usage", "&7用法：&e/plugins [info|enable|disable <名称>]");
        zh.put("plugins.already-enabled", "&e插件 &f%s&e 已经是启用状态。");
        zh.put("plugins.already-disabled", "&e插件 &f%s&e 已经是禁用状态。");
        zh.put("plugins.enable.done", "&a已启用插件 &f%s&a。");
        zh.put("plugins.enable.failed", "&c启用插件 &f%s&c 失败：%s");
        zh.put("plugins.disable.done", "&a已禁用插件 &f%s&a。");
        zh.put("plugins.disable.failed", "&c禁用插件 &f%s&c 失败：%s");
        zh.put("compat.status.compatible", "&a兼容");
        zh.put("compat.status.resolved", "&e已解决冲突");
        zh.put("compat.status.investigation", "&c待调查");

        zh.put("worlds.title", "&6世界列表 &7(%s)");
        zh.put("worlds.entry", "&8- &f%s &7玩家数：&f%s");

        zh.put("debug.usage", "&7用法：&e/cardboard debug [list|<开关>|all on|off]");
        zh.put("debug.on", "&a开启");
        zh.put("debug.off", "&c关闭");
        zh.put("debug.toggled", "&6开关 &e%s&6：%s");
        zh.put("debug.master-auto", "&7已自动开启总开关 &edebug_mode&7。");
        zh.put("debug.reload-hint", "&8提示：这些是运行时开关，&7/cardboard reload&8 会被配置文件覆盖。");
        zh.put("debug.all-on", "&a已开启全部调试开关。");
        zh.put("debug.all-off", "&c已关闭全部调试开关。");
        zh.put("debug.list.title", "&6调试开关 &7(短名 / 状态 / 配置键)");
        zh.put("debug.list.line", "&e%s &7- %s &8(%s)");
        zh.put("debug.list.master", "&6总开关 debug_mode：%s");

        zh.put("reload.starting", "&6正在重新加载 Cardboard 配置……");
        zh.put("reload.done", "&a配置已重新加载。");
        zh.put("reload.failed", "&c重载失败：%s");

        zh.put("lang.usage", "&7用法：&e/cardboard lang <zh|en>");
        zh.put("lang.set", "&a语言已切换为：&f%s");
        zh.put("lang.unknown", "&c未知语言：&e%s");
        zh.put("lang.player-only", "&c该命令仅对玩家生效。");

        zh.put("tps.title", "&6服务器性能");
        zh.put("tps.line", "&7TPS：&f%s &8| &7MSPT：&f%s &7ms");
        zh.put("tps.memory", "&7内存：&f%s&7 / &f%s &7MB");
        zh.put("tps.uptime", "&7已运行：&f%s");

        zh.put("conflicts.running", "&7正在运行 Mixin 冲突扫描……（可能耗时）");
        zh.put("conflicts.failed", "&c冲突扫描失败，详见控制台日志。");
        zh.put("conflicts.line", "&7%s");

        zh.put("log.current", "&6当前根日志级别：&f%s");
        zh.put("log.unknown", "&c未知日志级别：&e%s");
        zh.put("log.set", "&a根日志级别已设为：&f%s");
        zh.put("log.verbose-warn", "&e注意：TRACE/DEBUG 级别会产生大量日志。");

        zh.put("doctor.title", "&6Cardboard 自检");
        zh.put("doctor.ok", "&a[OK]");
        zh.put("doctor.warn", "&e[!!]");
        zh.put("doctor.java", "&7Java 版本 %s（需要 %s+）");
        zh.put("doctor.memory", "&7内存占用 %s / %s MB");
        zh.put("doctor.addopens", "&7JVM --add-opens 覆盖 %s/%s");
        zh.put("doctor.addopens.missing", "&e  缺失：&7%s &8(见 README § Java 21 Flags)");
        zh.put("doctor.plugins", "&7插件 %s/%s 已启用");
        zh.put("doctor.conflicts.none", "&7无已知 Mixin 冲突（上次扫描）");
        zh.put("doctor.conflicts.some", "&7上次扫描发现 %s 个 Mixin 冲突");
        zh.put("doctor.conflicts.hint", "&8提示：&7/cardboard conflicts&8 可重新扫描；&7/cardboard dump&8 可导出诊断快照。");

        zh.put("dump.done", "&a诊断快照已写入：&f%s");
        zh.put("dump.failed", "&c导出失败：%s");

        zh.put("compat.title", "&6模组兼容规则 &7(%s)");
        zh.put("compat.empty", "&7没有已知兼容规则。");
        zh.put("compat.line", "&e%s &7- %s &8%s");

        zh.put("about.title", "&6Cardboard &7- 在 Fabric 上运行 Bukkit/Spigot API");
        zh.put("about.version", "&7Cardboard 版本：&f%s");
        zh.put("about.minecraft", "&7Minecraft：&f%s");
        zh.put("about.api", "&7Bukkit API：&f%s");
        zh.put("about.fork", "&7分支：&fSharkMI fork（仅维护 1.21.11）");
        zh.put("about.upstream", "&7上游：&fhttps://github.com/CardboardPowered/cardboard");
        TABLES.put(Lang.ZH_CN, zh);

        Map<String, String> en = new HashMap<>();
        en.put("cmd.no-permission", "&cYou do not have permission to use this command.");
        en.put("cmd.unknown", "&cUnknown subcommand: &e%s&c. Type &e/cardboard help&c for help.");
        en.put("cmd.help.usage", "&7Usage: &e%s");
        en.put("cmd.help.title", "&6Cardboard &7command help");
        en.put("cmd.help.line", "&e%s &8- &7%s");
        en.put("cmd.desc.help", "Show this help");
        en.put("cmd.desc.version", "Show server version information");
        en.put("cmd.desc.about", "Show Cardboard project information");
        en.put("cmd.desc.tps", "Show TPS / MSPT / memory / uptime");
        en.put("cmd.desc.mods", "List loaded Fabric mods");
        en.put("cmd.desc.plugins", "Show plugins (optionally: info / enable / disable)");
        en.put("cmd.desc.debug", "Debug flags: list / toggle / all on|off");
        en.put("cmd.desc.worlds", "List all worlds and their player counts");
        en.put("cmd.desc.conflicts", "Run a Mixin conflict scan now");
        en.put("cmd.desc.log", "Change the root log level at runtime");
        en.put("cmd.desc.doctor", "One-shot health check with suggestions");
        en.put("cmd.desc.dump", "Write a diagnostic snapshot (incl. thread dump) to a file");
        en.put("cmd.desc.compat", "List known mod compatibility rules");
        en.put("cmd.desc.reload", "Reload the Cardboard config");
        en.put("cmd.desc.lang", "Switch language: zh | en (self only)");

        en.put("version.line", "&6%s &7version &f%s&7 (implementing API &f%s&7)");
        en.put("version.checking", "&7Checking version, please wait...");
        en.put("version.latest", "&aYou are running the latest version");
        en.put("version.behind", "&eYou are %s version(s) behind");
        en.put("version.custom", "&7Unknown version, custom build?");
        en.put("version.plugin-not-found", "&cThis server is not running any plugin by that name.");
        en.put("version.use-plugins", "&7Use &e/plugins&7 to get a list of plugins.");

        en.put("mods.title", "&6Loaded Fabric mods &7(%s)");
        en.put("mods.empty", "&7No mods loaded.");

        en.put("plugins.title", "&6Plugins &7(&a%s&7 enabled / &f%s&7 total)");
        en.put("plugins.legend", "&8Legend: &agreen = enabled &cred = disabled");
        en.put("plugins.empty", "&7No plugins loaded.");
        en.put("plugins.info.header", "&6Plugin: &f%s");
        en.put("plugins.info.version", "&7Version: &f%s");
        en.put("plugins.info.authors", "&7Author(s): &f%s");
        en.put("plugins.info.website", "&7Website: &f%s");
        en.put("plugins.info.enabled", "&7Status: &aenabled");
        en.put("plugins.info.disabled", "&7Status: &cdisabled");
        en.put("plugins.info.not-found", "&cNo plugin found: &e%s");
        en.put("plugins.info.compat", "&7Compatibility: %s &8%s");
        en.put("plugins.usage", "&7Usage: &e/plugins [info|enable|disable <name>]");
        en.put("plugins.already-enabled", "&ePlugin &f%s&e is already enabled.");
        en.put("plugins.already-disabled", "&ePlugin &f%s&e is already disabled.");
        en.put("plugins.enable.done", "&aEnabled plugin &f%s&a.");
        en.put("plugins.enable.failed", "&cFailed to enable plugin &f%s&c: %s");
        en.put("plugins.disable.done", "&aDisabled plugin &f%s&a.");
        en.put("plugins.disable.failed", "&cFailed to disable plugin &f%s&c: %s");
        en.put("compat.status.compatible", "&acompatible");
        en.put("compat.status.resolved", "&econflict resolved");
        en.put("compat.status.investigation", "&cneeds investigation");

        en.put("worlds.title", "&6Worlds &7(%s)");
        en.put("worlds.entry", "&8- &f%s &7players: &f%s");

        en.put("debug.usage", "&7Usage: &e/cardboard debug [list|<flag>|all on|off]");
        en.put("debug.on", "&aON");
        en.put("debug.off", "&cOFF");
        en.put("debug.toggled", "&6Flag &e%s&6: %s");
        en.put("debug.master-auto", "&7Master switch &edebug_mode&7 turned on automatically.");
        en.put("debug.reload-hint", "&8Note: these are runtime flags; &7/cardboard reload&8 resets them from config.");
        en.put("debug.all-on", "&aAll debug flags enabled.");
        en.put("debug.all-off", "&cAll debug flags disabled.");
        en.put("debug.list.title", "&6Debug flags &7(name / state / config key)");
        en.put("debug.list.line", "&e%s &7- %s &8(%s)");
        en.put("debug.list.master", "&6Master switch debug_mode: %s");

        en.put("reload.starting", "&6Reloading Cardboard config...");
        en.put("reload.done", "&aConfig reloaded.");
        en.put("reload.failed", "&cReload failed: %s");

        en.put("lang.usage", "&7Usage: &e/cardboard lang <zh|en>");
        en.put("lang.set", "&aLanguage set to: &f%s");
        en.put("lang.unknown", "&cUnknown language: &e%s");
        en.put("lang.player-only", "&cThis command only works for players.");

        en.put("tps.title", "&6Server performance");
        en.put("tps.line", "&7TPS: &f%s &8| &7MSPT: &f%s &7ms");
        en.put("tps.memory", "&7Memory: &f%s&7 / &f%s &7MB");
        en.put("tps.uptime", "&7Uptime: &f%s");

        en.put("conflicts.running", "&7Running Mixin conflict scan... (may take a while)");
        en.put("conflicts.failed", "&cConflict scan failed, see console log.");
        en.put("conflicts.line", "&7%s");

        en.put("log.current", "&6Current root log level: &f%s");
        en.put("log.unknown", "&cUnknown log level: &e%s");
        en.put("log.set", "&aRoot log level set to: &f%s");
        en.put("log.verbose-warn", "&eNote: TRACE/DEBUG levels produce a lot of log output.");

        en.put("doctor.title", "&6Cardboard self-check");
        en.put("doctor.ok", "&a[OK]");
        en.put("doctor.warn", "&e[!!]");
        en.put("doctor.java", "&7Java version %s (requires %s+)");
        en.put("doctor.memory", "&7Memory usage %s / %s MB");
        en.put("doctor.addopens", "&7JVM --add-opens coverage %s/%s");
        en.put("doctor.addopens.missing", "&e  missing: &7%s &8(see README § Java 21 Flags)");
        en.put("doctor.plugins", "&7Plugins %s/%s enabled");
        en.put("doctor.conflicts.none", "&7No known Mixin conflicts (last scan)");
        en.put("doctor.conflicts.some", "&7Last scan found %s Mixin conflict(s)");
        en.put("doctor.conflicts.hint", "&8Tip: &7/cardboard conflicts&8 to rescan; &7/cardboard dump&8 to export a snapshot.");

        en.put("dump.done", "&aDiagnostic snapshot written to: &f%s");
        en.put("dump.failed", "&cExport failed: %s");

        en.put("compat.title", "&6Mod compatibility rules &7(%s)");
        en.put("compat.empty", "&7No known compatibility rules.");
        en.put("compat.line", "&e%s &7- %s &8%s");

        en.put("about.title", "&6Cardboard &7- Bukkit/Spigot API on Fabric");
        en.put("about.version", "&7Cardboard version: &f%s");
        en.put("about.minecraft", "&7Minecraft: &f%s");
        en.put("about.api", "&7Bukkit API: &f%s");
        en.put("about.fork", "&7Fork: &fSharkMI fork (1.21.11 only)");
        en.put("about.upstream", "&7Upstream: &fhttps://github.com/CardboardPowered/cardboard");
        TABLES.put(Lang.EN_US, en);
    }

}
