<div align="center">

<img width="130" src="https://cardboardpowered.org/assets/cardboard-box.png">

# Cardboard (SharkMI Fork)

**在 Fabric 服务器上运行 Bukkit / Spigot / Paper 插件**

[![License](https://img.shields.io/badge/License-GPL--3.0-orange)](LICENSE)
[![Fabric](https://img.shields.io/badge/Fabric-0.18%2B-%23dacfa4)](https://fabricmc.net/)
[![Stars](https://img.shields.io/github/stars/SharkMI-0x7E/CardBoard?style=flat&logo=github&color=yellow)](../../stargazers)

[English](README.md)

</div>

> **个人维护分支。**
>
> 基于 [CardboardPowered/cardboard](https://github.com/CardboardPowered/cardboard)，由 [SharkMI](https://github.com/SharkMI-0x7E) 为自己的服务器维护并原样发布。
> 目标版本为 **Minecraft 1.21.11**，包含尚未合并到上游的兼容性修复。
>
> 更新跟随我自己服务器的需求，以尽力而为的方式推进——不承诺长期维护，不承诺覆盖所有插件与模组，不承诺跟随上游同步。这是个人自用的业余项目，不是有支持承诺的产品。

### 问题与反馈

- **与 Fork 相关的 Bug 或建议** → 请提交到 [本仓库的 Issues](https://github.com/SharkMI-0x7E/CardBoard/issues)。
- **一般性讨论或疑问** → 欢迎加入 [Cardboard Discord 社区](https://discord.gg/tddTWXZtaP)（上游社区）。

---

## 项目定位与预期

这个分支存在的原因很朴素：我在自己的服务器上遇到上游还没修的问题，就自己修了。这里发布的就是这些修改，顺便分享给有同样需要的人。

- **个人项目优先**：更新跟随我自己服务器的需求，更广泛的兼容性工作以尽力而为的方式推进。
- **不保证兼容性**：某个插件或模组能不能跑，需要实际验证。
- **欢迎反馈**：遇到问题可以提 Issue，只是别期待快速响应。带清晰复现步骤、并且我能实际验证的 PR，最有可能被合并。
- **上游优先**：Cardboard 的通用问题请参考[上游项目](https://github.com/CardboardPowered/cardboard)。

本分支不以"更完整的 Cardboard"为目标。上游项目才是正主，这里只是一份恰好公开的补丁集合。

---

## Fork 差异

本项目基于官方 [Cardboard](https://github.com/CardboardPowered/cardboard) 做了以下改动：

- **增强 Mixin 兼容性**：将 `@Overwrite` 替换为精确注入方法（`@Inject`、`@ModifyArg`、`@Redirect`），避免与其他 Fabric 模组冲突
- **MiniMOTD 兼容**：修复了服务器状态 Ping 与 MiniMOTD 模组的冲突
- **carpet-tis-addition 兼容**：修复了船物品放置冲突
- **Fabric API NPE 修复**：解决了 Fabric API 字段注入时序导致的崩溃
- **OWASP 安全扫描**：在构建流程中集成了 OWASP Dependency-Check
- **Mixin 冲突检测工具**：内置运行时扫描器，在服务器启动时报告所有已加载 Mod 的 Mixin 冲突（控制台输出，可选 JSON 报告，可选自动禁用 FATAL 冲突）

### 兼容性修复的原则

修复针对的是根因，而不是症状。当某个插件或模组触发报错时，目标是修底层机制，让同一类问题不再出现，而不是把这个报错按下去、留一个同类问题在后面等着。如果某次修复确实只覆盖了当前这个个案，会在对应的 commit 与发布说明里注明，避免被误当成通用解决方案。

---

## AI 辅助开发声明

本项目是 **重度 AI 辅助开发**。除继承自上游 [Cardboard](https://github.com/CardboardPowered/cardboard) 的原有代码外，
**本仓库中新增的代码绝大部分由 AI 编写**。代码在发布前经过构建与冒烟测试，但未经全面的人工审查。

主要使用的 AI 工具：

- [Trae SOLO](https://www.trae.ai/) — 日常编码助手
- [CodeBuddy](https://codebuddy.cn/) — AI 编程助手
- [OpenCode](https://github.com/opencode-ai/opencode) — 终端原生 AI 编码代理
- [DeepSeek Harness](https://www.deepseek.com/harness/) — 基于 DeepSeek 模型的自动化开发框架

每次发布前会运行构建与基础冒烟测试。

---

## 简介

Cardboard 是一个 **Bukkit/Spigot/Paper API 的 Fabric 实现**。它允许你在 Fabric 模组服务器上运行 Bukkit 生态的插件，使模组与插件可以在同一台服务器上共存。

## 特性

- 支持 Bukkit/Spigot/Paper 插件
- 较广的 Bukkit API 覆盖（持续完善中）
- NMS (`net.minecraft.server`) 支持，自动重映射
- 与 Fabric API 兼容
- 运行时映射：intermediary（`class_xxx`）

## 安装指南

### 前置要求

| 软件 | 版本要求 |
|------|----------|
| Java | 21+ |
| Fabric Loader | 0.18+ |
| Minecraft | 1.21.11 |

### 安装步骤

1. **安装 Fabric Server**
   - 从 [Fabric 官网](https://fabricmc.net/use/installer/) 下载并安装服务端
   - 或使用 [Fabric Server Installer](https://fabricmc.net/use/server/)

2. **下载 Cardboard**
   - 从 [GitHub Releases](../../releases) 或 [Modrinth](https://modrinth.com/mod/cardboard-sharkmi-fork) 获取最新 jar

3. **放入 mods 文件夹**
   ```
   server/
   ├── mods/
   │   ├── cardboard-xxx.jar    <-- 放入这里
   │   └── fabric-api-xxx.jar   <-- Fabric API（必须）
   ├── eula.txt
   └── server.properties
   ```

4. **启动服务器**
   - 首次启动会自动生成配置文件
   - 插件放入 `plugins/` 文件夹

## 使用方法

### 加载插件

将 `.jar` 插件文件放入 `plugins/` 目录，重启服务器即可。

### 配置文件

配置文件位于 `config/cardboard/cardboard-config.yml`：

```yaml
# 是否启用自动冲突处理
auto_conflict_resolution: true

# 强制禁用的 Mixin（解决冲突用）
mixin-force-disable: []

# Mixin 冲突检测（开发者诊断工具，默认关闭）
runtime_conflict_scan: false
conflict_scan_json_output: false
auto_disable_fatal_conflicts: false

# 调试选项
debug_mode: false
debug_print_event_call: false
debug_print_all_calls: false
debug_player: false
debug_other: false
debug_print_remaputil: false
```

详细的冲突检测配置说明，请参阅 [docs/mixin-conflict-detection/user-guide.md](docs/mixin-conflict-detection/user-guide.md)。

### 命令

Cardboard 提供 `/cardboard` 命令（别名 `/cb`）作为自身子命令的统一入口，并替换了原版的 `/plugins` 与 `/version` 命令。命令输出支持中英双语——根据玩家客户端语言自动选择，也可由玩家用 `/cardboard lang <zh|en>` 单独切换。

**`/cardboard`**（别名 `/cb`）—— 需要权限 `cardboard.command.admin`：

| 命令 | 说明 |
|------|------|
| `/cardboard help` | 显示本帮助 |
| `/cardboard version` | 显示服务器版本信息 |
| `/cardboard about` | 显示 Cardboard 项目信息 |
| `/cardboard tps` | 显示 TPS / MSPT / 内存 / 运行时长 |
| `/cardboard worlds` | 列出所有世界及玩家数 |
| `/cardboard mods` | 列出已加载的 Fabric 模组 |
| `/cardboard plugins [info\|enable\|disable <名称>]` | 查看插件列表（可 info / enable / disable） |
| `/cardboard debug [list\|<开关>\|all on\|off]` | 调试开关：列出 / 翻转 / 全开全关 |
| `/cardboard log <trace\|debug\|info\|warn\|error\|fatal\|all\|off>` | 运行时调整根日志级别 |
| `/cardboard doctor` | 一键健康自检并给出建议 |
| `/cardboard dump` | 导出诊断快照（含线程栈）到文件 |
| `/cardboard compat` | 列出模组兼容规则 |
| `/cardboard reload` | 重新加载 Cardboard 配置 |
| `/cardboard lang <zh\|en>` | 切换语言：zh \| en（仅影响自己） |

`debug` 接受的调试开关：`verbose`、`events`、`player`、`other`、`remap`、`worldedit`
（各自对应一个配置键；`list` 会同时显示当前状态与配置键）。

**`/plugins`**（别名 `/pl`）—— 列出列表需要权限 `bukkit.command.plugins`；
`enable` / `disable` 还需 `cardboard.command.admin`：

| 命令 | 说明 |
|------|------|
| `/plugins` | 列出插件（显示"已启用 N / 共 M"及图例：绿色=已启用，红色=未启用） |
| `/plugins info <名称>` | 查看单个插件的详细信息 |
| `/plugins enable <名称>` | 运行时启用插件 |
| `/plugins disable <名称>` | 运行时禁用插件 |

**`/version`**（别名 `ver`、`about`）—— 需要权限 `bukkit.command.version`：

| 命令 | 说明 |
|------|------|
| `/version` | 显示服务器（及 Cardboard）版本 |
| `/version <插件名>` | 显示某个插件的版本信息 |

控制台颜色可通过 `cardboard-config.yml` 中的 `colored-console`（默认 `true`）与 `console-color-pattern` 进行控制。

## 构建说明

### 环境要求

- Java 21+
- Gradle（内置 wrapper，无需额外安装）

### 编译命令

```powershell
# Windows
.\gradlew.bat build

# Linux/Mac
./gradlew build

# 跳过测试（更快）
.\gradlew.bat build -x test
```

编译产物位于 `build/libs/` 目录下。

## 版本支持

本 Fork **仅支持 Minecraft 1.21.11** —— 更高或更低的版本均不支持。

| Minecraft 版本 | Fabric 版本 | 分支 | 状态 |
|---------------|-------------|------|------|
| 1.21.11 | 0.18+ | main | 活跃维护 |

## Java 21 启动参数

本 Fork **仅支持 Minecraft 1.21.11**，需要 **Java 21**。Java 21 的模块系统会阻止对 JDK 内部 API 的运行时反射访问，而这正是一些插件所依赖的——例如 Citizens 等插件使用的 Libby 依赖注入。Paper 类启动器默认会带上这些参数，但手写的 `start.sh` 脚本如果缺少它们则会启动失败。

请将以下参数添加到服务器启动脚本中：

```bash
JAVA_OPTS="--add-opens java.base/java.lang=ALL-UNNAMED \
  --add-opens java.base/java.lang.invoke=ALL-UNNAMED \
  --add-opens java.base/java.lang.reflect=ALL-UNNAMED \
  --add-opens java.base/java.io=ALL-UNNAMED \
  --add-opens java.base/java.net=ALL-UNNAMED \
  --add-opens java.base/java.nio=ALL-UNNAMED \
  --add-opens java.base/java.util=ALL-UNNAMED \
  --add-opens java.base/java.util.concurrent=ALL-UNNAMED \
  --add-opens java.base/java.util.jar=ALL-UNNAMED \
  --add-opens java.base/java.util.zip=ALL-UNNAMED \
  --add-opens java.base/java.text=ALL-UNNAMED \
  --add-opens java.base/sun.nio.ch=ALL-UNNAMED \
  --add-opens java.base/sun.security.x509=ALL-UNNAMED \
  --add-opens java.rmi/sun.rmi.transport=ALL-UNNAMED"
```

`start.sh` 使用示例：

```bash
java $JAVA_OPTS -jar fabric-server-launch.jar nogui
```

### 离线依赖目录（plugins/libraries）

`plugins/libraries/` 是 Cardboard 面向插件的外部依赖目录。Citizens 等插件会通过 Libby 在运行时联网下载依赖（如 adventure、mocha、ph-tree 等）。若服务器无法联网或不想等待在线下载，可将所需依赖 jar 放入 `plugins/libraries/` 目录。插件类加载器在插件自身 jar 与全局类加载器中都找不到类时，会兜底从该目录加载，从而无需联网下载。

## 已知问题

- **Mixin 冲突**：部分 Fabric 模组使用 `@Overwrite` 会与 Cardboard 冲突
  - 解决方案：在 `cardboard-config.yml` 中配置 `mixin-force-disable`
- **NMS 插件**：部分深度依赖 NMS 的插件可能无法工作
  - Cardboard 支持 NMS 自动重映射，但并非 100% 覆盖
- **部分事件**：少数 Bukkit 事件尚未实现

## 贡献指南

这是个人项目，投入的时间有限，贡献会被认真看待，但处理速度无法保证。

欢迎提交 Bug 报告与 Pull Request —— 构建命令、提交规范、Mixin 规则与 PR 清单见
[CONTRIBUTING.md](CONTRIBUTING.md)。

- **提交 Bug**：在 [Issues](../../issues) 中附上服务器日志（`latest.log`）、Cardboard 版本与复现步骤。
- **提交代码**：Fork → 建分支 → 按 [Conventional Commits](https://www.conventionalcommits.org/) 提交 → 开 PR。

## 文档

> **注意**：本 Fork 的完整文档仍在编写中，目前可参考以下资源：

- [Modrinth 页面](https://modrinth.com/mod/cardboard-sharkmi-fork) — 最新版本和版本历史
- [GitHub Releases](../../releases) — 发布说明和下载
- [上游 Wiki](https://github.com/CardboardPowered/cardboard/wiki) — Cardboard 通用文档
- [API Javadoc](https://cardboardpowered.org/javadoc/) — Bukkit API 参考
- [支持的 Bukkit 版本](https://github.com/CardboardPowered/cardboard/wiki/Supported-Versions)
- [常见问题 FAQ](https://github.com/CardboardPowered/cardboard/wiki/FAQ)

## 致谢

- [BukkitTeam](https://bukkit.org/)、[Spigot](https://spigotmc.org/) 和 [Paper](https://papermc.io/) 的 API 工作
- [Glowstone](https://glowstone.net) 的库加载器
- [md_5's SpecialSource](https://github.com/md-5/SpecialSource)、[SrgLib（Techcable & Orion）](https://github.com/OrionMinecraft/SrgLib)、[MinecraftMapping](https://github.com/phase/MinecraftMapping/)
- 所有 [Cardboard 贡献者](https://github.com/CardboardPowered/cardboard/graphs/contributors)
- 所有 [SharkMI Fork 贡献者](https://github.com/SharkMI-0x7E/CardBoard/graphs/contributors)
- [Trae SOLO](https://www.trae.ai/)、[CodeBuddy](https://codebuddy.cn/)、[OpenCode](https://github.com/opencode-ai/opencode) 与 [DeepSeek Harness](https://www.deepseek.com/harness/) 辅助代码编写

## 许可证

本项目使用 **GPL-3.0** 许可证。详见 [LICENSE](LICENSE) 文件。
SrgLib 使用 MIT 许可证。
