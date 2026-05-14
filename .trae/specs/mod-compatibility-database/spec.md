# Phase 2: Mod 兼容性数据库

## Why

Cardboard 与许多 Fabric 模组存在 Mixin 冲突风险。目前仅对 3 个已知冲突模组（carpet-tis-addition、minimotd-fabric、fabric-api）进行了修复。需要一个系统化的方式来管理和自动处理与不同模组的兼容性问题，减少用户手动配置的负担。

## What Changes

- 创建 `ModCompatibilityDatabase` 类和 `ModCompatibilityRule` 数据结构
- 创建 `mod-compatibility.yml` 配置文件，内置已知兼容模组数据库
- 增强 `CardboardMixinPlugin.shouldApplyMixin()` 方法，自动读取兼容性数据库并处理
- 在 `CardboardConfig` 中添加 `autoConflictResolution` 开关
- 服务器启动时输出已加载模组的兼容性状态报告

## Impact

- Affected specs: Mixin 加载系统、配置系统、冲突处理机制
- Affected code: `CardboardMixinPlugin.java`、`CardboardConfig.java`、新增 `ModCompatibilityDatabase.java`、新增 `mod-compatibility.yml`

## ADDED Requirements

### Requirement: Mod 兼容性数据结构
The system SHALL define a `ModCompatibilityRule` class containing: modId, modName, disabledMixins (Set<String>), priorityOverrides (Map<String, Integer>), notes (String), and status (COMPATIBLE/CONFLICT_RESOLVED/NEEDS_INVESTIGATION).

### Requirement: 兼容性配置文件
The system SHALL load compatibility rules from `config/cardboard/mod-compatibility.yml`. The file SHALL include:
- `auto-conflict-resolution: true` (default enabled)
- `known-conflicts` section with entries for each known mod
- Pre-populated rules for: carpet-tis-addition, minimotd-fabric, fabric-api, lithium, sodium, iris, architectury, dynmap

### Requirement: Mixin 插件自动冲突处理
When `auto-conflict-resolution` is enabled, `CardboardMixinPlugin.shouldApplyMixin()` SHALL:
1. Check if the current mixin is in any loaded mod's disabledMixins list
2. Apply priority overrides from the compatibility database
3. Log which rule is being applied for transparency

### Requirement: 启动兼容性报告
On server startup, the system SHALL output a summary report showing:
- Total loaded mods with mixins detected
- Number of compatibility rules applied
- Any mods with unresolved conflicts

### Requirement: 配置开关
The system SHALL provide a `auto_conflict_resolution` boolean config option in `cardboard-config.yml` (default: true) to allow users to disable automatic conflict resolution.

## MODIFIED Requirements

### Requirement: CardboardMixinPlugin.shouldApplyMixin
The existing `shouldApplyMixin` method SHALL be enhanced to integrate the compatibility database lookup while preserving all existing logic (disabledMixins check, TeleportRandomlyConsumeEffectMixin porting_lib check, ChatEvent architectury check, event-based mixin loading).

### Requirement: CardboardConfig
The config class SHALL be extended to support loading `auto_conflict_resolution` from `cardboard-config.yml`.

## REMOVED Requirements

No requirements removed.
