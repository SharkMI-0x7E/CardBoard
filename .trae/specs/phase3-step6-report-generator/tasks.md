# Tasks

- [x] Task 1: 创建 ConflictReportEntry DTO 类
  - [x] SubTask 1.1: 定义 DTO 字段（targetClass、targetMethod、conflictType、level、cardboardMixin、cardboardAnnotation、otherModId、otherMixin、otherAnnotation、suggestion、isResolved、resolutionNote）
  - [x] SubTask 1.2: 实现 fromConflict(MixinConflict) 静态工厂方法

- [x] Task 2: 创建 ConflictReport 主类框架
  - [x] SubTask 2.1: 类定义、Logger、构造函数（接收 List<MixinConflict> 和统计信息）
  - [x] SubTask 2.2: 内部按级别分组冲突（fatalResolved/fatalUnresolved/highResolved/highUnresolved 等）

- [x] Task 3: 实现控制台报告输出（printConsole）
  - [x] SubTask 3.1: 打印扫描摘要头部
  - [x] SubTask 3.2: 按级别打印 FATAL/HIGH/MEDIUM/LOW 未解决冲突
  - [x] SubTask 3.3: 打印 Resolved Conflicts 分区
  - [x] SubTask 3.4: 打印 Summary 汇总行

- [x] Task 4: 实现单条冲突的控制台格式化
  - [x] SubTask 4.1: 格式化单条冲突：Target 行、Cardboard 行、Other 行、Suggestion 行
  - [x] SubTask 4.2: 长文本换行处理（~75 字符限制，缩进对齐）

- [x] Task 5: 实现 JSON 报告输出（writeJson）
  - [x] SubTask 5.1: 构建 JSON 结构（timestamp、scanDurationMs、cardboardMixinCount、otherModCount）
  - [x] SubTask 5.2: 使用 Gson 序列化，冲突数组按级别分组
  - [x] SubTask 5.3: 写入文件到 config/cardboard/conflict-report.json

---

# Task Dependencies
- Task 1 无依赖
- Task 2 无依赖
- Task 3 依赖 Task 2 和 Task 4
- Task 4 依赖 Task 1
- Task 5 依赖 Task 1 和 Task 2
