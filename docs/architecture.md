# Cardboard Architecture

Cardboard is a **Bukkit-API-on-Fabric** bridge layer — it allows Bukkit/Spigot/Paper plugins to run on a Fabric server by intercepting Minecraft internals via Mixin.

---

## How Cardboard Works

```
1. Fabric loads Cardboard as a mod
   (entry point: com.javazilla.bukkitfabric.BukkitFabricMod -> CardboardMod)

2. CardboardMixinPlugin.onLoad() runs first (Mixin bootstrap):
   ├─ CardboardConfig.setup(); create plugins/ directory
   ├─ Libraries.loadLibs() (Paper API jars)
   └─ JarReader: scan plugins/ bytecode to learn which events they use
      (plugins are NOT loaded yet at this point)

3. DedicatedServer.initServer() is intercepted -> the Bukkit server appears:
   ├─ new CraftServer(server)
   └─ loadPlugins() -> enablePlugins(STARTUP)   <- plugins are really loaded here

4. Mixins intercept Minecraft methods:

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
│   ├── CardboardMod.java             # Mod logic (onInitialize). Fabric entry point is the @Deprecated com.javazilla.bukkitfabric.BukkitFabricMod
│   ├── CardboardConfig.java          # YAML config system
│   │
│   ├── mixin/                        # 226 Mixin classes
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
│   ├── util/                         # Utilities (MixinInfo annotation, JarReader, nms/)
│   ├── impl/                         # Bukkit API implementations
│   ├── api/                          # Cardboard-specific events
│   ├── extras/                       # Extra integrations
│   ├── fabric/                       # Fabric-side hooks
│   └── adventure/                    # Adventure text support
│
├── src/main/resources/
│   ├── bukkitfabric.mixins.json      # Mixin config (226 entries; repo-root copy has 224)
│   ├── bukkitfabric.accesswidener    # ~918 access widening entries
│   ├── cardboard/mod-compatibility.yml  # Known mod conflicts DB
│   └── fabric.mod.json               # Fabric mod metadata
│
└── (runtime only — generated into the Fabric config dir, not in the repo)
    └── config/cardboard/
        ├── cardboard-config.yml      # Runtime configuration
        └── mod-compatibility.yml     # Runtime copy of the resource above
```

---

## Core Components

### Mixin System

Cardboard uses the **SpongePowered Mixin** framework to intercept Minecraft methods. Key stats:

| Metric | Value |
|--------|-------|
| Mixin config file | `src/main/resources/bukkitfabric.mixins.json` — 226 registered entries (the gitignored repo-root copy has 224) |
| Mapping (development) | Mojang official (`loom.officialMojangMappings()`) |
| Mapping (runtime) | intermediary (`class_xxx`) — plugins use Spigot/obfuscated/named names, translated by `RemapUtils` |

> Injection counts (`@Mixin`, `@Inject`, `@Overwrite`, …) are **deliberately not listed here**.
> They drift with every commit, and worse, they swing wildly with the counting rule: counting
> "@Overwrite" as first-token-on-a-line gives 63, while excluding commented-out code gives 35 —
> both are "correct" under their own rule. A number in a hand-maintained doc has no way to stay
> honest. If you need one, count it yourself against `src/main/java/org/cardboardpowered/mixin/`
> and state the rule you used.

### Mixin Category Map

#### Server/Network

| Mixin | Target | Purpose | Inject Type |
|-------|--------|---------|-------------|
| `ServerStatusPacketListenerImplMixin` | Server status handler | ServerListPingEvent | `@ModifyArg` |
| `ServerGamePacketListenerImplMixin` | Game packet handler | Chat, movement, inventory | `@Redirect`, `@Inject` |
| `PlayerListMixin` | Player list | Player join/quit | `@Inject`, `@Redirect` |
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
├── <ClassName>Bridge.java        # One interface per Minecraft class (95 bridges + 1 dead leftover)
├── advancements/                 # Advancement progress
├── bukkit/                       # Material, Registry, EntityType
├── commands/                     # Command source
├── core/                         # Registries, components
├── level/                        # Level/world access
├── network/                      # Packet manipulation
├── resources/                    # Resource manager
├── server/                       # Server instance
└── world/                        # Entity, block, item
    ├── entity/                   # LivingEntity, Mob, Player bridges
    ├── inventory/                # Container bridges
    ├── item/                     # ItemStack bridges
    └── level/                    # Block/BlockState bridges
```

**Naming**: `<MinecraftClassName>Bridge` — **there is no `I` prefix**.
Examples: `Entity` → `EntityBridge`, `Level` → `LevelBridge`, `MinecraftServer` → `MinecraftServerBridge`, `ServerPlayer` → `ServerPlayerBridge`.
Some bridges are additionally declared in `src/main/resources/fabric.mod.json` under `loom:injected_interfaces` (that list covers only a handful of hot classes — the rest are wired purely through mixin `implements` + casts).

> `bridge/IMixinStyle.java` is a **dead leftover** (three `Style` setter methods plus a `// TODO`); nothing implements or references it. Do not treat it as a marker interface.

**Bridge pattern** — define an interface, implement it via a mixin, access it through the interface:

