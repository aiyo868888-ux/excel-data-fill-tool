# Claude-Mem 5 分钟快速开始

## ⚡ 30 秒上手

### 1. 检查服务状态
```bash
curl http://127.0.0.1:37777/api/health
```

### 2. 访问 Web 界面
在浏览器打开：http://127.0.0.1:37777/viewer.html

### 3. 查看记录
```bash
curl http://127.0.0.1:37777/api/observations
```

---

## 🎯 3 分钟了解核心概念

### 工作原理
```
工具调用 → 自动记录 → AI 分析 → 存储到数据库
```

### 控制记录
最简单的方法：使用 `<private>` 标签
```markdown
<private>
敏感内容不会被记录
</private>
```

### 配置文件
在项目根目录创建 `.claude-mem.json`：
```json
{
  "CLAUDE_MEM_SKIP_TOOLS": "Bash(git*),Read(.env)",
  "CLAUDE_MEM_CONTEXT_OBSERVATIONS": "20"
}
```

---

## 📚 5 分钟深入学习

1. 阅读 [00-quick-reference.md](00-quick-reference.md) - 快速参考
2. 阅读 [02-content-control.md](02-content-control.md) - 控制记录内容
3. 访问 Web 界面查看实际记录

---

## 💡 常用命令

### 查看状态
```bash
curl http://127.0.0.1:37777/api/health
curl http://127.0.0.1:37777/api/stats
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
```

---

## 🔒 隐私保护

### 保护敏感信息
```markdown
<private>
API_KEY=sk-xxxxxx
</private>
```

### 跳过敏感工具
```json
{
  "CLAUDE_MEM_SKIP_TOOLS": "Read(.env),Read(*key*)"
}
```

---

## 📖 更多文档

- [完整文档](README.md)
- [工作原理](01-workflow.md)
- [内容控制](02-content-control.md)
- [配置管理](03-configuration.md)

---

**版本：** v9.0.6
