# Phase 3 Step 1: Mixin 冲突检测工具 - 数据模型层

## Why
Phase 3 Mixin冲突检测工具需要统一的数据模型来存储Mixin类信息、方法注解、配置数据和冲突详情。这些模型是整个冲突检测系统的数据基础，所有后续步骤（扫描器、分析器、检测算法、报告生成）都依赖这些模型。没有这些模型，后续步骤无法进行。

## What Changes
- 新增 `src/main/java/org/cardboardpowered/conflict/model/` 目录
- 新增 5 个数据模型类：
  - `ConflictLevel.java` - 冲突级别枚举（FATAL/HIGH/MEDIUM/LOW）
  - `MixinMethod.java` - Mixin方法信息模型（存储单个方法注解详情）
  - `MixinClassInfo.java` - Mixin类信息模型（存储完整类级别信息）
  - `MixinConfigData.java` - mixin.json配置解析结果模型
  - `MixinConflict.java` - 冲突数据模型（存储检测到的冲突详情）

## Impact
- 新增包：`org.cardboardpowered.conflict.model`
- 不影响现有代码（纯新增，无修改）
- 为Phase 3后续所有步骤提供数据模型基础：
  - Step 2 (MixinConfigScanner) 使用 MixinConfigData
  - Step 3 (MixinAnnotationScanner) 使用 MixinClassInfo + MixinMethod
  - Step 5 (MixinConflictDetector) 使用 MixinConflict + ConflictLevel
  - Step 6 (ConflictReport) 使用所有模型生成报告

## ADDED Requirements

### Requirement: 冲突级别枚举
系统 SHALL 定义冲突级别的枚举类型，用于标识冲突的严重程度，帮助管理员快速判断是否需要立即处理。

#### Scenario: 枚举值定义
- **WHEN** 查看 `ConflictLevel.java`
- **THEN** 应包含以下四个级别：
  - `FATAL` - 致命冲突：双@Overwrite，服务器必然崩溃
  - `HIGH` - 高级冲突：@Overwrite vs @Inject/@Redirect，可能导致注入失效
  - `MEDIUM` - 中级冲突：@Redirect/@ModifyArg竞争，行为不确定
  - `LOW` - 低级冲突：多@Inject共存，通常无害但顺序依赖

#### Scenario: 枚举提供描述信息
- **WHEN** 调用 `ConflictLevel.getDescription()` 方法
- **THEN** 返回该级别的中文可读描述
- **AND** FATAL返回"致命冲突：多个Mod的@Overwrite修改同一方法，必然崩溃"
- **AND** HIGH返回"高级冲突：@Overwrite与@Inject/Redirect共存，可能导致注入失效"
- **AND** MEDIUM返回"中级冲突：多个@Redirect/ModifyArg竞争，行为不确定"
- **AND** LOW返回"低级冲突：多个@Inject共存，通常兼容但顺序可能影响行为"

#### Scenario: 枚举提供严重程度数值
- **WHEN** 调用 `ConflictLevel.getSeverity()` 方法
- **THEN** 返回数值：FATAL=4, HIGH=3, MEDIUM=2, LOW=1
- **AND** 用于冲突排序，严重程度高的排在前面

### Requirement: Mixin方法信息模型
系统 SHALL 提供数据模型来存储单个Mixin方法的注解信息，包括注解类型、目标方法、注入点等详细信息。

#### Scenario: 核心字段定义
- **WHEN** 查看 `MixinMethod.java`
- **THEN** 应包含以下字段：
  - `name` (String) - Mixin方法名称（如 `cardboard$onInteract`）
  - `descriptor` (String) - 方法描述符（如 `(LCallbackInfo;)V`）
  - `annotationType` (String) - 注解类型（"Overwrite", "Inject", "Redirect", "ModifyArg", "ModifyVariable", "ModifyReturnValue", "WrapWithCondition"）
  - `targetMethods` (List<String>) - 目标方法名列表（@Inject/@Redirect的method参数）
  - `atValues` (List<String>) - @At注入点列表（如 "HEAD", "RETURN", "INVOKE"）
  - `atTargets` (List<String>) - @At目标方法列表（INVOKE类型时的目标方法签名）
  - `cancellable` (boolean) - 是否标记为cancellable（@Inject专用）
  - `priority` (int) - 方法级优先级（默认1000）

