# Phase 3: Mixin 冲突检测工具 - 详细实施计划

> **版本**: 1.0
> **创建日期**: 2026-5-14
> **前置依赖**: Phase 1 (部分完成), Phase 2 (已完成)
> **预计工期**: 3-4 周

---

## 1. 目标

开发一个运行时 Mixin 冲突检测工具，在服务器启动时自动扫描所有已加载 Mod 的 Mixin 配置和字节码，检测并报告 Cardboard 与其他 Mod 之间的潜在 Mixin 冲突。

### 1.1 核心价值

| 当前痛点 | Phase 3 解决方案 |
|----------|-----------------|
| 冲突只在运行时崩溃时才发现 | 启动时主动扫描，提前预警 |
| 依赖手动维护的 mod-compatibility.yml | 自动检测未知 Mod 的冲突 |
| 用户不知道为什么服务器崩溃 | 输出清晰的冲突报告，包含修复建议 |

### 1.2 成功标准

| 指标 | 目标 |
|------|------|
| 能扫描所有已加载 Mod 的 Mixin 配置 | 100% |
| 能检测 @Overwrite vs @Overwrite 冲突 | 100% |
| 能检测 @Overwrite vs @Inject 冲突 | 100% |
| 能检测 @Redirect 竞争冲突 | 80%+ |
| 扫描耗时 | < 3 秒 |
| 误报率 | < 10% |
| 与 Phase 2 兼容性数据库集成 | 100% |

---

## 2. 技术方案

### 2.1 架构总览

```
┌──────────────────────────────────────────────────────────────┐
│                    MixinConflictScanner                       │
│                    (主扫描器，入口类)                           │
├──────────────────────────────────────────────────────────────┤
│                                                               │
│  Step 1: 扫描                                                 │
│  ├─ MixinConfigScanner: 扫描所有 Mod 的 *.mixins.json         │
│  ├─ MixinAnnotationScanner: ASM 字节码分析，提取注解信息        │
│  └─ MixinClassInfo / MixinMethod: 数据模型                    │
│                                                               │
│  Step 2: 分析                                                 │
│  ├─ MixinConflictDetector: 冲突检测核心算法                    │
│  ├─ MixinConflict: 冲突数据模型 (级别 + 类型 + 详情)           │
│  └─ MappingBridge: 映射转换 (intermediate → Mojang)           │
│                                                               │
│  Step 3: 报告                                                 │
│  ├─ ConflictReport: 格式化报告生成                             │
│  ├─ ConflictReportFormatter: 控制台/日志/JSON 输出             │
│  └─ AutoFixSuggester: 自动修复建议                             │
│                                                               │
│  Step 4: 集成                                                 │
│  ├─ CardboardConfig: 新增配置项                                │
│  ├─ CardboardMixinPlugin: onLoad() 中触发扫描                  │
│  └─ ModCompatibilityDatabase: 合并自动检测结果                 │
│                                                               │
└──────────────────────────────────────────────────────────────┘
```

### 2.2 关键技术决策

| 决策 | 选择 | 理由 |
|------|------|------|
| 字节码分析工具 | ASM (已有依赖) | 项目中 RemapUtils.java 已大量使用 ASM，无需引入新依赖 |
| Mixin 信息获取方式 | 扫描 JAR + ASM 分析 | 不依赖 Mixin 内部 API（不稳定），不依赖 Class.forName（ClassLoader 隔离） |
| 映射转换 | FabricLoader.getMappingResolver() | 其他 Mod 可能使用 intermediary 映射，需要转换到 Mojang 才能比较 |
| 扫描时机 | CardboardMixinPlugin.onLoad() | 在 Mixin 应用之前完成扫描，可以在 shouldApplyMixin() 中使用结果 |
| 报告输出 | SLF4J Logger + 可选 JSON 文件 | Logger 保证兼容性，JSON 方便工具解析 |

---

## 3. 类设计与文件结构

### 3.1 新增文件

