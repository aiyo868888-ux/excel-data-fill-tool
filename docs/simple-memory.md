# Simple Memory - 极简记忆系统

> **产品哲学**：专注单点，做到极致

---

## ✨ 功能特性

- ✅ **自动记录**：关键决策、bug修复、功能变更自动保存
- ✅ **智能检索**：自然语言搜索历史记录
- ✅ **项目隔离**：每个项目独立的记忆空间
- ✅ **极简设计**：单个 JSON 文件，无需数据库
- ✅ **双环境支持**：VS Code 扩展 + CLI

---

## 🚀 快速开始

### 方法 1：使用命令（推荐）

```bash
# 记录一条记忆
/mem 修复了用户登录的bug

# 搜索记忆
/mem-search 登录

# 查看最近记录
/mem-list
```

### 方法 2：直接调用 MCP 工具

在对话中直接说：
- "记录一下：我们决定使用 JWT 替换 Session"
- "搜索一下关于登录的历史记录"
- "查看最近 10 条记忆"

### 方法 3：命令行（脚本使用）

```bash
# 添加记录
node skills/simple-memory/memory.js add "实现了用户认证功能" feature

# 搜索记录
node skills/simple-memory/memory.js search 认证

# 查看最近 10 条
node skills/simple-memory/memory.js list

# 查看统计
node skills/simple-memory/memory.js stats
```

---

## 📂 数据存储

### 存储位置

```
{项目根目录}/
├── .memory/
│   └── memory.json          # 记忆数据
└── .memory-config.json      # 配置文件
```

### 数据格式

```json
[
  {
    "id": "mktecu56kh2m4et31wr",
    "timestamp": "2026-01-25T07:08:09.498Z",
    "content": "修复了用户登录的bug",
    "category": "bugfix",
    "metadata": {},
    "project": "my-project"
  }
]
```

---

## 🏷️ 分类系统

内置分类：
- `decision` - 技术决策
- `bugfix` - bug 修复
- `feature` - 新功能
- `refactor` - 重构
- `discovery` - 发现/学习
- `config` - 配置变更
- `workflow` - 工作流优化

---

## ⚙️ 配置

编辑 `.memory-config.json`：

```json
{
  "autoRecord": true,           // 是否自动记录
  "maxRecords": 1000,           // 最大记录数
  "categories": [               // 自定义分类
    "decision",
    "bugfix",
    "feature"
  ],
  "autoCapture": {              // 自动捕获模式
    "enabled": true,
    "patterns": [               // 触发关键词
      "修复了",
      "实现了",
      "决策"
    ]
  }
}
```

---

## 🎯 实际使用场景

### 场景 1：记录技术决策

```
你: /mem 决定使用 PostgreSQL 替代 MySQL，因为需要更好的 JSON 支持
AI: ✅ 已记录：决定使用 PostgreSQL 替代 MySQL [decision]
```

### 场景 2：搜索历史解决方案

```
你: /mem-search 登录
AI: 🔍 找到 2 条记录：

[bugfix] 2026-01-25T07:08:15.039Z
  修复了用户登录的bug，使用JWT替换Session

[feature] 2026-01-24T10:30:00.000Z
  实现了第三方登录（Google、GitHub）
```

### 场景 3：快速回顾项目历史

```
你: /mem-list 20
AI: 📋 最近 20 条记录：
  ...列出最近的重要决策和变更...
```

---

## 📊 API 参考

### MCP 工具

#### `mem_add`
记录一条记忆
- `content` (string): 要记录的内容
- `category` (string): 分类（可选）

#### `mem_search`
搜索历史记忆
- `query` (string): 搜索关键词

#### `mem_list`
查看最近的记忆
- `limit` (number): 返回数量（默认 10）

#### `mem_stats`
查看记忆统计信息

---

## 🔧 高级用法

### 批量导入历史记录

```bash
# 从文件导入
cat history.txt | while read line; do
  node skills/simple-memory/memory.js add "$line"
done
```

### 与 Git 集成

```bash
# 在 commit 时自动记录
git commit -m "feat: add auth" && \
node skills/simple-memory/memory.js add "实现了用户认证功能" feature
```

### 自动备份

```bash
# 定期备份到其他目录
cp .memory/memory.json backups/memory-$(date +%Y%m%d).json
```

---

## 💡 最佳实践

1. **及时记录**：做完决策立即记录，不要等
2. **精确分类**：使用正确的分类，方便后续搜索
3. **简洁明了**：记录核心信息，不需要写完整文档
4. **定期回顾**：使用 `/mem-list` 查看历史，避免重复问题

---

## 🚀 性能

- **存储**：单个 JSON 文件，通常 < 100KB
- **搜索**：线性搜索，1000 条记录约 10ms
- **内存**：几乎不占用内存（只在操作时加载）

---

## 🎉 与 Claude-Mem 对比

| 特性 | Simple Memory | Claude-Mem |
|------|--------------|------------|
| VS Code 扩展 | ✅ | ❌ (仅 CLI) |
| 配置复杂度 | ⭐ 极简 | ⭐⭐⭐⭐ 复杂 |
| 依赖 | 单个 JS 文件 | Bun + 多个服务 |
| 安装时间 | 1 分钟 | 10+ 分钟 |
| Hooks 支持 | ❌ | ✅ |
| 适用场景 | 手动记录 | 自动记录 |

**选择建议**：
- 需要 VS Code 扩展 → Simple Memory
- 需要全自动记录 → CLI + Claude-Mem
- 追求简单直接 → Simple Memory

---

## 📝 更新日志

### v1.0.0 (2026-01-25)
- ✅ 基础功能实现
- ✅ MCP 服务器集成
- ✅ 命令行工具
- ✅ 配置系统

---

## 🙏 致谢

灵感来自 Claude-Mem，但遵循"极简"原则重新实现。
