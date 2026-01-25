# Claude-Mem 记录内容控制完全指南

## 🎯 是否可以控制记录内容？

**答案：是的！你有多种方式控制 Claude-Mem 记录什么内容。**

---

## 1️⃣ 使用 `<private>` 标签 ⭐ 最简单

### 方法
在任何内容周围包裹 `<private>` 标签：

```markdown
<private>
这是敏感信息，不会被记录到 Claude-Mem 数据库
包括 API 密钥、密码、个人信息等
</private>
```

### 适用场景
- API 密钥和密码
- 个人隐私信息
- 临时测试数据
- 不重要的闲聊

### 示例

**不会被记录：**
```markdown
<private>
API_KEY=sk-xxxxxx
密码：mySecretPassword123
</private>
```

**会被记录：**
```markdown
配置文件需要更新 API 密钥字段
使用环境变量存储敏感信息
```

---

## 2️⃣ 配置跳过的工具

### 全局配置
编辑 `~/.claude-mem/settings.json`：

```json
{
  "CLAUDE_MEM_SKIP_TOOLS": "ListMcpResourcesTool,SlashCommand,Skill,TodoWrite,AskUserQuestion,Bash(git*),Read(.env)"
}
```

### 项目配置
编辑 `.claude-mem.json`：

```json
{
  "CLAUDE_MEM_SKIP_TOOLS": "Bash(git*),Read(secret*),Write(config*.json)"
}
```

### 工具匹配模式

| 模式 | 匹配 | 说明 |
|------|------|------|
| `Bash` | 所有 Bash 调用 | 跳过所有命令 |
| `Bash(git*)` | git 开头的命令 | 跳过 git 操作 |
| `Read(.env)` | 读取 .env 文件 | 跳过配置文件 |
| `Write(secret*)` | 写入 secret* 文件 | 跳过敏感文件 |

### 示例

**跳过 git 操作：**
```json
{
  "CLAUDE_MEM_SKIP_TOOLS": "Bash(git*),Bash(ssh*),Bash(scp*)"
}
```

**跳过敏感文件：**
```json
{
  "CLAUDE_MEM_SKIP_TOOLS": "Read(.env),Read(*key*),Write(*secret*)"
}
```

**跳过所有 Bash（不推荐）：**
```json
{
  "CLAUDE_MEM_SKIP_TOOLS": "Bash"
}
```

---

## 3️⃣ 调整观察类型过滤

### 项目配置
在 `.claude-mem.json` 中定义允许的观察类型：

```json
{
  "CLAUDE_MEM_CONTEXT_OBSERVATION_TYPES": "bugfix,feature,refactor,decision,discovery"
}
```

### 效果
- ✅ **只记录指定类型**的观察
- ❌ 其他类型（如 `change`）不会被记录

### 示例

**只记录重要变更：**
```json
{
  "CLAUDE_MEM_CONTEXT_OBSERVATION_TYPES": "feature,bugfix,decision"
}
```

**记录所有类型：**
```json
{
  "CLAUDE_MEM_CONTEXT_OBSERVATION_TYPES": "bugfix,feature,refactor,discovery,decision,change,documentation"
}
```

---

## 4️⃣ 控制概念标签

### 项目配置
在 `.claude-mem.json` 中定义允许的概念：

```json
{
  "CLAUDE_MEM_CONTEXT_OBSERVATION_CONCEPTS": "how-it-works,why-it-exists,problem-solution,pattern"
}
```

### 效果
- ✅ 只包含指定概念的观察
- ❌ 其他概念不会被添加

### 示例

**只关注核心问题：**
```json
{
  "CLAUDE_MEM_CONTEXT_OBSERVATION_CONCEPTS": "problem-solution,pattern,trade-off"
}
```

**关注所有概念：**
```json
{
  "CLAUDE_MEM_CONTEXT_OBSERVATION_CONCEPTS": "how-it-works,why-it-exists,what-changed,problem-solution,gotcha,pattern,trade-off,example"
}
```

---

## 5️⃣ 限制记录数量

### 观察数量限制
```json
{
  "CLAUDE_MEM_CONTEXT_OBSERVATIONS": "20",  // 只保留最近 20 个观察
  "CLAUDE_MEM_CONTEXT_FULL_COUNT": "5"      // 只完整加载 5 个
}
```

### 会话数量限制
```json
{
  "CLAUDE_MEM_CONTEXT_SESSION_COUNT": "5"   // 只保留最近 5 个会话
}
```

### 效果
- 减少存储占用
- 提高搜索速度
- 降低 token 使用

---

## 6️⃣ 暂时禁用记录

### 方法 1：停止 Worker 服务
```bash
# 停止服务（不会记录）
taskkill /F /IM bun.exe

# 重启后恢复
bun ~/.claude/plugins/claude-mem/scripts/worker-wrapper.cjs
```

### 方法 2：临时禁用 Hooks
重命名 hooks 配置：
```bash
mv ~/.claude/plugins/claude-mem/hooks/hooks.json ~/.claude/plugins/claude-mem/hooks/hooks.json.disabled
```

### 方法 3：使用 `<private>` 包裹整个会话
```markdown
<private>
整个会话内容都不会被记录
包括所有工具调用和结果
</private>
```

---

## 7️⃣ 删除已记录的内容

### 通过 Web 界面
```
1. 访问 http://127.0.0.1:37777/viewer.html
2. 找到要删除的观察
3. 点击删除按钮
```

### 通过 API
```bash
# 删除特定观察（假设 ID 为 12345）
curl -X DELETE http://127.0.0.1:37777/api/observation/12345
```

