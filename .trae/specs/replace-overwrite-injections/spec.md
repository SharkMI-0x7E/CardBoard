# Phase 1: Replace All @Overwrite Mixins with Precise Injections

## Why

Cardboard 目前有 46 个 Mixin 文件使用 `@Overwrite` 注解，这会完全替换目标方法的字节码，导致与其他 Fabric 模组产生严重冲突（已发现 3 个实际冲突案例）。需要将所有 `@Overwrite` 替换为精确的注入方式（`@Inject`、`@ModifyArg`、`@Redirect`、`@ModifyReturnValue`、`@ModifyVariable`），从根本上消除冲突风险。其中 3 个已完成（BoatItemMixin、ServerStatusPacketListenerImplMixin、RecipeMapMixin），剩余 43 个需要处理。

## What Changes

- 将 43 个 Mixin 文件中的 `@Overwrite` 注解替换为精确的 Mixin 注入方法
- 每个文件根据方法实际行为选择最合适的注入类型
- 保持所有现有功能逻辑不变，仅改变注入方式
- 所有注入方法遵循 `cardboard$methodName` 命名约定
- 移除原有的 `@author` 和 `@reason` Javadoc 标签

## Impact

- Affected specs: Mixin 注入系统、事件触发机制、Bukkit API 兼容性
- Affected code: 43 个 Mixin 文件分布在 server、world、inventory、item、block、entity、core、bukkit、stats、resources 等模块
- **BREAKING**: 无。所有现有功能保持不变，仅提高兼容性

## ADDED Requirements

### Requirement: @Overwrite 替代方案
The system SHALL use the most precise injection method for each Mixin, following this priority order: `@ModifyArg` > `@ModifyReturnValue` > `@Redirect` > `@ModifyVariable` > `@Inject(at="HEAD")` > `@Inject(at="RETURN")`.

### Requirement: 事件拦截型 Mixin
For Mixins that trigger Bukkit events and may cancel execution, the system SHALL use `@Inject(at="HEAD", cancellable=true)` pattern. When event is cancelled, `ci.cancel()` shall be called.

### Requirement: 参数修改型 Mixin
For Mixins that modify method call arguments, the system SHALL use `@ModifyArg` with precise `@At(value="INVOKE", target="...")` targeting.

### Requirement: 返回值修改型 Mixin
For Mixins that modify method return values, the system SHALL use `@ModifyReturnValue(at=@At("RETURN"))` pattern.

### Requirement: 调用替换型 Mixin
For Mixins that replace internal method calls, the system SHALL use `@Redirect` with precise `@At(value="INVOKE", target="...")` targeting.

### Requirement: Fabric API 字段访问
For Mixins that access fields injected by Fabric API at runtime, the system SHALL use reflection instead of `@Shadow` annotation.

### Requirement: 编译验证
Each modified Mixin SHALL pass `gradlew compileJava` without errors.

## MODIFIED Requirements

### Requirement: Mixin 命名规范
Existing convention: All Cardboard mixin methods use `cardboard$methodName` prefix. This SHALL be maintained.

### Requirement: Mixin 优先级
Existing convention: Default priority 1000, 1001 for after Fabric API, -500 to -1000 for before other mods. This SHALL be maintained.

### Requirement: 注释语言
Existing convention: Code comments in English to avoid Windows console garbled text. This SHALL be maintained.

## REMOVED Requirements

### Requirement: @Overwrite 注解使用
**Reason**: @Overwrite completely replaces method bytecode, causing conflicts with other mods.
**Migration**: Replaced with precise injection methods as defined in ADDED requirements.
