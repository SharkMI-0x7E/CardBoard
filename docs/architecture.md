# Cardboard Architecture

Cardboard is a **Bukkit-API-on-Fabric** bridge layer — it allows Bukkit/Spigot/Paper plugins to run on a Fabric server by intercepting Minecraft internals via Mixin.

---

## How Cardboard Works

```
1. Fabric loads Cardboard as a mod
2. CardboardMod initializes:
   ├─ Creates CraftServer (Bukkit Server implementation)
   ├─ Sets up plugin manager
   └─ Loads plugins from plugins/ directory

3. Mixins intercept Minecraft methods:

   ┌────────────────────────────────────────┐
   │ Player clicks block (original flow):    │
   │   Client → ServerGamePacketListener     │
   │   → ServerPlayerGameMode.useItemOn()   │
   │   → Block.use()                         │
   │   → Result sent to client               │
   │                                          │
   │ With Cardboard:                          │
   │   ...same...                             │
   │   → Block.use()                          │
   │   → MIXIN INTERCEPTS HERE                │
   │   → Bukkit PlayerInteractEvent fires     │
   │   → If cancelled: return early           │
   │   → If not: continue with original logic │
   └────────────────────────────────────────┘
```

---

## Project Structure

```
Cardboard/
├── src/main/java/org/cardboardpowered/
│   ├── CardboardMod.java             # Entry point, Fabric mod init
│   ├── CardboardConfig.java          # YAML config system
│   │
│   ├── mixin/                        # 236+ Mixin classes
│   │   ├── CardboardMixinPlugin.java # Mixin lifecycle (config, conflict scan, compatibility)
│   │   ├── server/                   # Server lifecycle, networking, players
│   │   ├── world/                    # Entities, items, blocks, inventory
│   │   ├── core/                     # Registries, components, dispensers
│   │   ├── bukkit/                   # Bukkit API internals
│   │   ├── commands/                 # Command dispatch
│   │   ├── network/                  # Chat, protocol
│   │   ├── paper/                    # Paper API internals
│   │   ├── resources/                # Resource/registry loading
│   │   ├── advancements/             # Advancement events
│   │   └── stats/                    # Statistics tracking
│   │
│   ├── bridge/                       # Interface bridges (96 files)
│   │   └── ...                        # Cross-package access via interfaces
│   │
│   ├── compat/                       # Mod compatibility database
│   │   ├── ModCompatibilityDatabase.java
│   │   └── ModCompatibilityRule.java
│   │
│   ├── conflict/                     # Mixin conflict detection
│   │   ├── MixinConfigScanner.java   # Step 1: scan mixin.json files
│   │   ├── MixinAnnotationScanner.java # Step 2: ASM parse annotations
│   │   ├── MixinConflictDetector.java  # Step 3: R1-R6 rules
│   │   ├── ConflictReport.java       # Console + JSON output
│   │   └── model/                    # Data models
│   │
│   ├── library/                      # Dynamic library loading
│   ├── util/                         # Utilities (MixinInfo annotation, JarReader)
│   ├── impl/                         # Bukkit API implementations
│   ├── api/                          # Cardboard-specific events
│   └── adventure/                    # Adventure text support
│
├── src/main/resources/
│   ├── bukkitfabric.mixins.json      # Mixin config (146 entries)
│   ├── bukkitfabric.accesswidener    # 800+ access widening entries
│   └── fabric.mod.json               # Fabric mod metadata
│
└── config/cardboard/
    ├── cardboard-config.yml          # Runtime configuration
    └── mod-compatibility.yml         # Known mod conflicts DB
```

---

## Core Components

### Mixin System

Cardboard uses the **SpongePowered Mixin** framework to intercept Minecraft methods. Key stats:

