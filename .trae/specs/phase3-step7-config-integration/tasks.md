# Tasks

- [x] Task 1: 在 DEFAULT_CONF 中新增 ConfigSection
  - [x] SubTask 1.1: 添加 "mixin-conflict-detection" ConfigSection，包含注释说明
  - [x] SubTask 1.2: 定义 3 个配置键和默认值（runtime_conflict_scan: true, conflict_scan_json_output: false, auto_disable_fatal_conflicts: false）

- [x] Task 2: 新增静态配置字段
  - [x] SubTask 2.1: 添加 `public static boolean runtimeConflictScan = true`
  - [x] SubTask 2.2: 添加 `public static boolean conflictScanJsonOutput = false`
  - [x] SubTask 2.3: 添加 `public static boolean autoDisableFatalConflicts = false`

- [x] Task 3: 在 setup() 中读取配置值
  - [x] SubTask 3.1: 使用 config.getOrDefault() 读取 runtime_conflict_scan
  - [x] SubTask 3.2: 使用 config.getOrDefault() 读取 conflict_scan_json_output
  - [x] SubTask 3.3: 使用 config.getOrDefault() 读取 auto_disable_fatal_conflicts

---

# Task Dependencies
- Task 1 无依赖
- Task 2 无依赖
- Task 3 依赖 Task 2（字段必须先存在才能赋值）
