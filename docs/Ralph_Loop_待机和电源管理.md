# Ralph Loop 与电脑待机/电源管理

## ❗ 核心结论

**待机会中断 Ralph Loop！** 如果电脑进入待机、休眠或睡眠模式，正在运行的 Ralph Loop **会被中断**。

## 🖥️ 什么是电脑待机/休眠

### 待机（睡眠）模式
- **状态**：电脑进入低功耗状态
- **内存**：保持通电（数据保留在内存中）
- **CPU/硬盘**：停止运行
- **网络**：断开
- **恢复速度**：快（几秒钟）

### 休眠模式
- **状态**：电脑完全关闭
- **内存**：数据保存到硬盘
- **所有硬件**：完全断电
- **恢复速度**：慢（几十秒到几分钟）
- **优势**：不耗电

## ⚠️ 待机对 Ralph Loop 的影响

### 问题 1：Claude Code 进程被暂停

```
正常状态：
┌─────────────────────────────────┐
│  Ralph Loop 正在运行            │
│  - Claude 正在工作              │
│  - 第 5 次迭代                  │
│  - 正在编写代码...              │
└─────────────────────────────────┘
              ↓
    电脑进入待机模式 ⏸️
              ↓
┌─────────────────────────────────┐
│  所有进程被冻结！                │
│  - Claude Code 停止响应         │
│  - 网络连接断开                 │
│  - 定时器暂停                   │
└─────────────────────────────────┘
              ↓
    电脑从待机恢复 ▶️
              ↓
┌─────────────────────────────────┐
│  ❌ Ralph Loop 可能：           │
│  - 完全停止（需要手动重启）      │
│  - 连接断开（超时错误）          │
│  - 状态丢失（需从头开始）        │
└─────────────────────────────────┘
```

### 问题 2：网络请求超时

Ralph Loop 依赖 Claude API 的持续连接：

```
待机前：
Claude Code ←──网络──→ Claude API
     ✅ 连接正常

待机中：
Claude Code  ╳  网络  ╳  Claude API
     ❌ 网络断开

恢复后：
Claude Code ←──网络──→ Claude API
     ⚠️  连接可能超时失败
```

### 问题 3：时间敏感操作中断

```bash
# 假设 Ralph Loop 在第 3 次迭代

待机前：
iteration: 3
状态：正在运行测试...

待机 2 小时后恢复：
iteration: 3
状态：❌ 测试进程可能被杀死
       ❌ 临时文件可能被清理
       ❌ API 令牌可能过期
```

## ✅ 如何避免待机影响

### 方案 1：禁用待机和休眠（推荐长时间任务）

#### Windows 设置

**方法 A：使用设置应用**
```
1. 打开"设置" → "系统" → "电源和睡眠"
2. "睡眠"部分：
   - 在使用电池电源时，电脑在经过...之后进入睡眠状态 → 选择"从不"
   - 在接通电源时，电脑在经过...之后进入睡眠状态 → 选择"从不"
3. 关闭设置窗口
```

**方法 B：使用控制面板**
```
1. 右键"开始"按钮 → "电源选项"
2. 点击"选择计算机关闭按钮的功能"
3. 在"当前电源计划"中：
   - 关闭盖子时 → 选择"不采取任何操作"
   - 按电源按钮时 → 选择"不采取任何操作"
4. 点击"保存修改"
```

**方法 C：使用命令行（永久禁用睡眠）**
```bash
# 禁用睡眠模式
powercfg /change standby-timeout-ac 0
powercfg /change standby-timeout-dc 0

# 禁用休眠
powercfg /hibernate off

# 验证设置
powercfg /q
```

#### 检查当前电源设置

```bash
# 查看当前睡眠超时设置（分钟）
powercfg /q | grep standby-timeout

# 查看休眠是否启用
powercfg /h | grep HiberbootEnabled
```

### 方案 2：使用临时脚本保持唤醒

创建一个 PowerShell 脚本防止电脑待机：

