# 测试指南 - 验证内容类型修复

## 测试前准备

### 方案一：清理数据（推荐）
```bash
adb shell pm clear com.jishi.clipboard
```

### 方案二：升级安装（保留数据）
直接安装新 APK，数据库会自动迁移到 v7

## 测试步骤

### 测试 1：灵感类型
1. 点击悬浮窗
2. 选择【灵感】类型
3. 输入内容："这是一条灵感"
4. （可选）添加标签"工作"
5. 点击保存
6. **预期结果**：内容出现在导航栏【灵感】Tab

### 测试 2：启发类型
1. 点击悬浮窗
2. 选择【启发】类型
3. 输入内容："这是一条启发"
4. （可选）添加标签"读书"
5. 点击保存
6. **预期结果**：内容出现在导航栏【启发】Tab

### 测试 3：待办类型
1. 点击悬浮窗
2. 选择【待办】类型
3. 输入内容："明天下午3点开会"
4. 点击保存
5. **预期结果**：
   - 内容出现在导航栏【待办】Tab
   - 弹出提醒设置对话框

### 测试 4：标签独立性
1. 保存一条灵感，标签选择"启发"
2. **预期结果**：
   - 内容显示在【灵感】Tab（而不是启发）
   - 说明标签不影响导航栏分类

### 测试 5：无标签保存
1. 保存一条启发，不选择任何标签
2. **预期结果**：内容正常显示在【启发】Tab

## 查看日志验证

```bash
# 实时查看日志
adb logcat | findstr "ClipboardEdit\|UnifiedContent"
```

### 关键日志
```
ClipboardEdit: 内容类型: 灵感
ClipboardEdit: 执行新增操作，类型=灵感
ClipboardEdit: 保存成功！返回 ID: 1
UnifiedContentRepository: observeInspirations 被调用 - 筛选 type='灵感'
```

## 数据库验证

```bash
# 进入数据库
adb shell
su
cd /data/data/com.jishi.clipboard/databases
sqlite3 jishi_clipboard.db

# 查看表结构（应该有 type 列）
.schema clipboards

# 查看数据
SELECT id, substr(content, 1, 20) as content, type FROM clipboards;
```

### 预期结果
```
id|content|type
1|这是一条灵感|灵感
2|这是一条启发|启发
3|明天下午3点开会|待办
```

## 常见问题

### Q1: 保存后看不到内容
**检查点**：
- Toast 是否显示"已保存到【灵感】"？
- 日志是否显示 `type=灵感`？
- 切换到对应的导航栏 Tab

### Q2: 内容显示在错误的 Tab
**检查点**：
- 悬浮窗选择的类型是否正确？
- 数据库中 type 字段值

### Q3: 数据库迁移失败
**解决方案**：
```bash
# 完全卸载重装
adb uninstall com.jishi.clipboard
adb install app-debug.apk
```

## 成功标准

- ✅ 每种类型的内容都显示在正确的导航栏
- ✅ 标签选择不影响内容分类
- ✅ Toast 提示包含正确的类型名称
- ✅ 数据库中 type 字段值正确
