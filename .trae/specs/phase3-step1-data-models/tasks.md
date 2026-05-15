# Tasks

- [x] Task 1: 创建 ConflictLevel 枚举类
  - [x] SubTask 1.1: 创建 conflict/model 目录结构
  - [x] SubTask 1.2: 编写 ConflictLevel.java，包含 FATAL/HIGH/MEDIUM/LOW 四个级别
- [x] Task 2: 创建 MixinMethod 数据模型类
  - [x] SubTask 2.1: 编写 MixinMethod.java，包含所有必要字段
- [x] Task 3: 创建 MixinClassInfo 数据模型类
  - [x] SubTask 3.1: 编写 MixinClassInfo.java，包含所有必要字段和方法列表
- [x] Task 4: 创建 MixinConfigData 数据模型类
  - [x] SubTask 4.1: 编写 MixinConfigData.java，包含配置解析结果字段
- [x] Task 5: 创建 MixinConflict 数据模型类
  - [x] SubTask 5.1: 编写 MixinConflict.java，包含冲突详情字段和辅助方法

---

# Task Dependencies

| Task | Depends On | Can Run Parallel With |
|------|-----------|----------------------|
| Task 1 | 无 | 无（基础依赖） |
| Task 2 | Task 1 | 无 |
| Task 3 | Task 1, Task 2 | Task 4, Task 5 |
| Task 4 | Task 1 | Task 3, Task 5 |
| Task 5 | Task 1 | Task 3, Task 4 |

**执行顺序**: Task 1 → Task 2 → Task 3/4/5（后三个可并行）