```
src/main/java/org/cardboardpowered/conflict/
├── MixinConflictScanner.java          # 主扫描器 (入口)
├── MixinConfigScanner.java            # mixin.json 配置扫描
├── MixinAnnotationScanner.java        # ASM 字节码注解分析
├── MixinConflictDetector.java         # 冲突检测算法
├── MappingBridge.java                 # 映射转换桥接
├── model/
│   ├── MixinClassInfo.java            # Mixin 类信息模型
│   ├── MixinMethod.java               # Mixin 方法信息模型
│   ├── MixinConfigData.java           # mixin.json 解析结果
│   ├── MixinConflict.java             # 冲突数据模型
│   └── ConflictLevel.java             # 冲突级别枚举
└── report/
    ├── ConflictReport.java            # 报告生成器
    └── AutoFixSuggester.java          # 自动修复建议
```

### 3.2 修改文件

| 文件 | 修改内容 |
|------|---------|
| `CardboardConfig.java` | 新增 `runtime_conflict_scan` 和 `conflict_scan_json_output` 配置项 |
| `CardboardMixinPlugin.java` | 在 `onLoad()` 中触发扫描，在 `shouldApplyMixin()` 中使用扫描结果 |
| `ModCompatibilityDatabase.java` | 新增 `mergeAutoDetectedConflicts()` 方法 |
| `cardboard-config.yml` | 新增配置项 |

### 3.3 核心类设计

#### MixinClassInfo (数据模型)

```java
package org.cardboardpowered.conflict.model;

public class MixinClassInfo {
    public String className;
    public boolean isMixin;
    public List<String> targetClasses = new ArrayList<>();
    public int priority = 1000;
    public String sourceMod;

    public List<MixinMethod> overwrites = new ArrayList<>();
    public List<MixinMethod> injections = new ArrayList<>();
    public List<MixinMethod> redirects = new ArrayList<>();
    public List<MixinMethod> modifyArgs = new ArrayList<>();
    public List<MixinMethod> modifyVariables = new ArrayList<>();
    public List<MixinMethod> modifyReturnValues = new ArrayList<>();
}
```

#### MixinMethod (数据模型)

```java
package org.cardboardpowered.conflict.model;

public class MixinMethod {
    public String name;
    public String descriptor;
    public String annotationType;  // "Overwrite", "Inject", "Redirect", etc.
    public List<String> targetMethods = new ArrayList<>();
    public List<String> atValues = new ArrayList<>();
    public List<String> atTargets = new ArrayList<>();
    public boolean cancellable;
}
```

#### MixinConflict (冲突模型)

```java
package org.cardboardpowered.conflict.model;

public class MixinConflict {
    public ConflictLevel level;       // FATAL, HIGH, MEDIUM, LOW
    public String conflictType;       // "OVERWRITE_OVERWRITE", "OVERWRITE_INJECT", etc.
    public String targetClass;        // 冲突发生的目标类
    public String targetMethod;       // 冲突发生的目标方法
    public String cardboardMixin;     // Cardboard 的 Mixin 类名
    public String otherModId;         // 另一个 Mod 的 ID
    public String otherMixin;         // 另一个 Mod 的 Mixin 类名
    public String suggestion;         // 修复建议
}
```

#### ConflictLevel (冲突级别)

```java
package org.cardboardpowered.conflict.model;

public enum ConflictLevel {
    FATAL,    // 双 @Overwrite，必然崩溃
    HIGH,     // @Overwrite vs @Inject，可能导致 @Inject 失效
    MEDIUM,   // @Redirect 竞争，行为不确定
    LOW       // @Inject 共存，通常无害但顺序依赖
}
```

---

## 4. 实施步骤

### Step 1: 数据模型层 (预计 1 天)

**目标**: 创建所有数据模型类

**文件**:
- `model/MixinClassInfo.java`
- `model/MixinMethod.java`
- `model/MixinConfigData.java`
- `model/MixinConflict.java`
- `model/ConflictLevel.java`

**验证**: 编译通过，模型类可正确序列化/反序列化

---

