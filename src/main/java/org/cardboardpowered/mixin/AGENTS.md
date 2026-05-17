# Mixin Directory — Quick Reference

> **Parent**: See root `AGENTS.md` § Mixin Conventions, § Decision Tree, § Conflict Resolution Patterns
> **Stats**: 227 mixin classes, 11 subdirectories, up to 12 levels deep

---

## Directory Layout

```
mixin/
├── CardboardMixinPlugin.java    # IMixinConfigPlugin — config loading, library setup, conflict scan
├── advancements/     (2)        # Advancement-related events
├── bukkit/           (8)        # Bukkit API internals (Material, Registry, PluginManager)
├── commands/         (3)        # Command dispatch & tab-complete
├── core/             (4)        # Registries, components, dispensers
├── network/          (5)        # Chat, protocol, sync
├── paper/            (2)        # Paper API internals
├── resources/        (4)        # Resource/registry loading
├── server/           (34)       # Server lifecycle, networking, players
│   ├── dedicated/    (1)        #   DedicatedServer
│   ├── level/        (11)       #   ServerLevel, Level
│   ├── network/      (11)       #   Packet listeners (chat, move, login, inventory)
│   └── players/      (4)        #   PlayerList, Stats
├── stats/            (3)        # Statistics tracking
└── world/            (158)      # World, entities, blocks, items, inventory
    ├── entity/       (43)       #   AI, animals, monsters, projectiles, players
    ├── inventory/    (25)       #   Container menus (crafting, chest, lectern, etc.)
    ├── item/         (21)       #   Item use (boat, bucket, dye, enderpearl, etc.)
    └── level/        (63)       #   Block, chunk, storage, block entities
```

---

## Naming Conventions

| Pattern | Example | When |
|---------|---------|------|
| `{Target}Mixin.java` | `BoatItemMixin.java` | Standard mixin |
| `{Target}Mixin_{Event}.java` | `ServerGamePacketListenerImplMixin_InventoryClickEvent.java` | Complex events split from main mixin |
| `cardboard$methodName()` | `cardboard$onInteract()` | ALL mixin methods |

---

## Where to Find Mixin By Domain

| Task | Look in | Example file |
|------|---------|-------------|
| Player interact / block place | `world/item/` or `world/level/block/` | `BoatItemMixin.java`, `BlockItemMixin.java` |
| Inventory clicks | `world/inventory/` | `CraftingMenuMixin.java` |
| Entity damage / death | `world/entity/` | `LivingEntityMixin.java` |
| Chat / commands | `server/network/` | `ServerGamePacketListenerImplMixin_ChatEvent.java` |
| Player join / quit | `server/players/` | `PlayerListMixin.java` |
| World load / save | `world/level/` | `LevelMixin.java` |
| Recipe / registry | `core/` or `resources/` | `RecipeManagerMixin.java` |
| Bukkit API internals | `bukkit/` | `BukkitMaterialMixin.java` |

---

## Cardinal Rules

1. **NO new @Overwrite** — use `@Inject` / `@ModifyArg` / `@Redirect` / `@ModifyVariable` / `@ModifyReturnValue`
2. **`cardboard$` prefix on ALL methods** — avoids collisions with other mods' mixins
3. **`cancellable = true` when calling `ci.cancel()`** — without it, cancel() is a no-op
4. **Check `instanceof ServerPlayer` before casting** — some methods receive non-player entities
5. **`require = 0` on @ModifyArg/@Redirect** when another mod might @Overwrite the target method
6. **Use reflection for Fabric API injected fields** — @Shadow won't work (field may not exist at mixin load time)

## Known @Overwrite Holdouts

Some mixins still use `@Overwrite` with TODO justifications (cannot be split into precise injections):
- `BukkitSimplePluginManagerMixin.java` (29 methods — plugin lifecycle)
- `CraftingMenuMixin.java` (PreCraftEvent)
- `ServerGamePacketListenerImplMixin.java` (teleport logic)
- `BukkitMaterialMixin.java` (modded material support)
- `ServerLoginPacketListenerImplMixin.java` (login sequence)

When modifying these: document WHY @Overwrite is necessary in a comment.

---

## Priority Quick Guide

| Priority | When |
|----------|------|
| `1000` (default) | Standard mixins |
| `1001` | Must run AFTER Fabric API injects |
| `-500` | Must run BEFORE other mods (conflict resolution) |

---

## Gotchas

- `Component` in 1.21.11 returns value directly — no `.orElse()`
- Recipe internal classes changed in 1.21.11 — use `Object` type + reflection
- Access widener (`bukkitfabric.accesswidener`) has 800+ entries — check it before @Shadow
- Empty mixin classes (`EnderpearlItemMixin.java`, `SnowballItemMixin.java`) exist as registration placeholders