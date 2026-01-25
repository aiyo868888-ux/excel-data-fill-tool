# Claude-Mem 工作原理完整解析

## 🔄 工作流程概览

Claude-Mem 通过 **4 个生命周期 Hook** 自动捕获和记忆上下文：

```
会话启动 → 用户消息 → 工具调用 → 会话结束
    ↓         ↓         ↓         ↓
加载历史   初始化   记录观察   生成摘要
```

---

## 1️⃣ SessionStart Hook（会话启动）

### 触发时机
- Claude Code 会话开始时

### 执行步骤
```
1. smart-install.js
   └─> 检查依赖完整性
   
2. worker-service.cjs start
   └─> 启动 Worker 服务（端口 37777）
   
3. worker-service.cjs hook claude-code context
   └─> 从数据库加载历史上下文
   └─> 应用渐进式披露策略
   └─> 注入到 CLAUDE.md 的 <claude-mem-context> 标签
   
4. worker-service.cjs hook claude-code user-message
   └─> 准备接收用户消息
```

### 渐进式披露策略（三层加载）

**第一层：摘要层** (~500 tokens)
```
- 最近会话摘要
- 关键决策记录
- 重大变更概述
```

**第二层：观察摘要** (~1000 tokens)
```
- 相关观察的简短描述
- 按概念标签分组
- 按观察类型过滤
```

**第三层：完整内容** (按需加载)
```
- 完整的观察叙述
- 工具调用结果
- 代码片段和文件路径
```

### 控制参数
```json
{
  "CLAUDE_MEM_CONTEXT_OBSERVATIONS": "30",      // 注入观察数量
  "CLAUDE_MEM_CONTEXT_FULL_COUNT": "5",         // 完整加载数量
  "CLAUDE_MEM_CONTEXT_SESSION_COUNT": "10"      // 历史会话数量
}
```

---

## 2️⃣ UserPromptSubmit Hook（用户消息）

### 触发时机
- 用户提交新消息时

### 执行步骤
```
1. 确保服务运行
   └─> worker-service.cjs start
   
2. 初始化会话
   └─> worker-service.cjs hook claude-code session-init
   └─> 创建新会话记录
   └─> 生成会话 ID
   └─> 记录时间戳
```

### 记录内容
```
- 会话 ID
- 开始时间
- 项目路径
- 初始用户消息
```

---

## 3️⃣ PostToolUse Hook（工具调用后）⭐ 核心

### 触发时机
- 每次工具调用后（Bash、Read、Write 等）

### 执行步骤
```
1. 确保服务运行
   └─> worker-service.cjs start
   
2. 记录观察
   └─> worker-service.cjs hook claude-code observation
   └─> 提取工具调用信息
   └─> 生成语义标签
   └─> 存储到数据库
```

### 记录的数据结构

```json
{
  "id": "观察 ID",
  "sessionId": "会话 ID",
  "timestamp": "时间戳",
  "tool": "工具名称（Bash/Read/Write等）",
  "observationType": "观察类型（bugfix/feature/refactor等）",
  "concepts": ["概念标签（how-it-works/pattern等）"],
  "narrative": "自然语言描述",
  "files": ["相关文件列表"],
  "summary": "简短摘要",
  "readTokens": 使用的 Token 数量
}
```

### 观察类型自动分类

| 工具行为 | 自动类型 | 说明 |
|---------|---------|------|
| 修复错误 | `bugfix` | 解决问题 |
| 添加功能 | `feature` | 新增能力 |
| 重构代码 | `refactor` | 结构优化 |
| 发现信息 | `discovery` | 学习系统 |
| 做出决策 | `decision` | 架构选择 |
| 其他变更 | `change` | 通用修改 |

### 概念标签自动提取

```
- how-it-works      → 包含"工作原理"、"如何"等关键词
- why-it-exists     → 包含"原因"、"目的"等关键词
- what-changed      → 包含"修改"、"变更"等关键词
- problem-solution  → 包含"问题"、"解决"等关键词
- gotcha            → 包含"注意"、"警告"等关键词
- pattern           → 包含"模式"、"设计"等关键词
- trade-off         → 包含"权衡"、"利弊"等关键词
- example           → 包含"示例"、"演示"等关键词
```

---

## 4️⃣ Stop Hook（会话结束）

### 触发时机
- Claude Code 会话结束时

### 执行步骤
```
1. 确保服务运行
   └─> worker-service.cjs start
   
2. 生成摘要
   └─> worker-service.cjs hook claude-code summarize
   └─> 分析所有观察
   └─> 提取关键信息
   └─> 生成会话摘要
   └─> 压缩上下文
```

### 摘要生成过程

```
原始观察（可能数千 tokens）
    ↓
AI 压缩提取（Claude Sonnet）
    ↓
关键信息摘要（~500 tokens）
    ↓
存储到数据库
```

### 摘要包含内容
```
- 完成的主要任务
- 关键决策和理由
- 重要发现和学习
- 遇到的问题和解决方案
- 下一步行动建议
```

---

## 🗄️ 数据存储架构

### 数据库结构（SQLite）

```
claude-mem.db
├── sessions          # 会话记录
│   ├── id
│   ├── startTime
│   ├── endTime
│   ├── projectPath
│   └── summary
│
├── observations      # 观察记录
│   ├── id
│   ├── sessionId
│   ├── timestamp
│   ├── tool
│   ├── observationType
│   ├── concepts[]
│   ├── narrative
│   ├── files[]
│   └── summary
│
├── prompts          # 用户消息
│   ├── id
│   ├── sessionId
│   ├── content
│   └── timestamp
│
└── fts_observations  # 全文搜索索引（FTS5）
    └── 虚拟表，用于快速搜索
```