### Step 2: Mixin 配置扫描器 (预计 2 天)

**目标**: 扫描所有已加载 Mod 的 `*.mixins.json` 配置文件

**文件**: `MixinConfigScanner.java`

**核心逻辑**:
1. 遍历 `FabricLoader.getInstance().getAllMods()`
2. 对每个 Mod，通过 `ModContainer.getRootPath()` 获取 JAR/目录路径
3. 扫描路径中的 `*.mixins.json` 文件
4. 解析 JSON，提取 `package`、`mixins`、`server`、`client` 列表
5. 同时处理 JAR 文件和目录两种情况（生产环境 vs 开发环境）

**跳过列表**:
- `minecraft`、`fabricloader`、`java`、`cardboard`（自身在 Step 3 单独扫描）

**JAR 扫描性能优化 (v1.0)**:
- **快速预检机制**: 在打开 JAR 进行完整 ASM 扫描之前，先快速检查 JAR 内是否存在 `*.mixins.json` 文件
- 如果 JAR 中没有 `*.mixins.json` → **直接跳过整个 JAR 的后续 ASM 字节码分析**，节省大量扫描时间
- 预检步骤: 使用 `ZipFile.entries()` 快速遍历，遇到第一个 `*.mixins.json` 即停止预检并标记为 "has mixins"
- 预检耗时通常 < 50ms/JAR，完整 ASM 扫描可能 > 500ms/JAR，节省约 10 倍时间
- 开发环境目录不需要预检（目录内文件数量通常有限）

**验证**: 能正确列出所有 Mod 的 Mixin 配置和类列表

---

### Step 3: ASM 字节码注解分析器 (预计 3 天)

**目标**: 从 .class 文件字节码中提取 Mixin 注解信息

**文件**: `MixinAnnotationScanner.java`

**核心逻辑**:
1. 读取 .class 文件字节码
2. 使用 ASM `ClassReader` + `ClassNode` 解析
3. 提取 `@Mixin` 注解 → 目标类、优先级
4. 提取方法级注解:
   - `@Overwrite` → 方法名、描述符
   - `@Inject` → 目标方法、@At 位置、cancellable
   - `@Redirect` → 目标方法、@At INVOKE 目标
   - `@ModifyArg` → 目标方法、@At INVOKE 目标
   - `@ModifyVariable` → 目标方法
   - `@ModifyReturnValue` → 目标方法
   - `@WrapWithCondition` (MixinExtras) → 目标方法
   - `@WrapOperation` (MixinExtras) → 目标方法
   - `@ModifyExpressionValue` (MixinExtras) → 目标方法

**注解处理策略 (v1.0 鲁棒性优先)**:
- 已知注解 (上述列表) → 正常提取并记录到 `MixinClassInfo`
- 未知注解 → **跳过并记录一条 DEBUG 日志**，避免程序崩溃
- 确保工具本身在遇到未来新增的 Mixin/MixinExtras 注解时不会报错
- DEBUG 日志格式: `Unknown mixin annotation: {descriptor} in {className}.{methodName}`

**关键实现细节**:
- **注解描述符集中化**: 所有 Mixin 注解的描述符（如 `Lorg/spongepowered/asm/mixin/Overwrite;`）集中在 `MixinAnnotationDescriptor` 常量类中，不硬编码散落
- **`@At` 嵌套注解解析**: `@Inject(method="use", at=@At(value="INVOKE", target="..."))` 中 `@At` 是嵌套 `AnnotationNode`，需从 `AnnotationNode.values` 列表中找到 `at=` key，然后递归解析其内部的 `value=` 和 `target=` 字段
- **`@Mixin` 优先级提取**: 从 `@Mixin` 注解的 `values` 中找到 `priority=` key（默认 1000），处理 `Integer` 类型
- **跳过 `@Shadow`**: `@Shadow` 字段/方法不参与冲突检测，显式跳过，避免无用处理

