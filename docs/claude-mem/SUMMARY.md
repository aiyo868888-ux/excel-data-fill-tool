# Claude-Mem 文档总结

本文档目录包含 Claude-Mem v9.0.6 的完整使用文档。

---

## 📁 文档列表

| 文件 | 大小 | 说明 |
|------|------|------|
| [README.md](README.md) | 4.4 KB | 主文档，包含完整导航 |
| [00-quick-reference.md](00-quick-reference.md) | 3.5 KB | 快速参考卡 ⭐ |
| [01-workflow.md](01-workflow.md) | 12 KB | 工作原理详解 |
| [02-content-control.md](02-content-control.md) | 8.0 KB | 内容控制指南 |
| [03-configuration.md](03-configuration.md) | 6.9 KB | 配置管理指南 |
| [04-config-summary.md](04-config-summary.md) | 1.6 KB | 配置总结 |

---

## 🎯 快速导航

### 我想...

**了解基本概念**
→ 阅读 [00-quick-reference.md](00-quick-reference.md)

**理解工作原理**
→ 阅读 [01-workflow.md](01-workflow.md)

**控制记录内容**
→ 阅读 [02-content-control.md](02-content-control.md)

**配置记忆系统**
→ 阅读 [03-configuration.md](03-configuration.md)

**快速查找配置**
→ 阅读 [04-config-summary.md](04-config-summary.md)

---

## 💡 核心要点

### 工作原理
- 4 个生命周期 Hook 自动运行
- PostToolUse Hook 是核心记录点
- 每次工具调用后自动记录
- 使用 AI 分析和分类信息

### 控制方法
1. `<private>` 标签 - 最简单有效 ⭐
2. 跳过工具 - 过滤特定工具
3. 类型过滤 - 只记录重要内容
4. 数量限制 - 控制存储容量

### 配置文件
- 全局：`~/.claude-mem/settings.json`
- 项目：`.claude-mem.json` ⭐ 推荐
- 优先级：项目 > 全局 > 插件

---

## 🔗 相关链接

- **Web 界面：** http://127.0.0.1:37777/viewer.html
- **官方文档：** https://docs.claude-mem.ai
- **GitHub：** https://github.com/thedotmack/claude-mem

---

**版本：** v9.0.6  
**更新日期：** 2026-01-25
