# Ralph Loop 使用演示

## 📖 什么是 Ralph Loop？

**简单来说**：Ralph Loop 就像一个"自动重复工作"的机器人，它会不断重复执行同一个任务，直到任务完成或达到重复次数限制。

**类比**：
- 想象你在做一道菜，Ralph Loop 就是：
  1. 你做菜 → 尝味道
  2. 不够好？→ 重新做 → 再尝
  3. 还不够好？→ 继续做 → 继续尝
  4. 直到味道完美为止！

## 🎯 核心工作原理

```
用户运行一次命令：
┌─────────────────────────────────────────┐
│  /ralph-loop "任务" --max-iterations 10  │
└─────────────────────────────────────────┘
                  ↓
    ┌─────────────────────────────────┐
    │  第1次迭代：Claude 开始工作      │
    │  - 编写代码                      │
    │  - 修改文件                      │
    └─────────────────────────────────┘
                  ↓
    ┌─────────────────────────────────┐
    │  Claude 尝试退出                │
    └─────────────────────────────────┘
                  ↓
    ┌─────────────────────────────────┐
    │  Stop Hook 拦截退出！           │
    │  - 检查：任务完成了吗？          │
    │  - 没完成？重新喂入相同提示词    │
    │  - 迭代次数 +1                  │
    └─────────────────────────────────┘
                  ↓
    ┌─────────────────────────────────┐
    │  第2次迭代：Claude 继续工作      │
    │  - 看到之前的代码               │
    │  - 基于上次结果改进             │
    └─────────────────────────────────┘
                  ↓
              循环重复...
                  ↓
    ┌─────────────────────────────────┐
    │  停止条件触发：                  │
    │  - 达到 max_iterations          │
    │  - 或输出 <promise>DONE</promise>│
    └─────────────────────────────────┘
```

## 🚀 基础使用（3分钟上手）

### 步骤 1：确认安装

首先确保插件已安装：

```bash
# 检查插件文件
ls ~/.claude/plugins/ralph-loop/

# 检查 jq 工具
jq --version

# 应该看到：
# ralph-loop/
#   .claude-plugin/
#   commands/
#   hooks/
#   scripts/
#   README.md

# jq-1.7-dirty (或类似版本号)
```

### 步骤 2：最简单的示例

让我们从一个最简单的任务开始：

```bash
/ralph-loop "创建一个 hello.py 文件，打印 'Hello, World!'" --max-iterations 3
```

**会发生什么**：

1. **第1次迭代**：
   - Claude 创建 `hello.py`
   - 内容：`print("Hello, World!")`
   - 尝试退出

2. **Stop Hook 拦截**：
   - 检查迭代次数：1 < 3
   - 重新喂入提示词

3. **第2次迭代**：
   - Claude 看到已存在的 `hello.py`
   - 可能改进代码（添加注释、错误处理等）
   - 尝试退出

4. **Stop Hook 再次拦截**：
   - 检查迭代次数：2 < 3
   - 重新喂入提示词

5. **第3次迭代**：
   - Claude 继续改进
   - 尝试退出

6. **Stop Hook 检查**：
   - 迭代次数：3 >= 3
   - 允许退出！✅

### 步骤 3：查看进度

在循环运行时（另一个终端），你可以监控进度：

```bash
# 查看当前迭代次数
grep '^iteration:' .claude/ralph-loop.local.md

# 查看完整状态
cat .claude/ralph-loop.local.md

# 输出示例：
# ---
# active: true
# iteration: 2
# max_iterations: 3
# completion_promise: null
# started_at: "2026-01-08T08:00:00Z"
# ---
#
# 创建一个 hello.py 文件，打印 'Hello, World!'
```

## 🎯 实用示例

### 示例 1：TDD 开发（推荐）

**任务**：使用测试驱动开发创建一个计算器类

```bash
/ralph-loop "按照 TDD 流程创建计算器类 calculator.py：

1. 编写失败的测试（test_calculator.py）
   - test_add: 测试加法
   - test_subtract: 测试减法
   - test_multiply: 测试乘法
   - test_divide: 测试除法
   - test_divide_by_zero: 测试除零错误

2. 实现计算器类使测试通过

3. 运行 pytest 并查看结果

4. 如果测试失败：
   - 分析失败原因
   - 修复代码
   - 重新运行测试
   - 重复直到所有测试通过

5. 完成标准：
   - 所有 5 个测试都通过
   - pytest 输出显示 5 passed

6. 完成后输出：<promise>TESTS_PASSING</promise>

注意事项：
- 每次迭代都要运行 pytest
- 根据测试输出调整代码
- 保持代码简洁" --completion-promise "TESTS_PASSING" --max-iterations 15
```