**性能优化**:
- **ASM API 分层**: 先用 `ClassVisitor`（流式，省内存）快速过滤出 Mixin 类，再用 `ClassNode`（树式）详细解析有注解的方法
- **包名快速过滤**: Cardboard JAR 中包含大量非 Mixin 类（Bukkit API 实现等），遍历时只处理 `mixin/` 包下的 `.class` 文件
- **解析结果缓存**: 按类全限定名 `Map<String, MixinClassInfo>` 缓存，避免重复 ASM 开销

**复用现有基础设施**:
- 复用 `JarReader.java` 的 `JarFile.stream()` 遍历 `.class` 条目的模式
- 复用 `RemapUtils` 中的 `MyMappingResolver` 和 `MappingResolver` 做映射转换

**ASM 依赖注意**:
- `build.gradle` 中 `exclude(dependency('org.objectweb:asm:*'))` 排除了 ASM，ASM 通过 SpecialSource 等间接引入
- Step 3 需要**显式声明** ASM 依赖以确保版本可控: `implementation "org.ow2.asm:asm:9.8"` 和 `implementation "org.ow2.asm:asm-tree:9.8"`

**验证**: 能正确解析 Cardboard 自身的 236 个 Mixin 类的注解

---

### Step 4: 映射转换桥接 (预计 1 天)

**目标**: 将不同映射命名空间的类名/方法名统一到 Mojang 映射

**文件**: `MappingBridge.java`

**核心逻辑**:
1. 使用 `FabricLoader.getInstance().getMappingResolver()`
2. 将 intermediary 类名 (如 `class_1792`) 转换为 Mojang 类名 (如 `BoatItem`)
3. 将 intermediary 方法名 (如 `method_7836`) 转换为 Mojang 方法名 (如 `use`)
4. 缓存转换结果，避免重复查询

**关键 API**:
```java
MappingResolver resolver = FabricLoader.getInstance().getMappingResolver();
String mojangName = resolver.mapClassName("intermediary", intermediaryName);
```

**映射转换降级策略 (v1.0)**:
- 正常情况 → 返回 Mojang 映射名称
- 转换失败（MappingNotFoundException 等）→ **降级输出格式: `intermediary(unresolved):{originalName}`**
  - 例如: `intermediary(unresolved):class_1234`
  - 同时记录 WARN 日志: `Failed to map class "class_1234": {reason}`
- 降级后的名称仍可用于冲突比较（至少保证同一个 intermediary 名能匹配到）
- 保留原始 intermediary 名称，方便开发者后续排查映射缺失问题

**关键实现细节**:
- **复用 RemapUtils.MyMappingResolver**: 项目已有 `RemapUtils.getMyMappingResolver()` 封装了 MappingResolver。直接复用该实例，不重复初始化
- **输入格式约定**: Step 3 的 `parseTargetClasses` 已将 JVM 描述符转为点号格式。Step 4 接收点号格式输入（如 `net.minecraft.world.entity.vehicle.BoatItem`），不接收 JVM 描述符
- **intermediary 名称缓存**: 维护 `Map<String, String>` 缓存，同一个类名只查一次 MappingResolver
- **方法名映射的可行性注意**: `MappingResolver` 主要映射类名。方法名映射可能需要额外 tiny-mapping 文件，如果不可用则降级为原始方法名

**验证**: 能正确转换已知 Mod 的 Mixin 目标类名

---

### Step 5: 冲突检测算法 (预计 3 天)

**目标**: 实现核心冲突检测逻辑

**文件**: `MixinConflictDetector.java`

**检测规则**:

| 规则 | 条件 | 级别 | 说明 |
|------|------|------|------|
| R1 | 两个 Mod 的 @Overwrite 目标同一方法 | FATAL | Mixin 框架会抛异常，服务器无法启动 |
| R2 | @Overwrite + @Inject 目标同一方法 | HIGH | @Overwrite 覆盖整个方法，@Inject 失效 |
| R3 | @Overwrite + @Redirect 目标同一方法 | HIGH | @Redirect 的 INVOKE 目标被覆盖 |
| R4 | 两个 @Redirect 竞争同一 INVOKE 调用 | MEDIUM | 只有一个会生效，行为不确定 |
| R5 | 两个 @ModifyArg 竞争同一 INVOKE 调用 | MEDIUM | 两个都会生效但顺序不确定 |
| R6 | @Inject 共存于同一方法 | LOW | 通常兼容，但执行顺序依赖 priority |

