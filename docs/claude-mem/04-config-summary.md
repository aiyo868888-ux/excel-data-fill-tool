# ✅ Claude-Mem 配置文件完整答案

## 🎯 简短回答

**是的，Claude-Mem 有类似 CLAUDE.md 的配置文件系统！**

---

## 📁 三层配置文件

### 1️⃣ 全局配置（用户级别）
**文件：** `~/.claude-mem/settings.json`  
**作用：** 所有项目的默认配置  
**优先级：** 低

### 2️⃣ 插件配置（系统级别）
**目录：** `~/.claude/plugins/claude-mem/modes/*.json`  
**作用：** 预定义的模式配置  
**优先级：** 中

### 3️⃣ 项目配置（项目级别）⭐ **最推荐**
**文件：** `{项目根目录}/.claude-mem.json`  
**作用：** 项目特定的记忆配置，类似 CLAUDE.md  
**优先级：** 高

---

## 🆚 对比：CLAUDE.md vs .claude-mem.json

| 特性 | CLAUDE.md | .claude-mem.json |
|------|-----------|------------------|
| **用途** | Claude Code 项目指令 | 记忆系统配置 |
| **格式** | Markdown | JSON |
| **内容** | 工作流程、规则、上下文 | 观察类型、概念、参数 |
| **作用域** | 项目级 | 项目级 |
| **加载时机** | 会话启动 | 会话启动 |

---

## 🚀 快速开始

### 创建项目配置

```bash
# 在项目根目录创建
cat > .claude-mem.json << 'EOF'
{
  "CLAUDE_MEM_CONTEXT_OBSERVATION_TYPES": "bugfix,feature,refactor,discovery,decision,change,documentation",
  "CLAUDE_MEM_CONTEXT_OBSERVATION_CONCEPTS": "how-it-works,why-it-exists,what-changed,problem-solution,gotcha,pattern,trade-off,example",
  "CLAUDE_MEM_CONTEXT_FULL_COUNT": "10",
  "CLAUDE_MEM_CONTEXT_SESSION_COUNT": "15"
}
