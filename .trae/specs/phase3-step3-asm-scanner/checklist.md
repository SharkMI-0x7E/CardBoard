# Verification Checklist

## MixinAnnotationDescriptor
- [x] 类位于 `src/main/java/org/cardboardpowered/conflict/MixinAnnotationDescriptor.java`
- [x] 包含 @Mixin 描述符常量
- [x] 包含 @Overwrite 描述符常量
- [x] 包含 @Inject 描述符常量
- [x] 包含 @Redirect 描述符常量
- [x] 包含 @ModifyArg 描述符常量
- [x] 包含 @ModifyVariable 描述符常量
- [x] 包含 @ModifyReturnValue 描述符常量
- [x] 包含 @Shadow 描述符常量
- [x] 包含 @WrapWithCondition 描述符常量（标准 + MixinExtras 旧包名）
- [x] 包含 @WrapOperation 描述符常量
- [x] 包含 @ModifyExpressionValue 描述符常量
- [x] 提供 KNOWN_INJECT_ANNOTATIONS 不可变集合

## ASM Parsing - Class Level
- [x] 使用 ClassReader + ClassNode 模式解析
- [x] 正确提取 @Mixin 的 value（目标类列表）
- [x] 正确提取 @Mixin 的 priority（默认 1000）
- [x] 正确设置 isMixin = true
- [x] 目标类名从 JVM 内部格式（Lnet/minecraft/.../BoatItem;）转为点号格式

## ASM Parsing - Method Level
- [x] @Overwrite: 提取方法名和描述符
- [x] @Inject: 提取 method 字段、cancellable、@At 嵌套注解
- [x] @Redirect: 提取 method 字段、@At 嵌套注解
- [x] @ModifyArg: 提取 method 字段、@At 嵌套注解
- [x] @ModifyVariable: 提取 method 字段
- [x] @ModifyReturnValue: 提取 method 字段
- [x] @WrapWithCondition: 提取 method 字段、@At 嵌套注解
- [x] @WrapOperation: 提取 method 字段、@At 嵌套注解
- [x] @ModifyExpressionValue: 提取 method 字段、@At 嵌套注解

## @At Nested Annotation Parsing
- [x] 从 AnnotationNode.values 列表中找到 key="at" 的元素
- [x] 该元素值是另一个 AnnotationNode（嵌套）
- [x] 从嵌套节点中提取 value 字段 → atValues
- [x] 从嵌套节点中提取 target 字段 → atTargets
- [x] target 字段为字符串或数组时都正确处理

## Error Handling
- [x] 未知注解跳过并记录 DEBUG 日志
- [x] @Shadow 注解静默跳过，不记录日志
- [x] .class 文件读取失败记录 WARN 日志，不中断流程
- [x] ASM 解析异常记录 WARN 日志，不中断流程
- [x] JSON 中列出的类在 JAR 中不存在时记录 WARN 日志

## Batch Scanning
- [x] scanAllConfigs(List<MixinConfigData>) 方法存在
- [x] 支持从 JAR 文件读取 .class 字节码
- [x] 支持从目录读取 .class 字节码
- [x] 包名快速过滤：只处理 mixin/ 包下的类
- [x] 解析结果缓存按类全限定名 key
- [x] 扫描开始/完成时输出日志

## Build Verification
- [x] build.gradle 包含 org.ow2.asm:asm 依赖
- [x] build.gradle 包含 org.ow2.asm:asm-tree 依赖
- [x] `gradlew compileJava -x createVersionFile` 编译通过
- [x] 所有注释使用英文
- [x] 代码不含 emoji
- [x] 无未使用的 import
