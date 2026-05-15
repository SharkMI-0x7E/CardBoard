# Tasks

- [x] Task 1: 在 build.gradle 中添加 JUnit 5 测试依赖
  - [x] SubTask 1.1: 添加 `testImplementation 'org.junit.jupiter:junit-jupiter:5.10.0'`
  - [x] SubTask 1.2: 添加 `test { useJUnitPlatform() }`

- [x] Task 2: 创建测试目录结构
  - [x] SubTask 2.1: 创建 `src/test/java/org/cardboardpowered/conflict/` 目录
  - [x] SubTask 2.2: 创建测试工具类（TestBytecodeHelper.java）用于 ASM 生成测试字节码

- [x] Task 3: 创建 MixinConflictDetector 单元测试
  - [x] SubTask 3.1: 测试 R1（双 @Overwrite FATAL）
  - [x] SubTask 3.2: 测试 R2（@Overwrite vs @Inject HIGH）
  - [x] SubTask 3.3: 测试自冲突过滤
  - [x] SubTask 3.4: 测试空输入（无冲突）

- [x] Task 4: 创建 MappingBridge 单元测试
  - [x] SubTask 4.1: 测试降级策略（无 Fabric 运行时返回 unresolved 格式）
  - [x] SubTask 4.2: 测试 normalizeClassName 方法

---

# Task Dependencies
- Task 1 无依赖
- Task 2 依赖 Task 1
- Task 3 依赖 Task 2
- Task 4 无依赖
