# Tasks

> 31 个剩余文件的详细分析已完成。根据分析结果重新分类。
>
> 关键发现：
> - 2 个 "僵尸" @Overwrite（无事件触发，纯原版逻辑复制）
> - 2 个文件代码全部被注释（空文件）
> - 6 个可以安全拆分为 @Inject
> - 19 个需要保留 @Overwrite（完整方法重写或结构性替换）
> - BukkitSimplePluginManagerMixin 有 35 个 @Overwrite（Paper 插件系统回退）

## 优先级 0: Bug 修复（立即执行）

- [x] Task 0.1: 修复 ServerGamePacketListenerImplMixin.teleport 方法的坐标取值 bug（L262-264） ✅ 已完成

## 优先级 1: 高价值简单替换（6 个文件，低风险）

- [x] Task 1.1: StatsCounterMixin — @Overwrite → @Inject(at="HEAD", cancellable=true) ✅ 已完成
- [x] Task 1.2: DyeItemMixin — @Overwrite → @Inject(at="HEAD", cancellable=true) + @ModifyVariable ✅ 已完成
- [x] Task 1.3: EndCrystalItemMixin — @Overwrite → @Inject(at="HEAD", cancellable=true) + @Redirect ✅ 已完成
- [x] Task 1.4: LecternMenuMixin — @Overwrite → @Inject(at="HEAD", cancellable=true) ✅ 已完成
- [x] Task 1.5: ProjectileWeaponItemMixin — @Overwrite → @Inject(at="HEAD", cancellable=true) ✅ 已完成
- [x] Task 1.6: EnderpearlItemMixin — @Overwrite → 移除方法保留空类 ✅ 已完成

## 优先级 2: 评估型替换（5 个文件）

- [x] Task 2.1: EnderpearlItemMixin 已完成（移除 @Overwrite）。SnowballItemMixin — 评估是否需要 PlayerLaunchProjectileEvent
  - 两个文件都是纯原版逻辑复制，无任何事件
  - SnowballItemMixin 注释中有 PlayerLaunchProjectileEvent 的替代实现但未启用
  - 决策: 如果不需要 → 移除 @Overwrite；如果需要 → 用 @Inject 添加事件
  - 验证: gradlew compileJava 通过
- [ ] Task 2.2: LevelStorageSource_LevelStorageAccessMixin — @Overwrite → @ModifyReturnValue
  - 方法: getDimensionPath
  - 原因: 只是简单的路径选择逻辑，无事件
  - 验证: gradlew compileJava 通过
- [x] Task 2.3: ContainerLevelAccessMixin — 评估后保留 @Overwrite ✅（接口扩展，合理设计）
- [x] Task 2.4: BukkitEntityTypeMixin — 评估后保留 @Overwrite ✅（动态 Map 查找）
- [x] Task 2.5: LeadItemMixin — 保留 @Overwrite ✅ + TODO 注释

## 优先级 3: 保留 @Overwrite（添加 TODO 注释）

- [x] Task 3.1: 为以下文件添加 TODO 注释（批量处理）

  **完整方法重写类（无法拆分）:**
  - ExplosionMixin — 完全重写爆炸逻辑（当前代码已被注释）
  - ServerGamePacketListenerImplMixin_PlayerCommandPreprocessEvent — 完整命令处理
  - ServerGamePacketListenerImplMixin_PlayerMove — 完整移动验证
  - LegacyQueryHandlerMixin — 完整字节流解析
  - DedicatedServerMixin — 完整控制台循环
  - ServerScoreboardMixin — 完整目标跟踪
  - ItemStackMixin — 完整方块放置逻辑
  - BambooStalkBlockMixin — 完整竹子生长
  - LeashFenceKnotEntityMixin — 复杂交互
  - PiglinAiMixin — 完整 AI 行为
  - PlayerListMixin_ChatEvent — 完整聊天广播
  - ServerLoginPacketListenerImplMixin — 完整登录验证

  **结构性替换（无法用 @Inject 实现）:**
  - ContainerLevelAccessMixin — 接口扩展 ✅
  - BuiltInRegistriesMixin — 注册表初始化
  - RegistryDataLoaderMixin — 注册表上下文创建
  - ReloadableServerRegistriesMixin — 注册表加载替换
  - BukkitEntityTypeMixin — 动态 Map 查找 ✅
  - BukkitSimplePluginManagerMixin — Paper 插件系统回退（35 个 @Overwrite）

## 优先级 4: 清理工作

- [ ] Task 4.1: 删除 ServerGamePacketListenerImplMixin_ChatEvent.java（空文件）
  - 所有代码已注释，标记为 TODO: 1.19
  - 功能已合并到 PlayerListMixin_ChatEvent
  - 验证: 从 bukkitfabric.mixins.json 中移除注册
- [ ] Task 4.2: 评估 ExplosionMixin — 代码全部注释，是否需要重新实现爆炸事件
- [ ] Task 4.3: 更新 AGENTS.md 的 Mixin Category Map

## 优先级 5: 最终验证

- [ ] Task 5.1: 运行 gradlew compileJava
- [ ] Task 5.2: 运行 gradlew build -x test
- [ ] Task 5.3: 更新 plan.md 标记最终状态
- [ ] Task 5.4: Grep @Overwrite 统计最终数量

# Task Dependencies

- Task 0.1 独立，可立即执行
- Task 1.x 可并行执行（每个文件独立修改）
- Task 2.x 依赖 Task 1.x 的验证模式
- Task 3.x 依赖 Task 2.x 的决策结果
- Task 4.x 依赖 Task 3.x 的评估结果
- Task 5.x 依赖所有前置任务完成
