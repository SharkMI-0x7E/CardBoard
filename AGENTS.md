# Cardboard AI Agent Context File 🤖

> This file is designed to give AI assistants maximum context when working on this project.
> Read this first before making any changes.

---

## 📋 Project Overview

**What is Cardboard?**
- A **Bukkit-API-on-Fabric** bridge layer
- Allows Bukkit plugins to run on a Fabric server
- Uses SpongePowered Mixin framework to intercept and modify Minecraft internals

**Key Stats:**
- 236 Mixin classes
- 40 classes using `@Overwrite` (legacy, being replaced)
- 124 classes using `@Inject`
- 31 classes using precise injections (`@Redirect`, `@ModifyArg`, etc.)
- Target: Minecraft 1.21.11
- Mapping: Mojang official (not intermediary)

---

## 🏗️ Architecture

### Core Components

```
Cardboard Project Structure:
├── src/main/java/org/cardboardpowered/
│   ├── CardboardBootstrap.java        # Entry point, Fabric mod init
│   ├── CardboardServer.java          # Bukkit Server impl (singleton)
│   ├── CardboardConfig.java          # Config system (YAML)
│   │
│   ├── mixin/                        # 236 Mixin classes
│   │   ├── CardboardMixinPlugin.java # Mixin plugin (force-disable, compatibility)
│   │   ├── server/                   # Server/network mixins
│   │   ├── world/                    # World/block/entity mixins
│   │   ├── core/                     # Core registry mixins
│   │   └── bukkit/                   # Bukkit internal mixins
│   │
│   ├── impl/                         # Bukkit API implementations
│   │   ├── CardboardServerListPingEvent.java
│   │   └── ...
│   │
│   ├── bridge/                       # Interface bridges for cross-package access
│   │
│   ├── event/                        # Cardboard-specific events
│   │
│   └── util/                         # Utilities
│       └── MixinInfo.java            # Custom annotation for Mixin metadata
│
└── src/main/resources/
    ├── bukkitfabric.mixins.json      # Main mixin config (146 mixins)
    ├── bukkitfabric.accesswidener    # Access widening (800+ entries)
    └── fabric.mod.json               # Fabric mod metadata
```

### How Cardboard Works

```
1. Fabric loads Cardboard as a mod
2. CardboardBootstrap initializes:
   └─ Creates CraftServer (Bukkit Server impl)
   └─ Sets up plugin manager
   └─ Loads plugins from plugins/ directory

3. Mixins intercept Minecraft methods:
   ┌─────────────────────────────────────────┐
   │ Player clicks block (original flow):     │
   │   Client → ServerGamePacketListener      │
   │   → ServerPlayerGameMode.useItemOn()     │
   │   → Block.use()                          │
   │   → Result sent to client                │
   │                                           │
   │ With Cardboard:                           │
   │   ...same...                              │
   │   → Block.use()                           │
   │   → MIXIN INTERCEPTS HERE                 │
   │   → Bukkit PlayerInteractEvent fires      │
   │   → If cancelled: return early            │
   │   → If not: continue with original logic  │
   └─────────────────────────────────────────┘
```

---

## 📐 Mixin Conventions

### Naming

- All Cardboard mixin methods: `cardboard$methodName` prefix
- Mixin classes: `{Target}Mixin.java` (e.g., `BoatItemMixin.java`)
- Sub-mixins for complex events: `{Target}Mixin_EventName.java` (e.g., `ServerGamePacketListenerImplMixin_InventoryClickEvent.java`)

### Annotation Pattern

```java
@MixinInfo(events = {"PlayerInteractEvent", "BlockPlaceEvent"})  // Custom annotation
@Mixin(SomeClass.class)
public abstract class SomeClassMixin {
    
    // Shadows for private fields
    @Shadow
    private SomeField someField;
    
    // Shadows for methods
    @Shadow
    public abstract void someMethod();
    
    // Injection
    @Inject(method = "targetMethod", at = @At("HEAD"), cancellable = true)
    private void cardboard$onTarget(CallbackInfo ci) {
        // ...
    }
}
```

