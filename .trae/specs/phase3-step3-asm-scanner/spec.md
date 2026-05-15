# Phase 3 Step 3: ASM 字节码注解分析器 (MixinAnnotationScanner)

## Why
Step 2 扫描到了所有 Mod 的 mixin.json 配置（知道有哪些 Mixin 类），但不知道每个类具体用了哪些注解（@Overwrite/@Inject/@Redirect 等）和它们的目标方法。Step 3 需要用 ASM 字节码分析从 .class 文件中提取这些注解信息，填充 Step 1 创建的 `MixinClassInfo` 和 `MixinMethod` 数据模型。这是冲突检测算法（Step 5）的直接数据源。

## What Changes
- 新增 `src/main/java/org/cardboardpowered/conflict/MixinAnnotationDescriptor.java` - 注解描述符常量类
- 新增 `src/main/java/org/cardboardpowered/conflict/MixinAnnotationScanner.java` - ASM 字节码分析器
- 修改 `build.gradle` - 显式声明 ASM 依赖
- 不修改其他现有文件

## Impact
- 新增 2 个类
- 修改 1 个构建文件
- 依赖 Step 1 的 `MixinClassInfo` 和 `MixinMethod` 模型
- 依赖 Step 2 的 `MixinConfigData` 输出（待扫描类列表）

## ADDED Requirements

### Requirement: 注解描述符常量类
系统 SHALL 提供 `MixinAnnotationDescriptor` 类集中定义所有已知的 Mixin/MixinExtras 注解描述符，避免硬编码散落。

#### Scenario: 核心 Mixin 注解描述符
- **WHEN** 查看 `MixinAnnotationDescriptor` 类
- **THEN** 应包含以下常量（完整 JVM 描述符格式）：
  - `Lorg/spongepowered/asm/mixin/Mixin;` - @Mixin
  - `Lorg/spongepowered/asm/mixin/Overwrite;` - @Overwrite
  - `Lorg/spongepowered/asm/mixin/injection/Inject;` - @Inject
  - `Lorg/spongepowered/asm/mixin/injection/Redirect;` - @Redirect
  - `Lorg/spongepowered/asm/mixin/injection/ModifyArg;` - @ModifyArg
  - `Lorg/spongepowered/asm/mixin/injection/ModifyVariable;` - @ModifyVariable
  - `Lorg/spongepowered/asm/mixin/injection/ModifyReturnValue;` - @ModifyReturnValue
  - `Lorg/spongepowered/asm/mixin/Shadow;` - @Shadow
  - `Lorg/spongepowered/asm/mixin/Unique;` - @Unique
  - `Lorg/spongepowered/asm/mixin/injection/WrapWithCondition;` - @WrapWithCondition

#### Scenario: MixinExtras 注解描述符
- **WHEN** 查看 `MixinAnnotationDescriptor` 类
- **THEN** 应包含以下 MixinExtras 常量：
  - `Lcom/llamalad7/mixinextras/spongepowered/WrapWithCondition;` - MixinExtras @WrapWithCondition（旧包名）
  - `Lde/siphalor/modmenu/mixinextras/WrapWithCondition;` - 备用路径
  - `Lcom/llamalad7/mixinextras/spongepowered/WrapOperation;` - @WrapOperation
  - `Lcom/llamalad7/mixinextras/spongepowered/ModifyExpressionValue;` - @ModifyExpressionValue

#### Scenario: 已知注解集合
- **WHEN** 调用 `KNOWN_INJECT_ANNOTATIONS` 集合
- **THEN** 返回所有方法级注入注解的描述符集合（不含 @Mixin 和 @Shadow）
- **AND** 用于快速判断一个注解是否是已知的注入注解

### Requirement: ASM 字节码分析
系统 SHALL 使用 ASM 库从 .class 文件字节码中提取 Mixin 注解信息。

#### Scenario: ClassReader + ClassNode 模式
- **WHEN** 分析一个 .class 文件
- **THEN** 使用 `ClassReader` 读取字节码
- **AND** 使用 `ClassNode` 获取完整的类树结构（包含注解信息）
- **AND** `ClassNode.visibleAnnotations` 包含类级注解（如 @Mixin）
- **AND** `ClassNode.methods` 中每个 `MethodNode.visibleAnnotations` 包含方法级注解

#### Scenario: 类级 @Mixin 注解提取
- **WHEN** 解析到 `@Mixin` 注解
- **THEN** 提取 `value` 字段（目标类列表，JVM 内部格式如 `Lnet/minecraft/.../BoatItem;`）
- **AND** 提取 `priority` 字段（int，默认 1000）
- **AND** 设置 `MixinClassInfo.isMixin = true`
- **AND** 设置 `MixinClassInfo.priority`
- **AND** 将目标类名添加到 `MixinClassInfo.targetClasses`

#### Scenario: 方法级 @Overwrite 注解提取
- **WHEN** 解析到方法上的 `@Overwrite` 注解
- **THEN** 创建 `MixinMethod` 对象
- **AND** 设置 `annotationType = "Overwrite"`
- **AND** 设置 `name` 为方法名
- **AND** 设置 `descriptor` 为方法描述符
- **AND** 添加到 `MixinClassInfo.overwrites` 列表

#### Scenario: 方法级 @Inject 注解提取
- **WHEN** 解析到方法上的 `@Inject` 注解
- **THEN** 创建 `MixinMethod` 对象
- **AND** 设置 `annotationType = "Inject"`
- **AND** 提取 `method` 字段 → `targetMethods`（可能是单个字符串或字符串数组）
- **AND** 提取 `cancellable` 字段 → `cancellable`
- **AND** 提取 `@At` 嵌套注解 → `atValues` 和 `atTargets`
- **AND** 添加到 `MixinClassInfo.injections` 列表