| Metric | Value |
|--------|-------|
| Total Mixins | 236+ |
| `@Inject` | 124 classes |
| `@Overwrite` | 46 classes (legacy, being refactored) |
| `@Redirect` / `@ModifyArg` / `@ModifyVariable` / `@ModifyReturnValue` | 31 classes |
| Mixin config files | `bukkitfabric.mixins.json` |
| Mapping | Mojang official |

### Mixin Category Map

#### Server/Network

| Mixin | Target | Purpose | Inject Type |
|-------|--------|---------|-------------|
| `ServerStatusPacketListenerImplMixin` | Server status handler | ServerListPingEvent | `@ModifyArg` |
| `ServerGamePacketListenerImplMixin` | Game packet handler | Chat, movement, inventory | `@Redirect`, `@Inject` |
| `PlayerListMixin` | Player list | Player join/quit | `@Redirect` |
| `MinecraftServerMixin` | Server instance | Server lifecycle | `@Redirect` |

#### World/Level

| Mixin | Target | Purpose |
|-------|--------|---------|
| `LevelMixin` | World level | World events |
| `ExplosionMixin` | Explosions | Explosion events |
| `RecipeManagerMixin` | Recipe loading | Recipe management |

#### Entity

| Mixin | Target | Purpose |
|-------|--------|---------|
| `EntityMixin` | Base entity | Entity events |
| `LivingEntityMixin` | Living entity | Damage/heal events |
| `MobMixin` | Mob AI | Mob targeting |

---

### Bridge Interfaces

The `bridge/` package provides interface-only access to Minecraft internals. Rather than accessing private fields directly, Cardboard defines interfaces, implements them via mixins, and accesses internals through those interfaces.

```
bridge/
├── IMixinStyle.java              # Marker interface for all bridges
├── advancements/                 # Advancement progress
├── bukkit/                       # Material, Registry, EntityType
├── commands/                     # Command source
├── core/                         # Registries, components
├── level/                        # Level/world access
├── network/                      # Packet manipulation
├── resources/                    # Resource manager
├── server/                       # Server instance
└── world/                        # Entity, block, item
```

### Mixin Conflict Detection

See [Mixin Conflict Detection User Guide](mixin-conflict-detection/user-guide.md) for details.

---

## Build System

- **Build tool**: Gradle with Fabric Loom plugin
- **Target**: Minecraft 1.21.11, Fabric Loader 0.16+
- **Java**: 21+
- **CI/CD**: GitHub Actions (ci.yml, release.yml, release-please.yml)
- **Code conventions**: Conventional Commits, English comments, GPL-3.0 license

---

## Data Flow

```
Fabric Loader
  │
  ├─ Loads fabric.mod.json → discovers CardboardMod (entry point)
  │
  └─ CardboardMod.onInitialize()
       │
       ├─ CardboardConfig.setup()
       │   └─ Reads config/cardboard/cardboard-config.yml
       │
       ├─ CardboardMixinPlugin.onLoad()
       │   ├─ Loads libraries (Paper API jars)
       │   ├─ [Optional] ModCompatibilityDatabase.load()
       │   ├─ [Optional] MixinConflictDetector: scan → detect → report
       │   └─ JarReader.scan plugins/
       │
       └─ CardboardMixinPlugin.shouldApplyMixin()
           ├─ Check manual disable list
           ├─ Check compatibility database
           ├─ Check FATAL conflict auto-disable
           └─ Check per-mixin compatibility rules
```

---

## Injection Point Reference

Cardboard's mixins use these injection types in order of preference:

| Inject Type | Use When | Example |
|-------------|----------|---------|
| `@ModifyArg` | Change a constructor/method argument | Packet data modification |
| `@ModifyReturnValue` | Post-process a return value | Event result modification |
| `@Redirect` | Replace a method call entirely | Call replacement |
| `@Inject(HEAD)` | Intercept at method start, possibly cancel | Event cancellation |
| `@Inject(RETURN)` | Intercept before method returns | Post-processing |

See [`AGENTS.md`](../AGENTS.md) for detailed conflict resolution patterns and the decision tree for choosing the right injection type.