```powershell
# prevent-sleep.ps1
# 使用方法：在启动 Ralph Loop 前运行此脚本

Add-Type -AssemblyName System.Windows.Forms
$originalTimeout = 0

try {
    # 保存原始设置
    $originalTimeout = (powercfg /q | Select-String "standby-timeout-ac").ToString()

    Write-Host "========================================" -ForegroundColor Green
    Write-Host "防止待机模式已激活" -ForegroundColor Green
    Write-Host "========================================" -ForegroundColor Green
    Write-Host ""
    Write-Host "原始设置：$originalTimeout" -ForegroundColor Yellow
    Write-Host "当前状态：电脑将不会进入待机" -ForegroundColor Yellow
    Write-Host ""
    Write-Host "按 Ctrl+C 停止此脚本并恢复原始设置" -ForegroundColor Red
    Write-Host ""

    # 禁用睡眠
    powercfg /change standby-timeout-ac 0

    # 保持脚本运行
    while ($true) {
        # 模拟轻微活动以保持唤醒
        [Windows.Forms.Cursor]::Position = [Windows.Forms.Cursor]::Position

        # 每 60 秒检查一次
        Start-Sleep -Seconds 60

        # 显示状态（可选，注释掉以减少输出）
        $currentTime = Get-Date -Format "HH:mm:ss"
        Write-Host "[$currentTime] 电脑保持唤醒中..." -ForegroundColor Cyan
    }
}
finally {
    # 恢复原始设置
    Write-Host ""
    Write-Host "恢复原始电源设置..." -ForegroundColor Yellow
    # 这里需要手动恢复或保存原始值
    Write-Host "脚本结束" -ForegroundColor Green
}
```

**使用方法**：
```bash
# 在启动 Ralph Loop 之前，在新终端运行：
powershell.exe -ExecutionPolicy Bypass -File prevent-sleep.ps1

# 然后在另一个终端启动 Ralph Loop
/ralph-loop "你的任务" --max-iterations 20
```

### 方案 3：使用专业工具

#### 工具 1：PowerToys Awake（推荐）

**Microsoft 官方工具，专门用于保持电脑唤醒**

下载和安装：
```bash
# 使用 winget 安装
winget install Microsoft.PowerToys

# 或从官网下载
# https://github.com/microsoft/PowerToys
```

使用方法：
```
1. 打开 PowerToys 设置
2. 找到 "Awake" 模块
3. 启用 "保持屏幕唤醒" 开关
4. 选择模式：
   - 不限时（直到手动关闭）
   - 定时（例如 2 小时）
```

#### 工具 2：Caffeine（轻量级）

```bash
# 使用 Chocolatey 安装
choco install caffeine

# 或下载便携版
# https://www.zhornsoftware.co.uk/caffeine/
```

使用方法：
```
1. 运行 Caffeine
2. 点击系统托盘中的咖啡杯图标
3. 咖啡杯变满 = 电脑保持唤醒
4. 再次点击取消
```

### 方案 4：调整电源计划（平衡方案）

不完全禁用待机，但延长等待时间：

```
1. 打开"电源选项"
2. 当前电源计划 → "更改计划设置"
3. "使计算机进入睡眠状态"：
   - 改为 2-4 小时（根据任务预期时间）
4. "使硬盘进入睡眠状态"：
   - 改为"从不"（避免硬盘休眠影响文件操作）
```

## 📊 不同场景的建议

### 场景 1：短期任务（< 30 分钟）

**建议**：不需要特殊设置

```bash
# 大多数短期任务不会触发待机
/ralph-loop "修复这个 bug" --max-iterations 10
```

**如果担心**：
- 手动移动鼠标防止待机
- 或使用 PowerToys Awake 设置 30 分钟定时

### 场景 2：中期任务（30 分钟 - 2 小时）

**建议**：使用临时保持唤醒工具

