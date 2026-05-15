# Phase 3 Step 2: Mixin 配置扫描器 (MixinConfigScanner)

## Why
Phase 3 需要在服务器启动时自动扫描所有已加载 Mod 的 `*.mixins.json` 配置文件，提取每个 Mod 的 Mixin 包名、类列表等元数据。这是后续 ASM 字节码分析（Step 3）和冲突检测（Step 5）的数据基础。没有配置扫描器，就无法知道哪些 Mod 使用了 Mixin，以及它们的 Mixin 类在哪里。

## What Changes
- 新增 `src/main/java/org/cardboardpowered/conflict/MixinConfigScanner.java`
- 不修改任何现有文件（纯新增）

## Impact
- 新增类：`org.cardboardpowered.conflict.MixinConfigScanner`
- 依赖：`org.cardboardpowered.conflict.model.MixinConfigData`（Step 1 已创建）
- 依赖：FabricLoader API（项目中已广泛使用）
- 为 Step 3 (MixinAnnotationScanner) 提供待扫描的 Mixin 类列表

## ADDED Requirements

### Requirement: Mod 遍历
系统 SHALL 通过 `FabricLoader.getInstance().getAllMods()` 获取所有已加载的 Mod 列表。

#### Scenario: 获取所有 Mod
- **WHEN** 调用 `scanAllMods()` 方法
- **THEN** 遍历 `FabricLoader.getInstance().getAllMods()` 返回的所有 Mod
- **AND** 对每个 Mod 调用 `ModContainer.getRootPath()` 获取其根路径

### Requirement: 跳过列表
系统 SHALL 跳过不需要扫描的 Mod，避免冗余和不必要的处理。

#### Scenario: 跳过系统 Mod
- **WHEN** 遍历 Mod 列表时
- **THEN** 跳过以下 Mod ID：
  - `minecraft` - Minecraft 自身
  - `fabricloader` - Fabric Loader 自身
  - `java` - Java 运行时
  - `cardboard` - Cardboard 自身（在 Step 3 中单独处理）

### Requirement: Mixin 配置文件发现
系统 SHALL 在 Mod 的 JAR 文件或目录中扫描 `*.mixins.json` 文件。

#### Scenario: 在 JAR 文件中查找
- **WHEN** Mod 的 `getRootPath()` 返回 JAR 文件路径
- **THEN** 打开 JAR 文件，查找所有匹配 `*.mixins.json` 的文件
- **AND** 支持嵌套目录结构（如 `org/example/mod.mixins.json`）

#### Scenario: 在目录中查找（开发环境）
- **WHEN** Mod 的 `getRootPath()` 返回目录路径（如开发环境中的 `build/classes/java/main/`）
- **THEN** 递归扫描目录，查找所有匹配 `*.mixins.json` 的文件
- **AND** 正确处理文件系统路径和 ZIP/JAR 文件系统

### Requirement: Mixin 配置文件解析
系统 SHALL 解析找到的 `*.mixins.json` 文件，提取关键信息并构建 `MixinConfigData` 对象。

#### Scenario: 解析 JSON 配置
- **WHEN** 读取到一个 `*.mixins.json` 文件
- **THEN** 解析 JSON 内容，提取以下字段：
  - `package` → `MixinConfigData.packageName`
  - `mixins` → `MixinConfigData.mixins`
  - `server` → `MixinConfigData.server`
  - `client` → `MixinConfigData.client`
  - `refmap` → `MixinConfigData.refmap`
  - `required` → `MixinConfigData.required`
  - `minVersion` → `MixinConfigData.minVersion`
- **AND** 设置 `sourceModId` 为 Mod 的 ID
- **AND** 设置 `configFileName` 为配置文件名
- **AND** 设置 `configFilePath` 为配置文件完整路径

#### Scenario: JSON 解析失败处理
- **WHEN** JSON 解析失败（格式错误、编码问题等）
- **THEN** 记录 warning 日志，包含 Mod ID 和文件路径
- **AND** 跳过该配置文件，继续处理下一个
- **AND** 不中断整个扫描流程

### Requirement: 扫描结果收集
系统 SHALL 收集所有解析成功的 `MixinConfigData` 对象，形成完整的扫描结果列表。

#### Scenario: 返回扫描结果
- **WHEN** 调用 `scanAllMods()` 方法完成
- **THEN** 返回 `List<MixinConfigData>` 包含所有解析成功的配置
- **AND** 结果按 Mod ID 排序（便于阅读和调试）

### Requirement: 日志输出
系统 SHALL 在扫描过程中输出关键信息，方便调试和验证。

#### Scenario: 扫描开始日志
- **WHEN** 开始扫描时
- **THEN** 输出日志：`"[Cardboard] Scanning mods for mixin configs..."`

#### Scenario: 扫描完成日志
- **WHEN** 扫描完成时
- **THEN** 输出日志：`"[Cardboard] Scanned {N} mods, found {M} mixin configs"`
- **AND** 其中 N 是扫描的 Mod 数量，M 是找到的配置文件数量

#### Scenario: 单个 Mod 发现日志（可选，调试模式）
- **WHEN** 在某个 Mod 中发现 `*.mixins.json` 文件
- **THEN** 输出日志：`"[Cardboard] Found mixin config '{filename}' in mod '{modId}'"`

### Requirement: 文件系统兼容性
系统 SHALL 正确处理不同环境下的文件系统差异。

#### Scenario: JAR 文件处理
- **WHEN** `getRootPath()` 返回的路径是 JAR 文件（`file:/path/to/mod.jar` 格式）
- **THEN** 使用 `ZipFile` 或 `FileSystem` API 读取 JAR 内容
- **AND** 正确处理 JAR 内的相对路径

#### Scenario: 目录处理（开发环境）
- **WHEN** `getRootPath()` 返回的路径是普通目录
- **THEN** 使用标准 `Files.walk()` 递归扫描
- **AND** 正确处理符号链接（跟随符号链接）

### Requirement: 编码规范
系统 SHALL 遵循项目编码规范。

#### Scenario: 代码风格
- **WHEN** 查看代码
- **THEN** 所有注释使用英文
- **AND** 不使用 emoji
- **AND** 使用 SLF4J 或 Log4j 进行日志输出（与项目一致）
- **AND** 所有字段和方法使用 `private` 访问修饰符，除非需要公开
