# myclaude 工作流分析与复用方案

## 核心价值

myclaude 的工作流设计非常优秀，核心价值在于：

### 1. **结构化的开发流程**

```
需求澄清 → 技术分析 → 开发计划 → 并行执行 → 测试验证 → 完成总结
```

### 2. **智能任务分解**

- 将功能拆分为 2-5 个**独立任务**
- 每个任务明确：ID、类型、范围、依赖、测试命令
- 支持**并行执行**

### 3. **强制测试覆盖**

- 要求 ≥90% 代码覆盖率
- 每个任务必须有明确的测试命令
- 测试驱动开发（TDD）

### 4. **多后端路由**

- `default` → Codex（深度代码）
- `ui` → Gemini（UI/UX）
- `quick-fix` → Claude（快速修复）

---

## 可以复用的部分（不依赖多后端）

### ✅ **1. 开发计划生成器**

**文件**：`dev-plan-generator.md`

**核心思想**：
- 接收需求和分析结果
- 生成结构化的 `dev-plan.md`
- 包含任务分解、测试要求、验收标准

**复用方式**：
- 保留这个 agent 作为参考
- 在需要时手动应用这个模板

**示例输出结构**：
```markdown
# Feature Name - Development Plan

## Overview
[一句话描述核心功能]

## Task Breakdown

### Task 1: [Task Name]
- **ID**: task-1
- **type**: default|ui|quick-fix
- **Description**: [需要做什么]
- **File Scope**: [涉及的文件/目录]
- **Dependencies**: None 或 depends on task-x
- **Test Command**: [测试命令]
- **Test Focus**: [测试场景]

## Acceptance Criteria
- [ ] 功能点 1
- [ ] 功能点 2
- [ ] 所有单元测试通过
- [ ] 代码覆盖率 ≥90%

## Technical Notes
- [关键技术决策]
- [需要注意的约束]
```

---

### ✅ **2. 需求澄清流程**

**来源**：`/dev` command Step 1

**核心问题**（针对功能边界）：
1. **输入输出**：这个功能的输入是什么？期望的输出是什么？
2. **功能边界**：包含哪些功能？不包含哪些？
3. **约束条件**：性能要求？兼容性要求？
4. **测试要求**：需要什么样的单元测试覆盖率？
5. **优先级**：哪些是必须有的？哪些是可选的？

**复用价值**：
- ✅ 可以直接使用
- ✅ 不依赖多后端
- ✅ 提高需求理解质量

---

### ✅ **3. 任务分解原则**

**来源**：`dev-plan-generator.md`

**原则**：
1. **基于自然功能边界**（不是人为分割）
2. **任务独立性**（尽可能不依赖）
3. **明确范围**（具体文件/目录）
4. **可测试性**（每个任务都有测试命令）

**示例**：
```
❌ 不好的分解：
- Task 1: 写代码（太模糊）
- Task 2: 写测试（依赖 Task 1）

✅ 好的分解：
- Task 1: 实现用户认证逻辑（src/auth/*）
- Task 2: 实现登录页面 UI（src/login/*）
- Task 3: 编写集成测试（tests/auth/*）
```

---

### ✅ **4. 测试驱动要求**

**来源**：整个 `/dev` workflow

**核心要求**：
- ≥90% 代码覆盖率
- 每个任务必须有测试命令
- 测试场景必须具体（不能是"测试一切"）

**示例**：
```bash
# 明确的测试命令
pytest tests/auth --cov=src/auth --cov-report=term

# 不明确的测试命令（避免）
pytest tests/
```

---

## 需要调整的部分（依赖多后端）

### ❌ **1. 多后端路由**

**原文**：
- `default` → Codex
- `ui` → Gemini
- `quick-fix` → Claude

**问题**：你没有这些后端 CLI

**替代方案**：
- 所有任务都用 Claude Code（我）
- 保留 `type` 字段用于标记任务类型（作为参考）
- 去掉自动路由逻辑

---

### ❌ **2. codeagent-wrapper 调用**

**原文**：所有代码修改通过 `codeagent-wrapper`

**问题**：你没有安装 `codeagent-wrapper`

**替代方案**：
- 直接使用 Edit/Write 工具
- 保留任务分解的思想
- 手动执行（而不是通过 wrapper）

---

### ❌ **3. 并行执行**

**原文**：使用 `codeagent-wrapper --parallel`

**问题**：没有并行执行机制

**替代方案**：
- 串行执行任务
- 仍然按照任务分解的逻辑
- 按依赖顺序执行

---

## 推荐的复用方案

### **方案 A：轻量级复用（推荐）**

**保留**：
1. ✅ 需求澄清流程
2. ✅ 开发计划模板（`dev-plan.md`）
3. ✅ 任务分解原则
4. ✅ 测试覆盖要求

**不使用**：
1. ❌ codeagent-wrapper
2. ❌ 多后端路由
3. ❌ 自动并行执行

**效果**：
- 获得结构化的开发流程
- 不需要额外依赖
- 手动执行但思想一致

---

### **方案 B：创建简化版工作流**

基于 myclaude 的思想，创建适合你的简化版：

```markdown
# 简化版开发工作流

## 步骤 1：需求澄清
[使用 myclaude 的提问模板]

## 步骤 2：技术分析
[手动使用 Read/Glob/Grep 分析代码]

## 步骤 3：生成开发计划
[使用 dev-plan.md 模板]

## 步骤 4：串行执行
[按依赖顺序执行任务]

## 步骤 5：测试验证
[验证 90% 覆盖率]

## 步骤 6：完成总结
[总结修改和测试结果]
```

---

## 具体实施步骤

### 1. 提取核心文件

```powershell
# 复制开发计划生成器
Copy-Item "temp\myclaude\dev-workflow\agents\dev-plan-generator.md" ".claude\agents\"

# 复制 /dev 命令作为参考
Copy-Item "temp\myclaude\dev-workflow\commands\dev.md" ".claude\commands\dev-reference.md"
```

### 2. 创建简化版命令

创建 `.claude/commands/plan.md`：

```markdown
---
description: Generate structured development plan based on requirements
---

You are a Development Plan Generator. Follow this workflow:

## Step 1: Clarify Requirements
[使用 myclaude 的问题模板]

## Step 2: Generate dev-plan.md
[使用 dev-plan-generator.md 的模板]

## Step 3: Review with User
确认计划是否符合预期
```

### 3. 更新 CLAUDE.md

添加开发流程规范：

```markdown
<development_workflow>
1. Requirement Clarification
2. Technical Analysis
3. Development Plan (dev-plan.md)
4. Implementation
5. Testing (≥90% coverage)
6. Summary
</development_workflow>
```

---

## 总结

### **myclaude 的核心价值**

1. ✅ **结构化思维**：需求 → 分析 → 计划 → 执行 → 验证
2. ✅ **任务分解**：2-5 个独立任务
3. ✅ **测试驱动**：≥90% 覆盖率
4. ✅ **文档化**：dev-plan.md 规范

### **你可以复用的**

1. ✅ 工作流思想
2. ✅ 开发计划模板
3. ✅ 需求澄清问题
4. ✅ 任务分解原则

### **你需要调整的**

1. ⚠️ 去掉多后端依赖
2. ⚠️ 去掉 codeagent-wrapper
3. ⚠️ 串行代替并行

---

## 要我帮你实施吗？

我可以：
1. **提取核心文件**到你的 `.claude/`
2. **创建简化版命令**
3. **更新 CLAUDE.md** 添加工作流规范

选择哪个？
