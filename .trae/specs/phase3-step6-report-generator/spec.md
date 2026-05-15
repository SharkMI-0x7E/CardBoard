# Phase 3 Step 6: 冲突报告生成器 Spec

## Why
将 MixinConflictDetector 检测到的冲突结果格式化为人类可读的控制台报告和机器可读的 JSON 文件，方便管理员查看和排查冲突问题。

## What Changes
- 新增 `ConflictReport.java`：报告生成器，支持控制台输出和 JSON 文件输出
- 新增 `report/ConflictReportEntry.java`：JSON 序列化 DTO，避免 MixinMethod 对象嵌套序列化
- 控制台报告按冲突级别分区（FATAL/HIGH/MEDIUM/LOW/已解决）
- JSON 报告包含完整的扫描元数据和冲突详情
- 行长度控制适配 Windows 终端（~75 字符）

## Impact
- 新增代码：
  - `src/main/java/org/cardboardpowered/conflict/ConflictReport.java`
  - `src/main/java/org/cardboardpowered/conflict/report/ConflictReportEntry.java`
- 依赖已有代码：`model/MixinConflict`、`model/MixinMethod`、`model/ConflictLevel`、`MixinConflictDetector`
- 使用项目已有 Gson 库进行 JSON 序列化
- 无破坏性变更

## ADDED Requirements
### Requirement: 控制台报告输出
系统 SHALL 使用 SLF4J Logger 输出格式化的冲突报告，包含以下分区：
1. 扫描摘要（Cardboard mixin 数量、其他 Mod 数量、扫描耗时）
2. FATAL conflicts (N)
3. HIGH conflicts (N)
4. MEDIUM conflicts (N)
5. LOW conflicts (N)
6. Resolved Conflicts (N) — 已解决冲突单独分区
7. Summary 汇总行

#### Scenario: 无冲突时输出
- **WHEN** 检测到 0 个冲突
- **THEN** 控制台输出 "No conflicts detected" 和扫描摘要

#### Scenario: 有冲突时输出
- **WHEN** 检测到 FATAL/HIGH/MEDIUM/LOW 冲突
- **THEN** 按级别分区输出，每个冲突包含目标类方法、Cardboard Mixin 信息、其他 Mod 信息、修复建议

#### Scenario: 已解决冲突输出
- **WHEN** 冲突的 `isResolved=true`
- **THEN** 该冲突放在 "Resolved Conflicts (N)" 分区，而非按级别分区

#### Scenario: 终端行长度限制
- **WHEN** 输出修复建议等长文本
- **THEN** 每条信息控制在 ~75 字符内，超长部分换行并对齐到缩进位置

### Requirement: JSON 报告输出
系统 SHALL 输出 JSON 格式报告文件到 `config/cardboard/conflict-report.json`，使用 Gson 序列化。

#### Scenario: DTO 转换避免嵌套
- **WHEN** 序列化 MixinConflict 为 JSON
- **THEN** 使用 ConflictReportEntry DTO，只保留关键字段（类名、注解类型、目标方法），不直接序列化 MixinMethod 对象

#### Scenario: JSON 包含完整元数据
- **WHEN** 生成 JSON 报告
- **THEN** 包含 timestamp、scanDurationMs、cardboardMixinCount、otherModCount、conflicts（fatal/high/medium/low 数组）

### Requirement: 报告数据输入
ConflictReport SHALL 接收以下输入：
- `List<MixinConflict>` 冲突列表
- 扫描统计信息（cardboardMixinCount、otherModCount、scanDurationMs）

#### Scenario: 构建报告
- **WHEN** new ConflictReport(conflicts, stats) 被调用
- **THEN** 内部按级别分组冲突，区分已解决和未解决冲突