### 数据流转

```
工具调用 → Hook 捕获 → AI 分析 → 数据库存储
    ↓
会话结束 → AI 摘要 → 压缩存储 → 长期记忆
```

---

## 🔍 搜索机制

### 1. 全文搜索（FTS5）
```sql
SELECT * FROM observations 
WHERE narrative MATCH '数据库优化'
```

### 2. 概念搜索
```sql
SELECT * FROM observations 
WHERE 'pattern' IN concepts
```

### 3. 类型搜索
```sql
SELECT * FROM observations 
WHERE observationType = 'bugfix'
```

### 4. 文件搜索
```sql
SELECT * FROM observations 
WHERE 'src/main.py' IN files
```

### 5. 混合搜索（组合）
```sql
SELECT * FROM observations 
WHERE narrative MATCH 'API'
  AND observationType = 'feature'
  AND 'how-it-works' IN concepts
```

---

## 🎯 记忆注入时机

### 何时注入历史上下文？

1. **新会话启动时**
   - 自动加载最近 10 个会话的摘要
   - 注入最近 30 个相关观察

2. **检测到相关话题时**
   - 用户提问历史问题
   - 涉及之前处理的文件
   - 关键词匹配

3. **达到 token 阈值时**
   - 优先加载摘要
   - 按需加载完整内容

### 注入内容示例

```markdown
<claude-mem-context>
# Recent Activity

### Jan 25, 2026

| ID | Time | T | Title | Read |
|----|------|---|-------|------|
| #12345 | 3:44 PM | 🔵 | 配置 Claude-Mem 插件 | ~276 tokens |
| #12346 | 4:15 PM | 🟢 | 修复 Worker 启动问题 | ~342 tokens |

### Key Context

**最近完成：**
- ✅ 安装 Claude-Mem v9.0.6
- ✅ 配置项目级记忆文件 .claude-mem.json
- ✅ 修复 Windows 下 Bun 启动问题

**关键决策：**
- 使用 worker-wrapper.cjs 启动服务
- 启用 DEBUG 日志级别
- 项目级配置覆盖全局设置

**待处理：**
- 测试记录第一条观察
- 配置记忆参数
- 查看其他功能
</claude-mem-context>
```

---

## 📊 Token 优化策略

### 渐进式披露原理

```
会话开始：只注入摘要（节省 tokens）
    ↓
用户继续：添加相关观察（按需加载）
    ↓
深入话题：加载完整叙述（完整上下文）
```

### Token 节省统计

```
原始上下文：50,000 tokens
压缩摘要：5,000 tokens (90% 节省)
观察精选：10,000 tokens (80% 节省)
──────────────────────────────
实际注入：15,000 tokens (70% 总节省)
```

---

## 🚦 工作流程图

```
┌─────────────────────────────────────────────┐
│          Claude Code 会话开始                │
└──────────────────┬──────────────────────────┘
                   │
                   ↓
┌─────────────────────────────────────────────┐
│  SessionStart Hook                          │
│  1. 启动 Worker 服务                         │
│  2. 加载历史摘要（最近 10 个会话）            │
│  3. 注入相关观察（最近 30 个）                │
│  4. 插入到 CLAUDE.md                         │
└──────────────────┬──────────────────────────┘
                   │
                   ↓
┌─────────────────────────────────────────────┐
│  用户输入消息                                │
└──────────────────┬──────────────────────────┘
                   │
                   ↓
┌─────────────────────────────────────────────┐
│  UserPromptSubmit Hook                      │
│  1. 创建新会话记录                           │
│  2. 记录初始消息                             │
└──────────────────┬──────────────────────────┘
                   │
                   ↓
┌─────────────────────────────────────────────┐
│  Claude 使用工具（Bash/Read/Write）          │
└──────────────────┬──────────────────────────┘
                   │
                   ↓
┌─────────────────────────────────────────────┐
│  PostToolUse Hook ⭐ 核心记录点               │
│  1. 捕获工具调用结果                         │
│  2. AI 分析提取信息                          │
│  3. 自动分类（类型+概念）                    │
│  4. 生成自然语言叙述                         │
│  5. 存储到 SQLite 数据库                     │
└──────────────────┬──────────────────────────┘
                   │
                   ↓
              （循环多次）
                   │
                   ↓
┌─────────────────────────────────────────────┐
│  Claude Code 会话结束                        │
└──────────────────┬──────────────────────────┘
                   │
                   ↓
┌─────────────────────────────────────────────┐
│  Stop Hook                                  │
│  1. 分析所有观察记录                         │
│  2. AI 生成会话摘要                          │
│  3. 压缩关键信息                             │
│  4. 保存长期记忆                             │
└─────────────────────────────────────────────┘
```

---

## 💡 关键特性

### 自动化
- ✅ 无需手动操作
- ✅ 后台静默运行
- ✅ 智能分类和标签

### 持久化
- ✅ 跨会话保留
- ✅ SQLite 存储
- ✅ FTS5 全文索引

### 智能化
- ✅ AI 自动摘要
- ✅ 语义标签提取
- ✅ 渐进式披露

### 可控性
- ✅ 多层配置
- ✅ 隐私保护
- ✅ Token 优化