#### Scenario: 工具方法
- **WHEN** 调用 `isOverwrite()` 方法
- **THEN** 当annotationType为"Overwrite"时返回true
- **WHEN** 调用 `isInject()` 方法
- **THEN** 当annotationType为"Inject"时返回true
- **WHEN** 调用 `isRedirect()` 方法
- **THEN** 当annotationType为"Redirect"时返回true
- **WHEN** 调用 `getAtTargetKey()` 方法
- **THEN** 返回atTargets的第一个元素（用于冲突比较），为空时返回空字符串

### Requirement: Mixin类信息模型
系统 SHALL 提供数据模型来存储完整的Mixin类级别信息，包括目标类、优先级、来源Mod以及所有方法级注解的分组列表。

#### Scenario: 核心字段定义
- **WHEN** 查看 `MixinClassInfo.java`
- **THEN** 应包含以下字段：
  - `className` (String) - Mixin类全限定名（如 `org.cardboardpowered.mixin.BoatItemMixin`）
  - `isMixin` (boolean) - 是否为有效的Mixin类（有@Mixin注解）
  - `targetClasses` (List<String>) - 目标类列表（@Mixin的value，如 `["net.minecraft.world.entity.vehicle.BoatItem"]`）
  - `priority` (int) - Mixin优先级（默认1000）
  - `sourceModId` (String) - 来源Mod ID（如 "cardboard", "lithium", "carpet"）
  - `sourceJarPath` (String) - 来源JAR路径（用于调试）
  - `overwrites` (List<MixinMethod>) - 所有@Overwrite注解方法
  - `injections` (List<MixinMethod>) - 所有@Inject注解方法
  - `redirects` (List<MixinMethod>) - 所有@Redirect注解方法
  - `modifyArgs` (List<MixinMethod>) - 所有@ModifyArg注解方法
  - `modifyVariables` (List<MixinMethod>) - 所有@ModifyVariable注解方法
  - `modifyReturnValues` (List<MixinMethod>) - 所有@ModifyReturnValue注解方法
  - `wrapWithConditions` (List<MixinMethod>) - 所有@WrapWithCondition注解方法

#### Scenario: 工具方法
- **WHEN** 调用 `hasOverwrite()` 方法
- **THEN** 当overwrites列表非空时返回true
- **WHEN** 调用 `hasInject()` 方法
- **THEN** 当injections列表非空时返回true
- **WHEN** 调用 `hasRedirect()` 方法
- **THEN** 当redirects列表非空时返回true
- **WHEN** 调用 `getAllMethods()` 方法
- **THEN** 返回所有注解方法列表的合并结果（去重）
- **WHEN** 调用 `getTargetClass()` 方法
- **THEN** 返回targetClasses的第一个元素（通常只有一个目标类），为空时返回空字符串
- **WHEN** 调用 `getMethodCount()` 方法
- **THEN** 返回所有注解方法的总数

### Requirement: Mixin配置数据模型
系统 SHALL 提供数据模型来存储从 `*.mixins.json` 配置文件中解析出的数据，包括包名、Mixin类列表、refmap等信息。

#### Scenario: 核心字段定义
- **WHEN** 查看 `MixinConfigData.java`
- **THEN** 应包含以下字段：
  - `packageName` (String) - Mixin包名（如 `org.cardboardpowered.mixin`）
  - `mixins` (List<String>) - 通用Mixin类名列表（不带包名，如 `["BoatItemMixin", "BlockItemMixin"]`）
  - `server` (List<String>) - 服务端专用Mixin类名列表
  - `client` (List<String>) - 客户端专用Mixin类名列表
  - `refmap` (String) - 映射文件路径（如 `bukkitfabric.refmap.json`）
  - `sourceModId` (String) - 来源Mod ID（如 "cardboard"）
  - `configFileName` (String) - 配置文件名（如 `bukkitfabric.mixins.json`）
  - `configFilePath` (String) - 配置文件完整路径（用于调试）
  - `required` (boolean) - 是否为必需配置（required字段）
  - `minVersion` (String) - 最低Mixin版本要求

