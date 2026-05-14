# Tasks

> 分析结果：大部分 @Overwrite 方法不是简单的 "fire event, if cancelled return" 模式，而是**完整重写**了原版方法逻辑（原版游戏逻辑 + Bukkit 事件逻辑混在一起）。替换需要精确拆分原版逻辑和新增逻辑，复杂度远超预期。

> 已完成的 @Overwrite 替换（3 个早期完成）：
> - BoatItemMixin - @Inject HEAD cancellable
> - ServerStatusPacketListenerImplMixin - @ModifyArg  
> - RecipeMapMixin - 反射初始化

## Wave 1: P0 - 高冲突风险文件

- [x] Task 1.1: ServerGamePacketListenerImplMixin 的 `handleSetCarriedItem` 替换为 @Inject HEAD cancellable ✅ 已完成
- [ ] Task 1.2: ServerLoginPacketListenerImplMixin 的 `handleKey` 和 `verifyLoginAndFinishConnectionSetup` — **保留 @Overwrite**，添加 TODO 注释说明原因 ✅ 已处理
- [ ] Task 1.3: ServerGamePacketListenerImplMixin_PlayerCommandPreprocessEvent 的 2 个 @Overwrite — **保留 @Overwrite**，完整命令处理逻辑（包含命令解析/签名验证/Bukkit 事件），无法拆分
- [ ] Task 1.4: ServerGamePacketListenerImplMixin_PlayerMove 的 `handleMovePlayer` — **保留 @Overwrite**，完整的移动验证/碰撞检测/事件触发逻辑，无法安全拆分
- [ ] Task 1.5: LegacyQueryHandlerMixin 的 `channelRead` — **保留 @Overwrite**，完整的字节流解析+多版本 ping 处理，无法拆分
- [ ] Task 1.6: DedicatedServerMixin 的 `handleConsoleInputs` — **保留 @Overwrite**，完整的 while 循环+事件+分发逻辑，无法拆分

## Wave 2-4: 剩余 37 个 @Overwrite 文件

经过实际代码分析，这些文件**全部无法用简单模式替换**：

### Wave 2: 世界/方块/物品 (10 files) - 全部保留 @Overwrite
- [ ] ExplosionMixin - 完整爆炸逻辑
- [ ] LevelStorageSource_LevelStorageAccessMixin - 完整路径计算
- [ ] LecternMenuMixin - 复杂多按钮处理
- [ ] ContainerLevelAccessMixin - 静态工厂方法
- [ ] DyeItemMixin - 完整染色逻辑
- [ ] EndCrystalItemMixin - 完整水晶放置逻辑
- [ ] EnderpearlItemMixin - 完整投掷逻辑
- [ ] LeadItemMixin - 静态方法+绑定逻辑
- [ ] ProjectileWeaponItemMixin - 完整射击循环
- [ ] SnowballItemMixin - 完整投掷逻辑

### Wave 3: 实体/AI/注册表 (8 files) - 全部保留 @Overwrite
- [ ] ItemStackMixin - useOn 完整逻辑
- [ ] BambooStalkBlockMixin - 完整竹子生长
- [ ] ResetProfessionMixin - 静态工厂
- [ ] LeashFenceKnotEntityMixin - 复杂交互
- [ ] PiglinAiMixin - 完整 AI 行为
- [ ] BuiltInRegistriesMixin - 注册表初始化
- [ ] RegistryDataLoaderMixin - 注册表上下文
- [ ] ServerScoreboardMixin - 目标跟踪

### Wave 4: Bukkit 核心/Stats/Paper (8 files)
- [ ] BukkitMaterialMixin - getMaxDurability 可替换为 @ModifyReturnValue
- [ ] BukkitMixin - getVersionMessage 可替换为 @ModifyReturnValue
- [ ] BukkitRegistryMixin - legacyRegistryFor 可替换为 @ModifyReturnValue
- [ ] BukkitEntityTypeMixin - fromName/fromId 查找逻辑
- [ ] StatsCounterMixin - 统计递增完整逻辑
- [ ] PaperInternalAPIBridgeMixin - get 静态方法
- [ ] PaperPluginLoggerMixin - getLogger 静态方法
- [ ] BukkitSimplePluginManagerMixin - 22 个 Paper 插件回退

### Wave 5: 最终验证
- [ ] Task 5.1: 运行 gradlew compileJava
- [ ] Task 5.2: 运行 gradlew build -x test
- [ ] Task 5.3: 更新 plan.md
- [ ] Task 5.4: 更新 AGENTS.md

# Task Dependencies

- Wave 4 中的部分简单返回值修改任务可独立处理
- 所有复杂文件（Wave 2-3 全部 + Wave 1 大部分）需要逐个分析原版方法才能安全拆分
