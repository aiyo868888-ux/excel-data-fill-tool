# Ralph Loop 使用指南

## ✅ 安装状态

**安装完成！** 所有组件已成功安装并测试通过。

### 安装详情

| 组件 | 状态 | 位置 |
|------|------|------|
| **Ralph Loop 插件** | ✅ 已安装 | `C:\Users\15085\.claude\plugins\ralph-loop` |
| **jq 工具** | ✅ 已安装 (v1.7) | `C:\Users\15085\bin\jq.exe` |
| **Git** | ✅ 已安装 (v2.51.0) | 系统已安装 |
| **插件命令** | ✅ 已配置 | `/ralph-loop`, `/cancel-ralph` |

## 🚀 快速开始

### 1. 重启 VSCode 或 Claude Code

**重要**：必须重启才能使插件生效！

- **VSCode 用户**：完全关闭并重新打开 VSCode
- **命令行用户**：关闭并重新打开终端

### 2. 验证插件可用

重启后，在 Claude Code 中输入：

```
/help
```

应该能看到 Ralph Loop 相关的命令帮助。

## 📖 使用方法

### 基本语法

```bash
/ralph-loop "任务描述" [选项]
```

### 选项说明

| 选项 | 说明 | 示例 |
|------|------|------|
| `--max-iterations <n>` | 最大迭代次数 | `--max-iterations 20` |
| `--completion-promise '<文本>'` | 完成标志（必须用引号） | `--completion-promise 'DONE'` |
| `--help` | 显示帮助信息 | `--help` |

## 💡 使用示例

### 示例 1：构建简单 API（推荐新手）

```bash
/ralph-loop "创建一个简单的 Flask REST API：
1. 端点：GET /api/items（获取所有项目）
2. 端点：POST /api/items（创建新项目）
3. 端点：GET /api/items/<id>（获取单个项目）
4. 包含输入验证
5. 包含单元测试

完成标准：
- 所有测试通过
- 代码覆盖率 > 70%

完成后输出 <promise>API_COMPLETE</promise>" --completion-promise "API_COMPLETE" --max-iterations 30
```

### 示例 2：修复测试失败

```bash
/ralph-loop "修复项目中所有失败的测试：
1. 运行 pytest 并查看失败原因
2. 修复代码
3. 重新运行测试
4. 重复直到所有测试通过
5. 输出 <promise>TESTS_PASSING</promise>" --completion-promise "TESTS_PASSING" --max-iterations 15
```

### 示例 3：重构代码

```bash
/ralph-loop "重构数据处理模块：
1. 提高代码可读性
2. 添加错误处理
3. 优化性能
4. 保持测试通过

完成后输出 <promise>REFACTOR_DONE</promise>" --completion-promise "REFACTOR_DONE" --max-iterations 10
```

### 示例 4：创建完整的 Web 应用

```bash
/ralph-loop "创建一个待办事项 Web 应用：
前端：
- 使用 HTML/CSS/JavaScript
- 用户界面：添加、删除、标记完成
- 本地存储数据

后端（Flask）：
- REST API 端点
- 数据持久化（JSON 文件或 SQLite）
- 输入验证

测试：
- 前端功能测试
- 后端 API 测试
- 集成测试

完成标准：
- 所有功能正常工作
- 测试覆盖率 > 80%
- 包含 README 文档

完成后输出 <promise>APP_COMPLETE</promise>" --completion-promise "APP_COMPLETE" --max-iterations 50
```

## 🛑 如何停止循环

### 方法 1：达到最大迭代次数
```bash
# 自动停止
/ralph-loop "任务" --max-iterations 10
# 迭代 10 次后自动停止
```

### 方法 2：输出完成标志
```bash
/ralph-loop "任务" --completion-promise "DONE"

# Claude 在完成时输出：
<promise>DONE</promise>
# 循环立即停止
```

### 方法 3：手动取消
```bash
/cancel-ralph
```

## 📊 监控循环进度

```bash
# 查看当前迭代次数
grep '^iteration:' .claude/ralph-loop.local.md

# 查看完整状态
cat .claude/ralph-loop.local.md

# 查看开始时间和配置
head -10 .claude/ralph-loop.local.md
```

## 🎯 编写好的提示词

### ✅ 好的提示词特征

1. **明确的目标**
   ```
   ✅ 好：创建一个用户认证系统（注册、登录、JWT）
   ❌ 差：做一个认证功能
   ```

2. **具体的完成标准**
   ```
   ✅ 好：所有测试通过，代码覆盖率 > 80%
   ❌ 差：代码质量要好
   ```

3. **包含自检步骤**
   ```
   ✅ 好：运行测试 → 查看失败 → 修复 → 重新运行 → 重复
   ❌ 差：修复测试
   ```

4. **分阶段任务**
   ```
   ✅ 好：
   阶段1：数据库设计
   阶段2：API 实现
   阶段3：测试编写
   ❌ 差：做一个完整的电商系统
   ```

### 📝 提示词模板

#### TDD 开发模板
```bash
/ralph-loop "按照 TDD 流程开发 [功能名称]：
1. 编写失败的测试
2. 实现最小可行代码
3. 运行测试
4. 如果失败，调试并修复
5. 重复直到所有测试通过
6. 输出 <promise>TESTS_PASSING</promise>" --completion-promise "TESTS_PASSING" --max-iterations 20
```

#### 代码重构模板
```bash
/ralph-loop "重构 [模块名称]：
1. 提取重复代码为函数
2. 改善变量命名
3. 添加注释和文档
4. 优化算法复杂度
5. 保持所有测试通过

完成后输出 <promise>REFACTOR_COMPLETE</promise>" --completion-promise "REFACTOR_COMPLETE" --max-iterations 15
```

