<div align="center">

<img width="130" src="https://cardboardpowered.org/assets/cardboard-box.png">

# Cardboard (SharkMI Fork)

**Run Bukkit / Spigot / Paper plugins on Fabric servers**

[![License](https://img.shields.io/badge/License-GPL--3.0-orange)](LICENSE)
[![Fabric](https://img.shields.io/badge/Fabric-0.16%2B-%23dacfa4)](https://fabricmc.net/)
[![Build Status](https://img.shields.io/github/actions/workflow/status/SharkMI-0x7E/CardBoard/ci.yml?branch=main)](.github/workflows/ci.yml)
[![Discord](https://img.shields.io/badge/Discord-Community-7289DA?logo=discord&style=flat-square)](https://discord.gg/tddTWXZtaP)
[![GitHub Release](https://img.shields.io/github/v/release/SharkMI-0x7E/CardBoard?sort=semver)](../../releases)
[![GitHub Downloads (all assets, all releases)](https://img.shields.io/github/downloads/SharkMI-0x7E/CardBoard/total)](../../releases)
[![GitHub last commit](https://img.shields.io/github/last-commit/SharkMI-0x7E/CardBoard)](../../commits/main)
[![GitHub Repo stars](https://img.shields.io/github/stars/SharkMI-0x7E/CardBoard?style=flat)](../../stargazers)

</div>

> **This is a community fork** maintained by [SharkMI](https://github.com/SharkMI-0x7E), based on [CardboardPowered/cardboard](https://github.com/CardboardPowered/cardboard).
>
> It includes additional compatibility fixes that have not yet been merged upstream.

### 🐛 Issues & Feedback

- **Fork-specific bugs or suggestions** → Please open an [Issue in this repository](https://github.com/SharkMI-0x7E/CardBoard/issues).
- **General discussion or questions** → Feel free to join the [Cardboard Discord](https://discord.gg/tddTWXZtaP) (the upstream community).

---

## Fork Differences

This is a fork of the official [Cardboard](https://github.com/CardboardPowered/cardboard) project with the following changes:

- **Enhanced mixin compatibility**: Replaced `@Overwrite` annotations with precise injection methods (`@Inject`, `@ModifyArg`, `@Redirect`) to avoid conflicts with other Fabric mods
- **MiniMOTD compatibility**: Fixed server status ping conflicts with MiniMOTD mod
- **carpet-tis-addition compatibility**: Fixed boat item placement conflicts
- **Fabric API NPE fix**: Resolved crash caused by Fabric API field injection timing
- **OWASP security scanning**: Integrated OWASP Dependency-Check into the build pipeline
- **Mixin conflict detection tool**: Built-in runtime scanner that detects mixin conflicts across all loaded mods at startup (6 detection rules, FATAL/HIGH/MEDIUM/LOW severity levels, console + JSON report output, optional auto-disable for fatal conflicts)

---

## Overview

Cardboard is an implementation of the **Bukkit/Spigot/Paper API for FabricMC**. It allows you to run plugins from the Bukkit ecosystem on a Fabric modded server, giving you the best of both worlds: mods and plugins.

## Features

- Support for Bukkit/Spigot/Paper plugins
- Full Bukkit API implementation (work in progress)
- NMS (`net.minecraft.server`) support with automatic remapping
- Compatible with Fabric API
- Mojang official mappings

## Installation

### Prerequisites

| Software | Version |
|----------|---------|
| Java | 21+ |
| Fabric Loader | 0.16+ |
| Minecraft | 1.21.1+ |

### Steps

1. **Install Fabric Server**
   - Download the [Fabric Installer](https://fabricmc.net/use/installer/)
   - Or use the [Fabric Server Installer](https://fabricmc.net/use/server/)

2. **Download Cardboard**
   - Get the latest jar from the [official website](https://cardboardpowered.org/download/) or [GitHub Releases](../../releases)

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
debug-print-event-call: false
debug-print-all-calls: false
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

| Minecraft Version | Fabric Version | Branch | Status |
|-------------------|----------------|--------|--------|
| 1.21.11 | 0.16+ | ver/1.21.11 | Active |
| <= 1.21.8 | - | - | No Longer Supported |

This fork focuses exclusively on the latest Minecraft version. Older versions are not maintained. See [Supported Versions](https://github.com/CardboardPowered/cardboard/wiki/Supported-Versions) for upstream details.

## Plugin Compatibility

### Tested Working

| Plugin | Status | Notes |
|--------|--------|-------|
| EssentialsX | Partial | Core features work |
| WorldEdit | Partial | Basic operations work |
| Vault | Experimental | Economy system integration |
| LuckPerms | Experimental | Permission system |

### Known Incompatibilities

| Plugin/Mod | Reason | Workaround |
|------------|--------|------------|
| Some mods using Mixin | Injection point conflicts | Disable conflicting mixins in config |

## Known Issues

- **Mixin Conflicts**: Some Fabric mods using `@Overwrite` may conflict with Cardboard
  - Solution: Configure `mixin-force-disable` in `cardboard-config.yml`
- **NMS Plugins**: Some plugins deeply dependent on NMS may not work
  - Cardboard supports automatic NMS remapping, but coverage is not 100%
- **Missing Events**: A few Bukkit events are not yet implemented

## Contributing

We welcome contributions of all kinds.

### Reporting Bugs

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

> **Note**: Comprehensive documentation for this fork is still being written. For now, refer to the upstream resources below.

- [Wiki](https://github.com/CardboardPowered/cardboard/wiki)
- [API Javadoc](https://cardboardpowered.org/javadoc/)
- [Supported Bukkit Versions](https://github.com/CardboardPowered/cardboard/wiki/Supported-Versions)
- [FAQ](https://github.com/CardboardPowered/cardboard/wiki/FAQ)

## Credits

- [BukkitTeam](https://bukkit.org/), [Spigot](https://spigotmc.org/), and [Paper](https://papermc.io/) for their work on the API
- [Glowstone](https://glowstone.net) for the library loader
- [md_5's SpecialSource](https://github.com/md-5/SpecialSource), [SrgLib by Techcable & Orion](https://github.com/OrionMinecraft/SrgLib), [MinecraftMapping by Phase](https://github.com/phase/MinecraftMapping/)
- All [Cardboard contributors](https://github.com/CardboardPowered/cardboard)
- All [SharkMI fork contributors](https://github.com/SharkMI-0x7E/CardBoard/graphs/contributors)
- [Trae SOLO](https://www.trae.ai/) and [OpenCode](https://github.com/opencode-ai/opencode) for AI-assisted development

## License

This project inherits the license from Paper. See [Paper's License](https://github.com/PaperMC/Paper/blob/master/LICENSE.md) for full details.
SrgLib is licensed under MIT.

This project is licensed under the **GPL-3.0** License. See [LICENSE](LICENSE) for details.
