# Ralph Loop 快速参考卡片

## 🚀 基本语法

```bash
/ralph-loop "任务描述" [选项]
```

## 📋 选项

| 选项 | 说明 | 示例 |
|------|------|------|
| `--max-iterations <n>` | 最大迭代次数 | `--max-iterations 20` |
| `--completion-promise '<文本>'` | 完成标志 | `--completion-promise 'DONE'` |
| `--help` | 显示帮助 | `--help` |

## ⚡ 快速开始

### 1. 最简单的示例
```bash
/ralph-loop "创建 hello.py 打印 Hello World" --max-iterations 5
```

### 2. 带完成标志
```bash
/ralph-loop "创建计算器，完成后输出 <promise>DONE</promise>" --completion-promise "DONE" --max-iterations 20
```

### 3. TDD 开发
```bash
/ralph-loop "按TDD流程开发计算器：
1. 编写测试
2. 实现功能
3. 运行测试
4. 修复失败
5. 输出 <promise>TESTS_PASSING</promise>" --completion-promise "TESTS_PASSING" --max-iterations 15
```

## 🛑 停止循环

```bash
/cancel-ralph
```

## 📊 监控进度

```bash
# 查看当前迭代
grep '^iteration:' .claude/ralph-loop.local.md

# 查看完整状态
cat .claude/ralph-loop.local.md
```

## ✅ 好的提示词特征

1. **明确的目标** ✅
   ```
   创建用户认证系统（注册、登录、JWT）
   ```

2. **具体的完成标准** ✅
   ```
   所有测试通过，代码覆盖率 > 80%
   ```

3. **包含自检步骤** ✅
   ```
   运行测试 → 查看失败 → 修复 → 重新运行
   ```

4. **分阶段任务** ✅
   ```
   阶段1：数据库设计
   阶段2：API实现
   阶段3：测试编写
   ```

## ❌ 避免的错误

1. **不要设置模糊目标** ❌
   ```
   做一个好的 API
   ```

2. **不要忘记设置限制** ❌
   ```
   /ralph-loop "优化代码"  # 可能永远运行
   ```

3. **不要需要人工决策** ❌
   ```
   设计架构，让我审核
   ```

## 🎯 实用模板

### Bug 修复
```bash
/ralph-loop "修复测试失败：
1. 运行 pytest
2. 分析失败原因
3. 修复代码
4. 重新运行
5. 输出 <promise>TESTS_PASSING</promise>" --completion-promise "TESTS_PASSING" --max-iterations 10
```

### 代码重构
```bash
/ralph-loop "重构模块：
1. 改善代码结构
2. 添加文档
3. 优化性能
4. 保持测试通过
5. 输出 <promise>REFACTOR_COMPLETE</promise>" --completion-promise "REFACTOR_COMPLETE" --max-iterations 15
```

### 功能开发
```bash
/ralph-loop "开发功能：
1. 设计数据模型
2. 实现API端点
3. 编写测试
4. 验证功能
5. 输出 <promise>FEATURE_COMPLETE</promise>" --completion-promise "FEATURE_COMPLETE" --max-iterations 30
```

## ⚠️ 重要提醒

1. **总是设置 `--max-iterations`** 作为安全网
2. **完成标志必须精确匹配**（区分大小写）
3. **使用明确的 XML 标签**：`<promise>DONE</promise>`
4. **任务要具体可衡量**
5. **包含验证步骤**

## 📖 更多信息

- 完整指南：[RALPH_LOOP_使用指南.md](RALPH_LOOP_使用指南.md)
- 详细演示：[Ralph_Loop_使用演示.md](Ralph_Loop_使用演示.md)
- 官方文档：https://github.com/anthropics/claude-plugins-official/tree/main/plugins/ralph-loop

---

**安装位置**：`C:\Users\15085\.claude\plugins\ralph-loop`
**jq 工具**：`C:\Users\15085\bin\jq.exe` (v1.7)
**状态文件**：`.claude/ralph-loop.local.md`