**算法流程**:
```
1. 按目标类分组所有 Mixin（按 sourceModId + targetClass）
2. 对每个目标类:
   a. 按目标方法分组
   b. 对每个目标方法:
      - 收集所有 @Overwrite → 如果 >1 个且来自不同 Mod → FATAL
      - 收集所有 @Overwrite + 其他注入 → 如果来自不同 Mod → HIGH
      - 收集所有 @Redirect/@ModifyArg 的 INVOKE 目标 → 按 @At target 精确匹配 → 如果重复 → MEDIUM
      - 收集所有 @Inject → 如果 >5 个来自不同 Mod → LOW (信息)
3. 过滤: 排除 sourceModId 相同的组合（自冲突不是跨 Mod 冲突）
4. 合并已有规则: 如果某个冲突在 mod-compatibility.yml 中已有处理 → 标记 isResolved=true
5. 生成 MixinConflict 列表
```

**关键实现细节**:
- **@At target 精确匹配**: 两个 `@Redirect` 的 `method` 都是 `use`，但 `@At(INVOKE, target="LItemStack;isEmpty()Z")` 不同 → 不算冲突。只有 method + @At target 都相同才算
- **通配符处理**: `method` 字段支持通配符（如 `method_7836` 或 `*`）。需要先展开通配符为精确方法名列表，再做冲突匹配，避免漏检
- **自冲突过滤**: 同一 Mod 的两个 Mixin 类 @Overwrite 同一方法不属于"跨 Mod 冲突"，应排除。检测条件: `cardboard.sourceModId != other.sourceModId`
- **已有规则优先**: `mod-compatibility.yml` 中的规则应在检测结果中标记为 `isResolved=true, resolutionNote="Disabled in mod-compatibility.yml"`
- **R6 阈值控制**: @Inject 共存通常无害。只在 @Inject 数量超过 5 个时报告 LOW，避免大量噪音
- **预构建快速查找**: 避免在 `shouldApplyMixin` 中做 O(N) 遍历。预构建 `Map<String, Set<String>>`（目标类+方法 → 冲突 Mixin 集合），`shouldApplyMixin` 只做 O(1) 查找

**验证**: 能正确检测已知冲突（carpet-tis-addition、minimotd、fabric-api）

---

### Step 6: 报告生成器 (预计 2 天)

**目标**: 生成格式化的冲突报告

**文件**: `ConflictReport.java`, `AutoFixSuggester.java`

**输出格式**:

**控制台输出 (SLF4J)**:
```
[Cardboard] Mixin Conflict Detection Report
[Cardboard] Scanned 236 Cardboard mixins, 12 other mods with mixins
[Cardboard] 
[Cardboard] FATAL conflicts (0):
[Cardboard]   None
[Cardboard] 
[Cardboard] HIGH conflicts (1):
[Cardboard]   [1] Target: net.minecraft.world.level.Level#updateSkyLight
[Cardboard]       Cardboard: LevelMixin @Overwrite [priority=1000]
[Cardboard]       Other: lithium LevelMixin @Redirect [priority=1000]
[Cardboard]       Suggestion: Consider replacing @Overwrite with @Inject
[Cardboard] 
[Cardboard] MEDIUM conflicts (0):
[Cardboard]   None
[Cardboard] 
[Cardboard] LOW conflicts (3):
[Cardboard]   [1] Target: net.minecraft.server.network.ServerGamePacketListenerImpl#tick
[Cardboard]       Cardboard: ServerGamePacketListenerImplMixin @Inject [priority=1000]
[Cardboard]       Other: carpet CarpetServerGamePacketListenerImplMixin @Inject [priority=1000]
[Cardboard]       Note: Both inject at HEAD, should coexist safely
[Cardboard] 
[Cardboard] Summary: 0 FATAL, 1 HIGH, 0 MEDIUM, 3 LOW
[Cardboard] Scan completed in 1.2s
```

