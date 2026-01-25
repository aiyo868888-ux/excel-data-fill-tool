# Claude-Mem 配置文件完整指南

## 📋 配置文件层次结构

Claude-Mem 使用三层配置系统：

```
1. 全局配置（用户级别）
   ~/.claude-mem/settings.json

2. 插件配置（系统级别）
   ~/.claude/plugins/claude-mem/modes/*.json

3. 项目配置（项目级别）⭐ 推荐
   .claude-mem.json（放在项目根目录）
```

---

## 1️⃣ 全局配置文件

**位置：** `~/.claude-mem/settings.json`

**用途：** 用户默认配置，适用于所有项目

**当前配置：**
```json
{
  "CLAUDE_MEM_MODEL": "claude-sonnet-4-5",
  "CLAUDE_MEM_CONTEXT_OBSERVATIONS": "30",
  "CLAUDE_MEM_WORKER_PORT": "37777",
  "CLAUDE_MEM_WORKER_HOST": "127.0.0.1",
  "CLAUDE_MEM_LOG_LEVEL": "DEBUG",
  "CLAUDE_MEM_MODE": "code",
  "CLAUDE_MEM_CONTEXT_OBSERVATION_TYPES": "bugfix,feature,refactor,discovery,decision,change",
  "CLAUDE_MEM_CONTEXT_OBSERVATION_CONCEPTS": "how-it-works,why-it-exists,what-changed,problem-solution,gotcha,pattern,trade-off",
  "CLAUDE_MEM_CONTEXT_FULL_COUNT": "5",
  "CLAUDE_MEM_CONTEXT_SESSION_COUNT": "10",
  "CLAUDE_MEM_CONTEXT_SHOW_LAST_SUMMARY": "true",
  "CLAUDE_MEM_CONTEXT_SHOW_LAST_MESSAGE": "true"
}
```

---

## 2️⃣ 插件模式配置

**位置：** `~/.claude/plugins/claude-mem/modes/`

**可用模式：**
- `code.json` - 软件开发（默认）
- `code--chill.json` - 轻松模式
- `email-investigation.json` - 邮件调查
- 多语言模式（`code--zh.json`, `code--ja.json` 等）

**用途：** 预定义的观察类型和概念标签

---

## 3️⃣ 项目配置文件 ⭐ 推荐

**位置：** `{项目根目录}/.claude-mem.json`

**用途：** 项目特定的记忆配置，类似 CLAUDE.md 的作用

### 创建项目配置

```bash
# 在项目根目录创建
cp ~/.claude-mem/project-settings.json.template .claude-mem.json
```

### 示例配置

#### Web 项目配置
```json
{
  "CLAUDE_MEM_CONTEXT_OBSERVATION_TYPES": "bugfix,feature,refactor,discovery,decision,change,documentation,css,api",
  "CLAUDE_MEM_CONTEXT_OBSERVATION_CONCEPTS": "how-it-works,why-it-exists,what-changed,problem-solution,gotcha,pattern,trade-off,example,component",
  "CLAUDE_MEM_CONTEXT_FULL_COUNT": "15",
  "CLAUDE_MEM_CONTEXT_SESSION_COUNT": "20",
  "CLAUDE_MEM_MODE": "code"
}
```

#### 数据库项目配置
```json
{
  "CLAUDE_MEM_CONTEXT_OBSERVATION_TYPES": "bugfix,feature,refactor,discovery,decision,change,migration,schema",
  "CLAUDE_MEM_CONTEXT_OBSERVATION_CONCEPTS": "how-it-works,why-it-exists,what-changed,problem-solution,gotcha,pattern,trade-off,example,query,performance",
  "CLAUDE_MEM_CONTEXT_FULL_COUNT": "20",
  "CLAUDE_MEM_CONTEXT_SESSION_COUNT": "25"
}
```

#### 机器学习项目配置
```json
{
  "CLAUDE_MEM_CONTEXT_OBSERVATION_TYPES": "bugfix,feature,refactor,discovery,decision,change,experiment,model,data",
  "CLAUDE_MEM_CONTEXT_OBSERVATION_CONCEPTS": "how-it-works,why-it-exists,what-changed,problem-solution,gotcha,pattern,trade-off,example,hyperparameter,metric",
  "CLAUDE_MEM_CONTEXT_FULL_COUNT": "25",
  "CLAUDE_MEM_CONTEXT_SESSION_COUNT": "30"
}
```

---

## 🎯 完整配置参数说明

### 上下文控制

| 参数 | 默认值 | 说明 |
|------|--------|------|
| `CLAUDE_MEM_CONTEXT_OBSERVATIONS` | 50 | 注入的观察数量 |
| `CLAUDE_MEM_CONTEXT_FULL_COUNT` | 5 | 完整加载的观察数 |
| `CLAUDE_MEM_CONTEXT_SESSION_COUNT` | 10 | 注入的历史会话数 |
| `CLAUDE_MEM_CONTEXT_FULL_FIELD` | narrative | 完整字段名（narrative/content） |