### Priority Convention

| Priority | Usage |
|----------|-------|
| `1000` (default) | Standard Mixins |
| `1001` | Mixins that need to run AFTER Fabric API |
| `-500` to `-1000` | Mixins that should run BEFORE other mods (rare) |

### Inject Point Reference

| Scenario | Inject Type | Example |
|----------|-------------|---------|
| Intercept at method start | `@Inject(at="HEAD")` | Event cancellation |
| Intercept before return | `@Inject(at="RETURN")` | Modify return value |
| Intercept before specific call | `@Inject(at=@At("INVOKE"))` | Pre-call logic |
| Replace method call | `@Redirect` | Change what method is called |
| Change constructor arg | `@ModifyArg` | Change packet data |
| Change local variable | `@ModifyVariable` | Modify intermediate values |
| Change return value | `@ModifyReturnValue` | Post-process result |

---

## ⚠️ Critical Knowledge

### DO NOT Do

1. **Never use `@Overwrite` for new code** - It replaces the entire method and conflicts with other mods
2. **Never assume a Mixin is the only one targeting a method** - Other mods likely inject too
3. **Never use `@Shadow` to access Fabric API injected fields** - They may not exist at Mixin load time
4. **Never delete existing Mixins without checking** - They may be registered in multiple config files
5. **Never assume `Component` returns `Optional`** - In 1.21.11, many methods return the value directly

### DO Do

1. **Always use the most precise injection possible** - `@ModifyArg` > `@Redirect` > `@Inject` > `@Overwrite`
2. **Always check if the event has listeners before firing** - Performance
3. **Always set `cancellable = true` when using `ci.cancel()`** - Or it won't work
4. **Always use `cardboard$` prefix** - Avoid naming collisions
5. **Always use English comments** - Windows console doesn't handle UTF-8 Chinese well

### Known Gotchas

| Issue | Solution |
|-------|----------|
| `@Shadow field not found` | Field may be injected by another mod at runtime; use reflection instead |
| `InvalidInjectionException` | Another mod has `@Overwrite` on this method; Cardboard needs to use `@Inject` |
| `ClassCastException ServerPlayer` | Check `instanceof ServerPlayer` before casting |
| `Component.orElse()` error | In 1.21.11, `description()` returns `Component`, not `Optional<Component>` |
| Windows console garbled Chinese | Use English comments in code, or add `-Dfile.encoding=UTF-8` to JVM args |

---

## 🔧 Mixin Conflict Resolution Patterns

### Pattern 1: @Overwrite → @Inject (Event Interception)

```java
// BEFORE (conflicting):
@Overwrite
public void onInteract() {
    if (event.cancelled) return;
    // original logic...
}

// AFTER (compatible):
@Inject(method = "onInteract", at = @At("HEAD"), cancellable = true)
public void cardboard$onInteract(CallbackInfo ci) {
    if (event.cancelled) ci.cancel();
}
```

### Pattern 2: @Overwrite → @ModifyArg (Packet Modification)

```java
// BEFORE (conflicting):
@Overwrite
public void sendStatus() {
    ServerStatus modified = createCustomStatus();
    connection.send(new ClientboundStatusResponsePacket(modified));
}

// AFTER (compatible):
@ModifyArg(
    method = "sendStatus",
    at = @At(value = "INVOKE", target = "LClientboundStatusResponsePacket;<init>(LServerStatus;)V"),
    require = 0
)
private ServerStatus cardboard$modifyStatus(ServerStatus original) {
    if (needsModification) {
        return createCustomStatus();
    }
    return original; // Preserve other mods' changes!
}
```

### Pattern 3: @Overwrite → @Redirect (Call Replacement)

