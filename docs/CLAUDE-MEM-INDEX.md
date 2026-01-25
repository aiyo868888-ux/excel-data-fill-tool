# Claude-Mem 文档索引

**Claude-Mem** 是 Claude Code 的持久化记忆系统，能够跨会话保存上下文并提供智能搜索。

---

## 📚 完整文档目录

所有文档位于：[docs/claude-mem/](claude-mem/)

### 🚀 快速开始
- **[快速参考卡](claude-mem/00-quick-reference.md)** ⭐ 推荐首先阅读
  - 工作原理概览
  - 8 种控制方法
  - 快速命令参考
  - 常用配置示例

### 📖 详细文档

#### 1. [工作原理详解](claude-mem/01-workflow.md)
- 4 个生命周期 Hook
- 数据流转过程
- 渐进式披露策略
- Token 优化机制
- 搜索机制解析

#### 2. [内容控制指南](claude-mem/02-content-control.md)
- 8 种控制方法详解
- 隐私保护最佳实践
- 实际场景示例
- 删除和管理记录
- 验证配置效果

#### 3. [配置管理完整指南](claude-mem/03-configuration.md)
- 三层配置系统
- 完整参数说明
- 项目配置示例
- 最佳实践建议
- 故障排查方法

#### 4. [配置文件总结](claude-mem/04-config-summary.md)
- CLAUDE.md vs .claude-mem.json
- 快速创建配置
- 参数优先级
- 管理配置

---

## 🎯 核心概念

### 自动记忆流程
```
会话启动 → 用户消息 → 工具调用 → 会话结束
   ↓          ↓          ↓          ↓
加载历史    初始化    记录观察    生成摘要
```

### 三层配置系统
```
项目配置 (.claude-mem.json) - 最高优先级
    ↓ 覆盖
全局配置 (~/.claude-mem/settings.json)
    ↓ 覆盖
插件默认 (modes/*.json)
```

### 控制记录内容的方法
1. `<private>` 标签 - 保护敏感内容 ⭐
2. 跳过工具 - 过滤工具调用
3. 观察类型过滤 - 只记录指定类型
4. 概念标签过滤 - 只包含指定概念
5. 数量限制 - 控制记忆容量
6. 暂时禁用 - 停止记录
7. 删除记录 - 清除历史
8. 数据库操作 - 直接管理

---

## 💡 快速开始

### 1. 访问 Web 界面
```
http://127.0.0.1:37777/viewer.html
```

### 2. 查看系统状态
```bash
curl http://127.0.0.1:37777/api/health
```

### 3. 创建项目配置
```bash
# 在项目根目录创建 .claude-mem.json
cat > .claude-mem.json << 'EOF'
{
  "CLAUDE_MEM_CONTEXT_OBSERVATION_TYPES": "bugfix,feature,refactor,decision,change",
  "CLAUDE_MEM_SKIP_TOOLS": "Bash(git*),Read(.env)",
  "CLAUDE_MEM_CONTEXT_OBSERVATIONS": "20",
  "CLAUDE_MEM_CONTEXT_SESSION_COUNT": "10"
}