#### Scenario: 工具方法
- **WHEN** 调用 `getFullClassName(String mixinName)` 方法
- **THEN** 返回 `packageName + "." + mixinName`（将短类名转换为全限定名）
- **WHEN** 调用 `getAllMixins()` 方法
- **THEN** 返回 mixins + server + client 的合并列表
- **WHEN** 调用 `getMixinCount()` 方法
- **THEN** 返回所有Mixin类的总数
- **WHEN** 调用 `isServerOnly()` 方法
- **THEN** 当mixins和client为空，server非空时返回true

### Requirement: 冲突数据模型
系统 SHALL 提供数据模型来存储检测到的两个Mixin之间的冲突详情，包括冲突级别、类型、涉及的方法和类、修复建议等。

#### Scenario: 核心字段定义
- **WHEN** 查看 `MixinConflict.java`
- **THEN** 应包含以下字段：
  - `level` (ConflictLevel) - 冲突级别（FATAL/HIGH/MEDIUM/LOW）
  - `conflictType` (String) - 冲突类型标识（如 "OVERWRITE_OVERWRITE", "OVERWRITE_INJECT", "REDIRECT_REDIRECT"）
  - `targetClass` (String) - 冲突发生的目标类全限定名（Mojang映射，如 `net.minecraft.world.entity.vehicle.BoatItem`）
  - `targetMethod` (String) - 冲突发生的目标方法名（如 `use`）
  - `cardboardMixinClass` (String) - Cardboard的Mixin类全限定名
  - `cardboardMethod` (MixinMethod) - Cardboard的Mixin方法详情
  - `otherModId` (String) - 另一个Mod的ID（如 "lithium"）
  - `otherMixinClass` (String) - 另一个Mod的Mixin类全限定名
  - `otherMethod` (MixinMethod) - 另一个Mod的Mixin方法详情
  - `suggestion` (String) - 修复建议（人类可读）
  - `isResolved` (boolean) - 冲突是否已通过配置解决
  - `resolutionNote` (String) - 解决说明（如 "Disabled in mod-compatibility.yml"）

#### Scenario: 构造函数便捷方法
- **WHEN** 调用 `ofFatal(cardboardMethod, otherMethod, suggestion)` 静态方法
- **THEN** 返回一个level为FATAL的MixinConflict对象
- **WHEN** 调用 `ofHigh(cardboardMethod, otherMethod, suggestion)` 静态方法
- **THEN** 返回一个level为HIGH的MixinConflict对象
- **WHEN** 调用 `ofMedium(cardboardMethod, otherMethod, suggestion)` 静态方法
- **THEN** 返回一个level为MEDIUM的MixinConflict对象
- **WHEN** 调用 `ofLow(cardboardMethod, otherMethod, suggestion)` 静态方法
- **THEN** 返回一个level为LOW的MixinConflict对象

#### Scenario: 工具方法
- **WHEN** 调用 `isFatal()` 方法
- **THEN** 当level为FATAL时返回true
- **WHEN** 调用 `getConflictDescription()` 方法
- **THEN** 返回格式化的冲突描述（如 "FATAL: OVERWRITE_OVERWRITE on BoatItem.use"）
- **WHEN** 调用 `getShortId()` 方法
- **THEN** 返回唯一标识符（如 `FATAL_BoatItem_use_cardboard_vs_lithium`），用于日志追踪

### Requirement: 包组织结构
系统 SHALL 按照 `org.cardboardpowered.conflict.model` 包结构组织所有数据模型类。

#### Scenario: 文件位置
- **WHEN** 查看文件结构
- **THEN** 所有5个类位于 `src/main/java/org/cardboardpowered/conflict/model/` 目录
- **AND** 目录结构为 `org/cardboardpowered/conflict/model/`
- **AND** 所有类使用 `package org.cardboardpowered.conflict.model;` 声明

#### Scenario: 编码规范
- **WHEN** 查看任何模型类
- **THEN** 所有注释使用英文
- **AND** 所有字段使用public访问修饰符（简化后续ASM解析代码的访问）
- **AND** 所有类提供无参构造函数（Java默认）
- **AND** 所有类提供有意义的toString()方法实现
- **AND** 不使用emoji