```java
// BEFORE:
@Overwrite
public void doSomething() {
    this.doThing(customArg);  // Replace the call
}

// AFTER:
@Redirect(
    method = "doSomething",
    at = @At(value = "INVOKE", target = "LTargetClass;doThing(LArg;)V")
)
private void cardboard$redirectDoThing(TargetClass instance, Arg arg) {
    instance.doThing(modifiedArg);
}
```

### Pattern 4: Fabric API Field Access (Reflection)

```java
// Fabric API injects: @Unique private Map<..., ...> bySyncedSerializer;
// Cardboard needs to initialize it because Fabric API's Mixin may not have loaded yet:

try {
    Field field = RecipeMap.class.getDeclaredField("bySyncedSerializer");
    field.setAccessible(true);
    field.set(recipeMap, new IdentityHashMap<>());
} catch (NoSuchFieldException | IllegalAccessException e) {
    // Fabric API not loaded or field name changed
}
```

---

## 📂 Important Files Reference

### Build & Config

| File | Purpose |
|------|---------|
| `build.gradle` | Gradle build config, Fabric Loom setup |
| `gradle.properties` | Version info (Minecraft 1.21.11, Fabric Loader 0.16.14) |
| `config/cardboard/cardboard-config.yml` | Runtime config (debug flags, mixin overrides) |

### Mixin Config

| File | Mixins Listed |
|------|---------------|
| `bukkitfabric.mixins.json` | 146 mixins (main config) |
| `fabric.mod.json` | Mod metadata, entry points |

### Entry Points

| File | Role |
|------|------|
| `CardboardBootstrap.java` | Fabric mod entry point |
| `CardboardServer.java` | Bukkit Server singleton |
| `CardboardMixinPlugin.java` | Mixin lifecycle hooks |

---

## 🐛 Debug Configuration

### Enable Debug Logging

Edit `config/cardboard/cardboard-config.yml`:

```yaml
debug_print_event_call: true     # Log event firing
debug_print_all_calls: true      # Verbose method call logging
debug_player: true               # Player-related debug info
debug_other: true                # Miscellaneous debug
debug_print_remaputil: true      # Reflection/remapping debug
```

### Build Commands

```powershell
# Build
.\gradlew.bat build

# Build without tests (faster)
.\gradlew.bat build -x test

# Clean build
.\gradlew.bat clean build

# Check for compilation errors only
.\gradlew.bat compileJava
```

### Common Build Errors

| Error | Cause | Fix |
|-------|-------|-----|
| `找不到符号: 方法 orElse` | `Component` is not `Optional` in 1.21.11 | Remove `.orElse()` call |
| `找不到符号: 方法 getFavicon()` | Field is `event.icon`, not `event.getFavicon()` | Use `event.icon.value` |
| `@Shadow field not located` | Field injected by another mod at runtime | Use reflection instead |
| `RecipeBySerializerHolder not found` | Internal class structure changed in 1.21.11 | Use `Object` type or reflection |

---

## 🗺️ Mixin Category Map

### Server/Network (14 files)

| Mixin | Target | Purpose | Inject Type |
|-------|--------|---------|-------------|
| `ServerStatusPacketListenerImplMixin` | `ServerStatusPacketListenerImpl` | ServerListPingEvent | `@ModifyArg` ✅ |
| `ServerGamePacketListenerImplMixin` | `ServerGamePacketListenerImpl` | Packet handling | `@Redirect`, `@Inject` |
| `ServerGamePacketListenerImplMixin_ChatEvent` | `ServerGamePacketListenerImpl` | AsyncChatEvent | 已删除 |
| `ServerPlayerMixin` | `ServerPlayer` | Player events | `@Redirect` |
| `PlayerListMixin` | `PlayerList` | Player join/quit | `@Redirect` |
| `MinecraftServerMixin` | `MinecraftServer` | Server lifecycle | `@Redirect` |
| `StatsCounterMixin` | `StatsCounter` | Stats tracking | `@Inject` ✅ |

### World/Level (15 files)

