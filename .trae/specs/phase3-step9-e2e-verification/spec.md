# Phase 3 Step 9: 端到端验证 Spec

## Why
验证 Phase 3 整个工具链（扫描器 → ASM 分析 → 冲突检测 → 报告生成 → 启动集成）的编译正确性和组件协作，确保所有模块可以无缝协同工作。

## What Changes
- 创建 `ConflictDetectorUnitTest.java`：使用 ASM ClassWriter 生成测试字节码，验证 MixinAnnotationScanner 解析和 MixinConflictDetector 检测逻辑
- 创建 `MappingBridgeUnitTest.java`：验证 MappingBridge 的映射转换和降级策略
- 不需要创建完整的端到端集成测试（需要 Fabric 运行时）

## Impact
- 新增代码：
  - `src/test/java/org/cardboardpowered/conflict/ConflictDetectorUnitTest.java`
  - `src/test/java/org/cardboardpowered/conflict/MappingBridgeUnitTest.java`
- 无修改已有代码
- 无破坏性变更

## ADDED Requirements
### Requirement: 冲突检测单元测试
系统 SHALL 提供单元测试验证冲突检测算法的核心逻辑，无需 Fabric 运行时。

#### Scenario: R1 FATAL 冲突检测
- **WHEN** 两个不同 Mod 的 @Overwrite 目标同一方法
- **THEN** 检测到 FATAL 级别的 OVERWRITE_OVERWRITE 冲突

#### Scenario: R2 HIGH 冲突检测
- **WHEN** 一个 Mod 的 @Overwrite 和另一个 Mod 的 @Inject 目标同一方法
- **THEN** 检测到 HIGH 级别的 OVERWRITE_INJECT 冲突

#### Scenario: 自冲突过滤
- **WHEN** 两个冲突 Mixin 来自同一个 Mod
- **THEN** 不报告该冲突

### Requirement: ASM 解析器单元测试
系统 SHALL 使用 ASM ClassWriter 动态生成测试类字节码，验证 MixinAnnotationScanner 的解析能力。

#### Scenario: 解析 @Mixin 注解
- **WHEN** 使用 ASM 生成带 @Mixin 注解的测试类
- **THEN** MixinAnnotationScanner 能正确解析目标类和优先级

### Requirement: 映射转换降级策略验证
系统 SHALL 提供单元测试验证 MappingBridge 的降级策略。

#### Scenario: 转换失败降级
- **WHEN** 映射转换抛出异常（无 Fabric 运行时）
- **THEN** 返回 `intermediary(unresolved):{originalName}` 格式

## MODIFIED Requirements
### Requirement: build.gradle 测试依赖
添加 JUnit 5 测试依赖以支持单元测试。

#### Scenario: 运行测试
- **WHEN** 执行 `gradlew test`
- **THEN** 测试编译通过并执行