**JSON 输出 (可选，写入 config/cardboard/conflict-report.json)**:
```json
{
  "timestamp": "2026-07-02T10:30:00Z",
  "scanDurationMs": 1200,
  "cardboardMixinCount": 236,
  "otherModCount": 12,
  "conflicts": {
    "fatal": [],
    "high": [
      {
        "targetClass": "net.minecraft.world.level.Level",
        "targetMethod": "updateSkyLight",
        "cardboardMixin": "org.cardboardpowered.mixin.world.level.LevelMixin",
        "otherModId": "lithium",
        "otherMixin": "me.jellysquid.mod.lithium.mixin.LevelMixin",
        "suggestion": "Consider replacing @Overwrite with @Inject"
      }
    ],
    "medium": [],
    "low": []
  }
}
```

**自动修复建议**:
- FATAL → "Disable one of the conflicting @Overwrite mixins via mod-compatibility.yml"
- HIGH → "Consider replacing @Overwrite with @Inject/@ModifyArg in Cardboard"
- MEDIUM → "Check if both @Redirect are needed, adjust priority if necessary"
- LOW → "No action needed, both @Inject should coexist"

**关键实现细节**:
- **JSON 序列化用 Gson**: 项目已有 Gson。`MixinConflict.cardboardMethod` 和 `otherMethod` 是 `MixinMethod` 对象，直接序列化会输出嵌套 JSON。应使用 DTO 转换（如 `ConflictReportEntry`），只保留关键字段（类名、注解类型、目标方法）
- **控制台报告行长度限制**: Windows 终端默认 ~80 字符宽度。每条冲突信息应控制在 ~75 字符内，长信息（如修复建议）换行并对齐到缩进位置
- **已解决冲突单独分区**: `isResolved=true` 的冲突应放在 "Resolved Conflicts (N)" 分区，与未解决的分开。管理员能看到工具自动处理了哪些冲突

**验证**: 报告格式正确，信息完整，可读性好

---

### Step 7: 配置集成 (预计 1 天)

**目标**: 将冲突检测工具集成到 Cardboard 的配置和启动流程中

**修改文件**:
- `CardboardConfig.java` — 新增配置项
- `cardboard-config.yml` — 新增默认值

**新增配置项**:

```yaml
# Mixin Conflict Detection
# When enabled, Cardboard scans all loaded mods at startup
# and reports potential Mixin conflicts.
runtime_conflict_scan: true

# Output conflict report as JSON file
# Saved to config/cardboard/conflict-report.json
conflict_scan_json_output: false

# Auto-disable FATAL conflict mixins
# Only applies to FATAL level conflicts (double @Overwrite)
# Default: false (admins should review report before auto-disabling)
auto_disable_fatal_conflicts: false
```

**关键实现细节**:
- **与现有配置系统保持一致**: 使用现有的 `ConfigSection` + `ConfigEntry` DSL 模式（如 `addSection(new ConfigSection("mixin-conflict-detection"))`），不直接硬编码 YAML
- **配置项命名一致性**: 现有配置使用下划线（如 `auto_conflict_resolution`）。新增配置项保持一致风格
- **默认值考虑生产环境**: `auto_disable_fatal_conflicts` 默认值为 `false`。自动禁用 Mixin 可能改变服务器行为，管理员应先看到报告再决定是否启用

**验证**: 配置项可正确读取，默认值合理

---

### Step 8: 启动流程集成 (预计 2 天)

**目标**: 将扫描器集成到 CardboardMixinPlugin 的生命周期中

**修改文件**:
- `CardboardMixinPlugin.java` — onLoad() 中触发扫描，shouldApplyMixin() 中使用结果
- `ModCompatibilityDatabase.java` — 合并自动检测结果

