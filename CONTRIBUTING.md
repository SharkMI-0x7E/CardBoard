# Contributing to Cardboard

We welcome contributions — bug fixes, compatibility improvements, documentation, and new features.

---

## Development Setup

### Prerequisites

| Software | Version |
|----------|---------|
| Java | 21+ |
| Gradle | Included via wrapper (no installation needed) |
| Git | Any recent version |

### Clone & Build

```bash
git clone https://github.com/SharkMI-0x7E/CardBoard.git
cd CardBoard
```

```powershell
# Windows — compile
.\gradlew.bat compileJava

# Windows — full build
.\gradlew.bat build

# Windows — build without tests (faster for iteration)
.\gradlew.bat build -x test
```

Build artifacts are in `build/libs/`.

---

### Common Build Errors

| Error | Cause | Fix |
| --- | --- | --- |
| `找不到符号: 方法 orElse` | `Component` is not `Optional` in 1.21.11 | Remove the `.orElse()` call |
| `找不到符号: 方法 getFavicon()` | The field is `event.icon`, not `event.getFavicon()` | Use `event.icon.value` |
| `@Shadow field not located` | Field injected by another mod at runtime | Use reflection instead |
| `RecipeBySerializerHolder not found` | Internal class structure changed in 1.21.11 | Use `Object` type or reflection |

## Commit Convention

This project uses **Conventional Commits**. Every commit message must follow:

```
<type>(<optional scope>): <description>
```

| Type | When to Use |
|------|-------------|
| `feat` | New feature |
| `fix` | Bug fix |
| `refactor` | Code restructuring (no behavior change) |
| `docs` | Documentation only |
| `build` | Build system changes |
| `ci` | CI/CD changes |
| `config` | Configuration changes |
| `perf` | Performance improvement |
| `test` | Adding or fixing tests |
| `style` | Formatting, whitespace |
| `chore` | Maintenance tasks |

**Breaking changes**: append `!` after the type/scope, or add a `BREAKING CHANGE:` footer.

```
feat(mixin)!: change event API signature

BREAKING CHANGE: PlayerInteractEvent constructor now takes 3 args instead of 2
```

### Why Conventional Commits matters

Each commit becomes a line in the release notes. Keep commits **atomic** — one logical change per commit, related files grouped together.

---

## Branch Strategy

```
main (always deployable)
  └── feature/your-feature  (branch off main)
       └── commits → PR → review → squash-merge to main
```

- Branch from `main`, PR back to `main`
- One PR = one feature or fix
- Delete feature branch after merge

---

## Code Style

### General Rules

- **Use English** for all comments, variable names, and documentation
- **Match existing style** — don't reformat adjacent code
- **Surgical changes** — touch only what you must change
- **No type suppression** — never use `@SuppressWarnings("unchecked")` to hide real issues
- **Fix the root cause, not the symptom** — fix the underlying mechanism so the same class of failure cannot recur; never silence an error just to make one case pass. If a fix only covers the specific case, say so explicitly in the commit/PR rather than presenting it as a general fix

### License Header

Every Java file must start with the GPL-3.0 license header:

```java
/**
 * Cardboard - Spigot/Paper for Fabric
 * Copyright (C) 2020-2026 CardboardPowered.org and contributors
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 * ...
 */
```

---

## Mixin Conventions

### Cardinal Rules

1. **NO new `@Overwrite`** — Use `@Inject`, `@ModifyArg`, `@Redirect`, `@ModifyVariable`, or `@ModifyReturnValue`
2. **`cardboard$` prefix on ALL methods** — Prevents collisions with other mods' mixins
3. **`cancellable = true` when calling `ci.cancel()`** — Without it, cancel() does nothing
4. **Check `instanceof ServerPlayer` before casting** — Some methods receive non-player entities
5. **`require = 0` on `@ModifyArg`/`@Redirect`** when another mod might `@Overwrite` the same method

### Named Mixin Convention

- Standard: `{Target}Mixin.java` → `BoatItemMixin.java`
- Sub-mixin for complex events: `{Target}Mixin_{Event}.java` → `ServerGamePacketListenerImplMixin_InventoryClickEvent.java`

### Annotation Preference (most compatible → least compatible)

```
@ModifyArg / @ModifyVariable > @ModifyReturnValue > @Redirect > @Inject > @Overwrite
```

Always choose the **most precise** injection type available for the task.

### `@MixinInfo` Annotation

Cardboard uses a custom annotation to document which Bukkit events each mixin triggers:

```java
@MixinInfo(events = {"PlayerInteractEvent", "BlockPlaceEvent"})
@Mixin(SomeClass.class)
public abstract class SomeClassMixin { ... }
```

Every new mixin should include this annotation. It helps the conflict scanner and serves as self-documentation.

### Priority Guide

| Priority | When |
|----------|------|
| `1000` (default) | Standard mixins |
| `1001` | Must run AFTER Fabric API injects its fields |
| `-500` | Must run BEFORE other mods (conflict resolution) |

### Conversion Patterns（`@Overwrite` → precise injection）

#### Pattern 1: `@Overwrite` → `@Inject` (Event Interception)

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

#### Pattern 2: `@Overwrite` → `@ModifyArg` (Packet Modification)

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

#### Pattern 3: `@Overwrite` → `@Redirect` (Call Replacement)

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

#### Pattern 4: Fabric API Field Access (Reflection)

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

### Decision Tree (when modifying a Mixin)

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
├─ YES → Check the project issue tracker / Mixin conflict-scanner output for known conflicts
│   └─ If not resolved → Use lower priority (-500) and test
└─ NO → Use default priority (1000)

Q4: Does the Mixin access Fabric API injected fields?
├─ YES → Use reflection (shadow won't work)
└─ NO → Use @Shadow normally
```

---

## Before Submitting a PR

### Pre-commit Checklist

```bash
# 1. Compile — must pass
.\gradlew.bat compileJava

# 2. Tests — must pass
.\gradlew.bat test

# 3. Build verification
.\gradlew.bat build -x test

# 4. No hardcoded secrets
git diff --cached | findstr /I "password secret token api_key"
```

### PR Template

PRs use [`.github/PULL_REQUEST_TEMPLATE.md`](.github/PULL_REQUEST_TEMPLATE.md), which GitHub
loads automatically when you open a pull request. It asks for What / Why / How / Testing plus a
checklist. It is not duplicated here, so the two cannot drift apart.

---

## Reporting Bugs

1. Search [existing issues](../../issues) for duplicates
2. If new, create an issue with:
   - Server log (`logs/latest.log`)
   - Cardboard version
   - Steps to reproduce
   - List of other mods loaded

---

## Documentation

- Architecture overview: [`docs/architecture.md`](docs/architecture.md)
- Mixin conflict detection guide: [`docs/mixin-conflict-detection/user-guide.md`](docs/mixin-conflict-detection/user-guide.md)

---

## Community

- [Discord](https://discord.gg/tddTWXZtaP) — General discussion and support
- [Upstream Repo](https://github.com/CardboardPowered/cardboard) — Original Cardboard project
- [Fabric Wiki](https://fabricmc.net/wiki/start) — Fabric modding reference
- [Mixin Wiki](https://github.com/SpongePowered/Mixin/wiki) — Mixin documentation