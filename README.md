<div align="center">

<img width="130" src="https://cardboardpowered.org/assets/cardboard-box.png">

# Cardboard (SharkMI Fork)

**Run Bukkit / Spigot / Paper plugins on Fabric servers**

[![License](https://img.shields.io/badge/License-GPL--3.0-orange)](LICENSE)
[![Fabric](https://img.shields.io/badge/Fabric-0.18%2B-%23dacfa4)](https://fabricmc.net/)
[![Stars](https://img.shields.io/github/stars/SharkMI-0x7E/CardBoard?style=flat&logo=github&color=yellow)](../../stargazers)

[中文](README.zh_CN.md)

</div>

> **A personal maintenance fork.**
>
> Based on [CardboardPowered/cardboard](https://github.com/CardboardPowered/cardboard), kept by
> [SharkMI](https://github.com/SharkMI-0x7E) for personal use on a private server and published
> as-is. It targets **Minecraft 1.21.11** and contains compatibility fixes that have not been
> merged upstream.
>
> Updates follow my own server's needs and are made on a best-effort basis — no commitment to
> long-term maintenance, no guarantee that a specific plugin or mod will work, and no promise to
> track upstream. It is a hobby project, not a supported product.

### Issues & Feedback

- **Fork-specific bugs or suggestions** → Please open an [Issue in this repository](https://github.com/SharkMI-0x7E/CardBoard/issues).
- **General discussion or questions** → Feel free to join the [Cardboard Discord](https://discord.gg/tddTWXZtaP) (the upstream community).

---

## Scope & Expectations

This fork exists for a plain reason: I hit problems on my own server that upstream had not
fixed yet, so I fixed them. What is published here is that work, shared in case someone else
runs into the same thing.

- **Personal project first.** Changes follow what my own server needs; broader compatibility
  work happens on a best-effort basis.
- **Compatibility is not guaranteed.** A given plugin or mod may or may not work.
- **Feedback is welcome.** Open an issue if something breaks — just don't expect fast
  turnaround. A PR with a clear reproduction, and something I can verify, has the best chance
  of being merged.
- **The upstream project comes first.** For general Cardboard questions, use the
  [upstream project](https://github.com/CardboardPowered/cardboard).

This is not meant to be "a more complete Cardboard". The upstream project is the real one;
this is a patch set that happens to be public.

---

## Fork Differences

This is a fork of the official [Cardboard](https://github.com/CardboardPowered/cardboard) project with the following changes:

- **Enhanced mixin compatibility**: Replaced `@Overwrite` annotations with precise injection methods (`@Inject`, `@ModifyArg`, `@Redirect`) to avoid conflicts with other Fabric mods
- **MiniMOTD compatibility**: Fixed server status ping conflicts with MiniMOTD mod
- **carpet-tis-addition compatibility**: Fixed boat item placement conflicts
- **Fabric API NPE fix**: Resolved crash caused by Fabric API field injection timing
- **OWASP security scanning**: Integrated OWASP Dependency-Check into the build pipeline
- **Mixin conflict detection tool**: Built-in runtime scanner that reports mixin conflicts across all loaded mods at startup (console output, optional JSON report, optional auto-disable for fatal conflicts)

### How compatibility fixes are made

Fixes target the root cause, not the symptom. When a specific plugin or mod triggers a failure,
the goal is to fix the underlying mechanism so that the same class of failure no longer occurs —
not to silence that one error and leave the next one waiting. A fix that only covers the case at
hand is noted as such in the commit and release notes, so it is not mistaken for a general
solution.

---

## AI-Assisted Development

This fork is **heavily AI-assisted**. Except for the original code inherited from
the upstream [Cardboard](https://github.com/CardboardPowered/cardboard) project,
**most of the additional code in this repository is written by AI**. Code is
build-checked and smoke-tested before release, but is not fully manually reviewed.

Primary AI tools used:

- [Trae SOLO](https://www.trae.ai/) — daily coding companion
- [CodeBuddy](https://codebuddy.cn/) — AI coding assistant
- [OpenCode](https://github.com/opencode-ai/opencode) — terminal-native AI coding agent
- [DeepSeek Harness](https://www.deepseek.com/harness/) — automated development harness powered by DeepSeek models

A build and basic smoke tests are run before each release.

---

## Overview

Cardboard is an implementation of the **Bukkit/Spigot/Paper API for FabricMC**. It allows you to run plugins from the Bukkit ecosystem on a Fabric modded server, so mods and plugins can run side by side.

## Features

- Support for Bukkit/Spigot/Paper plugins
- Broad Bukkit API coverage (work in progress)
- NMS (`net.minecraft.server`) support with automatic remapping
- Compatible with Fabric API
- Runtime mapping: intermediary (`class_xxx`)

## Installation

### Prerequisites

| Software | Version |
|----------|---------|
| Java | 21+ |
| Fabric Loader | 0.18+ |
| Minecraft | 1.21.11 |

### Steps

1. **Install Fabric Server**
   - Download the [Fabric Installer](https://fabricmc.net/use/installer/)
   - Or use the [Fabric Server Installer](https://fabricmc.net/use/server/)

2. **Download Cardboard**
   - Get the latest jar from [GitHub Releases](../../releases) or [Modrinth](https://modrinth.com/mod/cardboard-sharkmi-fork)

3. **Place in mods folder**
   ```
   server/
   ├── mods/
   │   ├── cardboard-xxx.jar    <-- put here
   │   └── fabric-api-xxx.jar   <-- Fabric API (required)
   ├── eula.txt
   └── server.properties
   ```

4. **Start the server**
   - The config file will be generated automatically on first launch
   - Place plugins in the `plugins/` folder

## Usage

### Loading Plugins

Simply drop `.jar` plugin files into the `plugins/` directory and restart the server.

### Configuration

Configuration file is located at `config/cardboard/cardboard-config.yml`:

```yaml
# Enable automatic conflict resolution
auto-conflict-resolution: true

# Force-disabled mixins (for resolving conflicts)
mixin-force-disable: []

# Mixin conflict detection
runtime-conflict-scan: true
conflict-scan-json-output: false
auto-disable-fatal-conflicts: false

# Debug options
debug_mode: false
debug_print_event_call: false
debug_print_all_calls: false
debug_player: false
debug_other: false
debug_print_remaputil: false
```

For detailed conflict detection configuration, see [docs/mixin-conflict-detection/user-guide.md](docs/mixin-conflict-detection/user-guide.md).

## Building

### Requirements

- Java 21+
- Gradle (wrapper included, no extra installation needed)

### Build Commands

```powershell
# Windows
.\gradlew.bat build

# Linux/Mac
./gradlew build

# Skip tests (faster)
.\gradlew.bat build -x test
```

Build artifacts are located in `build/libs/`.

## Version Support

This fork targets **Minecraft 1.21.11 only** — no older or newer versions are supported.

| Minecraft Version | Fabric Version | Branch | Status |
|-------------------|----------------|--------|--------|
| 1.21.11 | 0.18+ | ver/1.21.11 | Active |

## Java 21 Flags

This fork targets **Minecraft 1.21.11** and requires **Java 21**. Java 21's
module system blocks runtime reflection into internal JDK APIs, which is used by
some plugins — e.g. the Libby dependency injection used by Citizens and similar
plugins. Paper-like launchers include these flags by default, but hand-written
`start.sh` scripts will fail without them.

Add the following to your server startup script:

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

Example `start.sh`:

```bash
java $JAVA_OPTS -jar fabric-server-launch.jar nogui
```

### Offline Dependency Directory (plugins/libraries)

`plugins/libraries/` is Cardboard's external dependency directory for plugins.
Plugins such as Citizens use Libby to download their runtime dependencies
(adventure, mocha, ph-tree, etc.) over the network on the fly. If your server
has no internet access, or you would rather skip the download, drop the
required dependency jars into `plugins/libraries/`. Cardboard's plugin class
loader will fall back to loading classes from this directory when they are not
found in the plugin jar or the global class loader, so no online download is
needed.

## Known Issues

- **Mixin Conflicts**: Some Fabric mods using `@Overwrite` may conflict with Cardboard
  - Solution: Configure `mixin-force-disable` in `cardboard-config.yml`
- **NMS Plugins**: Some plugins deeply dependent on NMS may not work
  - Cardboard supports automatic NMS remapping, but coverage is not 100%
- **Missing Events**: A few Bukkit events are not yet implemented

## Contributing

This is a personal project with limited time behind it, so contributions are appreciated but
are not always quick to be handled.

### Reporting Bugs

A good report is still useful, even if it does not get an answer right away.

1. Search [Issues](../../issues) for existing reports of the same problem
2. If not found, create a new issue with:
   - Server log (`latest.log`)
   - Cardboard version
   - Steps to reproduce

### Submitting Code

1. Fork this repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'feat: add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Create a Pull Request

### Development Guidelines

- Follow [Conventional Commits](https://www.conventionalcommits.org/) specification
- Use English for code comments
- Use `@Inject` instead of `@Overwrite` for new mixins

## Documentation

> **Note**: Comprehensive documentation for this fork is still being written. The following resources are available:

- [Modrinth Page](https://modrinth.com/mod/cardboard-sharkmi-fork) — Latest releases and version history
- [GitHub Releases](../../releases) — Release notes and downloadable artifacts
- [Upstream Wiki](https://github.com/CardboardPowered/cardboard/wiki) — General Cardboard documentation
- [API Javadoc](https://cardboardpowered.org/javadoc/) — Bukkit API reference
- [Supported Bukkit Versions](https://github.com/CardboardPowered/cardboard/wiki/Supported-Versions)
- [FAQ](https://github.com/CardboardPowered/cardboard/wiki/FAQ)

## Credits

- [BukkitTeam](https://bukkit.org/), [Spigot](https://spigotmc.org/), and [Paper](https://papermc.io/) for their work on the API
- [Glowstone](https://glowstone.net) for the library loader
- [md_5's SpecialSource](https://github.com/md-5/SpecialSource), [SrgLib by Techcable & Orion](https://github.com/OrionMinecraft/SrgLib), [MinecraftMapping by Phase](https://github.com/phase/MinecraftMapping/)
- All [Cardboard contributors](https://github.com/CardboardPowered/cardboard/graphs/contributors)
- All [SharkMI fork contributors](https://github.com/SharkMI-0x7E/CardBoard/graphs/contributors)
- [Trae SOLO](https://www.trae.ai/), [CodeBuddy](https://codebuddy.cn/), [OpenCode](https://github.com/opencode-ai/opencode), and [DeepSeek Harness](https://www.deepseek.com/harness/) for AI-assisted development

## License

This project inherits the license from Paper. See [Paper's License](https://github.com/PaperMC/Paper/blob/master/LICENSE.md) for full details.
SrgLib is licensed under MIT.

This project is licensed under the **GPL-3.0** License. See [LICENSE](LICENSE) for details.
