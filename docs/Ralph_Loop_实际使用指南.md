# Ralph Loop 实际使用指南

## 🎯 如何使用 Ralph Loop

### 重要提示

⚠️ **Ralph Loop 是一个 Claude Code 插件命令**，你需要：

1. **重启 VSCode 或 Claude Code**（如果刚安装）
2. **在 Claude Code 对话中直接输入命令**
3. **命令以 `/` 开头**（类似 slash command）

---

## 📝 基本使用方法

### 步骤 1：确认插件可用

在 Claude Code 中输入：

```
/help
```

如果看到 Ralph Loop 相关的帮助信息，说明插件已加载成功。

### 步骤 2：使用 Ralph Loop 命令

**基本语法**：

```
/ralph-loop "任务描述" --max-iterations 数字 --completion-promise "完成标志"
```

**最简单的示例**：

```
/ralph-loop "创建一个 hello.py 文件，打印 Hello World" --max-iterations 5
```

---

## 💡 实际使用示例

### 示例 1：创建简单计算器（推荐新手）

**在 Claude Code 中输入**：

```
/ralph-loop "创建一个简单的计算器应用：

1. 创建 calculator.py 文件
2. 实现加减乘除四个函数
3. 创建 test_calculator.py 测试文件
4. 编写至少 5 个测试用例
5. 每次迭代运行 pytest 验证
6. 根据测试结果改进代码

完成后输出：<promise>CALCULATOR_COMPLETE</promise>" --completion-promise "CALCULATOR_COMPLETE" --max-iterations 20
```

**会发生什么**：

```
第 1 次迭代：
├─ Claude 创建 calculator.py
├─ 实现基本函数
└─ 尝试退出 → Stop Hook 拦截

第 2 次迭代：
├─ Claude 看到已存在的 calculator.py
├─ 创建 test_calculator.py
├─ 编写测试用例
└─ 尝试退出 → Stop Hook 拦截

第 3-5 次迭代：
├─ 运行 pytest
├─ 修复失败的测试
├─ 改进代码结构
└─ 继续迭代...

第 20 次迭代或输出 <promise>CALCULATOR_COMPLETE</promise>：
└─ Stop Hook 允许退出，循环结束 ✅
```

### 示例 2：修复 Bug

```
/ralph-loop "修复项目中所有失败的测试：

1. 运行 pytest 查看失败结果
2. 分析每个失败的原因
3. 修复代码
4. 重新运行测试
5. 如果还有失败，重复步骤 2-4

完成后输出：<promise>TESTS_PASSING</promise>" --completion-promise "TESTS_PASSING" --max-iterations 15
```

### 示例 3：重构代码

```
/ralph-loop "重构数据填充模块：

1. 改善代码结构和可读性
2. 提取重复代码为函数
3. 添加注释和文档
4. 优化性能
5. 每次迭代后运行测试验证

完成后输出：<promise>REFACTOR_COMPLETE</promise>" --completion-promise "REFACTOR_COMPLETE" --max-iterations 10
```

### 示例 4：创建完整功能

```
/ralph-loop "创建一个待办事项 REST API：

技术栈：Flask + SQLite

功能要求：
1. GET /api/todos - 获取所有待办事项
2. POST /api/todos - 创建新待办事项
3. GET /api/todos/<id> - 获取单个待办事项
4. PUT /api/todos/<id> - 更新待办事项
5. DELETE /api/todos/<id> - 删除待办事项

完成标准：
- 所有 5 个端点正常工作
- 输入验证完整
- 至少 10 个测试用例
- 测试覆盖率 > 80%
- 包含 README.md 文档

完成后输出：<promise>API_COMPLETE</promise>" --completion-promise "API_COMPLETE" --max-iterations 50
```

---

## 🛑 如何停止循环

### 方法 1：自然停止（推荐）

```bash
# 设置最大迭代次数
/ralph-loop "任务" --max-iterations 10
# 10 次迭代后自动停止
```

```bash
# 设置完成标志
/ralph-loop "任务，完成后输出 <promise>DONE</promise>" --completion-promise "DONE"
# Claude 输出标志后自动停止
```

```bash
# 双重保险（推荐）
/ralph-loop "任务" --completion-promise "DONE" --max-iterations 20
```

### 方法 2：手动停止

在 Claude Code 中输入：

```
/cancel-ralph
```

---

## 📊 监控循环进度

### 查看当前迭代次数

```bash
# 在另一个终端运行
cat .claude/ralph-loop.local.md
```

输出示例：

```yaml
---
active: true
iteration: 5
max_iterations: 20
completion_promise: "CALCULATOR_COMPLETE"
started_at: "2026-01-08T09:00:00Z"
---

创建一个简单的计算器应用：
1. 创建 calculator.py 文件
...
```

### 使用 Git 追踪进度

```bash
# 查看每次迭代的修改
git log --oneline

# 查看特定文件的修改
git log --follow -- calculator.py

# 对比两次迭代
git diff commit1 commit2
```

---

## ⚙️ 命令选项详解

### --max-iterations

**作用**：设置最大迭代次数（安全网）

```bash
# 无限循环（危险！）
/ralph-loop "任务"

# 限制迭代次数（推荐）
/ralph-loop "任务" --max-iterations 20
```

**建议值**：
- 简单任务：5-10
- 中等任务：10-20
- 复杂任务：20-50
- 大型项目：50-100

### --completion-promise

**作用**：设置完成标志

