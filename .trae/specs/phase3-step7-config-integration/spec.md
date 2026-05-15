# Phase 3 Step 7: 配置集成 Spec

## Why
将 Phase 3 冲突检测工具的开关和参数暴露给管理员，通过 cardboard-config.yml 配置运行时扫描、JSON 输出、自动禁用 FATAL 冲突等行为。

## What Changes
- 修改 `CardboardConfig.java`：新增 3 个静态配置字段 + ConfigSection + setup() 中读取逻辑
- 修改 `cardboard-config.yml`（通过 DEFAULT_CONF 生成）：新增默认值

## Impact
- 修改代码：`src/main/java/org/cardboardpowered/CardboardConfig.java`
- 无破坏性变更，仅新增配置项

## ADDED Requirements
### Requirement: 新增配置项
系统 SHALL 在 cardboard-config.yml 中新增以下配置项：
1. `runtime_conflict_scan`（默认 true）：启动时自动扫描所有 Mod 的 Mixin 冲突
2. `conflict_scan_json_output`（默认 false）：输出冲突报告为 JSON 文件
3. `auto_disable_fatal_conflicts`（默认 false）：自动禁用 FATAL 级别的冲突 Mixin

#### Scenario: 默认配置生成
- **WHEN** 首次启动服务器，cardboard-config.yml 不存在
- **THEN** 自动生成的配置文件包含新配置项和对应默认值

#### Scenario: 配置读取
- **WHEN** CardboardConfig.setup() 执行
- **THEN** 从配置文件读取 3 个新配置项的值并赋值到静态字段

### Requirement: 配置节结构
新增配置应使用独立的 ConfigSection，命名为 "mixin-conflict-detection"，包含注释说明每个配置的用途。

#### Scenario: 配置节格式
- **WHEN** 查看生成的 YAML 文件
- **THEN** 新配置项在独立的 "# Mixin Conflict Detection" 注释块中

## MODIFIED Requirements
### Requirement: CardboardConfig.java
在 DEFAULT_CONF 链式调用中新增一个 ConfigSection，在 setup() 中新增配置读取逻辑。

#### Scenario: 新增静态字段
- **WHEN** 其他代码引用配置
- **THEN** 通过 `CardboardConfig.runtimeConflictScan`、`CardboardConfig.conflictScanJsonOutput`、`CardboardConfig.autoDisableFatalConflicts` 访问