### 直接操作数据库
```bash
# 打开数据库
sqlite3 ~/.claude-mem/claude-mem.db

# 删除特定会话
DELETE FROM observations WHERE sessionId = 'session-id';

# 删除所有观察
DELETE FROM observations;

# 删除所有会话
DELETE FROM sessions;
```

---

## 8️⃣ 隐私最佳实践

### ✅ 推荐做法

1. **敏感信息使用 `<private>` 标签**
```markdown
<private>
API 密钥、密码、个人信息
</private>
```

2. **跳过包含敏感数据的工具**
```json
{
  "CLAUDE_MEM_SKIP_TOOLS": "Read(.env),Read(*key*),Bash(git credential*)"
}
```

3. **定期清理数据库**
```bash
# 清理 30 天前的数据
sqlite3 ~/.claude-mem/claude-mem.db "DELETE FROM observations WHERE timestamp < datetime('now', '-30 days')"
```

4. **项目级配置使用 `.gitignore`**
```bash
echo ".claude-mem.json" >> .gitignore
```

### ❌ 避免做法

1. ❌ 不要在记忆中记录 API 密钥
2. ❌ 不要记录生产环境密码
3. ❌ 不要记录个人信息（身份证、银行卡等）
4. ❌ 不要记录客户敏感数据

---

## 9️⃣ 实际控制示例

### 场景 1：开发个人项目
```json
{
  "CLAUDE_MEM_SKIP_TOOLS": "Bash(git*),Read(.env)",
  "CLAUDE_MEM_CONTEXT_OBSERVATION_TYPES": "bugfix,feature,refactor,decision",
  "CLAUDE_MEM_CONTEXT_OBSERVATIONS": "30",
  "CLAUDE_MEM_CONTEXT_SESSION_COUNT": "10"
}
```

### 场景 2：处理敏感数据
```json
{
  "CLAUDE_MEM_SKIP_TOOLS": "Read(secret*),Read(*key*),Write(*private*),Bash(encrypt*),Bash(decrypt*)",
  "CLAUDE_MEM_CONTEXT_OBSERVATION_TYPES": "decision,documentation",
  "CLAUDE_MEM_CONTEXT_OBSERVATIONS": "10"
}
```

### 场景 3：只记录重要决策
```json
{
  "CLAUDE_MEM_CONTEXT_OBSERVATION_TYPES": "decision,feature,bugfix",
  "CLAUDE_MEM_CONTEXT_OBSERVATION_CONCEPTS": "problem-solution,trade-off,pattern",
  "CLAUDE_MEM_CONTEXT_FULL_COUNT": "3",
  "CLAUDE_MEM_CONTEXT_SESSION_COUNT": "5"
}
```

### 场景 4：完全禁用记录
```json
{
  "CLAUDE_MEM_SKIP_TOOLS": "*"
}
```

---

## 🔟 验证配置是否生效

### 检查当前配置
```bash
curl http://127.0.0.1:37777/api/settings
```

### 查看实际记录
```bash
curl http://127.0.0.1:37777/api/observations
```

### 测试 `<private>` 标签
```markdown
<private>
这条消息不应该出现在记忆中
</private>
```

然后检查：
```bash
curl http://127.0.0.1:37777/api/observations | grep "不应该出现"
```

---

## 📊 控制级别总结

| 控制级别 | 粒度 | 难度 | 推荐度 |
|---------|------|------|--------|
| `<private>` 标签 | 单条内容 | ⭐ 简单 | ⭐⭐⭐⭐⭐ 强烈推荐 |
| 跳过工具 | 工具类型 | ⭐⭐ 中等 | ⭐⭐⭐⭐ 推荐 |
| 观察类型过滤 | 观察类别 | ⭐⭐ 中等 | ⭐⭐⭐⭐ 推荐 |
| 概念标签过滤 | 语义标签 | ⭐⭐ 中等 | ⭐⭐⭐ 一般 |
| 数量限制 | 总体容量 | ⭐ 简单 | ⭐⭐⭐⭐ 推荐 |
| 暂时禁用 | 全局 | ⭐⭐ 中等 | ⭐⭐ 一般 |
| 删除记录 | 单条/全部 | ⭐⭐⭐ 复杂 | ⭐⭐ 谨慎使用 |

---

## 💡 最佳实践建议

### 开发环境
```json
{
  "CLAUDE_MEM_SKIP_TOOLS": "Bash(git*),Read(.env)",
  "CLAUDE_MEM_CONTEXT_OBSERVATION_TYPES": "bugfix,feature,refactor,discovery,decision,change,documentation",
  "CLAUDE_MEM_CONTEXT_OBSERVATIONS": "30",
  "CLAUDE_MEM_LOG_LEVEL": "INFO"
}
```

### 生产环境
```json
{
  "CLAUDE_MEM_SKIP_TOOLS": "Read(.env),Read(*key*),Bash(git*),Bash(ssh*)",
  "CLAUDE_MEM_CONTEXT_OBSERVATION_TYPES": "bugfix,feature,decision,documentation",
  "CLAUDE_MEM_CONTEXT_OBSERVATIONS": "20",
  "CLAUDE_MEM_LOG_LEVEL": "WARN"
}
```

### 高隐私需求
```json
{
  "CLAUDE_MEM_SKIP_TOOLS": "Read(secret*),Write(*private*),Bash(*)",
  "CLAUDE_MEM_CONTEXT_OBSERVATION_TYPES": "decision,documentation",
  "CLAUDE_MEM_CONTEXT_OBSERVATIONS": "10",
  "CLAUDE_MEM_LOG_LEVEL": "ERROR"
}
```

