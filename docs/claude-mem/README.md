# Claude-Mem 完整使用文档

## 📚 文档导航

本文档目录包含 Claude-Mem 持久化记忆系统的完整使用指南。

### 🚀 快速开始
- **[00-quick-reference.md](00-quick-reference.md)** - 快速参考卡（推荐首先阅读）

### 📖 详细文档

#### 1. [工作原理](01-workflow.md)
Claude-Mem 如何通过 4 个生命周期 Hook 自动捕获和记忆上下文。

#### 2. [内容控制](02-content-control.md)
如何控制记录内容，包括 8 种控制方法和隐私保护最佳实践。

#### 3. [配置管理](03-configuration.md)
完整的配置参数说明、三层配置系统和最佳实践。

#### 4. [配置总结](04-config-summary.md)
配置文件系统的快速总结，类似 CLAUDE.md 的作用。

---

## 🎯 快速链接

### 核心概念
- **4 个 Hook：** SessionStart → UserPromptSubmit → PostToolUse → Stop
- **核心记录点：** PostToolUse Hook（每次工具调用后自动记录）
- **数据存储：** SQLite 数据库 (~/.claude-mem/claude-mem.db)

### 控制方法
1. `<private>` 标签 - 保护敏感内容 ⭐
2. 跳过工具 - 过滤特定工具调用
3. 观察类型过滤 - 只记录指定类型
4. 概念标签过滤 - 只包含指定概念
5. 数量限制 - 控制记忆容量
6. 暂时禁用 - 停止记录
7. 删除记录 - 清除历史
8. 数据库操作 - 直接管理数据

### 配置文件
- **全局配置：** `~/.claude-mem/settings.json`
- **项目配置：** `{项目根目录}/.claude-mem.json` ⭐ 推荐
- **插件配置：** `~/.claude/plugins/claude-mem/modes/*.json`

### Web 界面
- **查看器：** http://127.0.0.1:37777/viewer.html
- **API 健康检查：** http://127.0.0.1:37777/api/health
- **统计信息：** http://127.0.0.1:37777/api/stats

---

## 🔍 快速命令

### 查看状态
```bash
curl http://127.0.0.1:37777/api/health
curl http://127.0.0.1:37777/api/stats
curl http://127.0.0.1:37777/api/settings
```

### 查看记录
```bash
curl http://127.0.0.1:37777/api/observations
curl http://127.0.0.1:37777/api/summaries
```

### 搜索历史
直接在对话中提问：
```
"搜索我们上次讨论的 API 设计"
"查找所有 bug 修复记录"
"显示本周的功能开发"
```

---

## 💡 常见场景

### 保护 API 密钥
```markdown
<private>
API_KEY=sk-xxxxxx
密码：mySecretPassword123
</private>
```

### 项目配置（推荐在项目根目录创建 `.claude-mem.json`）
```json
{
  "CLAUDE_MEM_CONTEXT_OBSERVATION_TYPES": "bugfix,feature,refactor,decision,change",
  "CLAUDE_MEM_SKIP_TOOLS": "Bash(git*),Read(.env)",
  "CLAUDE_MEM_CONTEXT_OBSERVATIONS": "20",
  "CLAUDE_MEM_CONTEXT_SESSION_COUNT": "10"
}
```

### 高隐私需求
```json
{
  "CLAUDE_MEM_SKIP_TOOLS": "Read(secret*),Write(*private*),Bash(*)",
  "CLAUDE_MEM_CONTEXT_OBSERVATION_TYPES": "decision,documentation",
  "CLAUDE_MEM_CONTEXT_OBSERVATIONS": "10"
}
```

---

## 🔒 隐私保护

### ✅ 推荐做法
- 使用 `<private>` 标签保护敏感信息
- 跳过 `.env`、`*key*` 等敏感文件
- 定期清理旧数据
- 项目配置加入 `.gitignore`

### ❌ 避免做法
- 记录 API 密钥
- 记录密码
- 记录个人信息（身份证、银行卡等）
- 记录客户敏感数据

---

## 📊 观察类型

| 类型 | 说明 | Emoji |
|------|------|-------|
| `bugfix` | 错误修复 | 🐛 |
| `feature` | 新功能 | ✨ |
| `refactor` | 重构 | 🔧 |
| `discovery` | 发现 | 🔍 |
| `decision` | 决策 | 🎯 |
| `change` | 变更 | 📝 |
| `documentation` | 文档 | 📚 |

---

## 🏷️ 概念标签

| 标签 | 说明 |
|------|------|
| `how-it-works` | 工作原理 |
| `why-it-exists` | 存在原因 |
| `what-changed` | 变更内容 |
| `problem-solution` | 问题与方案 |
| `gotcha` | 注意事项 |
| `pattern` | 设计模式 |
| `trade-off` | 权衡考虑 |
| `example` | 示例 |

---

## 📞 获取帮助

- **Web 界面：** http://127.0.0.1:37777/viewer.html
- **GitHub：** https://github.com/thedotmack/claude-mem
- **官方文档：** https://docs.claude-mem.ai
- **日志位置：** `~/.claude-mem/logs/claude-mem-YYYY-MM-DD.log`

---

## 🎓 学习路径

1. **初学者：** 阅读 [00-quick-reference.md](00-quick-reference.md)
2. **进阶用户：** 阅读 [01-workflow.md](01-workflow.md) 和 [02-content-control.md](02-content-control.md)
3. **高级用户：** 阅读 [03-configuration.md](03-configuration.md) 并根据项目需求定制配置

---

**版本：** Claude-Mem v9.0.6  
**最后更新：** 2026-01-25  
**维护者：** Your Name