### 观察类型（逗号分隔）

```
bugfix      - 错误修复
feature     - 新功能
refactor    - 重构
discovery   - 发现
decision    - 决策
change      - 变更
documentation - 文档
```

### 观察概念（逗号分隔）

```
how-it-works      - 工作原理
why-it-exists     - 存在原因
what-changed      - 变更内容
problem-solution  - 问题与方案
gotcha            - 注意事项
pattern           - 设计模式
trade-off         - 权衡考虑
example           - 示例
```

### 显示选项

| 参数 | 默认值 | 说明 |
|------|--------|------|
| `CLAUDE_MEM_CONTEXT_SHOW_READ_TOKENS` | true | 显示读取 Token |
| `CLAUDE_MEM_CONTEXT_SHOW_WORK_TOKENS` | true | 显示工作 Token |
| `CLAUDE_MEM_CONTEXT_SHOW_SAVINGS_AMOUNT` | true | 显示节省数量 |
| `CLAUDE_MEM_CONTEXT_SHOW_SAVINGS_PERCENT` | true | 显示节省百分比 |
| `CLAUDE_MEM_CONTEXT_SHOW_LAST_SUMMARY` | true | 显示最后摘要 |
| `CLAUDE_MEM_CONTEXT_SHOW_LAST_MESSAGE` | false | 显示最后消息 |

### 系统配置

| 参数 | 默认值 | 说明 |
|------|--------|------|
| `CLAUDE_MEM_WORKER_PORT` | 37777 | Web 服务端口 |
| `CLAUDE_MEM_WORKER_HOST` | 127.0.0.1 | Web 服务地址 |
| `CLAUDE_MEM_LOG_LEVEL` | INFO | 日志级别（DEBUG/INFO/WARN/ERROR） |
| `CLAUDE_MEM_MODE` | code | 运行模式 |
| `CLAUDE_MEM_MODEL` | claude-sonnet-4-5 | 使用的模型 |

### 跳过的工具

```
CLAUDE_MEM_SKIP_TOOLS: ListMcpResourcesTool,SlashCommand,Skill,TodoWrite,AskUserQuestion
```

这些工具不会被记录到记忆中。

---

## 📝 配置优先级

```
项目配置 > 全局配置 > 插件默认
```

1. **项目配置** - `.claude-mem.json`（最高优先级）
2. **全局配置** - `~/.claude-mem/settings.json`
3. **插件默认** - `modes/*.json`

---

## 🔄 配置重新加载

### 自动重载
- 修改 `settings.json` 后需要重启 Worker
- 项目配置 `.claude-mem.json` 会在下次会话自动加载

### 手动重启
```bash
# 重启 Worker 服务
bun ~/.claude/plugins/claude-mem/scripts/worker-wrapper.cjs
```

---

## 💡 最佳实践

### 1. 使用项目配置
✅ 推荐：在项目根目录创建 `.claude-mem.json`
❌ 不推荐：直接修改全局 `settings.json`

### 2. 根据项目类型调整
- **小型项目**：减少 `CONTEXT_OBSERVATIONS`（20-30）
- **大型项目**：增加 `CONTEXT_SESSION_COUNT`（15-20）
- **实验性项目**：添加自定义观察类型（experiment, test）

### 3. Token 优化
- 生产环境：关闭 `SHOW_SAVINGS` 显示
- 开发环境：启用所有显示选项以调试
- Token 紧张：减少 `CONTEXT_FULL_COUNT`

### 4. 隐私保护
```json
{
  "CLAUDE_MEM_SKIP_TOOLS": "ListMcpResourcesTool,SlashCommand,Skill,TodoWrite,AskUserQuestion,Bash(git*)"
}
```

跳过包含敏感信息的工具调用。

---

## 🔧 故障排查

### 配置不生效
```bash
# 1. 检查语法
cat ~/.claude-mem/settings.json | jq .

# 2. 检查权限
ls -la ~/.claude-mem/settings.json

# 3. 重启服务
bun ~/.claude/plugins/claude-mem/scripts/worker-wrapper.cjs
```

### 查看当前配置
```bash
curl http://127.0.0.1:37777/api/settings
```

### 配置文件位置
```bash
# 全局配置
echo ~/.claude-mem/settings.json

# 项目配置
echo {项目根目录}/.claude-mem.json

# 插件配置
echo ~/.claude/plugins/claude-mem/modes/code.json
```

---

## 📚 相关资源

- Web 界面：http://127.0.0.1:37777/viewer.html
- API 文档：http://127.0.0.1:37777/api/health
- GitHub：https://github.com/thedotmack/claude-mem
- 官方文档：https://docs.claude-mem.ai

