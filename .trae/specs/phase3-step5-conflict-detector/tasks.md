# Tasks

- [x] Task 1: 创建 MixinConflictDetector 主类框架
  - [x] SubTask 1.1: 类定义、Logger、构造函数、核心 detect() 方法签名
  - [x] SubTask 1.2: 注入 MappingBridge 实例

- [x] Task 2: 实现数据预处理和分组逻辑
  - [x] SubTask 2.1: 按目标类分组（Map<String, List<MixinClassInfo>>）
  - [x] SubTask 2.2: 按目标方法分组（Map<String, List<MixinMethod>>）
  - [x] SubTask 2.3: 通配符展开工具方法（expandWildcard -> List<String>）
  - [x] SubTask 2.4: 自冲突过滤工具方法（isCrossModConflict）

- [x] Task 3: 实现 R1 规则（双 @Overwrite FATAL 冲突）
  - [x] SubTask 3.1: 收集所有 Overwrite 方法
  - [x] SubTask 3.2: 跨 Mod 配对比较
  - [x] SubTask 3.3: 生成 FATAL 冲突并填充字段

- [x] Task 4: 实现 R2 规则（@Overwrite vs @Inject HIGH 冲突）
  - [x] SubTask 4.1: 收集 Overwrite + Inject 方法
  - [x] SubTask 4.2: 跨 Mod 配对比较
  - [x] SubTask 4.3: 生成 HIGH 冲突

- [x] Task 5: 实现 R3 规则（@Overwrite vs @Redirect HIGH 冲突）
  - [x] SubTask 5.1: 收集 Overwrite + Redirect 方法
  - [x] SubTask 5.2: 跨 Mod 配对比较（method 相同即冲突）
  - [x] SubTask 5.3: 生成 HIGH 冲突

- [x] Task 6: 实现 R4 规则（双 @Redirect MEDIUM 冲突）
  - [x] SubTask 6.1: 收集所有 Redirect 方法
  - [x] SubTask 6.2: 按 method + @At target 精确匹配分组
  - [x] SubTask 6.3: 跨 Mod 配对生成 MEDIUM 冲突

- [x] Task 7: 实现 R5 规则（双 @ModifyArg MEDIUM 冲突）
  - [x] SubTask 7.1: 收集所有 ModifyArg 方法
  - [x] SubTask 7.2: 按 method + @At target 精确匹配分组
  - [x] SubTask 7.3: 跨 Mod 配对生成 MEDIUM 冲突

- [x] Task 8: 实现 R6 规则（多 @Inject 共存 LOW 冲突）
  - [x] SubTask 8.1: 按方法分组所有 Inject
  - [x] SubTask 8.2: 统计不同 Mod 的 Inject 数量
  - [x] SubTask 8.3: 超过 5 个不同 Mod 时生成 LOW 冲突

- [x] Task 9: 实现已有规则标记和预构建快速查找
  - [x] SubTask 9.1: 查询 ModCompatibilityDatabase 标记 isResolved
  - [x] SubTask 9.2: 构建 fatalMixinSet（FATAL 冲突的 Mixin 类名集合）
  - [x] SubTask 9.3: 构建 conflictMethodMap（targetClass#method -> 冲突 Mixin 集合）

- [x] Task 10: 集成 ModCompatibilityDatabase
  - [x] SubTask 10.1: 读取 ModCompatibilityDatabase 中 getKnownConflict() 方法
  - [x] SubTask 10.2: 实现冲突匹配逻辑（按 targetClass + targetMethod + otherModId 匹配）

---

# Task Dependencies
- Task 1 无依赖
- Task 2 依赖 Task 1
- Task 3,4,5,6,7,8 均依赖 Task 2（可并行执行）
- Task 9 依赖 Task 3-8
- Task 10 依赖 Task 9
