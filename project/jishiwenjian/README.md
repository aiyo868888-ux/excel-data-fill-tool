# 即时剪贴板 (Jishi Clipboard)

一个简洁的 Android 悬浮窗剪贴板管理应用，帮助用户快速保存剪贴板内容并添加标签。

## 功能特点

- ✅ **悬浮窗快速保存**：点击悬浮窗即可快速保存剪贴板内容
- ✅ **标签管理**：为保存的内容添加多个标签，方便分类
- ✅ **可拖动悬浮窗**：悬浮窗可以自由拖动到任意位置
- ✅ **本地存储**：使用 Room 数据库本地存储
- ✅ **简洁界面**：Material Design 3 风格

## 使用方法

1. 启动应用并授权悬浮窗权限
2. 点击"开启悬浮窗"按钮
3. 复制任意文本内容
4. 点击屏幕上的悬浮窗图标（📋）
5. 在弹出的对话框中编辑内容并输入标签
6. 点击保存

## 技术栈

- **Kotlin** - 开发语言
- **Jetpack Compose** - UI 框架
- **Hilt** - 依赖注入
- **Room** - 本地数据库
- **Coroutines** - 异步处理
- **Material Design 3** - UI 设计

## 系统要求

- Android 8.0 (API 26) 或更高版本

## 构建项目

```bash
# 克隆项目
git clone <repository-url>

# 进入项目目录
cd jishiwenjian

# 构建 APK
./gradlew assembleDebug

# 安装到设备
./gradlew installDebug
```

## 项目结构

```
app/src/main/java/com/jishi/clipboard/
├── data/           # 数据层（Entity、DAO、Database）
├── repository/     # 仓库层
├── service/        # 服务（悬浮窗、剪贴板监听）
├── ui/            # 界面（Activity、Dialog）
├── di/            # 依赖注入模块
└── ClipboardApp.kt # Application 类
```

## 许可证

MIT License