| Mixin | Target | Purpose | Inject Type |
|-------|--------|---------|-------------|
| `LevelMixin` | `Level` | World events | `@Overwrite` ⚠️ |
| `ExplosionMixin` | `Explosion` | Explosion events | `@Overwrite` ⚠️ |
| `RecipeMapMixin` | `RecipeMap` | Recipe management | `@Inject` ✅ |
| `RecipeManagerMixin` | `RecipeManager` | Recipe loading | `@Inject` ✅ |

### Entity (20+ files)

| Mixin | Target | Purpose | Inject Type |
|-------|--------|---------|-------------|
| `EntityMixin` | `Entity` | Entity events | `@Redirect` |
| `LivingEntityMixin` | `LivingEntity` | Damage/heal events | `@Redirect` |
| `MobMixin` | `Mob` | Mob targeting | `@Overwrite` ⚠️ |
| `BoatItemMixin` | `BoatItem` | PlayerInteractEvent | `@Inject` ✅ |

### Inventory (17 files)

| Mixin | Target | Purpose | Inject Type |
|-------|--------|---------|-------------|
| `CraftingMenuMixin` | `CraftingMenu` | Craft events | `@Overwrite` ⚠️ |
| `ChestMenuMixin` | `ChestMenu` | Chest events | `@Overwrite` ⚠️ |
| `LecternMenuMixin` | `LecternMenu` | Lectern events | `@Inject` ✅ |
| All inventory mixins | Various menus | InventoryClickEvent | Mostly `@Overwrite` ⚠️ |

### Items (15 files)

| Mixin | Target | Purpose | Inject Type |
|-------|--------|---------|-------------|
| `ItemStackMixin` | `ItemStack` | Item events | `@Inject` ✅ |
| `BoatItemMixin` | `BoatItem` | Boat placement | `@Inject` ✅ |
| `BucketItemMixin` | `BucketItem` | Bucket events | `@Overwrite` ⚠️ |
| `DyeItemMixin` | `DyeItem` | Dye events | `@Inject` ✅ |
| `EndCrystalItemMixin` | `EndCrystalItem` | End crystal placement | `@Inject` ✅ |
| `EnderpearlItemMixin` | `EnderpearlItem` | Enderpearl throw | 空类 ✅ |
| `SnowballItemMixin` | `SnowballItem` | Snowball throw | 空类 ✅ |
| `ProjectileWeaponItemMixin` | `ProjectileWeaponItem` | Projectile weapon use | `@Inject` ✅ |

### Blocks (20+ files)

| Mixin | Target | Purpose | Inject Type |
|-------|--------|---------|-------------|
| `BlockItemMixin` | `BlockItem` | Block placement | `@Overwrite` ⚠️ |
| `DispenserBlockMixin` | `DispenserBlock` | Dispenser events | `@Overwrite` ⚠️ |
| `TntBlockMixin` | `TntBlock` | TNT priming | `@Overwrite` ⚠️ |

### Legend
- ✅ = Compatible injection method (keep)
- ⚠️ = Uses @Overwrite (needs refactoring)

---

## 🎯 Decision Tree for AI Agents

When modifying a Mixin, follow this decision tree:

```
Q1: Does the Mixin use @Overwrite?
├─ YES → Go to Q2
└─ NO → Check if current injection is the most precise
    └─ If @Inject but could use @ModifyArg/@Redirect → Refactor to more precise

Q2: What does the @Overwrite method do?
├─ Triggers event + may cancel → Use @Inject(at="HEAD", cancellable=true)
├─ Modifies return value → Use @ModifyReturnValue
├─ Modifies method argument → Use @ModifyArg
├─ Replaces a method call → Use @Redirect
├─ Modifies a local variable → Use @ModifyVariable
└─ Completely rewrites logic → Split into multiple precise injections

Q3: Are there known conflicting mods?
├─ YES → Check plan.md for resolution status
│   └─ If not resolved → Use lower priority (-500) and test
└─ NO → Use default priority (1000)

Q4: Does the Mixin access Fabric API injected fields?
├─ YES → Use reflection (shadow won't work)
└─ NO → Use @Shadow normally
```

