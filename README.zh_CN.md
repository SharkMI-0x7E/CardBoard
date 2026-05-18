<div align="center">

<img width="130" src="https://cardboardpowered.org/assets/cardboard-box.png">

# Cardboard (SharkMI Fork)

**在 Fabric 服务器上运行 Bukkit / Spigot / Paper 插件**

[![License](https://img.shields.io/badge/License-GPL--3.0-orange)](LICENSE)
[![Fabric](https://img.shields.io/badge/Fabric-0.16%2B-%23dacfa4)](https://fabricmc.net/)
[![Build Status](https://img.shields.io/github/actions/workflow/status/SharkMI-0x7E/CardBoard/ci.yml?branch=main)](.github/workflows/ci.yml)
[![Discord](https://img.shields.io/badge/Discord-社区讨论-7289DA?logo=discord&style=flat-square)](https://discord.gg/tddTWXZtaP)

</div>

> **这是一个社区维护的分支 (Fork)**，由 [SharkMI](https://github.com/SharkMI-0x7E) 基于 [CardboardPowered/cardboard](https://github.com/CardboardPowered/cardboard) 维护。
>
> 本分支包含了一些尚未被上游合并的兼容性修复。

### 🐛 问题与反馈

- **与 Fork 相关的 Bug 或建议** → 请提交到 [本仓库的 Issues](https://github.com/SharkMI-0x7E/CardBoard/issues)。
- **一般性讨论或疑问** → 欢迎加入 [Cardboard Discord 社区](https://discord.gg/tddTWXZtaP)（上游社区）。

---

## Fork 差异

本项目基于官方 [Cardboard](https://github.com/CardboardPowered/cardboard) 进行了以下改进：

- **增强 Mixin 兼容性**：将 `@Overwrite` 替换为精确注入方法（`@Inject`、`@ModifyArg`、`@Redirect`），避免与其他 Fabric 模组冲突
- **MiniMOTD 兼容**：修复了服务器状态 Ping 与 MiniMOTD 模组的冲突
- **carpet-tis-addition 兼容**：修复了船物品放置冲突
- **Fabric API NPE 修复**：解决了 Fabric API 字段注入时序导致的崩溃
- **OWASP 安全扫描**：在构建流程中集成了 OWASP Dependency-Check
- **改进的文档**：更详细的描述项目 并添加中文版README
- **Mixin 冲突检测工具**：内置运行时扫描器，在服务器启动时自动检测所有已加载 Mod 的 Mixin 冲突（6 条检测规则，FATAL/HIGH/MEDIUM/LOW 四级分类，控制台 + JSON 报告输出，可选自动禁用 FATAL 冲突）

---

## 简介

Cardboard 是一个 **Bukkit/Spigot/Paper API 的 Fabric 实现**。它允许你在 Fabric 模组服务器上运行 Bukkit 生态的插件，让你同时享受模组和插件的强大功能。

## 特性

- 支持 Bukkit/Spigot/Paper 插件
- 完整的 Bukkit API 实现（持续完善中）
- NMS (`net.minecraft.server`) 支持，自动重映射
- 与 Fabric API 兼容
- 支持 Mojang 官方映射

## 安装指南

### 前置要求

| 软件 | 版本要求 |
|------|----------|
| Java | 21+ |
| Fabric Loader | 0.16+ |
| Minecraft | 1.21.1+ |

### 安装步骤

1. **安装 Fabric Server**
   - 从 [Fabric 官网](https://fabricmc.net/use/installer/) 下载并安装服务端
   - 或使用 [Fabric Server Installer](https://fabricmc.net/use/server/)

2. **下载 Cardboard**
   - 从 [官网下载页](https://cardboardpowered.org/download/) 或 [GitHub Releases](../../releases) 获取最新 jar

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
auto-conflict-resolution: true

# 强制禁用的 Mixin（解决冲突用）
mixin-force-disable: []

# Mixin 冲突检测
runtime-conflict-scan: true
conflict-scan-json-output: false
auto-disable-fatal-conflicts: false

# 调试选项
debug-print-event-call: false
debug-print-all-calls: false
```

详细的冲突检测配置说明，请参阅 [docs/mixin-conflict-detection/user-guide.md](docs/mixin-conflict-detection/user-guide.md)。

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

| Minecraft 版本 | Fabric 版本 | 分支 | 状态 |
|---------------|-------------|------|------|
| 1.21.11 | 0.16+ | ver/1.21.11 | 活跃维护 |
| <= 1.21.8 | - | - | 不再支持 |

本 Fork 专注于最新 Minecraft 版本，不再维护旧版本。上游版本支持详见 [Supported Versions](https://github.com/CardboardPowered/cardboard/wiki/Supported-Versions)。

## 插件兼容性

### 已测试可用

| 插件 | 状态 | 备注 |
|------|------|------|
| EssentialsX | 部分 | 核心功能可用 |
| WorldEdit | 部分 | 基础操作可用 |
| Vault | 实验中 | 经济系统集成 |
| LuckPerms | 实验中 | 权限系统 |

### 已知不兼容

| 插件/模组 | 原因 | 解决方案 |
|-----------|------|----------|
| 某些使用 Mixin 的模组 | 注入点冲突 | 在配置中禁用冲突 Mixin |

## 已知问题

- **Mixin 冲突**：部分 Fabric 模组使用 `@Overwrite` 会与 Cardboard 冲突
  - 解决方案：在 `cardboard-config.yml` 中配置 `mixin-force-disable`
- **NMS 插件**：部分深度依赖 NMS 的插件可能无法工作
  - Cardboard 支持 NMS 自动重映射，但并非 100% 覆盖
- **部分事件**：少数 Bukkit 事件尚未实现

## 贡献指南

我们欢迎所有形式的贡献。

### 提交 Bug

1. 在 [Issues](../../issues) 中搜索是否已有相同问题
2. 如果没有，创建新 Issue 并包含：
   - 服务器日志（`latest.log`）
   - Cardboard 版本
   - 复现步骤

### 提交代码

1. Fork 本仓库
2. 创建特性分支 (`git checkout -b feature/amazing-feature`)
3. 提交更改 (`git commit -m 'feat: add amazing feature'`)
4. 推送到分支 (`git push origin feature/amazing-feature`)
5. 创建 Pull Request

### 开发规范

- 遵循 [Conventional Commits](https://www.conventionalcommits.org/) 规范
- 代码注释使用英文
- 新增 Mixin 时使用 `@Inject` 而非 `@Overwrite`

## 文档

> **注意**：本 Fork 的完整文档仍在编写中，目前请优先参考下方上游资源。

- [Wiki](https://github.com/CardboardPowered/cardboard/wiki)
- [API Javadoc](https://cardboardpowered.org/javadoc/)
- [支持的 Bukkit 版本](https://github.com/CardboardPowered/cardboard/wiki/Supported-Versions)
- [常见问题 FAQ](https://github.com/CardboardPowered/cardboard/wiki/FAQ)

## 致谢

- [BukkitTeam](https://bukkit.org/)、[Spigot](https://spigotmc.org/) 和 [Paper](https://papermc.io/) 的 API 工作
- [Glowstone](https://glowstone.net) 的库加载器
- [md_5's SpecialSource](https://github.com/md-5/SpecialSource)、[SrgLib](https://github.com/OrionMinecraft/SrgLib)、[MinecraftMapping](https://github.com/phase/MinecraftMapping/)
- 所有 [Cardboard 贡献者](https://github.com/CardboardPowered/cardboard/graphs/contributors)
- 所有 [SharkMI Fork 贡献者](https://github.com/SharkMI-0x7E/CardBoard/graphs/contributors)
- [Trae SOLO](https://www.trae.ai/) 与 [OpenCode](https://github.com/opencode-ai/opencode) 辅助代码编写

## 许可证

本项目继承自 Paper 的许可证。详见 [Paper 许可证](https://github.com/PaperMC/Paper/blob/master/LICENSE.md)。
SrgLib 使用 MIT 许可证。

本项目使用 **GPL-3.0** 许可证。详见 [LICENSE](LICENSE) 文件。