#### Scenario: 方法级 @Redirect 注解提取
- **WHEN** 解析到方法上的 `@Redirect` 注解
- **THEN** 创建 `MixinMethod` 对象
- **AND** 设置 `annotationType = "Redirect"`
- **AND** 提取 `method` 字段 → `targetMethods`
- **AND** 提取 `@At` 嵌套注解 → `atValues` 和 `atTargets`
- **AND** 添加到 `MixinClassInfo.redirects` 列表

#### Scenario: @At 嵌套注解解析
- **WHEN** 解析 `@Inject(method="use", at=@At(value="HEAD"))` 中的 `@At`
- **THEN** 从 `AnnotationNode.values` 列表中找到 key="at" 的元素
- **AND** 该元素的值是另一个 `AnnotationNode`（嵌套注解）
- **AND** 从嵌套 `AnnotationNode.values` 中提取 `value` 字段 → `atValues`
- **AND** 从嵌套 `AnnotationNode.values` 中提取 `target` 字段 → `atTargets`（target 可能是单个字符串或字符串数组）

#### Scenario: 未知注解处理
- **WHEN** 遇到不在已知注解集合中的方法级注解
- **THEN** 跳过该注解，不添加到任何列表
- **AND** 记录 DEBUG 级别日志：`Unknown mixin annotation: {descriptor} in {className}.{methodName}`
- **AND** 不抛出异常，不中断解析流程

#### Scenario: @Shadow 注解处理
- **WHEN** 遇到方法上的 `@Shadow` 注解
- **THEN** 跳过，不添加到任何 MixinMethod 列表
- **AND** 不记录日志（Shadow 方法不参与冲突检测，记录会增加噪音）

#### Scenario: 非 Mixin 类处理
- **WHEN** 扫描一个 .class 文件后发现没有 `@Mixin` 注解
- **THEN** 返回 `MixinClassInfo` 对象，但 `isMixin = false`
- **AND** 仍记录类名和方法信息（用于调试）

### Requirement: JAR/目录字节码读取
系统 SHALL 复用项目现有的 JAR 扫描模式来读取 .class 文件字节码。

#### Scenario: 从 JAR 文件读取
- **WHEN** 需要扫描 Mod JAR 中的类
- **THEN** 复用 `JarReader` 的模式：`JarFile.stream()` → 过滤 `.class` → 读取字节码
- **AND** 使用 `ClassReader(byte[], 0)` 从字节数组构建

#### Scenario: 从目录读取（开发环境）
- **WHEN** 需要扫描开发环境目录中的类
- **THEN** 使用 `Files.walk()` 递归查找 `.class` 文件
- **AND** 使用 `Files.readAllBytes(path)` 读取字节码

### Requirement: 批量扫描入口
系统 SHALL 提供 `scanAllConfigs(List<MixinConfigData>)` 方法，接受 Step 2 的输出作为输入。

#### Scenario: 批量扫描
- **WHEN** 调用 `scanAllConfigs(configs)` 方法
- **THEN** 对每个 `MixinConfigData`，遍历其所有 Mixin 类
- **AND** 从对应 Mod 的 JAR/目录中读取 .class 字节码
- **AND** 解析注解信息，构建 `MixinClassInfo` 对象
- **AND** 返回 `List<MixinClassInfo>` 包含所有解析结果
- **AND** 输出日志：`"[Cardboard] Analyzed {N} mixin classes from {M} configs"`

#### Scenario: 缓存机制
- **WHEN** 多次扫描同一个类
- **THEN** 使用 `Map<String, MixinClassInfo>` 缓存结果（按全限定名 key）
- **AND** 命中缓存时直接返回，不重复 ASM 解析

### Requirement: 性能优化
系统 SHALL 实现性能优化措施以减少扫描时间。

#### Scenario: 包名快速过滤
- **WHEN** 遍历 JAR 中的 .class 文件
- **THEN** 只处理 `mixin/` 包路径下的文件（路径包含 `/mixin/` 或以 `mixin/` 开头）
- **AND** 其他 .class 文件跳过，不进行 ASM 解析

#### Scenario: ClassVisitor 快速预检
- **WHEN** 需要判断一个类是否是 Mixin 类
- **THEN** 先用 `ClassVisitor`（流式）快速检查类是否有 `@Mixin` 注解
- **AND** 没有 `@Mixin` 注解的类直接跳过，不构建 `ClassNode`（省内存）

### Requirement: 错误处理
系统 SHALL 在解析过程中正确处理各种异常情况。

#### Scenario: .class 文件读取失败
- **WHEN** 读取某个 .class 文件时发生 IOException
- **THEN** 记录 WARN 日志，包含类名和错误信息
- **AND** 跳过该类，继续处理下一个
- **AND** 不中断整个扫描流程

#### Scenario: ASM 解析异常
- **WHEN** ASM 解析某个类时抛出异常（如格式错误）
- **THEN** 记录 WARN 日志
- **AND** 跳过该类
- **AND** 不中断整个扫描流程

#### Scenario: JSON 中的类名在 JAR 中不存在
- **WHEN** mixin.json 中列出了某个 Mixin 类，但 JAR 中找不到对应的 .class 文件
- **THEN** 记录 WARN 日志
- **AND** 跳过该类
- **AND** 继续处理其他类

### Requirement: ASM 依赖声明
系统 SHALL 确保 `build.gradle` 中显式声明 ASM 依赖。

#### Scenario: 依赖声明
- **WHEN** 查看 `build.gradle`
- **THEN** 包含 `implementation "org.ow2.asm:asm:9.8"`
- **AND** 包含 `implementation "org.ow2.asm:asm-tree:9.8"`
- **AND** 这些依赖不会被其他 exclude 规则影响