#### Bug 修复模板
```bash
/ralph-loop "修复 [Bug 描述]：
1. 运行测试并查看失败原因
2. 添加日志输出调试信息
3. 分析根本原因
4. 实施修复
5. 验证测试通过
6. 移除调试日志

完成后输出 <promise>BUG_FIXED</promise>" --completion-promise "BUG_FIXED" --max-iterations 10
```

## ⚠️ 注意事项

### 1. 总是设置安全网

```bash
# ✅ 推荐：设置最大迭代次数
/ralph-loop "任务" --max-iterations 20

# ✅ 推荐：设置完成标志
/ralph-loop "任务" --completion-promise "DONE" --max-iterations 20

# ❌ 危险：无限循环
/ralph-loop "任务"
```

### 2. 明确的完成标准

```bash
# ✅ 好：可验证的标准
/ralph-loop "创建 API，所有测试通过"

# ❌ 差：模糊的标准
/ralph-loop "创建一个好的 API"
```

### 3. 避免需要人工决策

```bash
# ❌ 不好：需要人类选择
/ralph-loop "设计数据库架构，让我审核"

# ✅ 好：自主决策
/ralph-loop "根据需求设计最优数据库架构，用测试验证"
```

### 4. 任务分解

```bash
# ✅ 好：小任务
/ralph-loop "实现用户注册功能"

# ❌ 差：大任务
/ralph-loop "实现完整的电商系统"
```

## 🔧 故障排查

### 问题 1：命令不可用

**症状**：输入 `/ralph-loop` 显示命令不存在

**解决**：
1. 确认已重启 VSCode 或 Claude Code
2. 检查插件目录：`ls ~/.claude/plugins/ralph-loop/`
3. 检查插件配置：`cat ~/.claude/plugins/ralph-loop/.claude-plugin/plugin.json`

### 问题 2：jq 未找到

**症状**：提示 `jq: command not found`

**解决**：
1. 检查 jq 安装：`C:\Users\15085\bin\jq.exe --version`
2. 检查 PATH：`echo $PATH | grep 15085`
3. 手动添加到当前会话：`export PATH="/c/Users/15085/bin:$PATH"`
4. 重启终端使 PATH 生效

### 问题 3：循环无法停止

**症状**：循环持续运行不停止

**解决**：
```bash
# 方法1：删除状态文件
rm .claude/ralph-loop.local.md

# 方法2：使用取消命令
/cancel-ralph
```

### 问题 4：完成标志不生效

**症状**：输出了 `<promise>DONE</promise>` 但循环不停止

**检查**：
1. 标志文本是否完全匹配（区分大小写）
2. 是否使用了正确的 XML 标签：`<promise>...</promise>`
3. 查看状态文件：`cat .claude/ralph-loop.local.md`

## 📚 进阶技巧

### 1. 使用 Git 追踪进度

Ralph Loop 会保留每次迭代的文件修改，你可以用 Git 查看进化历史：

```bash
# 查看每次迭代的修改
git log --oneline

# 查看特定文件的修改历史
git log --follow -- file.py

# 对比两次迭代
git diff commit1 commit2
```

### 2. 分阶段开发

```bash
# 阶段 1：基础功能
/ralph-loop "实现基础 CRUD 功能" --max-iterations 10

# 验证通过后，阶段 2：高级功能
/ralph-loop "添加搜索和过滤功能" --max-iterations 10

# 阶段 3：优化和测试
/ralph-loop "优化性能并完善测试" --max-iterations 10
```

### 3. 利用文件持久性

Ralph Loop 会保留每次迭代创建的文件，你可以：

- 在提示词中引用之前创建的文件
- 让 Claude 查看自己的代码并改进
- 利用之前迭代的学习成果

```bash
/ralph-loop "改进 test.py 中的测试用例：
1. 阅读现有的 test.py
2. 找出覆盖率不足的地方
3. 添加新的测试用例
4. 运行 pytest
5. 修复失败的测试
6. 重复直到覆盖率 > 90%
7. 输出 <promise>TESTS_IMPROVED</promise>" --completion-promise "TESTS_IMPROVED" --max-iterations 15
```

### 4. 监控资源使用

长时间运行时，监控系统资源：

```bash
# 在另一个终端监控
watch -n 5 'ps aux | grep python'
```

## 🎉 开始使用

### 建议的第一个任务

试试这个简单的任务来熟悉 Ralph Loop：

```bash
/ralph-loop "创建一个简单的计算器应用：
1. 支持加减乘除运算
2. 包含输入验证（除数不能为0）
3. 编写单元测试覆盖所有运算
4. 创建 README 文档说明使用方法

完成标准：
- 所有测试通过
- 包含至少 10 个测试用例
- README 文档完整

完成后输出 <promise>CALCULATOR_COMPLETE</promise>" --completion-promise "CALCULATOR_COMPLETE" --max-iterations 20
```

## 📖 更多资源

- [官方文档](https://github.com/anthropics/claude-plugins-official/tree/main/plugins/ralph-loop)
- [Ralph Wiggum 技术](https://ghuntley.com/ralph/)
- [Claude Code 文档](https://docs.anthropic.com/claude-code/overview)

---

**安装时间**：2026-01-08
**版本**：Ralph Loop (Official Anthropic Plugin)
**jq 版本**：1.7
**系统**：Windows (Git Bash)

祝您使用愉快！🚀
