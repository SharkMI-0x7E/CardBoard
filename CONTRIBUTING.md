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

Each commit becomes a line in the changelog via `release-please`. Keep commits **atomic** — one logical change per commit, related files grouped together.

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

Every PR should answer:

- **What**: One-sentence description of the change
- **Why**: What problem does it solve? Reference issues if applicable
- **How**: Summary of approach, key decisions, and trade-offs
- **Testing**: How did you verify this works?

### PR Checklist

- [ ] `./gradlew compileJava` passes
- [ ] `./gradlew test` passes
- [ ] `./gradlew build -x test` passes
- [ ] No secrets/keys/tokens hardcoded
- [ ] Commits follow conventional commit format
- [ ] Mixin changes use `cardboard$` prefix for all new methods
- [ ] Mixin changes use the most precise injection type possible
- [ ] No new `@Overwrite` (existing ones are being refactored)
- [ ] `@MixinInfo` added for new mixins

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
- Development plan: [`plan.md`](plan.md)
- AI agent reference: [`AGENTS.md`](AGENTS.md)

---

## Community

- [Discord](https://discord.gg/tddTWXZtaP) — General discussion and support
- [Upstream Repo](https://github.com/CardboardPowered/cardboard) — Original Cardboard project
- [Fabric Wiki](https://fabricmc.net/wiki/start) — Fabric modding reference
- [Mixin Wiki](https://github.com/SpongePowered/Mixin/wiki) — Mixin documentation