**预期行为**：

- 第1-3次：编写测试用例
- 第4-8次：实现基本功能
- 第9-12次：修复边界情况（除零等）
- 第13-15次：优化和完善
- 最终输出：`<promise>TESTS_PASSING</promise>`

### 示例 2：修复测试失败

**场景**：项目中已有测试但失败了

```bash
/ralph-loop "修复所有失败的测试：

1. 首先运行 pytest 并查看失败结果
2. 分析每个失败测试的原因
3. 修复导致失败的代码
4. 重新运行 pytest
5. 如果还有失败，重复步骤 2-4
6. 直到所有测试都通过

完成后输出：<promise>ALL_TESTS_PASSING</promise>

当前状态：
- 在项目根目录运行测试
- 使用 pytest 命令" --completion-promise "ALL_TESTS_PASSING" --max-iterations 20
```

### 示例 3：代码重构

**任务**：重构现有代码，提高质量

```bash
/ralph-loop "重构 data_processor.py 模块：

改进目标：
1. 提取重复代码为函数
2. 改善变量和函数命名
3. 添加类型提示
4. 添加 docstring 文档
5. 优化算法性能
6. 添加错误处理

约束条件：
- 保持现有功能不变
- 所有测试必须通过
- 每次迭代后运行 pytest 验证

工作流程：
1. 阅读当前代码
2. 识别改进点
3. 实施改进
4. 运行测试验证
5. 如果测试失败，回退修改
6. 重复直到代码质量显著提升

完成后输出：<promise>REFACTOR_COMPLETE</promise>" --completion-promise "REFACTOR_COMPLETE" --max-iterations 15
```

### 示例 4：创建完整功能

**任务**：创建一个待办事项 REST API

```bash
/ralph-loop "创建一个 Flask 待办事项 API：

技术栈：
- Flask (Web 框架)
- SQLite (数据库)
- pytest (测试)

功能要求：
1. GET /api/todos - 获取所有待办事项
2. POST /api/todos - 创建新待办事项
3. GET /api/todos/<id> - 获取单个待办事项
4. PUT /api/todos/<id> - 更新待办事项
5. DELETE /api/todos/<id> - 删除待办事项

数据模型：
- id: 整数，主键
- title: 字符串，必填
- completed: 布尔值，默认 False
- created_at: 时间戳

完成标准：
1. 所有 5 个端点正常工作
2. 输入验证（title 不能为空）
3. 至少 10 个测试用例
4. 测试覆盖率 > 80%
5. 包含 README.md 说明如何使用

完成后输出：<promise>API_COMPLETE</promise>

注意：
- 按照 TDD 流程开发
- 每次迭代都要运行测试
- 根据测试结果调整代码" --completion-promise "API_COMPLETE" --max-iterations 50
```

## 🛑 如何停止循环

### 方法 1：自然停止（推荐）

```bash
# 设置最大迭代次数
/ralph-loop "任务" --max-iterations 10

# 或者设置完成标志
/ralph-loop "任务，完成后输出 <promise>DONE</promise>" --completion-promise "DONE"

# 或者两者都用（双重保险）
/ralph-loop "任务" --completion-promise "DONE" --max-iterations 20
```

### 方法 2：手动停止

```bash
# 使用取消命令
/cancel-ralph

# 或手动删除状态文件
rm .claude/ralph-loop.local.md
```

## 📊 监控和调试

### 查看当前状态

```bash
# 查看完整状态文件
cat .claude/ralph-loop.local.md

# 查看迭代次数
grep '^iteration:' .claude/ralph-loop.local.md

# 查看开始时间
grep 'started_at:' .claude/ralph-loop.local.md

# 查看提示词
tail -n +10 .claude/ralph-loop.local.md
```

### 查看文件变化

```bash
# 使用 Git 查看每次迭代的修改
git log --oneline

# 查看特定文件的修改历史
git log --follow -- path/to/file.py

# 对比两次迭代
git diff commit1 commit2

# 查看最近的修改
git diff HEAD~1 HEAD
```

## ⚙️ 高级用法

### 1. 分阶段开发

```bash
# 阶段 1：基础结构
/ralph-loop "创建项目基础结构和配置" --max-iterations 5

# 阶段 2：核心功能
/ralph-loop "实现核心业务逻辑" --max-iterations 15

# 阶段 3：测试和文档
/ralph-loop "编写测试和文档" --max-iterations 10
```

