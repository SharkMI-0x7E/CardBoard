# Fabric Mod 版本管理 & CI/CD 发布指南（个人维护者定制版）

> **适用项目**: Cardboard Fork（Bukkit-API-on-Fabric 桥接层）
> **目标版本**: Minecraft 1.21.11 + Fabric Loader
> **文档版本**: v2.0 (个人维护者定制版)
> **最后更新**: 2026-05-15

**本版本已根据个人维护者需求进行定制**：
- 去除了 Conventional Commits 自动发版、release-please、定时安全扫描等重型流程
- 采用 **手动控制版本号**（`年份.月份.发布序号` 格式，如 `2026.05.01`）
- 发布时 **自动收集 commit 记录** 作为 GitHub Release 正文，无需手写 changelog
- 仅保留两个轻量工作流：`build.yml`（构建验证）和 `release.yml`（一键发布）

---

## 目录

- [1. 版本管理策略](#1-版本管理策略)
  - [1.1 手动版本号方案](#11-手动版本号方案)
  - [1.2 Minecraft 版本兼容性](#12-minecraft-版本兼容性)
  - [1.3 依赖版本锁定策略](#13-依赖版本锁定策略)
  - [1.4 热门 Mod 兼容性表](#14-热门-mod-兼容性表)
- [2. Modrinth Minotaur Gradle Plugin 配置](#2-modrinth-minotaur-gradle-plugin-配置)
  - [2.1 插件安装](#21-插件安装)
  - [2.2 基础配置](#22-基础配置)
  - [2.3 完整生产配置](#23-完整生产配置)
  - [2.4 依赖关系声明](#24-依赖关系声明)
  - [2.5 安全认证管理](#25-安全认证管理)
- [3. GitHub Actions CI/CD 工作流](#3-github-actions-cicd-工作流)
  - [3.1 build.yml - 构建验证](#31-buildyml---构建验证)
  - [3.2 release.yml - 一键发布](#32-releaseyml---一键发布)
- [4. 发布流程实战](#4-发布流程实战)
  - [4.1 标准发布流程](#41-标准发布流程)
  - [4.2 紧急修复流程](#42-紧急修复流程)
- [5. 故障排查指南](#5-故障排查指南)
  - [5.1 常见构建错误](#51-常见构建错误)
  - [5.2 Modrinth API 错误](#52-modrinth-api-错误)
  - [5.3 依赖冲突解决](#53-依赖冲突解决)
- [6. 最佳实践清单](#6-最佳实践清单)
  - [6.1 发布前检查清单](#61-发布前检查清单)
  - [6.2 安全合规检查](#62-安全合规检查)
  - [6.3 构建性能优化](#63-构建性能优化)
- [附录 A: Gradle 命令速查表](#附录-a-gradle-命令速查表)
- [附录 B: 环境变量速查表](#附录-b-环境变量速查表)
- [附录 C: 完整 build.gradle 配置示例](#附录-c-完整-buildgradle-配置示例)

---

## 1. 版本管理策略

### 1.1 手动版本号方案

本项目采用 **手动控制版本号**，由维护者在 `gradle.properties` 中自行修改 `mod_version`。

#### 版本号格式

```
格式: 年份.月份.发布序号
示例: 2026.05.01  (2026年5月第1次发布)
      2026.05.02  (2026年5月第2次发布)
      2026.06.01  (2026年6月第1次发布)
```

#### 在 `gradle.properties` 中的配置

```properties
# ============================================
# 版本管理中心 - 所有版本号在此定义
# ============================================

# Minecraft & Fabric (核心)
minecraft_version=1.21.11
loader_version=0.16.14
fabric_version=0.100.7+1.21.11

# 模组版本号 (手动修改)
mod_version=2026.05.01

# 构建输出
archives_base_name=CardBoard-Fork-SharkMI
maven_group=org.cardboardpowered

# 构建工具
loom_version=1.14-SNAPSHOT
shadow_version=8.3.6
minotaur_version=2.+
```

#### 为什么用这个格式？

| 优点 | 说明 |
|------|------|
| 一眼看出时间 | `2026.05` = 2026年5月 |
| 序号简单明了 | `.01` = 当月第1次发布 |
| 无需计算语义版本 | 不需要判断是 major/minor/patch |
| 个人项目够用 | 不需要大型团队的复杂规则 |

#### JAR 输出文件名

```
格式: {archives_base_name}-{mod_version}.jar
示例: CardBoard-Fork-SharkMI-2026.05.01.jar
```

**注意：** JAR 文件名中不包含 Minecraft 版本号（如 1.21.11），因为：
1. 在 `fabric.mod.json` 中已声明 `depends.minecraft` 版本
2. 在 Modrinth 上发布时会标注 `game_versions`
3. 保持文件名简洁

### 1.2 Minecraft 版本兼容性

Fabric 生态包含多个核心组件，需要严格管理版本兼容性：

#### 核心组件版本矩阵

| 组件 | 当前版本 | 最低版本 | 说明 |
|------|----------|----------|------|
| Minecraft | 1.21.11 | - | 游戏本体 |
| Fabric Loader | 0.16.14 | 0.15.0 | 模组加载器 |
| Fabric API | 0.100.7+1.21.11 | - | Fabric 核心 API |
| Yarn Mappings | 1.21.11+build.X | - | Mojang 官方映射 |
| Java | 21 | 21 | 运行环境 |

#### 多版本支持说明

每个 Minecraft 版本都需要 **单独编译**，因为 MC 内部代码结构（类名、方法签名、字段位置）在不同版本间可能变化。

```
CardBoard-Fork-SharkMI-2026.05.01.jar  → 仅支持 MC 1.21.11
```

如果要支持其他 MC 版本（如 1.21.10），需要：
1. 修改 `gradle.properties` 中的 `minecraft_version`、`yarn_mappings`、`fabric_version`
2. 重新编译
3. 在 Modrinth 上发布为独立版本

### 1.3 依赖版本锁定策略

#### 为什么要锁定依赖版本

```
问题场景:
1. Fabric API 自动更新到新版本，但 API 有破坏性变更
2. 本地开发用版本 X，CI 构建拉到版本 Y，行为不一致
3. 依赖被删除或标记为废弃，构建突然失败

解决方案: 精确锁定版本号
```

#### Gradle 依赖锁定配置

```groovy
// build.gradle
configurations.all {
    resolutionStrategy {
        // 强制使用特定版本
        force "net.fabricmc:fabric-loader:${project.loader_version}"
        force "net.fabricmc.fabric-api:fabric-api:${project.fabric_version}"
        
        // 禁止动态版本
        failOnDynamicVersions()
        failOnChangingVersions()
    }
}
```

### 1.4 热门 Mod 兼容性表

以下是 Cardboard 项目需要重点关注的热门 Fabric Mod：

| Mod 名称 | Modrinth ID | 最低兼容版本 | 依赖类型 | 说明 |
|----------|-------------|-------------|----------|------|
| Fabric API | `P7dR8mSH` | 0.100.0+1.21.11 | 必需 (required) | Fabric 核心 API |
| Lithium | `gvvqyEAF` | 0.14.0+1.21.11 | 可选 (optional) | 服务器性能优化 |
| Sodium | `AANobbMI` | 0.6.0+1.21.11 | 可选 (optional) | 客户端渲染优化 |
| Iris | `YL57xq9U` | 1.8.0+1.21.11 | 可选 (optional) | 着色器支持 |
| Mod Menu | `mOgUt4GM` | 12.0.0+1.21.11 | 可选 (optional) | 模组管理界面 |

---

## 2. Modrinth Minotaur Gradle Plugin 配置

### 2.1 插件安装

```groovy
// build.gradle - plugins 块
plugins {
    id 'fabric-loom' version '1.14-SNAPSHOT'
    id 'maven-publish'
    id 'com.gradleup.shadow' version '8.3.6'
    id 'java-library'
    
    // Modrinth 发布插件 (Minotaur)
    id "com.modrinth.minotaur" version "2+"
}
```

### 2.2 基础配置

```groovy
// build.gradle
modrinth {
    // 认证令牌 (从环境变量读取)
    token = System.getenv("MODRINTH_TOKEN")
    
    // 项目 ID (在 Modrinth 项目页面 URL 中获取)
    // 例如: https://modrinth.com/mod/MLYQ9VGP -> projectId = "MLYQ9VGP"
    projectId = "YOUR_PROJECT_ID"
    
    // 要上传的 JAR 文件 (Fabric 项目必须使用 remapJar)
    uploadFile = remapJar
    
    // 支持的游戏版本
    gameVersions = ["1.21.11"]
    
    // 支持的模组加载器
    loaders = ["fabric"]
}
```

### 2.3 完整生产配置

```groovy
// build.gradle
modrinth {
    // 认证
    token = System.getenv("MODRINTH_TOKEN")
    
    // 项目信息
    projectId = "YOUR_PROJECT_ID"
    
    // 版本信息 (直接使用 gradle.properties 中的 mod_version)
    versionNumber = project.mod_version
    versionName = "CardBoard Fork SharkMI ${project.mod_version}"
    versionType = "release"  // release | beta | alpha
    
    // 更新日志 (支持 Markdown)
    changelog = provider {
        def changelogFile = file("CHANGELOG.md")
        if (changelogFile.exists()) {
            return changelogFile.text
        }
        return "Version ${project.mod_version}"
    }.get()
    
    // 上传文件
    uploadFile = remapJar
    
    // 目标平台和版本
    gameVersions = ["1.21.11"]
    loaders = ["fabric"]
    
    // 依赖声明
    dependencies {
        required.project "fabric-api"
        optional.project "lithium"
        optional.project "sodium"
    }
}
```

### 2.4 依赖关系声明

| 依赖类型 | 含义 | 用户影响 |
|----------|------|----------|
| `required.project` | 必须安装 | 安装时会强制下载 |
| `optional.project` | 可选安装 | 用户可选择是否安装 |
| `incompatible.project` | 不兼容 | 安装时会警告用户 |
| `embedded.library` | 内置依赖 | 已打包在 JAR 中 |

```groovy
modrinth {
    dependencies {
        // 必需依赖
        required.project "fabric-api"
        
        // 可选依赖
        optional.project "lithium"
        optional.project "sodium"
        optional.project "iris"
        
        // 不兼容的 Mods (其他 Bukkit-on-Fabric 桥接层)
        incompatible.project "mohist"
        incompatible.project "arclight"
    }
}
```

### 2.5 安全认证管理

#### API 令牌生成

1. 登录 [Modrinth](https://modrinth.com)
2. 访问 [Account Settings](https://modrinth.com/settings/account)
3. 滚动到 "Access Tokens" 区域
4. 点击 "Create Token"
5. 设置权限范围:
   - `CREATE_VERSION`: 上传新版本 (必需)
   - `PROJECT_WRITE`: 修改项目信息 (同步描述需要)
6. 保存令牌 (只显示一次!)

#### 环境变量配置

**本地开发 (Windows PowerShell)**:
```powershell
# 临时设置 (当前会话有效)
$env:MODRINTH_TOKEN = "your_token_here"

# 永久设置 (用户级别)
[System.Environment]::SetEnvironmentVariable("MODRINTH_TOKEN", "your_token_here", "User")
```

**GitHub Actions**:
```
Settings -> Secrets and variables -> Actions -> New repository secret
Name: MODRINTH_TOKEN
Value: your_token_here
```

#### 安全注意事项

```
安全规则:
1. 永远不要在代码中硬编码令牌
2. 永远不要在 PR/Issue 中暴露令牌
3. 使用环境变量或 GitHub Secrets 存储
4. 定期轮换令牌 (建议每 90 天)
5. 令牌泄露后立即撤销并重新生成
6. 在 .gitignore 中排除包含令牌的文件
```

---

## 3. GitHub Actions CI/CD 工作流

### 3.1 build.yml - 构建验证

**触发条件**: PR 或推送到 main 分支

**作用**: 编译代码、运行测试，确保提交没问题

```yaml
# .github/workflows/build.yml
name: Build

on:
  push:
    branches: [ main ]
  pull_request:
    branches: [ main ]

concurrency:
  group: ${{ github.workflow }}-${{ github.ref }}
  cancel-in-progress: true

env:
  JAVA_VERSION: '21'
  GRADLE_OPTS: "-Dorg.gradle.daemon=false -Dorg.gradle.parallel=true"

jobs:
  build:
    name: Build & Test
    runs-on: ubuntu-latest
    timeout-minutes: 30

    steps:
      - name: Checkout Repository
        uses: actions/checkout@v4

      - name: Set up JDK 21
        uses: actions/setup-java@v4
        with:
          distribution: 'temurin'
          java-version: ${{ env.JAVA_VERSION }}
          cache: 'gradle'

      - name: Grant execute permission for gradlew
        run: chmod +x gradlew

      - name: Compile Java
        run: ./gradlew compileJava

      - name: Run Tests
        run: ./gradlew test

      - name: Build Fat Jar
        run: ./gradlew build -x test

      - name: Upload Build Artifacts
        if: success()
        uses: actions/upload-artifact@v4
        with:
          name: cardboard-jar
          path: build/libs/*.jar
          retention-days: 14

      - name: Upload Test Reports
        if: failure()
        uses: actions/upload-artifact@v4
        with:
          name: test-reports
          path: build/reports/tests/
          retention-days: 30
```

### 3.2 release.yml - 一键发布

**触发条件**: 推送 `v*` 标签（如 `v2026.05.01`）

**作用**:
1. 从标签提取版本号
2. 构建 fat jar
3. **自动收集上次发布以来的所有 commit 记录**
4. 创建 GitHub Release（附上 commit 记录）
5. 发布到 Modrinth

```yaml
# .github/workflows/release.yml
name: Release

on:
  push:
    tags:
      - 'v*'

permissions:
  contents: write

env:
  JAVA_VERSION: '21'
  GRADLE_OPTS: "-Dorg.gradle.daemon=false -Dorg.gradle.parallel=true"

jobs:
  release:
    name: Build & Release
    runs-on: ubuntu-latest
    timeout-minutes: 30

    steps:
      - name: Checkout Repository
        uses: actions/checkout@v4
        with:
          fetch-depth: 0  # 获取完整 Git 历史

      - name: Set up JDK 21
        uses: actions/setup-java@v4
        with:
          distribution: 'temurin'
          java-version: ${{ env.JAVA_VERSION }}
          cache: 'gradle'

      - name: Extract Version from Tag
        id: get_version
        run: |
          VERSION=${GITHUB_REF#refs/tags/v}
          echo "version=${VERSION}" >> $GITHUB_OUTPUT
          echo "发布版本: ${VERSION}"

      - name: Grant execute permission for gradlew
        run: chmod +x gradlew

      - name: Build Fat Jar
        run: ./gradlew build -x test

      - name: Collect Commit History
        id: changelog
        run: |
          # 获取上一个 tag
          PREV_TAG=$(git describe --tags --abbrev=0 HEAD^ 2>/dev/null || echo "")
          
          if [ -z "$PREV_TAG" ]; then
            # 没有上一个 tag，收集所有 commit
            echo "首次发布，收集所有 commit..."
            COMMITS=$(git log --pretty=format:"- %s (%h)" --no-merges)
          else
            # 收集从上一个 tag 到现在的 commit
            echo "收集从 ${PREV_TAG} 到 ${GITHUB_REF_NAME} 的 commit..."
            COMMITS=$(git log --pretty=format:"- %s (%h)" --no-merges ${PREV_TAG}..HEAD)
          fi
          
          # 写入文件 (避免特殊字符问题)
          echo "${COMMITS}" > changelog.txt
          
          # 也设置 output (用于 GitHub Release)
          # 使用 EOF 避免转义问题
          {
            echo "changelog<<EOF"
            echo "## Changes since last release"
            echo ""
            echo "${COMMITS}"
            echo "EOF"
          } >> $GITHUB_OUTPUT

      - name: Create GitHub Release
        uses: softprops/action-gh-release@v2
        with:
          tag_name: ${{ github.ref_name }}
          name: CardBoard Fork SharkMI ${{ steps.get_version.outputs.version }}
          body: ${{ steps.changelog.outputs.changelog }}
          files: build/libs/*.jar
          draft: false
          prerelease: false

      - name: Publish to Modrinth
        run: ./gradlew modrinth
        env:
          MODRINTH_TOKEN: ${{ secrets.MODRINTH_TOKEN }}

      - name: Publish Success Summary
        if: success()
        run: |
          echo "发布成功!"
          echo "版本: ${{ steps.get_version.outputs.version }}"
          echo "GitHub Release: https://github.com/${{ github.repository }}/releases/tag/${{ github.ref_name }}"
          echo "Modrinth: https://modrinth.com/mod/YOUR_PROJECT_ID/version/${{ steps.get_version.outputs.version }}"
```

---

## 4. 发布流程实战

### 4.1 标准发布流程

只需要 **3 步**：

```
Step 1: 修改版本号
┌─────────────────────────────────────┐
│ # 编辑 gradle.properties            │
│ mod_version=2026.05.01              │
│                                     │
│ # 本地验证构建                       │
│ .\gradlew.bat build -x test         │
│                                     │
│ # 提交更改                           │
│ git add gradle.properties           │
│ git commit -m "Bump version to      │
│   2026.05.01"                       │
│ git push origin main                │
└─────────────────────────────────────┘

Step 2: 创建并推送 tag
┌─────────────────────────────────────┐
│ # 创建 tag (注意 v 前缀)             │
│ git tag v2026.05.01                 │
│                                     │
│ # 推送到 GitHub (触发 release.yml)   │
│ git push origin v2026.05.01         │
│                                     │
│ # CI 会自动:                        │
│ # 1. 构建 fat jar                   │
│ # 2. 收集 commit 记录               │
│ # 3. 创建 GitHub Release            │
│ # 4. 发布到 Modrinth                │
└─────────────────────────────────────┘

Step 3: 等待完成
┌─────────────────────────────────────┐
│ # 在 GitHub Actions 页面查看进度     │
│ https://github.com/你的用户名/       │
│ cardboard-ver-1.21.11/actions       │
│                                     │
│ # 完成后检查:                       │
│ - GitHub Release 是否创建成功        │
│ - Modrinth 版本是否上传成功          │
└─────────────────────────────────────┘
```

### 4.2 紧急修复流程

```
场景: 发现严重 Bug，需要快速修复并发布

Step 1: 修复并提交
┌─────────────────────────────────────┐
│ # 直接在 main 上修复 (或开分支)      │
│ git checkout main                   │
│                                     │
│ # 修复 Bug...                       │
│ vim BugFile.java                    │
│                                     │
│ # 提交                               │
│ git add .                           │
│ git commit -m "Fix: resolve crash   │
│   in PlayerInteractEvent"           │
│ git push origin main                │
└─────────────────────────────────────┘

Step 2: 打新版本 tag
┌─────────────────────────────────────┐
│ # 升级版本号 (gradle.properties)     │
│ mod_version=2026.05.02              │
│ git add gradle.properties           │
│ git commit -m "Bump version to      │
│   2026.05.02"                       │
│ git push origin main                │
│                                     │
│ # 打 tag 并发布                     │
│ git tag v2026.05.02                 │
│ git push origin v2026.05.02         │
│                                     │
│ # CI 自动完成剩下的工作              │
└─────────────────────────────────────┘
```

---

## 5. 故障排查指南

### 5.1 常见构建错误

| 错误信息 | 原因 | 解决方案 |
|----------|------|----------|
| `MODRINTH_TOKEN not found` | 环境变量未设置 | 检查 GitHub Secrets 或本地环境变量 |
| `Version already exists` | 版本号重复 | 修改 `mod_version` 后重试 |
| `Invalid project ID` | 项目 ID 错误或无权限 | 检查 Modrinth 项目页面 URL |
| `Invalid authentication` | 令牌过期或权限不足 | 重新生成令牌，确保有 `CREATE_VERSION` |
| `remapJar task not found` | Loom 插件未正确配置 | 检查 `id 'fabric-loom'` 是否在 plugins 块中 |
| `Fabric API not detected` | 依赖声明不匹配 | 检查 `dependencies` 块中的 project ID |

### 5.2 Modrinth API 错误

| HTTP 状态码 | 含义 | 解决方案 |
|-------------|------|----------|
| `401 Unauthorized` | 认证失败 | 检查令牌是否有效 |
| `403 Forbidden` | 权限不足 | 确认令牌有 `CREATE_VERSION` 权限 |
| `404 Not Found` | 项目不存在 | 检查 `projectId` 是否正确 |
| `409 Conflict` | 版本重复 | 修改版本号或删除旧版本 |
| `422 Unprocessable` | 参数错误 | 检查 `gameVersions`, `loaders` 格式 |
| `500 Server Error` | Modrinth 服务问题 | 等待后重试，或联系 Modrinth 支持 |

### 5.3 依赖冲突解决

```
场景: Fabric API 版本冲突

问题:
- Cardboard 依赖 Fabric API 0.100.7
- 其他 Mod 依赖 Fabric API 0.99.0
- 运行时出现 NoSuchMethodError

解决方案 1: 升级所有依赖到最新 Fabric API
┌─────────────────────────────────────┐
│ # 更新 gradle.properties            │
│ fabric_version=0.100.7+1.21.11      │
│                                     │
│ # 检查其他 Mod 的兼容性              │
│ # 确保它们也支持 0.100.7            │
└─────────────────────────────────────┘

解决方案 2: 使用依赖强制解析
┌─────────────────────────────────────┐
│ # build.gradle                      │
│ configurations.all {                │
│     resolutionStrategy {            │
│         force "net.fabricmc.fabric-api:fabric-api:0.100.7+1.21.11"│
│     }                               │
│ }                                   │
└─────────────────────────────────────┘
```

---

## 6. 最佳实践清单

### 6.1 发布前检查清单

```
发布前必须检查的项目:

[ ] 所有测试通过 (./gradlew test)
[ ] 构建成功 (./gradlew build -x test)
[ ] 版本号正确 (gradle.properties 中的 mod_version)
[ ] 依赖声明完整 (Modrinth dependencies 块)
[ ] 项目描述最新 (README.md)
[ ] MODRINTH_TOKEN 已设置且有效
[ ] 无硬编码的密钥/令牌在代码中
```

### 6.2 安全合规检查

```
安全规则:

1. 令牌管理
   [ ] MODRINTH_TOKEN 存储在 GitHub Secrets 中
   [ ] 令牌每 90 天轮换一次
   [ ] 不在日志/输出中打印令牌

2. 代码安全
   [ ] 无硬编码的密码/密钥/API 密钥
   [ ] 敏感配置通过环境变量传递
   [ ] .env 文件在 .gitignore 中
```

### 6.3 构建性能优化

```
构建性能优化:

1. Gradle 缓存
   - CI 中使用 cache: 'gradle'
   - 本地: ~/.gradle/caches

2. 并行构建
   GRADLE_OPTS="-Dorg.gradle.parallel=true -Dorg.gradle.daemon=true"

3. 任务优化
   - 使用 -x test 跳过测试 (仅本地迭代)
   - 本地开发用 compileJava 快速验证

4. CI 优化
   - 使用 concurrency 取消重复运行
   - 使用 artifact 缓存中间产物
```

---

## 附录 A: Gradle 命令速查表

```bash
# 构建命令
./gradlew compileJava              # 编译 Java 代码
./gradlew test                     # 运行测试
./gradlew build                    # 完整构建 (编译 + 测试)
./gradlew build -x test            # 构建但不运行测试
./gradlew clean build              # 清理后构建

# Modrinth 相关
./gradlew modrinth                 # 发布到 Modrinth
./gradlew modrinthSyncBody         # 同步项目描述

# 依赖管理
./gradlew dependencies             # 查看依赖树
./gradlew dependencies --configuration api  # 查看 API 依赖

# 调试
./gradlew build --info             # 详细信息
./gradlew build --debug            # 调试模式
./gradlew build --dry-run          # 预览执行顺序
```

---

## 附录 B: 环境变量速查表

| 变量名 | 用途 | 必需 | 设置位置 |
|--------|------|------|----------|
| `MODRINTH_TOKEN` | Modrinth API 认证 | 发布时需要 | GitHub Secrets / 本地环境变量 |
| `JAVA_HOME` | Java 安装路径 | 是 | CI 自动设置 / 本地配置 |
| `GRADLE_OPTS` | Gradle JVM 参数 | 推荐 | 本地 ~/.bashrc 或 CI 环境 |

---

## 附录 C: 完整 build.gradle 配置示例

```groovy
plugins {
    id 'fabric-loom' version '1.14-SNAPSHOT'
    id 'maven-publish'
    id 'com.gradleup.shadow' version '8.3.6'
    id 'java-library'
    id "com.peterabeles.gversion" version "1.10.3"
    id "com.modrinth.minotaur" version "2+"
}

// ============================================
// 项目基本信息
// ============================================
version = project.mod_version
group = project.maven_group
base {
    archivesName = project.archives_base_name
}

// ============================================
// Loom 配置
// ============================================
loom {
    accessWidenerPath = file("src/main/resources/bukkitfabric.accesswidener")
}

// ============================================
// 仓库配置
// ============================================
repositories {
    maven { url = 'https://jitpack.io' }
    maven { url = 'https://repo.codemc.io/repository/maven-releases/' }
    maven { url = 'https://repo.spongepowered.org/maven' }
    maven { url = 'https://repo.papermc.io/repository/maven-public/' }
    maven { url = 'https://maven.izzel.io/releases' }
    mavenCentral()
}

// ============================================
// 依赖锁定
// ============================================
configurations.all {
    resolutionStrategy {
        force "net.fabricmc:fabric-loader:${project.loader_version}"
        force "net.fabricmc.fabric-api:fabric-api:${project.fabric_version}"
        failOnDynamicVersions()
        failOnChangingVersions()
    }
}

// ============================================
// 依赖
// ============================================
dependencies {
    minecraft "com.mojang:minecraft:${project.minecraft_version}"
    mappings "net.fabricmc:yarn:${project.minecraft_version}+build.${project.yarn_build}:v2"
    modImplementation "net.fabricmc:fabric-loader:${project.loader_version}"
    modImplementation "net.fabricmc.fabric-api:fabric-api:${project.fabric_version}"
    
    // Shadow dependencies (打包进 JAR)
    implementation 'org.yaml:snakeyaml:2.2'
    shadow 'org.yaml:snakeyaml:2.2'
}

// ============================================
// Modrinth 发布配置
// ============================================
modrinth {
    token = System.getenv("MODRINTH_TOKEN")
    projectId = "YOUR_PROJECT_ID"
    versionNumber = project.mod_version
    versionName = "CardBoard Fork SharkMI ${project.mod_version}"
    versionType = "release"
    
    changelog = provider {
        def changelogFile = file("CHANGELOG.md")
        if (changelogFile.exists()) {
            return changelogFile.text
        }
        return "Version ${project.mod_version} - See GitHub for details."
    }.get()
    
    uploadFile = remapJar
    gameVersions = [project.minecraft_version]
    loaders = ["fabric"]
    
    dependencies {
        required.project "fabric-api"
        optional.project "lithium"
        optional.project "sodium"
        optional.project "iris"
        incompatible.project "mohist"
        incompatible.project "arclight"
    }
    
    syncBodyFrom = rootProject.file("README.md").text
}

tasks.modrinth.dependsOn(tasks.modrinthSyncBody)

// ============================================
// 编译选项
// ============================================
tasks.withType(JavaCompile).configureEach {
    options.encoding = "UTF-8"
    options.deprecation = false
    options.compilerArgs << '-Xlint:-deprecation'
    options.compilerArgs << '-Xlint:-removal'
    options.compilerArgs << '-Xlint:-dep-ann'
    options.compilerArgs << '-XDignore.symbol.file'
    
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

// ============================================
// 测试配置
// ============================================
test {
    useJUnitPlatform()
}
```

---

**文档结束** - 祝你发布顺利! 🚀