**集成点 1: onLoad()**
```java
@Override
public void onLoad(String mixinPackage) {
    // ... existing code ...
    
    if (CardboardConfig.autoConflictResolution) {
        compatDatabase = ModCompatibilityDatabase.load();
        compatDatabase.generateStartupReport();
    }
    
    // Phase 3: Runtime conflict scan
    // MUST run early in onLoad() - results cached as static for shouldApplyMixin()
    if (CardboardConfig.runtimeConflictScan) {
        try {
            MixinAnnotationScanner asmScanner = new MixinAnnotationScanner();
            MixinConfigScanner configScanner = new MixinConfigScanner();
            List<MixinConfigData> configs = configScanner.scanAllMods();
            List<MixinClassInfo> classInfos = asmScanner.scanAllConfigs(configs);
            
            MixinConflictDetector detector = new MixinConflictDetector();
            scanResults = detector.detect(classInfos);
            
            ConflictReport report = new ConflictReport(scanResults);
            report.printConsole();
            
            if (CardboardConfig.conflictScanJsonOutput) {
                report.writeJson();
            }
        } catch (Exception e) {
            logger.warn("Mixin conflict scan failed: {}. Mixins will load without conflict checks.", e.getMessage());
        }
    }
}
```

**集成点 2: shouldApplyMixin()**
```java
@Override
public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
    // ... existing disabledMixins and compatDatabase checks ...
    
    // Phase 3: Auto-disable FATAL conflicts (O(1) lookup via pre-built set)
    if (scanResults != null && CardboardConfig.autoDisableFatalConflicts) {
        if (FATAL_MIXIN_SET.contains(mixinClassName)) {
            logger.warn("Auto-disabling mixin '" + mixinClassName + 
                "' due to FATAL conflict");
            return false;
        }
    }
    
    return true;
}
```

**关键实现细节**:
- **扫描在 onLoad() 早期执行**: `shouldApplyMixin()` 在 Mixin 加载过程中会被调用数百次。扫描必须在 `onLoad()` 中一次性完成，结果缓存到 `static` 变量，`shouldApplyMixin()` 只做 O(1) 查找
- **复用现有的 shouldApplyMixin 模式**: 现有代码第 91-98 行已有兼容数据库检查逻辑。新增的冲突检测应合并到同一个流程中，不重复遍历
- **扫描失败不阻止服务器启动**: 整个扫描流程在 try-catch 中，失败时记录 WARN 并返回空结果，不阻止 Mixin 加载
- **预构建 Set 快速查找**: 在 `detect()` 完成后预构建 `Set<String> FATAL_MIXIN_SET`（包含所有 FATAL 冲突的 Cardboard Mixin 类名）。`shouldApplyMixin` 只做 `FATAL_MIXIN_SET.contains()` O(1) 操作，避免每次调用都做 N 次遍历

**验证**: 启动时自动扫描，FATAL 冲突自动禁用，报告正确输出

---

### Step 9: 端到端测试 (预计 2 天)

**目标**: 验证整个工具链的正确性

**测试场景**:

| 场景 | 预期结果 |
|------|---------|
| 只有 Cardboard，无其他 Mod | 报告 "No conflicts detected" |
| Cardboard + fabric-api | 检测到 RecipeMapMixin 冲突（如果仍有 @Overwrite） |
| Cardboard + carpet-tis-addition | 检测到 BoatItemMixin 相关冲突（LOW 级别） |
| Cardboard + lithium | 检测到 LevelMixin 相关冲突 |
| Cardboard + minimotd | 检测到 ServerStatusMixin 冲突（LOW 级别，已解决） |
| 双 @Overwrite 场景 (模拟) | 检测为 FATAL，自动禁用 |
| runtime_conflict_scan = false | 不扫描，无输出 |
| conflict_scan_json_output = true | 生成 JSON 报告文件 |