```bash
# 步骤 1：启动 PowerToys Awake（1 小时）
# 步骤 2：运行 Ralph Loop
/ralph-loop "实现 API 功能" --max-iterations 30
```

### 场景 3：长期任务（> 2 小时）

**建议**：禁用待机或使用专业工具

```bash
# 步骤 1：禁用系统待机
powercfg /change standby-timeout-ac 0

# 步骤 2：启动 Ralph Loop
/ralph-loop "开发完整功能" --max-iterations 100

# 步骤 3：任务完成后恢复待机
powercfg /change standby-timeout-ac 30
```

### 场景 4：过夜任务

**建议**：
- ✅ 禁用所有省电模式（待机、休眠、硬盘睡眠）
- ✅ 保持电源连接
- ✅ 关闭屏幕（可选，省电）
- ✅ 设置合理的 `--max-iterations` 作为安全网

```bash
# 完整配置示例

# 1. 禁用所有省电功能
powercfg /change standby-timeout-ac 0      # 禁用待机
powercfg /change standby-timeout-dc 0      # 禁用电池待机
powercfg /change disk-timeout-ac 0         # 禁用硬盘睡眠
powercfg /hibernate off                    # 禁用休眠

# 2. 验证设置
powercfg /q

# 3. 启动长期任务
/ralph-loop "开发完整应用，包括测试和文档" --max-iterations 200 --completion-promise "PROJECT_COMPLETE"

# 4. （可选）关闭屏幕省电但保持系统运行
# 在"电源选项"中："关闭屏幕" → 10 分钟后
# 但"使计算机进入睡眠状态" → 从不

# 5. 第二天恢复设置
powercfg /change standby-timeout-ac 30
powercfg /change disk-timeout-ac 20
```

## 🔍 检测和恢复

### 如何检查 Ralph Loop 是否被待机中断

```bash
# 查看状态文件
cat .claude/ralph-loop.local.md

# 检查迭代次数是否长时间不变
watch -n 10 'grep "^iteration:" .claude/ralph-loop.local.md'

# 检查 Claude Code 进程
ps aux | grep claude
```

### 如果被中断了怎么办

```bash
# 情况 1：状态文件还在
if [ -f .claude/ralph-loop.local.md ]; then
    echo "✅ 状态文件存在，可以继续"
    cat .claude/ralph-loop.local.md
    # 查看进度，然后可以手动继续工作
fi

# 情况 2：状态文件丢失
if [ ! -f .claude/ralph-loop.local.md ]; then
    echo "❌ 状态文件丢失，需要重新开始"
    # 查看文件修改时间，确定最后的工作点
    ls -lt *.py | head -5
fi
```

## ⚡ 最佳实践总结

### ✅ 推荐做法

1. **预估任务时间**
   ```bash
   # 短任务（< 30 分钟）→ 不需要特殊设置
   # 中任务（30 分钟 - 2 小时）→ 使用 PowerToys Awake
   # 长任务（> 2 小时）→ 禁用待机
   ```

2. **双重保险**
   ```bash
   # 总是设置 max-iterations
   /ralph-loop "任务" --max-iterations 20

   # 即使被中断，也不会无限运行
   ```

3. **定期保存**
   ```bash
   # Ralph Loop 会保留每次迭代的文件
   # 使用 Git 追踪进度
   git add .
   git commit -m "Iteration 5: Progress"
   ```

4. **使用 Git 保护工作成果**
   ```bash
   # 在提示词中包含 Git 提交
   /ralph-loop "开发功能：
   1. 每次迭代后提交到 Git
   2. 提交信息：'feat: 进度描述'
   3. 这样即使中断也能恢复" --max-iterations 30
   ```

### ❌ 避免的做法

1. **不要假设电脑不会待机**
   ```bash
   # ❌ 危险：长时间任务但没有禁用待机
   /ralph-loop "过夜任务" --max-iterations 100
   # 电脑可能在 30 分钟后待机，任务中断
   ```

