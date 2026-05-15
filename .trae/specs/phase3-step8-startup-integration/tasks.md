# Tasks

- [x] Task 1: 新增静态字段存储扫描结果
  - [x] SubTask 1.1: 添加 `private static List<MixinConflict> scanResults`
  - [x] SubTask 1.2: 添加 `private static Set<String> fatalMixinSet = new HashSet<>()`
  - [x] SubTask 1.3: 添加必要的 import 语句

- [x] Task 2: 在 onLoad() 中集成冲突扫描
  - [x] SubTask 2.1: 在 compatDatabase 加载后、插件加载前插入扫描代码
  - [x] SubTask 2.2: 使用 CardboardConfig.runtimeConflictScan 判断是否执行扫描
  - [x] SubTask 2.3: 创建扫描器链：configScanner → asmScanner → detector → report
  - [x] SubTask 2.4: 将 scanResults 和 fatalMixinSet 存储到静态字段
  - [x] SubTask 2.5: 按 CardboardConfig.conflictScanJsonOutput 判断是否输出 JSON
  - [x] SubTask 2.6: 整个扫描流程包裹在 try-catch 中，失败时记录 WARN 日志

- [x] Task 3: 在 shouldApplyMixin() 中集成 FATAL 冲突自动禁用
  - [x] SubTask 3.1: 在 compatDatabase 检查之后插入 FATAL 冲突检查
  - [x] SubTask 3.2: 判断 scanResults != null 且 CardboardConfig.autoDisableFatalConflicts
  - [x] SubTask 3.3: 检查 mixinClassName 是否在 fatalMixinSet 中，是则返回 false 并记录 WARN

---

# Task Dependencies
- Task 1 无依赖
- Task 2 依赖 Task 1（静态字段必须先存在）
- Task 3 依赖 Task 1（fatalMixinSet 必须先存在）