---

## 📚 References

- **SpongePowered Mixin Docs**: https://github.com/SpongePowered/Mixin/wiki
- **Fabric Loom Docs**: https://fabricmc.net/wiki/documentation:fabric_loom
- **Mojang Mappings**: https://github.com/FabricMC/yarn
- **Bukkit API Javadocs**: https://hub.spigotmc.org/javadocs/bukkit/

---

## 💡 Tips for AI Agents

1. **Always read the target class first** - Understand what the original method does before modifying it
2. **Check for existing tests** - Run them after changes
3. **Look at similar Mixins** - Cardboard has established patterns, follow them
4. **Be conservative** - Minimum change to achieve the goal
5. **Test with conflicts** - If possible, verify the change doesn't break other mods
6. **Use plan.md** - It has the roadmap and current progress
7. **Update this file** - If you discover new patterns or gotchas, add them here

---

## 🤝 Contribution Guide & CI/CD

### CRITICAL: Batch Commit Rule

**NEVER squash all changes into a single commit.** Each logical change MUST be committed separately.

| Rule | Reason |
|------|--------|
| Each logical change = 1 commit | release-please reads each commit's conventional message for changelog |
| Related file changes can go together | e.g., adding a new feature across 2 files |
| Unrelated changes = separate commits | e.g., fixing a bug + adding a feature |
| One commit message per logical change | Each becomes a line in the changelog |

**Why this matters:** `release-please` scans all commits since the last release tag and uses each commit's conventional message to generate the changelog and release notes. A single mega-commit = a single meaningless changelog line.

### Commit Workflow

```
1. Create feature branch: git checkout -b feature/your-feature
2. Make changes (follow CLAUDE.md behavior guidelines below)
3. Run local checks:
   ./gradlew compileJava          # Compilation check
   ./gradlew test                  # Run tests
   ./gradlew build -x test         # Build verification
4. Commit EACH logical change separately with conventional commits:
   git commit -m "fix: resolve mixin conflict in ServerPlayerMixin"
   git add src/main/java/org/cardboardpowered/compat/
   git commit -m "feat: add mod compatibility database"
   git add src/main/resources/cardboard/mod-compatibility.yml
   git commit -m "config: add mod-compatibility.yml with 8 known mod rules"
5. Push and open PR
```

### Conventional Commit Format

```
<type>: <description>

<optional body>
```

| Type | When to use | Example |
|------|-------------|---------|
| `feat` | New feature | `feat: add mod compatibility database` |
| `fix` | Bug fix | `fix: resolve mixin conflict in BoatItemMixin` |
| `refactor` | Code restructuring (no behavior change) | `refactor: replace @Overwrite with @Inject in LevelMixin` |
| `docs` | Documentation only | `docs: update AGENTS.md with CI/CD workflow` |
| `build` | Build system changes | `build: add OWASP dependency check` |
| `ci` | CI/CD changes | `ci: add release-please workflow` |
| `debug` | Debug logging (temporary) | `debug: add MOTD debug logs in ServerStatusPacketListenerImplMixin` |
| `config` | Configuration changes | `config: add auto_conflict_resolution setting` |

### Local Pre-commit Checklist

Run these before every commit. All must pass.

```bash
# 1. Compilation (must pass)
./gradlew compileJava

# 2. Tests (must pass)
./gradlew test

# 3. Build (must pass)
./gradlew build -x test

# 4. No hardcoded secrets (manual check)
git diff --cached | grep -iE "password|secret|token|api_key"
```

**Fails above = cannot commit. No exceptions.**

### PR Template Requirements

Every PR must include:
- **What**: Describe the change in one sentence
- **Why**: Explain the problem this solves
- **How**: Summarize the implementation approach
- **Testing**: List how you verified it works