2. **不要依赖屏幕保持唤醒**
   ```
   ⚠️ 关闭屏幕 ≠ 待机
   ⚠️ 但省电设置可能同时关闭两者
   ```

3. **不要忽视笔记本盖子**
   ```
   合上笔记本盖子通常触发待机
   → 在"电源选项"中设置"关闭盖子时" → "不采取任何操作"
   ```

## 🛠️ 实用脚本

### 一键配置脚本

保存为 `setup-ralph-power.ps1`：

```powershell
# Ralph Loop 电源配置脚本
# 使用方法：在启动 Ralph Loop 前运行此脚本

Write-Host "========================================" -ForegroundColor Green
Write-Host "Ralph Loop 电源配置" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Green
Write-Host ""

# 检查当前设置
Write-Host "当前电源设置：" -ForegroundColor Yellow
$standbyAC = powercfg /q | Select-String "standby-timeout-ac"
Write-Host "  待机（AC）：$standbyAC" -ForegroundColor Cyan
Write-Host ""

# 显示菜单
Write-Host "请选择操作：" -ForegroundColor Yellow
Write-Host "  1. 禁用待机（推荐长时间任务）" -ForegroundColor White
Write-Host "  2. 延长待机时间到 4 小时" -ForegroundColor White
Write-Host "  3. 恢复默认设置（30 分钟）" -ForegroundColor White
Write-Host "  4. 查看当前设置" -ForegroundColor White
Write-Host "  0. 退出" -ForegroundColor White
Write-Host ""

$choice = Read-Host "输入选项 (0-4)"

switch ($choice) {
    '1' {
        Write-Host ""
        Write-Host "禁用待机..." -ForegroundColor Yellow
        powercfg /change standby-timeout-ac 0 | Out-Null
        powercfg /change standby-timeout-dc 0 | Out-Null
        Write-Host "✅ 待机已禁用" -ForegroundColor Green
    }
    '2' {
        Write-Host ""
        Write-Host "设置待机时间为 4 小时..." -ForegroundColor Yellow
        powercfg /change standby-timeout-ac 240 | Out-Null
        Write-Host "✅ 待机时间已设置为 4 小时" -ForegroundColor Green
    }
    '3' {
        Write-Host ""
        Write-Host "恢复默认设置..." -ForegroundColor Yellow
        powercfg /change standby-timeout-ac 30 | Out-Null
        powercfg /change standby-timeout-dc 15 | Out-Null
        Write-Host "✅ 已恢复默认设置" -ForegroundColor Green
    }
    '4' {
        Write-Host ""
        Write-Host "详细电源设置：" -ForegroundColor Yellow
        powercfg /q
    }
    '0' {
        Write-Host "退出" -ForegroundColor Yellow
        exit
    }
}

Write-Host ""
Write-Host "完成！" -ForegroundColor Green
```

**使用方法**：
```bash
powershell.exe -ExecutionPolicy Bypass -File setup-ralph-power.ps1
```

## 📝 快速参考

### 短期任务（< 30 分钟）
```bash
# 不需要特殊设置
/ralph-loop "任务" --max-iterations 10
```

### 中期任务（30 分钟 - 2 小时）
```bash
# 使用 PowerToys Awake 或临时禁用待机
powercfg /change standby-timeout-ac 120
/ralph-loop "任务" --max-iterations 30
```

### 长期任务（> 2 小时）
```bash
# 完全禁用待机
powercfg /change standby-timeout-ac 0
powercfg /hibernate off
/ralph-loop "任务" --max-iterations 100
# 任务完成后恢复
powercfg /change standby-timeout-ac 30
```

---

**重要提醒**：
- ✅ 总是根据任务时长配置电源设置
- ✅ 使用 `--max-iterations` 作为双重保险
- ✅ 用 Git 追踪进度，防止工作丢失
- ❌ 不要让笔记本电脑合盖运行（会待机）

需要我帮您配置电源设置吗？或者有其他关于 Ralph Loop 的问题？
