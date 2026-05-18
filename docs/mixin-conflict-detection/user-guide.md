# Mixin 冲突检测工具 - 使用指南

> **版本**: 1.0
> **适用 Cardboard 版本**: 1.21.11+

---

## 简介

Mixin 冲突检测工具是 Cardboard 内置的运行时扫描器，用于在服务器启动时自动检测 Cardboard 与其他 Fabric Mod 之间的 Mixin 冲突。

### 它能做什么？

| 功能 | 说明 |
|------|------|
| 自动扫描 | 启动时扫描所有已加载 Mod 的 Mixin 配置和字节码 |
| 冲突检测 | 识别 6 种不同类型的 Mixin 冲突（FATAL / HIGH / MEDIUM / LOW） |
| 报告输出 | 控制台格式化报告 + 可选 JSON 文件 |
| 自动禁用 | 可配置自动禁用 FATAL 级别冲突的 Mixin |
| 兼容规则库 | 与 Phase 2 的 `mod-compatibility.yml` 手动规则互补 |

### 工作原理

```
服务器启动
  │
  ▼
1. 加载 mod-compatibility.yml（手动规则）
  │
  ▼
2. 扫描所有 Mod 的 *.mixins.json 配置文件
  │
  ▼
3. ASM 字节码分析，提取 @Mixin、@Overwrite、@Inject、@Redirect 等注解
  │
  ▼
4. 映射转换（intermediary → Mojang），统一命名空间
  │
  ▼
5. 冲突检测算法（R1-R6 六条规则）
  │
  ▼
6. 输出报告（控制台 + 可选 JSON）
  │
  ▼
7. 在 Mixin 加载时自动禁用 FATAL 冲突的 Mixin（如果启用）
```

---

## 配置

所有配置项位于 `config/cardboard/cardboard-config.yml` 中。

### 新增配置项

```yaml
# ==========================================
# Mixin Conflict Detection
# ==========================================

# 启动时自动扫描所有 Mod 的 Mixin 冲突
# 默认: true
runtime_conflict_scan: true

# 输出冲突报告为 JSON 文件（保存到 config/cardboard/conflict-report.json）
# 默认: false
conflict_scan_json_output: false

# 自动禁用 FATAL 级别冲突的 Mixin（双 @Overwrite 等必然崩溃的场景）
# 默认: false（建议先查看报告再决定是否启用）
auto_disable_fatal_conflicts: false
```

### 配置项详解

| 配置项 | 类型 | 默认值 | 说明 |
|--------|------|--------|------|
| `runtime_conflict_scan` | boolean | `true` | 是否启用运行时冲突扫描。设为 `false` 则完全跳过扫描 |
| `conflict_scan_json_output` | boolean | `false` | 是否在控制台报告之外额外生成 JSON 报告文件 |
| `auto_disable_fatal_conflicts` | boolean | `false` | 是否自动禁用 FATAL 级别的冲突 Mixin |

### 推荐配置方案

| 场景 | 推荐配置 |
|------|----------|
| **首次安装**（想看看有没有冲突） | `runtime_conflict_scan: true`，其余保持默认 |
| **生产服务器**（稳定运行中） | 保持默认即可 |
| **遇到崩溃排查** | 开启所有 3 个配置项 |
| **不需要此功能** | `runtime_conflict_scan: false` |

---

## 冲突级别

工具将冲突分为 4 个级别：

| 级别 | 严重程度 | 检测规则 | 典型场景 | 建议操作 |
|------|----------|----------|----------|----------|
| **FATAL** | 服务器无法启动 | R1: 双 @Overwrite | 两个 Mod 都覆盖了同一方法的整个实现 | 必须禁用其中一个 |
| **HIGH** | 功能可能失效 | R2: @Overwrite vs @Inject<br>R3: @Overwrite vs @Redirect | Cardboard 覆盖方法导致其他 Mod 的注入失效 | 考虑将 @Overwrite 替换为 @Inject |
| **MEDIUM** | 行为不确定 | R4: 双 @Redirect<br>R5: 双 @ModifyArg | 两个 Mod 重定向同一方法调用 | 检查是否都需要，调整优先级 |
| **LOW** | 通常无害 | R6: 多 @Inject 共存（>5 个 Mod） | 多个 Mod 向同一方法注入代码 | 无需操作，注意执行顺序 |

### 检测规则详情

| 规则 | 条件 | 级别 | 说明 |
|------|------|------|------|
| R1 | 两个不同 Mod 的 `@Overwrite` 目标同一方法 | FATAL | Mixin 框架会抛出异常，服务器无法启动 |
| R2 | `@Overwrite` 与 `@Inject` 目标同一方法 | HIGH | `@Overwrite` 覆盖整个方法体，`@Inject` 可能失效 |
| R3 | `@Overwrite` 与 `@Redirect` 目标同一方法 | HIGH | `@Redirect` 的 INVOKE 目标被覆盖 |
| R4 | 两个 `@Redirect` 竞争同一 INVOKE 调用 | MEDIUM | 只有一个会生效，行为不确定 |
| R5 | 两个 `@ModifyArg` 竞争同一 INVOKE 调用 | MEDIUM | 两个都会生效但顺序不确定 |
| R6 | 超过 5 个不同 Mod 的 `@Inject` 共存于同一方法 | LOW | 通常兼容，但执行顺序依赖 priority |

---

## 使用方式

### 基本使用（开箱即用）

默认配置下，工具会在服务器启动时自动运行。你只需要：

1. 启动服务器
2. 查看启动日志中的报告

