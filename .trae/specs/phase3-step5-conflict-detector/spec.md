# Phase 3 Step 5: Mixin 冲突检测算法 Spec

## Why
实现核心冲突检测逻辑，根据 R1-R6 规则扫描所有已解析的 Mixin 类信息，识别跨 Mod 的 Mixin 冲突，为后续报告生成和自动禁用提供数据基础。

## What Changes
- 新增 `MixinConflictDetector.java`：核心冲突检测算法
- 实现 R1-R6 六条检测规则（FATAL/HIGH/MEDIUM/LOW）
- 实现按目标类/方法分组、@At target 精确匹配、通配符展开
- 实现自冲突过滤（同一 Mod 内不报告跨 Mod 冲突）
- 实现预构建快速查找（Map<String, Set<String>> 供 shouldApplyMixin O(1) 查询）
- 与 ModCompatibilityDatabase 集成，标记已解决冲突

## Impact
- 新增代码：`src/main/java/org/cardboardpowered/conflict/MixinConflictDetector.java`
- 依赖已有代码：`model/MixinClassInfo`、`model/MixinMethod`、`model/MixinConflict`、`model/ConflictLevel`、`MappingBridge`、`ModCompatibilityDatabase`
- 无破坏性变更

## ADDED Requirements
### Requirement: 冲突检测算法
系统 SHALL 实现 MixinConflictDetector 类，接收 `List<MixinClassInfo>` 输入，输出 `List<MixinConflict>` 结果。

#### Scenario: R1 规则检测（双 @Overwrite FATAL 冲突）
- **WHEN** 两个不同 Mod 的 Mixin 类都使用 `@Overwrite` 注解同一目标方法
- **THEN** 生成一条 FATAL 级别的 MixinConflict，conflictType="OVERWRITE_OVERWRITE"

#### Scenario: R2 规则检测（@Overwrite vs @Inject HIGH 冲突）
- **WHEN** 一个 Mod 的 `@Overwrite` 和另一个 Mod 的 `@Inject` 目标同一方法
- **THEN** 生成一条 HIGH 级别的 MixinConflict，conflictType="OVERWRITE_INJECT"

#### Scenario: R3 规则检测（@Overwrite vs @Redirect HIGH 冲突）
- **WHEN** 一个 Mod 的 `@Overwrite` 和另一个 Mod 的 `@Redirect` 目标同一方法
- **THEN** 生成一条 HIGH 级别的 MixinConflict，conflictType="OVERWRITE_REDIRECT"

#### Scenario: R4 规则检测（双 @Redirect MEDIUM 冲突）
- **WHEN** 两个不同 Mod 的 `@Redirect` 具有相同的方法名和相同的 `@At(INVOKE, target="...")` 
- **THEN** 生成一条 MEDIUM 级别的 MixinConflict，conflictType="REDIRECT_REDIRECT"

#### Scenario: R5 规则检测（双 @ModifyArg MEDIUM 冲突）
- **WHEN** 两个不同 Mod 的 `@ModifyArg` 具有相同的方法名和相同的 `@At(INVOKE, target="...")` 
- **THEN** 生成一条 MEDIUM 级别的 MixinConflict，conflictType="MODIFYARG_MODIFYARG"

#### Scenario: R6 规则检测（多 @Inject 共存 LOW 冲突）
- **WHEN** 同一方法上有超过 5 个来自不同 Mod 的 `@Inject`
- **THEN** 生成一条 LOW 级别的 MixinConflict，conflictType="INJECT_INJECT"

#### Scenario: 通配符展开
- **WHEN** `@Mixin(method="method_*")` 使用通配符
- **THEN** 通配符应展开为匹配的方法名列表，再做冲突匹配

#### Scenario: 自冲突过滤
- **WHEN** 两个冲突的 Mixin 来自同一个 Mod（sourceModId 相同）
- **THEN** 不报告该冲突（不是跨 Mod 冲突）

#### Scenario: @At target 精确匹配
- **WHEN** 两个 `@Redirect` 的 `method` 相同但 `@At target` 不同
- **THEN** 不算冲突（只有 method + @At target 都相同才冲突）

### Requirement: 预构建快速查找
系统 SHALL 预构建 `Map<String, Set<String>>` 结构，key 为 "targetClass#targetMethod"，value 为冲突 Mixin 类名集合，供 shouldApplyMixin 做 O(1) 查找。

#### Scenario: 构建 FATAL Mixin 集合
- **WHEN** detect() 完成
- **THEN** 所有 FATAL 冲突中的 Cardboard Mixin 类名被放入 FATAL 集合，可通过 `getFatalMixinSet()` 获取

### Requirement: 已有规则标记
系统 SHALL 使用 `ModCompatibilityDatabase` 查询已知规则，如果冲突在 mod-compatibility.yml 中已有处理，标记 `isResolved=true`。

#### Scenario: 标记已解决冲突
- **WHEN** 检测到冲突且该冲突在 mod-compatibility.yml 中存在记录
- **THEN** 设置 `conflict.isResolved = true`，`conflict.resolutionNote = "已知规则：[规则描述]"`
