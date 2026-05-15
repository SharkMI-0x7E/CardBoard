# Phase 3 Step 4: 映射转换桥接 (MappingBridge)

## Why
Step 3 提取的 Mixin 目标类名/方法名可能来自不同映射命名空间（intermediary/class_1234、official/net.minecraft、named/BoatItem）。冲突检测算法需要在统一的命名空间下比较这些名称。MappingBridge 提供统一的映射转换接口，将所有名称标准化到 Mojang 命名空间（named），并实现缓存和降级策略。

## What Changes
- 新增 `src/main/java/org/cardboardpowered/conflict/MappingBridge.java`
- 不修改任何现有文件（纯新增，但会调用 RemapUtils）

## Impact
- 新增类：`org.cardboardpowered.conflict.MappingBridge`
- 依赖：`RemapUtils.myMappingResolver`（项目已有）
- 依赖：FabricLoader MappingResolver API
- 为 Step 5 (MixinConflictDetector) 提供统一的名称转换接口

## ADDED Requirements

### Requirement: 映射转换接口
系统 SHALL 提供 `MappingBridge` 类，封装类名和方法名的映射转换逻辑。

#### Scenario: 类名转换（named → intermediary）
- **WHEN** 调用 `toIntermediary(String namedClassName)`
- **THEN** 使用 `FabricLoader.getMappingResolver().mapClassName("named", namedClassName)` 转换
- **AND** 返回 intermediary 格式（如 `class_1792`）
- **AND** 转换失败时返回 `intermediary(unresolved):{originalName}` 格式

#### Scenario: 类名转换（intermediary → named）
- **WHEN** 调用 `toNamed(String intermediaryClassName)`
- **THEN** 使用 `FabricLoader.getMappingResolver().mapClassName("intermediary", intermediaryClassName)` 转换
- **AND** 返回 named 格式（如 `BoatItem`）
- **AND** 转换失败时返回 `intermediary(unresolved):{originalName}` 格式

#### Scenario: 方法名转换
- **WHEN** 调用 `mapMethodName(String namespace, String className, String methodName, String descriptor)`
- **THEN** 使用 `FabricLoader.getMappingResolver().mapMethodName(namespace, className, methodName, descriptor)` 转换
- **AND** 转换失败时降级为原始方法名
- **AND** 记录 WARN 日志

### Requirement: 复用 RemapUtils
系统 SHALL 复用项目已有的 `RemapUtils.myMappingResolver`，不重复初始化。

#### Scenario: 获取 MappingResolver 实例
- **WHEN** MappingBridge 需要 MappingResolver
- **THEN** 通过 `RemapUtils.myMappingResolver` 获取已有实例
- **AND** 不创建新的 MappingResolver
- **AND** 如果 `myMappingResolver` 为 null（未初始化），降级为 `FabricLoader.getInstance().getMappingResolver()`

### Requirement: 缓存机制
系统 SHALL 缓存映射转换结果，避免重复查询。

#### Scenario: 类名缓存
- **WHEN** 调用 `toNamed("class_1792")`
- **THEN** 第一次查询 MappingResolver
- **AND** 将结果存入 `Map<String, String> classCache`
- **AND** 第二次调用相同参数时直接从缓存返回
- **AND** 缓存 key 格式：`{namespace}:{originalName}`（如 `intermediary:class_1792`）

#### Scenario: 方法名缓存
- **WHEN** 调用 `mapMethodName(...)`
- **THEN** 将结果存入 `Map<String, String> methodCache`
- **AND** 缓存 key 格式：`{namespace}:{className}:{methodName}:{descriptor}`

### Requirement: 降级策略
系统 SHALL 在映射转换失败时优雅降级，不中断流程。

#### Scenario: 类名转换失败
- **WHEN** MappingResolver 抛出 MappingNotFoundException 或 IllegalArgumentException
- **THEN** 返回降级格式：`intermediary(unresolved):{originalName}`
- **AND** 记录 WARN 日志：`Failed to map class "{originalName}": {reason}`
- **AND** 降级后的名称仍可用于冲突比较

#### Scenario: 方法名转换失败
- **WHEN** 方法名转换失败
- **THEN** 返回原始方法名
- **AND** 记录 DEBUG 日志：`Failed to map method "{className}.{methodName}": {reason}`
- **AND** 不中断流程

### Requirement: 统一命名空间
系统 SHALL 默认使用 named（Mojang）命名空间作为统一标准。

#### Scenario: 统一类名
- **WHEN** 调用 `normalizeClassName(String className)`
- **THEN** 如果 className 包含 `class_` 前缀，认为是 intermediary 格式，调用 `toNamed()`
- **AND** 如果 className 不包含 `class_` 前缀，认为是 named 格式，直接返回
- **AND** 返回统一的 named 格式

### Requirement: 编码规范
系统 SHALL 遵循项目编码规范。

#### Scenario: 代码风格
- **WHEN** 查看代码
- **THEN** 所有注释使用英文
- **AND** 不使用 emoji
- **AND** 使用 Log4j 进行日志输出
- **AND** 字段使用 private 访问修饰符，通过方法暴露

### Requirement: 缓存管理
系统 SHALL 提供缓存统计和清理方法。

#### Scenario: 缓存统计
- **WHEN** 调用 `getCacheStats()`
- **THEN** 返回缓存信息（类缓存大小、方法缓存大小、命中率）

#### Scenario: 缓存清理
- **WHEN** 调用 `clearCache()`
- **THEN** 清空所有缓存
