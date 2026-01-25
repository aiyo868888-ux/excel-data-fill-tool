# Claude-Mem 快速参考卡

## 🔄 工作原理（4 个 Hook）

```
SessionStart → UserPromptSubmit → PostToolUse → Stop
    ↓              ↓                  ↓           ↓
 加载历史       初始化会话        记录观察     生成摘要
```

### 关键点
- ✅ **PostToolUse Hook** 是核心记录点
- ✅ 每次工具调用后自动记录
- ✅ 使用 AI 分析和分类
- ✅ 存储到 SQLite 数据库

---

## 🎯 控制记录内容（8 种方法）

### 1. `<private>` 标签 ⭐ 最推荐
```markdown
<private>
敏感内容不会被记录
</private>
```

### 2. 跳过工具
```json
{
  "CLAUDE_MEM_SKIP_TOOLS": "Bash(git*),Read(.env)"
}
```

### 3. 过滤观察类型
```json
{
  "CLAUDE_MEM_CONTEXT_OBSERVATION_TYPES": "bugfix,feature,decision"
}
```

### 4. 过滤概念标签
```json
{
  "CLAUDE_MEM_CONTEXT_OBSERVATION_CONCEPTS": "problem-solution,pattern"
}
```

### 5. 限制数量
```json
{
  "CLAUDE_MEM_CONTEXT_OBSERVATIONS": "20",
  "CLAUDE_MEM_CONTEXT_SESSION_COUNT": "5"
}
```

### 6. 暂时禁用
```bash
taskkill /F /IM bun.exe
```

### 7. 删除记录
```bash
curl -X DELETE http://127.0.0.1:37777/api/observation/{id}
```

### 8. 直接操作数据库
```bash
sqlite3 ~/.claude-mem/claude-mem.db "DELETE FROM observations"
```

---

## 📁 配置文件位置

```
全局配置：~/.claude-mem/settings.json
项目配置：{项目根目录}/.claude-mem.json ⭐ 推荐
插件配置：~/.claude/plugins/claude-mem/modes/*.json
```

---

## 🌐 快速命令

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

### Web 界面
```
http://127.0.0.1:37777/viewer.html
```

### 搜索历史
```
"搜索我们上次讨论的 API 设计"
"查找所有 bug 修复记录"
```

---

## 🔒 隐私保护最佳实践

### ✅ 做
- 使用 `<private>` 标签保护敏感信息
- 跳过 `.env`、`*key*` 等文件
- 定期清理旧数据
- 项目配置加入 `.gitignore`

### ❌ 不做
- 记录 API 密钥
- 记录密码
- 记录个人信息
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

## 💡 快速示例

### 场景 1：保护 API 密钥
```markdown
<private>
API_KEY=sk-xxxxxx
</private>
```

### 场景 2：只记录重要内容
```json
{
  "CLAUDE_MEM_CONTEXT_OBSERVATION_TYPES": "feature,bugfix,decision",
  "CLAUDE_MEM_SKIP_TOOLS": "Bash(git*),Read(.env)"
}
```

### 场景 3：高隐私需求
```json
{
  "CLAUDE_MEM_SKIP_TOOLS": "Read(secret*),Write(*private*)",
  "CLAUDE_MEM_CONTEXT_OBSERVATIONS": "10"
}
```

---

## 📚 完整文档

- 工作原理：`/tmp/claude-mem-how-it-works.md`
- 控制指南：`/tmp/claude-mem-control-guide.md`
- 配置指南：`/tmp/claude-mem-configuration-guide.md`