```java
// 1. Define the bridge interface
public interface EntityBridge {
    CraftEntity getBukkitEntity();
    float cardboard$getBukkitYaw();
}

// 2. Implement it in a mixin
@Mixin(Entity.class)
public class EntityMixin implements EntityBridge {
    // ... implementation ...
}

// 3. Access through the bridge (double-cast via Object)
EntityBridge bridge = (EntityBridge) (Object) minecraftEntity;
CraftEntity bukkitEntity = bridge.getBukkitEntity();
```

**Bridge rules:**

1. **Interfaces only** — no implementation in `bridge/` (implementations go in `mixin/` or `impl/`)
2. **Cast pattern**: `(SomeBridge) (Object) mcObject` — double-cast through `Object` for cross-package access
3. **Mirror the Minecraft class hierarchy** — directory structure mirrors the `mixin/` and Minecraft class tree

**Key bridges:**

| Bridge | Provides access to | Real usage |
|--------|-------------------|------------|
| `EntityBridge` | Entity location, velocity, Bukkit entity | `((EntityBridge)(Object)e).getBukkitEntity()` |
| `LevelBridge` | Level / world access | `((LevelBridge)(Object)level)` |
| `MinecraftServerBridge` | Server instance, process queue | `((MinecraftServerBridge)CraftServer.server).getProcessQueue()` |
| `ServerPlayerBridge` | Player connection, Bukkit entity | `((ServerPlayerBridge)(Object)player).getBukkitEntity()` |
| `BlockStateBridge` | Block type, material, state | `((BlockStateBridge)(Object)state)` |

### Finding a Mixin by Domain

| Task | Look in | Example |
|------|---------|---------|
| Player interact / block place | `world/item/` or `world/level/block/` | `BoatItemMixin.java`, `BlockItemMixin.java` |
| Inventory clicks | `world/inventory/` | `CraftingMenuMixin.java` |
| Entity damage / death | `world/entity/` | `LivingEntityMixin.java` |
| Chat / commands | `server/players/` | `PlayerListMixin_ChatEvent.java` |
| Player join / quit | `server/players/` | `PlayerListMixin.java` |
| World load / save | `world/level/` | `LevelMixin.java` |
| Recipe / registry | `world/item/crafting/` | `RecipeManagerMixin.java` |
| Bukkit API internals | `bukkit/` | `BukkitMaterialMixin.java` |

Mixin naming: `{Target}Mixin.java`; sub-mixins for complex events: `{Target}Mixin_{Event}.java`; every mixin method uses the `cardboard$` prefix.

> Some mixin classes are intentionally empty (e.g. `EnderpearlItemMixin.java`, `SnowballItemMixin.java`) — they exist as registration placeholders.

> The category map above is an **illustrative overview**, not an exhaustive list. For the authoritative file list, read `src/main/java/org/cardboardpowered/mixin/`.

### Mixin Conflict Detection

See [Mixin Conflict Detection User Guide](mixin-conflict-detection/user-guide.md) for details.

---

## Build System

- **Build tool**: Gradle with Fabric Loom plugin
- **Target**: Minecraft 1.21.11, Fabric Loader 0.18.4 (see `gradle.properties`)
- **Java**: 21+
- **CI/CD**: GitHub Actions (`build.yml`, `release.yml`) — no release-please; releases use manual `v*` tags (see `MODRINTH_VERSION_MANAGEMENT_AND_CICD.md`)
- **Code conventions**: Conventional Commits, English comments, GPL-3.0 license

---

## Data Flow

```
Fabric Loader
  │
  ├─ Loads fabric.mod.json → discovers BukkitFabricMod (entry point, extends CardboardMod)
  │
  └─ CardboardMixinPlugin.onLoad()          ← runs FIRST (Mixin bootstrap)
       ├─ CardboardConfig.setup()
       │   └─ Reads <fabric config dir>/cardboard/cardboard-config.yml
       ├─ Libraries.loadLibs() (Paper API jars)
       ├─ JarReader.readEvents() / readPlugins(plugins/)
       ├─ [Optional] ModCompatibilityDatabase.load()
       └─ [Optional] MixinConflictDetector: scan → detect → report

  └─ BukkitFabricMod / CardboardMod.onInitialize()   ← mod init (no server yet)
       ├─ create plugins/ directory
       ├─ EventRegistery.registerAll()
       └─ CardboardEventManager.callCardboardEvents()
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
| `@ModifyArg` | Change a method/constructor argument | Packet data modification |
| `@ModifyVariable` | Change a local variable | Intermediate value tweaks |
| `@ModifyReturnValue` | Post-process a return value | Event result modification |
| `@Redirect` | Replace a method call entirely | Call replacement |
| `@Inject(HEAD)` | Intercept at method start, possibly cancel | Event cancellation |
| `@Inject(RETURN)` | Intercept before method returns | Post-processing |
| `@Overwrite` | **Last resort only** | Avoid — breaks other mods |

When converting a legacy `@Overwrite` to a precise injection, prefer in this order:
`@ModifyArg` / `@ModifyVariable` > `@ModifyReturnValue` > `@Redirect` > `@Inject` > `@Overwrite`.
Use `@Inject(HEAD) + ci.cancel()` only when the original method body must be skipped entirely —
a `@Inject(RETURN)` still runs the original body and can NPE.