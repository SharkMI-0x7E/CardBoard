# Verification Checklist

## Class Structure
- [ ] MappingBridge 类位于 `src/main/java/org/cardboardpowered/conflict/MappingBridge.java`
- [ ] 类使用 `package org.cardboardpowered.conflict;` 声明

## Reuse RemapUtils
- [ ] 通过 `RemapUtils.myMappingResolver` 获取已有实例
- [ ] 如果 myMappingResolver 为 null，降级为 FabricLoader.getInstance().getMappingResolver()
- [ ] 不创建新的 MappingResolver 实例

## Class Name Conversion
- [ ] toNamed(String) 方法正确转换 intermediary → named
- [ ] toIntermediary(String) 方法正确转换 named → intermediary
- [ ] 转换失败时返回 `intermediary(unresolved):{originalName}` 格式
- [ ] 转换失败时记录 WARN 日志

## Method Name Conversion
- [ ] mapMethodName(String, String, String, String) 正确转换
- [ ] 方法名转换失败时返回原始方法名
- [ ] 方法名转换失败时记录 DEBUG 日志

## Caching
- [ ] 类名使用 `Map<String, String>` 缓存
- [ ] 方法名使用 `Map<String, String>` 缓存
- [ ] 缓存 key 包含 namespace 前缀
- [ ] 命中缓存时直接返回，不查询 MappingResolver

## Normalize
- [ ] normalizeClassName(String) 正确判断 intermediary/named 格式
- [ ] 包含 `class_` 前缀时调用 toNamed()
- [ ] 不包含 `class_` 前缀时直接返回

## Cache Management
- [ ] getCacheStats() 返回缓存统计信息
- [ ] clearCache() 清空所有缓存

## Error Handling
- [ ] MappingNotFoundException 被正确捕获
- [ ] IllegalArgumentException 被正确捕获
- [ ] 所有异常都被捕获，不中断流程

## Build Verification
- [ ] `gradlew compileJava -x createVersionFile` 编译通过
- [ ] 所有注释使用英文
- [ ] 代码不含 emoji
- [ ] 无未使用的 import