```bash
/ralph-loop "任务，完成后输出 <promise>DONE</promise>" --completion-promise "DONE"
```

**重要**：
- 标志文本必须完全匹配（区分大小写）
- 使用 XML 标签：`<promise>DONE</promise>`
- Claude 只在任务真正完成时才输出

**示例**：

```bash
# ✅ 正确
/ralph-loop "任务" --completion-promise "DONE"
# Claude 输出：<promise>DONE</promise>

# ❌ 错误（大小写不匹配）
/ralph-loop "任务" --completion-promise "DONE"
# Claude 输出：<promise>done</promise>  ❌ 不会停止循环
```

---

## 🎯 编写好的任务描述

### ✅ 好的任务描述

**示例 1：明确具体**
```
创建用户认证系统：
1. 实现注册功能（email + 密码）
2. 实现登录功能（JWT token）
3. 添加密码验证（最少 8 位）
4. 编写 10 个测试用例
5. 完成后输出 <promise>AUTH_COMPLETE</promise>
```

**示例 2：包含验证步骤**
```
实现快速排序算法：
1. 编写算法
2. 编写测试用例（至少 5 个）
3. 运行测试验证
4. 如果失败，调试并修复
5. 完成后输出 <promise>SORT_COMPLETE</promise>
```

**示例 3：分阶段任务**
```
开发博客系统：

阶段 1：数据库模型
- 用户表
- 文章表
- 评论表

阶段 2：API 端点
- 文章 CRUD
- 用户认证

阶段 3：前端页面
- 首页
- 文章详情
- 登录页面

完成后输出 <promise>BLOG_COMPLETE</promise>
```

### ❌ 不好的任务描述

```
❌ "做一个好的 API"
   → 太模糊，没有明确标准

❌ "优化代码"
   → 没有说明优化什么、如何验证

❌ "创建完整的电商系统"
   → 任务太大，应该分解
```

---

## 🔄 工作流程示例

### 完整的开发流程

```
1️⃣  启动 Ralph Loop
    /ralph-loop "实现功能 X" --max-iterations 20

2️⃣  Claude 开始第 1 次迭代
    - 创建基础文件
    - 实现基本功能
    - 尝试退出

3️⃣  Stop Hook 拦截
    - 检查迭代次数（1 < 20）
    - 重新喂入相同提示词

4️⃣  Claude 第 2 次迭代
    - 看到已存在的文件
    - 改进代码
    - 添加测试
    - 尝试退出

5️⃣  循环继续...
    - 每次迭代都看到之前的进度
    - 基于上次结果改进
    - 逐步完善功能

6️⃣  达到停止条件
    - 迭代 20 次
    - 或输出 <promise>COMPLETE</promise>

7️⃣  循环结束 ✅
```

---

## 💡 实用技巧

### 技巧 1：使用 Git 保护工作

在任务描述中包含 Git 提交：

```
/ralph-loop "开发功能：
1. 实现功能
2. 每次迭代后提交到 Git
3. 提交信息格式：'feat: 简短描述'
4. 这样即使中断也能恢复进度" --max-iterations 20
```

### 技巧 2：分阶段开发

```bash
# 阶段 1：基础结构
/ralph-loop "创建项目基础结构" --max-iterations 5

# 阶段 2：核心功能
/ralph-loop "实现核心业务逻辑" --max-iterations 15

# 阶段 3：测试和文档
/ralph-loop "编写测试和文档" --max-iterations 10
```

### 技巧 3：利用文件持久性

```
/ralph-loop "改进 calculator.py：
1. 阅读现有的 calculator.py（如果存在）
2. 识别可以改进的地方
3. 实施改进
4. 运行测试验证
5. 确保不破坏现有功能" --max-iterations 10
```

### 技巧 4：TDD 开发流程

```
/ralph-loop "按 TDD 流程开发：
1. 编写失败的测试
2. 实现最小可行代码
3. 运行测试
4. 如果失败，修复代码
5. 重复直到测试通过
6. 输出 <promise>TESTS_PASSING</promise>" --completion-promise "TESTS_PASSING" --max-iterations 15
```

---

## ⚠️ 常见问题

### Q1: 命令不可用？

**A**: 确认已重启 VSCode/Claude Code，插件需要重启才能加载。

### Q2: 循环不停止？

**A**: 检查是否设置了 `--max-iterations` 或 `--completion-promise`。

### Q3: 完成标志不生效？

**A**: 确保标志文本完全匹配，使用正确的 XML 标签格式。

### Q4: 任务太复杂？

**A**: 分解成多个小任务，分阶段完成。

---

## 🚀 立即开始

### 推荐的第一个任务

复制以下命令到 Claude Code：

```
/ralph-loop "创建一个简单的计算器：

1. 创建 calculator.py 文件
2. 实现加减乘除四个函数
3. 创建 test_calculator.py 测试文件
4. 编写至少 5 个测试用例
5. 每次迭代运行 pytest
6. 根据测试结果改进代码

完成后输出：<promise>CALCULATOR_COMPLETE</promise>" --completion-promise "CALCULATOR_COMPLETE" --max-iterations 20
```

---

## 📚 更多资源

- [快速参考卡片](Ralph_Loop_快速参考.md)
- [详细使用演示](Ralph_Loop_使用演示.md)
- [完整使用指南](RALPH_LOOP_使用指南.md)
- [待机和电源管理](Ralph_Loop_待机和电源管理.md)

---

**准备好了吗？在 Claude Code 中尝试上面的命令吧！** 🎉
