# 悬浮窗不显示问题排查

## 使用步骤

### 1. 安装应用
```bash
# 在 Android Studio 中点击运行，或
adb install app/build/outputs/apk/debug/app-debug.apk
```

### 2. 授权悬浮窗权限
1. 打开应用
2. 如果显示"悬浮窗：未授权"，点击"授权悬浮窗权限"按钮
3. 在系统设置中开启"允许在其他应用上层显示"权限
4. 返回应用，应该显示"悬浮窗：已停止"

### 3. 开启悬浮窗
1. 点击"开启悬浮窗"按钮
2. 状态应该变为"悬浮窗：运行中"
3. 屏幕上应该出现一个圆形的剪贴板图标（📋）

## 排查步骤

### 检查日志
在 Android Studio 的 Logcat 中过滤标签 `FloatingWindowService`：

```
adb logcat -s FloatingWindowService
```

应该看到以下日志：
```
✅ 悬浮窗服务已创建
onStartCommand: action=com.jishi.clipboard.ACTION_SHOW, isRunning=false
✅ 开始显示悬浮窗
📱 创建悬浮窗视图
✅ 悬浮窗视图创建成功
✅ 悬浮窗已显示到屏幕
```

### 常见问题

#### 问题1: 没有授权悬浮窗权限
**日志**: `❌ 没有悬浮窗权限！`

**解决**:
1. 进入应用
2. 点击"授权悬浮窗权限"
3. 在系统设置中开启权限

#### 问题2: 服务未启动
**日志**: 无日志输出

**解决**:
1. 检查是否点击了"开启悬浮窗"按钮
2. 查看通知栏是否有"悬浮窗运行中"的通知
3. 重启应用

#### 问题3: 悬浮窗被系统遮挡
**症状**: 日志显示"悬浮窗已显示"但看不见

**解决**:
1. 检查屏幕边缘（默认位置在左上角 100,200）
2. 尝试拖动悬浮窗到屏幕中央
3. 检查是否有其他全屏应用遮挡

#### 问题4: Android 版本过低
**要求**: Android 8.0 (API 26) 或更高

**解决**: 升级 Android 系统或使用更高版本的设备

### 手动启动服务（通过 adb）
```bash
# 启动服务
adb shell am startservice -a com.jishi.clipboard.ACTION_SHOW com.jishi.clipboard/.service.FloatingWindowService

# 停止服务
adb shell am startservice -a com.jishi.clipboard.ACTION_HIDE com.jishi.clipboard/.service.FloatingWindowService
```

### 测试悬浮窗功能
1. 复制一段文本（长按选择 → 复制）
2. 点击悬浮窗图标
3. 应该弹出编辑对话框
4. 编辑内容并输入标签
5. 点击保存

## 预期效果

- ✅ 悬浮窗显示为圆形图标（📋）
- ✅ 可以拖动到任意位置
- ✅ 点击后弹出对话框
- ✅ 自动填充剪贴板内容
- ✅ 可以保存内容到数据库