See `.github/PULL_REQUEST_TEMPLATE.md` for the full checklist.

### Security & Licensing

```bash
# OWASP dependency check (CI runs this)
./gradlew dependencyCheckAnalyze

# License audit (allowed: MIT, Apache-2.0, BSD-3-Clause, LGPL-2.1-only, CC0-1.0)
./gradlew dependencies --configuration api | grep -E "MIT|Apache|BSD|LGPL|CC0"
```

**Rules:**
- No hardcoded secrets, API keys, or tokens in source code
- All dependencies must use allowed licenses
- Report vulnerabilities privately via GitHub Security Advisories
- Use environment variables for sensitive configuration (e.g., `MODRINTH` token)

### Build & Release

```bash
# Build fat jar (goes into build/libs/)
./gradlew build

# Build without tests (faster for local iteration)
./gradlew build -x test

# Output: build/libs/Cardboard-1.21.11.jar
```

**Full Release Workflow:**

```
Step 1: Developers push commits to main (each with conventional commit message)
    │
    ▼
Step 2: release-please.yml triggers on push to main
    ├─ Scans ALL commits since last release tag
    ├─ Parses conventional commit messages (feat:, fix:, etc.)
    ├─ Generates/updates CHANGELOG.md
    └─ Creates/updates a "Release vX.Y.Z" PR on GitHub
    │
    ▼
Step 3: Human reviews and merges the Release PR
    │
    ▼
Step 4: Merging the PR creates a v* tag (e.g., v1.21.11-1.0.0)
    │
    ▼
Step 5: release.yml triggers on the v* tag
    ├─ Builds the fat jar on ubuntu-latest
    ├─ Computes SHA256 checksum
    └─ Creates GitHub Release with jar file + checksum attached
```

**Key point:** release-please collects ALL commits between the last release tag and HEAD. Each commit's message becomes one line in the release notes. This is why batched commits matter.

**CI automates the rest:**
- PRs trigger full build + test + security scan (ci.yml)
- Tags (`v*`) trigger GitHub Release with jar upload + SHA256 (release.yml)
- `release-please` maintains changelog via conventional commits (release-please.yml)

### GitHub Workflows

| File | Trigger | Purpose |
|------|---------|---------|
| `.github/workflows/ci.yml` | PR, push to main | Format check, lint, test, build, dependency scan, license audit |
| `.github/workflows/security.yml` | Scheduled (weekly), dependency changes | CVE scanning + license audit |
| `.github/workflows/release.yml` | Tag push (`v*`) | Build fat jar, compute SHA256, create GitHub Release |
| `.github/workflows/release-please.yml` | Push to main | Conventional commits changelog + release PR |

---

## 🧠 AI Agent Behavior Guidelines (from CLAUDE.md)

When working on this project as an AI agent, follow these rules:

### 1. Think Before Coding
- State assumptions explicitly; ask if uncertain
- Present multiple interpretations if they exist - don't pick silently
- Push back when a simpler approach exists
- If something is unclear, stop and ask

### 2. Simplicity First
- Write only the minimum code needed to solve the problem
- No abstractions for single-use code
- No "flexibility" that wasn't requested
- No error handling for impossible scenarios
- If 200 lines can be 50, rewrite it

### 3. Surgical Changes
- Touch only what you must change
- Don't "improve" adjacent code, comments, or formatting
- Match existing style even if you'd do it differently
- Remove only the orphaned imports/variables your changes create
- If you notice unrelated dead code, mention it - don't delete it

### 4. Goal-Driven Execution
- Transform tasks into verifiable goals before starting
- Define success criteria: what specific behavior must work
- Multi-step tasks: state a brief plan first
- Loop until all success criteria are met

### 5. Self-Check After Completion
- Every changed line must trace directly to the user's request
- No scope reduction, no "demo" versions, no "simplified" implementations
- Finish 100% of what was asked
