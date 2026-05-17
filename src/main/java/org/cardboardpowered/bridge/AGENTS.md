# Bridge Directory — Quick Reference

> **Parent**: See root `AGENTS.md` § Architecture for overall project structure
> **Stats**: 96 files, 10 subdirectories

---

## What Bridges Are

Interface-only access layer for cross-package communication. Bridges expose Minecraft internals to Cardboard code without direct field/method access. Pattern: define an interface → implement via mixin → access through the interface.

---

## Directory Layout

```
bridge/
├── IMixinStyle.java             # Marker interface — all bridges implement this
├── advancements/                # Advancement progress bridges
├── bukkit/                      # Bukkit API internals
├── commands/                    # Command source bridges
├── core/                        # Core registry bridges
├── level/                       # Level/world access bridges
├── network/                     # Network/packet bridges
├── resources/                   # Resource manager bridges
├── server/                      # Server instance bridges
└── world/                       # Entity, block, item bridges
    ├── entity/                  #   LivingEntity, Mob, Player bridges
    ├── inventory/               #   Container bridges
    ├── item/                    #   ItemStack bridges
    └── level/                   #   Block/BlockState bridges
```

---

## Pattern

```java
// 1. Define the bridge interface
public interface IEntityBridge extends IMixinStyle {
    org.bukkit.Location getBukkitLocation();
    void setBukkitLocation(org.bukkit.Location loc);
}

// 2. Implement via mixin
@Mixin(Entity.class)
public class EntityMixin implements IEntityBridge {
    // ... implementation ...
}

// 3. Access through the bridge
IEntityBridge bridge = (IEntityBridge) (Object) minecraftEntity;
Location loc = bridge.getBukkitLocation();
```

---

## Key Bridges

| Bridge | Provides Access To | Used By |
|--------|-------------------|---------|
| `IEntityBridge` | Entity location, velocity, metadata | impl/entity/, event/ |
| `IWorldBridge` | World name, environment, difficulty | impl/world/ |
| `IServerBridge` | Server instance, player list | CardboardMod.java |
| `IBlockBridge` | Block type, material, state | impl/block/ |

---

## Rules

1. **Interfaces only** — no implementation in bridge/ (implementations go in mixin/ or impl/)
2. **Implement `IMixinStyle`** — marker interface for type checking
3. **Cast pattern**: `(IBridge) (Object) mcObject` — double-cast through Object for cross-package access
4. **Mirror Minecraft class hierarchy** — directory structure mirrors the mixin/ and Minecraft class tree