### 2. 利用文件持久性

Ralph Loop 会保留每次迭代的文件，利用这一点：

```bash
/ralph-loop "改进 calculator.py：

1. 阅读现有的 calculator.py（如果存在）
2. 识别可以改进的地方
3. 实施改进
4. 运行测试验证
5. 确保没有破坏现有功能

可以参考之前迭代的代码，持续改进。" --max-iterations 10
```

### 3. 结合 Git 使用

```bash
/ralph-loop "开发功能并提交到 Git：

1. 实现功能
2. 运行测试
3. 如果测试通过，提交到 Git
4. 提交信息格式：'feat: 简短描述'

完成后输出：<promise>FEATURE_COMPLETE</promise>" --completion-promise "FEATURE_COMPLETE" --max-iterations 20
```

## ⚠️ 常见错误和解决方案

### 错误 1：循环不停止

**症状**：循环一直运行，超过预期次数

**原因**：
- 没有设置 `--max-iterations`
- `--completion-promise` 的文本不匹配

**解决**：
```bash
# 立即停止
/cancel-ralph

# 下次使用时，务必设置
/ralph-loop "任务" --max-iterations 10
```

### 错误 2：完成标志不生效

**症状**：输出了 `<promise>DONE</promise>` 但循环继续

**检查**：
1. 标志文本是否完全匹配（区分大小写）
2. 是否使用了正确的 XML 标签
3. 标签前后是否有空格

**正确格式**：
```
<promise>DONE</promise>
```

**错误格式**：
```
❌ <promise> DONE </promise>  (有空格)
❌ <promise>done</promise>    (大小写错误)
❌ promise>DONE</promise>     (缺少尖括号)
```

### 错误 3：任务太复杂无法完成

**症状**：循环运行很多次但没有进展

**解决**：
```bash
# ❌ 不好：任务太大
/ralph-loop "创建一个完整的电商系统" --max-iterations 20

# ✅ 好：分解成小任务
/ralph-loop "创建电商系统的用户认证模块" --max-iterations 10

# 然后
/ralph-loop "创建电商系统的商品目录模块" --max-iterations 10
```

## 🎓 最佳实践

### 1. 明确的完成标准

```bash
# ❌ 模糊
/ralph-loop "做一个好的 API"

# ✅ 明确
/ralph-loop "创建一个 REST API，满足：
- 5 个端点都正常工作
- 测试覆盖率 > 80%
- 包含 README 文档
- 完成后输出 <promise>API_COMPLETE</promise>"
```

### 2. 包含自检步骤

```bash
# ❌ 缺少验证
/ralph-loop "实现排序算法"

# ✅ 包含验证
/ralph-loop "实现排序算法：
1. 编写测试用例
2. 实现算法
3. 运行测试
4. 如果失败，调试并修复
5. 重复直到所有测试通过"
```

### 3. 设置合理的限制

```bash
# ❌ 太激进
/ralph-loop "复杂任务" --max-iterations 100

# ✅ 合理
/ralph-loop "复杂任务" --max-iterations 20

# 如果没完成，可以再次运行
```

## 📝 提示词模板

### TDD 模板
```bash
/ralph-loop "TDD 开发 [功能名称]：
1. 编写失败的测试
2. 实现最小可行代码
3. 运行测试
4. 如果失败，修复代码
5. 重复直到测试通过
6. 输出 <promise>TESTS_PASSING</promise>" --completion-promise "TESTS_PASSING" --max-iterations 15
```

### Bug 修复模板
```bash
/ralph-loop "修复 [Bug 描述]：
1. 运行测试并查看失败
2. 分析根本原因
3. 实施修复
4. 验证测试通过
5. 输出 <promise>BUG_FIXED</promise>" --completion-promise "BUG_FIXED" --max-iterations 10
```

### 重构模板
```bash
/ralph-loop "重构 [模块名称]：
1. 改善代码结构
2. 优化性能
3. 添加文档
4. 保持测试通过
5. 输出 <promise>REFACTOR_COMPLETE</promise>" --completion-promise "REFACTOR_COMPLETE" --max-iterations 15
```

## 🚀 立即开始

现在你可以尝试第一个任务：

```bash
/ralph-loop "创建一个简单的计算器：
1. 创建 calculator.py
2. 实现加减乘除功能
3. 创建 test_calculator.py
4. 至少 5 个测试用例
5. 完成后输出 <promise>CALCULATOR_COMPLETE</promise>" --completion-promise "CALCULATOR_COMPLETE" --max-iterations 20
```

祝你使用愉快！🎉