**测试策略**:
- **单元测试优先**: 先为 `MixinAnnotationScanner`、`MixinConflictDetector`、`MappingBridge` 写单元测试，再跑端到端。集成测试需要 Fabric 运行时，调试困难
- **模拟字节码测试 ASM 解析**: 用 ASM `ClassWriter` 动态生成带 Mixin 注解的测试类字节码，验证 `analyzeClass()` 的解析正确性。不需要依赖真实的 Minecraft 类
- **历史场景标注**: 部分测试场景（如 fabric-api RecipeMapMixin、carpet BoatItemMixin）可能已被 Phase 2 修复。Step 9 应标注哪些是"历史已修复"场景，避免混淆

**验证方法**:
1. `gradlew build` 编译通过
2. 检查启动日志中的冲突报告
3. 检查 JSON 报告文件（如果启用）
4. 验证自动禁用逻辑（FATAL 冲突的 Mixin 不被应用）

---

## 5. 依赖关系图

```
Step 1: 数据模型
  │
  ├─→ Step 2: 配置扫描器
  │     │
  │     └─→ Step 3: ASM 注解分析器
  │           │
  │           ├─→ Step 4: 映射转换
  │           │     │
  │           │     └─→ Step 5: 冲突检测算法
  │           │           │
  │           │           └─→ Step 6: 报告生成器
  │           │                 │
  │           │                 └─→ Step 7: 配置集成
  │           │                       │
  │           │                       └─→ Step 8: 启动流程集成
  │           │                             │
  │           │                             └─→ Step 9: 端到端测试
  │           │
  │           └─→ (Step 4-9 同上)
  │
  └─→ (Step 2-9 同上)
```

**可并行的步骤**: Step 2 和 Step 3 可以并行开发（它们都依赖 Step 1 但互不依赖）

---

## 6. 风险与缓解

| 风险 | 概率 | 影响 | 缓解措施 |
|------|------|------|---------|
| ASM 字节码分析遇到非标准 Mixin 注解 | 中 | 低 | **对未知注解跳过并记录 DEBUG 日志**，确保 v1.0 鲁棒性 |
| 映射转换失败（intermediary → Mojang） | 中 | 中 | **降级输出 `intermediary(unresolved):class_1234`**，保留原始信息供排查 |
| 开发环境 getRootPath() 返回目录而非 JAR | 高 | 低 | 同时处理 JAR 和目录两种情况 |
| 扫描耗时过长 | 低 | 中 | **JAR 预检机制**: 先检查 `*.mixins.json` 是否存在，无则跳过 ASM 扫描 |
| MixinExtras 注解 (@WrapWithCondition, @WrapOperation, @ModifyExpressionValue) | 中 | 低 | v1.0 已知注解列表中已包含，未来新增注解走 DEBUG 日志降级 |
| Fabric 内部 Mod (fabric-api 分模块) | 低 | 低 | 跳过 `fabric-` 前缀的内部 Mod 或合并处理 |

---

## 7. 与 Phase 2 的集成

Phase 3 的自动扫描结果将与 Phase 2 的手动规则库互补：

```
启动时:
  1. 加载 mod-compatibility.yml (Phase 2 手动规则)
  2. 运行 MixinConflictScanner (Phase 3 自动扫描)
  3. 合并结果:
     - 手动规则的优先级更高（用户明确指定）
     - 自动扫描结果作为补充（检测未知冲突）
     - 如果手动规则已标记为 COMPATIBLE，自动扫描的 LOW 级别冲突不重复报告
  4. 输出合并后的报告
  5. 在 shouldApplyMixin() 中同时查询两个来源
```

---

## 8. 后续扩展 (Phase 3+)

| 扩展方向 | 描述 | 优先级 |
|---------|------|--------|
| `/cardboard conflicts` 命令 | 运行时查看冲突报告 | P1 |
| 冲突报告上传 | 自动上传到 GitHub Issue | P2 |
| Mod 白名单 | 跳过已知安全的 Mod 的扫描 | P2 |
| 历史对比 | 对比上次启动的冲突报告，发现新增冲突 | P3 |
| CI 集成 | 在 CI 中运行冲突检测，防止引入新冲突 | P3 |