**无冲突时：**
```
[Cardboard] Mixin Conflict Detection Report
[Cardboard] Scanned 236 Cardboard mixins, 0 other mods with mixins
[Cardboard] No conflicts detected
[Cardboard] Scan completed in 800ms
```

**有冲突时：**
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
[Cardboard]       Other (lithium): LevelMixin @Redirect [priority=1000]
[Cardboard]       Suggestion: @Overwrite covers method body, @Redirect
[Cardboard]         target may be unreachable.
[Cardboard] 
[Cardboard] MEDIUM conflicts (0):
[Cardboard]   None
[Cardboard] 
[Cardboard] LOW conflicts (2):
[Cardboard]   [1] Target: net.minecraft.server.network.ServerGamePacketListenerImpl#tick
[Cardboard]       Cardboard: ServerGamePacketListenerImplMixin @Inject [priority=1000]
[Cardboard]       Other (carpet): CarpetServerGamePacketListenerImplMixin @Inject [priority=1000]
[Cardboard]       Suggestion: Multiple @Inject coexist on the same method. 6 mods
[Cardboard]         total. Execution order depends on priority.
[Cardboard] 
[Cardboard] Summary: 0 FATAL, 1 HIGH, 0 MEDIUM, 2 LOW
[Cardboard] Scan completed in 1.2s
```

### 启用 JSON 报告

设置 `conflict_scan_json_output: true` 后，JSON 报告会保存到：

```
config/cardboard/conflict-report.json
```

JSON 报告结构：

```json
{
  "timestamp": "2026-05-15T10:30:00Z",
  "scanDurationMs": 1200,
  "cardboardMixinCount": 236,
  "otherModCount": 12,
  "conflicts": {
    "fatal": [],
    "high": [
      {
        "targetClass": "net.minecraft.world.level.Level",
        "targetMethod": "updateSkyLight",
        "conflictType": "OVERWRITE_REDIRECT",
        "level": "HIGH",
        "cardboardMixin": "org.cardboardpowered.mixin.world.level.LevelMixin",
        "cardboardAnnotation": "Overwrite",
        "cardboardPriority": 1000,
        "otherModId": "lithium",
        "otherMixin": "me.jellysquid.mod.lithium.mixin.LevelMixin",
        "otherAnnotation": "Redirect",
        "otherPriority": 1000,
        "suggestion": "@Overwrite covers method body, @Redirect target may be unreachable.",
        "isResolved": false,
        "resolutionNote": null
      }
    ],
    "medium": [],
    "low": [...],
    "resolved": [...]
  }
}
```

### 启用自动禁用 FATAL 冲突

设置 `auto_disable_fatal_conflicts: true` 后，FATAL 级别的冲突 Mixin 将被自动禁用：

```
[Cardboard] Auto-disabling mixin 'org.cardboardpowered.mixin.world.BoatItemMixin' due to FATAL conflict
```

> **警告**: 自动禁用 Mixin 会改变 Cardboard 的行为。建议先查看报告确认冲突详情，再决定是否启用。

---

## 已解决冲突

如果某个冲突在 `mod-compatibility.yml` 中已有处理规则，它会被标记为"已解决"并放在单独的分区：

```
[Cardboard] Resolved Conflicts (1):
[Cardboard]   [1] Target: net.minecraft.network.ServerStatus#setDescription
[Cardboard]       Cardboard: ServerStatusMixin @Inject [priority=1000]
[Cardboard]       Other (minimotd): ServerStatusMixin @Inject [priority=1000]
[Cardboard]       Note: Known compatible: Both inject at HEAD, confirmed safe
```

---

## 常见问题

### Q: 扫描会影响服务器启动速度吗？

通常不会。扫描耗时在 1-3 秒之间（取决于安装的 Mod 数量）。扫描在 `onLoad()` 阶段执行，不阻塞后续的 Mixin 加载。

### Q: 扫描失败会导致服务器崩溃吗？

不会。整个扫描流程包裹在 try-catch 中，失败时只会记录 WARN 日志并跳过扫描：

```
[Cardboard] Mixin conflict scan failed: <error message>. Mixins will load without conflict checks.
```

### Q: 如何完全禁用冲突检测？

在 `config/cardboard/cardboard-config.yml` 中设置：

```yaml
runtime_conflict_scan: false
```

### Q: JSON 报告文件在哪？

启用 `conflict_scan_json_output: true` 后，文件位于：

```
config/cardboard/conflict-report.json
```

### Q: 为什么有些冲突显示 `intermediary(unresolved):class_1234`？

这表示映射转换失败（可能是该类的映射文件缺失）。工具会降级使用 intermediary 名称进行冲突比较，不影响检测功能。你可以将此信息反馈给开发团队。

### Q: 冲突检测与 `mod-compatibility.yml` 有什么关系？

两者互补：
- `mod-compatibility.yml` 是**手动规则库**，由开发者维护已知 Mod 的兼容规则
- 冲突检测工具是**自动扫描器**，能发现未知 Mod 的潜在冲突
- 如果手动规则已标记某冲突为"已解决"，自动扫描结果会被标记为 `isResolved=true`

### Q: 自动禁用的 Mixin 会影响哪些功能？

禁用 FATAL 冲突的 Mixin 意味着对应的 Bukkit API 功能可能无法正常工作。具体影响取决于被禁用的 Mixin 实现的功能。建议在禁用后测试你需要的 Bukkit 功能是否正常。

---

## 报告问题

如果检测到冲突但不确定如何处理，可以：

1. 查看 `config/cardboard/conflict-report.json` 获取详细冲突信息
2. 将 JSON 报告提交到 [GitHub Issues](https://github.com/CardboardPowered/cardboard/issues)
3. 在 issue 中附上冲突报告和相关 Mod 信息
