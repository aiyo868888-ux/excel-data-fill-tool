# 闪念笔记 Android 项目状态报告

## 项目概述

**项目名称**: 闪念笔记 (Fleeting Notes)
**技术栈**: Kotlin + Jetpack Compose
**架构**: MVVM + Clean Architecture
**最低版本**: Android 8.0 (API 26)
**目标版本**: Android 14 (API 34)

## 当前状态

### ✅ 已完成

#### 1. 项目基础架构
- ✅ Gradle 配置完成
- ✅ 包结构搭建完成 (data/domain/presentation/service)
- ✅ Material 3 设计系统配置
- ✅ Service Locator 依赖注入（替代 Hilt）

#### 2. 数据层
- ✅ 数据模型 (`Note.kt`)
  - `Note` 基类
  - `IdeaNote` 灵感笔记
  - `InsightNote` 启发笔记
  - `TodoNote` 待办笔记
- ✅ 本地存储 (`JsonFileStorage.kt`)
  - JSON 文件读写
  - 每日文件管理 (YYYY-MM-DD.json)
- ✅ Repository 模式 (`NoteRepository.kt`, `NoteRepositoryImpl.kt`)
  - CRUD 操作
  - 搜索功能

#### 3. 表现层 - UI 组件
- ✅ **三个对话框**
  - `IdeaDialog.kt` - 灵感对话框
  - `InsightDialog.kt` - 启发对话框
  - `TodoDialog.kt` - 待办对话框
- ✅ **三个列表页面**
  - `IdeaListPage.kt` - 灵感列表页
  - `InsightListPage.kt` - 启发列表页
  - `TodoListPage.kt` - 待办列表页
- ✅ **主页面**
  - `MainScreen.kt` - 底部导航
  - `MainActivity.kt` - 权限请求和导航
- ✅ **输入组件**
  - `ImagePickerComponent.kt` - 图片选择器
  - `VoiceInputComponent.kt` - 语音输入

#### 4. 业务逻辑层
- ✅ `MainViewModel.kt`
  - 状态管理
  - 数据加载
  - 笔记保存/删除

#### 5. 资源文件
- ✅ 图标资源 (ic_idea.xml, ic_insight.xml, ic_todo.xml)
- ✅ 主题配置 (Color.kt, Type.kt, Theme.kt)
- ✅ XML 资源 (data_extraction_rules.xml, backup_rules.xml)

#### 6. 权限处理
- ✅ 悬浮窗权限 (SYSTEM_ALERT_WINDOW)
- ✅ 录音权限 (RECORD_AUDIO)
- ✅ 相机权限 (CAMERA)
- ✅ 通知权限 (POST_NOTIFICATIONS, Android 13+)

## ⚠️ 待完成功能

### 1. 悬浮窗服务
- [ ] `FloatingWindowService.kt` 实现
- [ ] WindowManager 配置
- [ ] 悬浮窗布局 (Compose View)
- [ ] 拖拽功能
- [ ] 三种状态切换

### 2. 剪切板监听
- [ ] `ClipboardService.kt` 实现
- [ ] Android 12+ 权限处理
- [ ] 内容变化检测

### 3. 对话框集成
- [ ] 点击 FAB 打开对应对话框
- [ ] 对话框数据保存到 Repository
- [ ] 保存后列表自动刷新

### 4. 图片和语音功能完善
- [ ] 图片数据序列化到 JSON
- [ ] 语音识别权限请求完善
- [ ] 图片显示优化

### 5. 测试
- [ ] 单元测试
- [ ] UI 测试
- [ ] 真机测试

## 技术决策记录

### 为什么移除 Hilt？
由于 Hilt 编译器在当前环境下持续出现 KSP/Annotation 处理错误，决定使用简单的 Service Locator 模式替代依赖注入。这减少了复杂性，提高了可维护性。

**变更影响**:
- ❌ 移除了所有 `@HiltAndroidApp`, `@AndroidEntryPoint`, `@HiltViewModel`, `@Inject` 注解
- ✅ 添加了 `ServiceLocator` 单例对象
- ✅ 在每个需要 ViewModel 的页面创建了 `ViewModelFactory`

## 如何构建项目

### 使用 Android Studio
1. 打开 Android Studio
2. 选择 "Open an Existing Project"
3. 导航到 `闪念笔记/FleetingNotes` 目录
4. 等待 Gradle 同步完成
5. 点击 Run 按钮或按 Shift+F10

### 使用命令行
**注意**: 项目目前缺少 Gradle Wrapper 可执行文件，需要使用 Android Studio 生成。

生成步骤:
1. 用 Android Studio 打开项目一次
2. Android Studio 会自动生成 `gradlew` 和 `gradlew.bat`
3. 之后可以使用命令行构建:
```bash
cd "d:/claude code -11/闪念笔记/FleetingNotes"
./gradlew assembleDebug  # Linux/Mac
gradlew.bat assembleDebug  # Windows
```

## 项目结构

```
FleetingNotes/
├── app/
│   ├── src/
│   │   └── main/
│   │       ├── java/com/fleetingnotes/
│   │       │   ├── FleetingNotesApp.kt           # Application 入口 + ServiceLocator
│   │       │   ├── data/
│   │       │   │   ├── model/                    # 数据模型
│   │       │   │   ├── local/                    # JSON 文件存储
│   │       │   │   └── repository/               # Repository 实现
│   │       │   ├── domain/
│   │       │   │   ├── model/                    # 领域模型
│   │       │   │   └── repository/               # Repository 接口
│   │       │   ├── presentation/
│   │       │   │   ├── MainActivity.kt
│   │       │   │   ├── ui/
│   │       │   │   │   ├── components/           # UI 组件
│   │       │   │   │   ├── dialog/               # 对话框
│   │       │   │   │   ├── home/                 # 主页面
│   │       │   │   │   └── theme/                # 主题
│   │       │   │   └── viewmodel/
│   │       │   └── service/                      # 服务 (待实现)
│   │       └── res/                              # 资源文件
│   └── build.gradle.kts
└── build.gradle.kts
```

## 已知问题

1. **Gradle Wrapper 缺失**: 需要用 Android Studio 打开项目以生成
2. **FloatingWindowService 未实现**: 代码已备份，需要移除 Hilt 依赖后恢复
3. **对话框 FAB 点击未连接**: 需要在列表页面添加对话框调用逻辑
4. **图片序列化未测试**: 图片数据保存到 JSON 需要进一步测试

## 下一步建议

1. **优先级 1**: 使用 Android Studio 打开项目并验证编译
2. **优先级 2**: 实现对话框的打开和保存逻辑
3. **优先级 3**: 实现悬浮窗服务
4. **优先级 4**: 添加剪切板监听
5. **优先级 5**: 完善测试

## 文件统计

- **Kotlin 文件**: 20+ 个
- **数据模型**: 7 个 (Note 基类 + 3 种笔记 + 辅助模型)
- **UI 组件**: 11+ 个 (对话框 + 列表页 + 组件)
- **代码行数**: 约 3000+ 行

## 依赖版本

```gradle
- Kotlin: 1.9.22
- AGP: 8.2.2
- Compose BOM: 2024.02.00
- Material3: 1.1.2
- Navigation: 2.7.5
- ViewModel: 2.6.2
- Coroutines: 1.7.3
- kotlinx-serialization: 1.6.0
- Coil: 2.5.0
- Timber: 5.0.1
```

---

**最后更新**: 2026-01-24
**状态**: 核心架构完成，UI 框架就绪，等待功能实现和测试
