# Phase 3 Step 8: 启动流程集成 Spec

## Why
将 Phase 3 冲突检测工具集成到 CardboardMixinPlugin 的生命周期中，在服务器启动时自动执行扫描，并在 Mixin 应用时自动禁用 FATAL 冲突的 Mixin。

## What Changes
- 修改 `CardboardMixinPlugin.java`：
  - onLoad() 中新增运行时冲突扫描流程
  - shouldApplyMixin() 中新增 FATAL 冲突自动禁用逻辑
  - 新增静态字段存储扫描结果（fatalMixinSet）
- 修改 `ModCompatibilityDatabase.java`（如需，但当前已有 getRuleForMod，无需修改）

## Impact
- 修改代码：`src/main/java/org/cardboardpowered/mixin/CardboardMixinPlugin.java`
- 依赖已有代码：`CardboardConfig`、`MixinConflictDetector`、`MixinAnnotationScanner`、`MixinConfigScanner`、`ConflictReport`、`ModCompatibilityDatabase`
- 无破坏性变更

## ADDED Requirements
### Requirement: onLoad() 中的冲突扫描
系统 SHALL 在 CardboardMixinPlugin.onLoad() 中，在 compatDatabase 加载完成后、插件加载之前，执行运行时冲突扫描。

#### Scenario: 扫描成功执行
- **WHEN** `CardboardConfig.runtimeConflictScan = true` 且 compatDatabase 已加载
- **THEN** 创建 MixinConfigScanner 和 MixinAnnotationScanner，扫描所有 Mod，创建 MixinConflictDetector 执行检测，生成 ConflictReport 并输出到控制台

#### Scenario: JSON 报告输出
- **WHEN** 扫描完成且 `CardboardConfig.conflictScanJsonOutput = true`
- **THEN** 调用 report.writeJson() 写入 JSON 报告文件

#### Scenario: 扫描失败不阻止启动
- **WHEN** 扫描过程中抛出异常
- **THEN** 记录 WARN 日志，包含异常消息，继续正常启动流程

### Requirement: shouldApplyMixin() 中的 FATAL 冲突自动禁用
系统 SHALL 在 CardboardMixinPlugin.shouldApplyMixin() 中，检查当前 Mixin 是否在 FATAL 冲突集合中，如果是则自动禁用。

#### Scenario: FATAL 冲突自动禁用
- **WHEN** `scanResults != null` 且 `CardboardConfig.autoDisableFatalConflicts = true` 且 mixinClassName 在 FATAL 集合中
- **THEN** 记录 WARN 日志并返回 false，阻止该 Mixin 被应用

#### Scenario: 正常 Mixin 不受影响
- **WHEN** mixinClassName 不在 FATAL 集合中，或 autoDisableFatalConflicts = false
- **THEN** shouldApplyMixin() 继续执行后续检查逻辑，不受影响

### Requirement: 扫描结果缓存
系统 SHALL 将 FATAL 冲突的 Mixin 类名集合缓存为静态字段，供 shouldApplyMixin() 做 O(1) 查找。

#### Scenario: 静态字段存储
- **WHEN** 扫描完成
- **THEN** `fatalMixinSet` 静态字段包含所有 FATAL 冲突的 Cardboard Mixin 类名

## MODIFIED Requirements
### Requirement: CardboardMixinPlugin.java
在 onLoad() 中 compatDatabase 加载后插入扫描逻辑，在 shouldApplyMixin() 中 compatDatabase 检查后插入 FATAL 冲突检查。
