# Verification Checklist

## Class Structure
- [x] MixinConfigScanner 类位于 `src/main/java/org/cardboardpowered/conflict/MixinConfigScanner.java`
- [x] 类使用 `package org.cardboardpowered.conflict;` 声明
- [x] 包含 `scanAllMods()` 方法，返回 `List<MixinConfigData>`

## Mod Traversal
- [x] 使用 `FabricLoader.getInstance().getAllMods()` 获取所有 Mod
- [x] 对每个 Mod 调用 `ModContainer.getRootPath()` 获取路径
- [x] 从 `ModContainer.getMetadata().getId()` 获取 Mod ID

## Skip List
- [x] 跳过 `minecraft` Mod
- [x] 跳过 `fabricloader` Mod
- [x] 跳过 `java` Mod
- [x] 跳过 `cardboard` Mod
- [x] 跳过列表使用常量或配置定义，便于维护

## JAR File Scanning
- [x] 能正确识别 `getRootPath()` 返回的是 JAR 文件路径
- [x] 使用 ZipFile 或 FileSystem API 打开 JAR 文件
- [x] 遍历 JAR 内所有条目，匹配 `*.mixins.json` 文件
- [x] 能正确读取 JAR 内文件内容为字符串
- [x] 支持嵌套目录结构中的配置文件

## Directory Scanning
- [x] 能正确识别 `getRootPath()` 返回的是目录路径
- [x] 使用 `Files.walk()` 递归扫描目录
- [x] 匹配 `*.mixins.json` 文件
- [x] 能正确读取文件内容为字符串

## JSON Parsing
- [x] 使用 Gson 解析 JSON（项目已有依赖）
- [x] 正确提取 `package` 字段 → packageName
- [x] 正确提取 `mixins` 字段 → mixins 列表
- [x] 正确提取 `server` 字段 → server 列表
- [x] 正确提取 `client` 字段 → client 列表
- [x] 正确提取 `refmap` 字段 → refmap
- [x] 正确提取 `required` 字段 → required
- [x] 正确提取 `minVersion` 字段 → minVersion
- [x] 设置 `sourceModId` 为当前 Mod ID
- [x] 设置 `configFileName` 为文件名
- [x] 设置 `configFilePath` 为完整路径

## Error Handling
- [x] JSON 解析失败时记录 warning 日志
- [x] 解析失败不中断整个扫描流程
- [x] 文件读取失败时记录 warning 日志
- [x] 空文件或 null 内容时正确处理

## Logging
- [x] 扫描开始时输出 `"[Cardboard] Scanning mods for mixin configs..."`
- [x] 扫描完成时输出 `"[Cardboard] Scanned {N} mods, found {M} mixin configs"`
- [x] 发现配置文件时输出日志（包含文件名和 Mod ID）
- [x] 使用与项目一致的日志框架（Log4j/SLF4J）

## Result Collection
- [x] 返回 `List<MixinConfigData>` 包含所有解析成功的配置
- [x] 结果按 Mod ID 排序
- [x] 每个 Mod 可能有多个配置文件，都能正确收集

## Build Verification
- [x] `gradlew compileJava -x createVersionFile` 编译通过
- [x] 所有注释使用英文
- [x] 代码不含 emoji
- [x] 无未使用的 import
