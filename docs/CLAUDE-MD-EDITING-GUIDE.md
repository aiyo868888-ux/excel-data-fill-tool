# CLAUDE.md 编辑指南

## 📋 文档目的

本指南专门说明如何正确编辑 **CLAUDE.md** 文件，确保 AI (Claude) 能够有效理解和利用项目文档。

---

## 🔑 重要概念：settings.json vs CLAUDE.md

在编辑项目文档前，**必须先理解**这两个文件的区别：

### 这两个文件有什么区别？

**简单来说**：
- **settings.json** → 控制 Claude Code **程序本身**（工具、服务器、权限）
- **CLAUDE.md** → 控制 Claude 在项目中的**工作方式**（规范、约定、风格）

### 📊 对比表

| 特性 | settings.json | CLAUDE.md |
|------|---------------|-----------|
| **位置** | `C:\Users\15085\.claude\`（全局） | 每个项目根目录 |
| **作用范围** | 所有项目/会话 | 当前项目 |
| **控制对象** | Claude Code **程序** | Claude 的**工作方式** |
| **内容类型** | JSON 配置 | Markdown 文档 |
| **示例内容** | MCP 服务器、权限、环境变量 | 项目规范、约定、说明 |
| **修改频率** | 很少（设置一次） | 经常（项目需求变化） |
| **生效方式** | 需要重启 Claude Code | 当前项目立即生效 |

### 📁 文件位置示例

**settings.json（全局配置）**：
```
C:\Users\15085\.claude\settings.json
```

**CLAUDE.md（项目配置）**：
```
d:\claude code -11\CLAUDE.md          # 当前项目
d:\projects\my-app\CLAUDE.md         # 其他项目
```

### 💡 使用场景对照表

| 任务 | 应该修改哪个文件？ | 原因 |
|------|-------------------|------|
| 添加新的 MCP 服务器（如浏览器工具） | ✅ settings.json | 这是"程序本身的功能设置" |
| 禁用某个工具的确认提示（如 AUTO_CONFIRM） | ✅ settings.json | 这是"工具的行为配置" |
| 配置环境变量 | ✅ settings.json | 这是"运行环境设置" |
| 要求代码必须包含注释 | ✅ CLAUDE.md | 这是"工作规范" |
| 指定文件存放目录（docs/、tests/） | ✅ CLAUDE.md | 这是"项目组织规则" |
| 定义"小白友好模式"解释要求 | ✅ CLAUDE.md | 这是"对话风格规范" |
| 配置 API 端点或数据库连接 | ✅ settings.json | 这是"基础设施配置" |
| 定义代码审查标准 | ✅ CLAUDE.md | 这是"工作流程规范" |

### 🎯 实际例子

#### 例子 1：浏览器工具自动确认

**问题**：测试时浏览器工具总提示 "Do you want to proceed with mcp_playwright_browser_click?"

**解决**：修改 `settings.json`
```json
{
  "mcpServers": {
    "chrome-devtools": {
      "env": {
        "AUTO_CONFIRM": "true"  // 添加这个配置
      }
    }
  }
}
```

**为什么改 settings.json？**
- ✅ 因为这是**工具本身的行为设置**
- ❌ 不是告诉 Claude 在项目中**如何工作**

**生效方式**：需要重启 Claude Code

---

#### 例子 2：要求代码必须加注释

**需求**：希望所有代码都包含详细注释

**解决**：在 `CLAUDE.md` 中添加
```markdown
## 代码规范
- 所有函数必须包含文档注释
- 复杂逻辑必须添加行内注释
```

**为什么改 CLAUDE.md？**
- ✅ 因为这是**项目的工作规范**
- ❌ 不是程序的配置

**生效方式**：当前项目立即生效

---

### 🚗 类比理解

**settings.json** 就像：
- 手机的"设置"菜单（关闭震动、调整亮度、配置 WiFi）
- 汽车的"配置"（座椅位置、后视镜角度、导航设置）

**CLAUDE.md** 就像：
- 工作的"操作手册"（如何回复客户、文件怎么命名、代码怎么写）
- 公司的"员工手册"（工作流程、规范标准、沟通方式）

### ⚙️ 修改前检查清单

**修改 settings.json 前**：
- [ ] 这是配置工具/服务器吗？
- [ ] 这是全局性的设置吗？
- [ ] 需要重启 Claude Code 才能生效吗？
- [ ] 这个设置会影响所有项目吗？

**修改 CLAUDE.md 前**：
- [ ] 这是项目规范吗？
- [ ] 这是工作风格要求吗？
- [ ] 当前项目立即生效吗？
- [ ] 这个设置只影响当前项目吗？

### 📝 快速判断流程图

```
需要修改配置？
    │
    ├─ 配置工具/服务器/权限？
    │   └─ ✅ → settings.json（全局，需重启）
    │
    ├─ 定义工作规范/风格？
    │   └─ ✅ → CLAUDE.md（项目级，立即生效）
    │
    ├─ 设置环境变量？
    │   └─ ✅ → settings.json（全局，需重启）
    │
    └─ 组织文件/目录结构？
        └─ ✅ → CLAUDE.md（项目级，立即生效）
```

### 💡 记住这个原则

```
settings.json  → 控制 Claude Code 程序本身（工具、服务器、权限）
CLAUDE.md      → 控制 Claude 在项目中的工作方式（规范、约定、风格）
```

**简单记忆**：
- 配置**工具行为** → settings.json（就像调整手机设置）
- 配置**工作规范** → CLAUDE.md（就像制定工作手册）

---

## 📖 settings.json 配置详解

### 常见配置内容

`settings.json` 是 Claude Code 的全局配置文件，通常包含以下配置内容：

#### 1️⃣ **MCP 服务器配置**（最常见）

**作用**：配置外部工具和服务，让 Claude Code 能使用更多功能

**示例配置**：
```json
{
  "mcpServers": {
    "chrome-devtools": {
      "command": "npx",
      "args": ["-y", "chrome-devtools-mcp@latest"],
      "disabled": false,
      "env": {
        "AUTO_CONFIRM": "true"
      }
    },
    "filesystem": {
      "command": "npx",
      "args": ["-y", "@modelcontextprotocol/server-filesystem"],
      "disabled": false
    }
  }
}
```

**常见 MCP 服务器**：
- `chrome-devtools` - 浏览器自动化
- `filesystem` - 文件系统访问
- `database` - 数据库连接
- `git` - Git 操作
- `slack` - Slack 集成

---

#### 2️⃣ **环境变量配置**

**作用**：设置全局环境变量，影响所有工具和服务器

**示例配置**：
```json
{
  "mcpServers": {
    "my-server": {
      "command": "node",
      "args": ["server.js"],
      "env": {
        "AUTO_CONFIRM": "true",
        "API_KEY": "your-api-key",
        "LOG_LEVEL": "debug",
        "TIMEOUT": "30000",
        "NODE_ENV": "development"
      }
    }
  }
}
```

**常用环境变量**：
- `AUTO_CONFIRM` - 自动确认工具操作
- `API_KEY` - API 密钥
- `LOG_LEVEL` - 日志级别（debug/info/warn/error）
- `TIMEOUT` - 超时时间
- `NODE_ENV` / `PYTHON_ENV` - 运行环境

---

#### 3️⃣ **日志和调试配置**

**作用**：控制日志输出和调试信息

**示例配置**：
```json
{
  "logging": {
    "level": "debug",
    "file": "/path/to/logs/claude-code.log",
    "maxSize": "10MB",
    "maxFiles": 5
  },
  "debug": {
    "showTimestamps": true,
    "showToolCalls": true,
    "verboseErrors": true
  }
}
```

---

#### 4️⃣ **UI 和编辑器集成配置**

**作用**：配置与编辑器的集成方式

**示例配置**：
```json
{
  "ui": {
    "theme": "dark",
    "fontSize": 14,
    "showLineNumbers": true,
    "autoSave": true,
    "tabSize": 2
  },
  "editor": {
    "formatOnSave": true,
    "enableAutoCompletion": true
  }
}
```

---

#### 5️⃣ **项目和工作区配置**

**作用**：配置默认项目位置和工作区设置

**示例配置**：
```json
{
  "projects": {
    "defaultLocation": "d:\\projects",
    "autoCreateFolders": true,
    "defaultFolders": ["docs", "tests", "temp"]
  },
  "workspace": {
    "restoreLastSession": true,
    "autoLoadProjects": []
  }
}
```

---

#### 6️⃣ **安全配置**

**作用**：配置安全相关的设置

**示例配置**：
```json
{
  "security": {
    "allowExternalAccess": false,
    "sandboxMode": true,
    "allowedDomains": ["example.com", "api.example.com"],
    "maxFileSize": "100MB"
  }
}
```

---

### 📊 完整示例

这是一个综合了多种配置的 `settings.json` 示例：

```json
{
  "mcpServers": {
    "chrome-devtools": {
      "command": "npx",
      "args": ["-y", "chrome-devtools-mcp@latest"],
      "disabled": false,
      "env": {
        "AUTO_CONFIRM": "true",
        "LOG_LEVEL": "info"
      }
    },
    "filesystem": {
      "command": "npx",
      "args": ["-y", "@modelcontextprotocol/server-filesystem", "d:\\"],
      "disabled": false
    }
  },
  "logging": {
    "level": "info",
    "file": "C:\\Users\\15085\\.claude\\logs\\claude-code.log"
  },
  "ui": {
    "theme": "dark",
    "fontSize": 14
  },
  "projects": {
    "defaultLocation": "d:\\projects"
  },
  "security": {
    "sandboxMode": true,
    "maxFileSize": "100MB"
  }
}
```

---

### 🎯 配置优先级

```
1. settings.json（全局配置）
        ↓
2. 项目级 .claude/settings.json（项目配置）
        ↓
3. 环境变量（运行时配置）
        ↓
4. 命令行参数（临时配置）
```

**优先级从低到高**：后面的配置会覆盖前面的配置。

---

### 💡 配置建议

#### ✅ 推荐配置到 settings.json

- ✅ MCP 服务器配置
- ✅ 全局环境变量（如 AUTO_CONFIRM）
- ✅ 日志级别
- ✅ UI 主题和字体
- ✅ 默认项目位置

#### ❌ 不推荐配置到 settings.json

- ❌ 项目特定的规范（应该放 CLAUDE.md）
- ❌ API 密钥（应该用环境变量或密钥管理工具）
- ❌ 临时性配置（应该用命令行参数）
- ❌ 敏感信息（应该放 .env 文件）

---

### 🔧 常用配置速查表

| 配置项 | 作用 | 示例值 |
|-------|------|--------|
| `mcpServers` | MCP 服务器列表 | `{ "browser": {...} }` |
| `AUTO_CONFIRM` | 自动确认工具操作 | `"true"` / `"false"` |
| `LOG_LEVEL` | 日志级别 | `"debug"` / `"info"` / `"warn"` |
| `timeout` | 超时时间（毫秒） | `30000` |
| `theme` | UI 主题 | `"dark"` / `"light"` |
| `defaultLocation` | 默认项目位置 | `"d:\\projects"` |

---

### 📝 总结

**settings.json 的核心作用**：
- 🛠️ 配置外部工具和服务（MCP 服务器）
- 🔐 设置全局权限和安全选项
- 🎨 配置 UI 和编辑器集成
- 📊 控制日志和调试输出
- 📁 设置默认项目位置

**记住**：
- ✅ settings.json 是**全局配置**，影响所有项目
- ✅ 修改后需要**重启 Claude Code** 才能生效
- ✅ 谨慎配置，避免影响所有项目

---

## 🎯 核心原则

**CLAUDE.md 的作用**：
- 项目的主要配置文件
- AI 理解项目的入口
- 定义工作方式和规范

**目标**：
- ✅ AI 能理解项目要求
- ✅ AI 能找到需要的文档
- ✅ 节省上下文空间
- ✅ 提高工作效率

---

## ✅ 正确的使用方式

### 1. `@` 引用本地文件（最佳）

**语法**：
```markdown
详见：@docs/api-reference.md
```

**工作原理**：
- AI 看到 `@` 符号
- 自动识别这是文件引用
- 需要时使用 Read 工具读取

**优点**：
- ✅ 节省上下文（只存路径）
- ✅ AI 能按需加载
- ✅ 内容自动更新

**示例**：
```markdown
# 项目文档

## API 文档
完整的 API 参考：@docs/api-reference.md

## 配置指南
配置说明：@docs/configuration.md

## 故障排查
常见问题：@docs/troubleshooting.md
```

---

### 2. 写出核心内容（最重要）

**原则**：经常需要的信息直接写在 CLAUDE.md 中

**示例**：
```markdown
# 项目规范

## 核心原则

### 小白友好模式
每次对话时必须：
1. ✅ 解释术语
2. ✅ 说明原因
3. ✅ 演示操作
4. ✅ 提供类比

### 错误示例
- "配置环境变量" ❌
- "初始化 Git 仓库" ❌

### 正确示例
- "配置环境变量（环境变量就像是程序的'记忆'，让程序记住配置）" ✅
- "初始化 Git 仓库（初始化就像建立'时光机'，可以随时回退）" ✅
```

**为什么这样写**：
- ✅ AI 每次对话都能看到
- ✅ 不需要额外读取文件
- ✅ 确保始终遵守这些规范

---

### 3. 摘要 + `@` 引用（平衡）

**结构**：
```markdown
# 主文档

## 核心要点
[写出最重要的 3-5 点]

## 详细文档
完整文档：@docs/detailed-guide.md
```

**示例**：
```markdown
# 项目文档

## 文件组织约定

### 目录结构
```
d:\claude code -11\
├── docs/           # 文档
├── tests/          # 测试
└── uploads/        # 上传文件
```

### 基本规则
- 文档放在 `docs/`
- 测试放在 `tests/`
- 截图放在 `test_screenshots/`

## 详细说明
完整的文件组织规范：@docs/file-organization.md
```

**优点**：
- ✅ 常用信息直接可用
- ✅ 详细文档按需读取
- ✅ 平衡上下文和可用性

---

### 4. 外部链接 + 访问说明

**语法**：
```markdown
完整规范：https://example.com/spec

**获取方式**：
- 直接访问网址
- 或在对话中告诉我："请访问这个网址"
```

**示例**：
```markdown
# Agent Skills 规范

## 官方文档
完整规范：https://agentskills.io/specification

**获取详细规范**：
- 访问上面的网址
- 或在对话中要求我："请访问 Agent Skills 规范网站"
- 我会使用 WebFetch 工具获取并总结
```

**关键**：
- ✅ 说明了如何访问
- ✅ 告诉用户如何与 AI 交互
- ✅ 提供了基本概念

---

## ❌ 错误的使用方式

### 1. 纯链接（完全无效）

**错误示例**：
```markdown
# 项目文档

完整的 API 文档：https://example.com/api-docs
```

**问题**：
- ❌ AI 无法访问链接
- ❌ 文档形同虚设
- ❌ 浪费文件位置

**正确做法**：
```markdown
# 项目文档

## 核心 API
### GET /api/users
获取用户列表

### POST /api/users
创建用户

## 完整 API 文档
在线文档：https://example.com/api-docs
或在对话中要求我访问该网址
```

---

### 2. 过长的内容（浪费上下文）

**错误示例**：
```markdown
# 项目文档

[这里复制了 5000 行的完整规范...]
```

**问题**：
- ❌ 每次对话都加载全部内容
- ❌ 浪费大量上下文
- ❌ 难以维护

**正确做法**：
```markdown
# 项目文档

## 核心要点
[最关键的 10-20 行]

## 详细规范
完整规范：@docs/full-specification.md
```

---

### 3. 不完整的 `@` 引用

**错误示例**：
```markdown
详见 @docs/../some/path/../file.md
```

**问题**：
- ❌ 路径混乱
- ❌ 难以维护
- ❌ 容易出错

**正确做法**：
```markdown
详见：@docs/api-reference.md
```

**要求**：
- ✅ 使用相对路径
- ✅ 从项目根目录开始
- ✅ 路径清晰明确

---

### 4. 缺少核心内容

**错误示例**：
```markdown
# 项目文档

详细文档：@docs/everything.md
```

**问题**：
- ❌ 没有核心信息
- ❌ 必须读取外部文件
- ❌ 不方便快速查看

**正确做法**：
```markdown
# 项目文档

## 核心原则
1. 原则一
2. 原则二
3. 原则三

## 详细文档
完整说明：@docs/everything.md
```

---

## 📊 对比表

| 方式 | 示例 | 上下文 | AI 能用 | 推荐度 |
|------|------|--------|---------|--------|
| **纯链接** | `https://example.com` | 极少 | ❌ | ⭐ |
| **完整复制** | [5000 行内容] | 很多 | ✅ | ⭐⭐ |
| **`@` 引用** | `@docs/api.md` | 少 | ✅ | ⭐⭐⭐⭐⭐ |
| **核心+引用** | [摘要]+`@docs/api.md` | 中 | ✅ | ⭐⭐⭐⭐⭐ |
| **链接+说明** | [摘要]+链接+访问方式 | 中 | ✅ | ⭐⭐⭐⭐ |

---

## 🔧 实战示例

### 示例 1：小项目（单个 CLAUDE.md）

**场景**：项目简单，文档不多

**推荐写法**：
```markdown
# 我的项目

## 项目概述
这是一个 Flask Web 应用，用于数据填充...

## 核心功能
1. 上传报表
2. 填充数据
3. 导出结果

## 开发规范
- 使用 Python 3.x
- 遵循 PEP 8 规范
- 添加类型提示

## 运行方式
```bash
python app.py
```
```

**优点**：
- ✅ 所有信息集中
- ✅ 不需要额外读取
- ✅ 适合小项目

---

### 示例 2：中型项目（CLAUDE.md + 子文档）

**场景**：项目较大，需要多个文档

**CLAUDE.md（主文件）**：
```markdown
# 我的项目

## 项目概述
[简短描述]

## 快速开始
1. 安装依赖
2. 配置环境
3. 运行应用

## 核心规范
- 编码规范：@docs/coding-standards.md
- API 设计：@docs/api-guidelines.md
- 测试规范：@docs/testing-guide.md

## 常见问题
故障排查：@docs/troubleshooting.md
```

**优点**：
- ✅ 主文件精简
- ✅ 详细文档分离开
- ✅ 按需读取

---

### 示例 3：大型项目（分层文档）

**场景**：复杂项目，大量文档

**CLAUDE.md（根目录）**：
```markdown
# 项目名称

## 快速导航
- **新手入门**：@docs/getting-started.md
- **开发指南**：@docs/development/index.md
- **API 参考**：@docs/api/index.md
- **部署指南**：@docs/deployment/index.md

## 核心原则
[列出最重要的 5-10 条原则]

## 文档索引
完整的文档结构：@docs/INDEX.md
```

**docs/INDEX.md**：
```markdown
# 文档索引

## 开发文档
- 编码规范：@docs/development/coding-standards.md
- 代码审查：@docs/development/code-review.md
- Git 工作流：@docs/development/git-workflow.md

## API 文档
- 用户 API：@docs/api/user-api.md
- 订单 API：@docs/api/order-api.md
- 支付 API：@docs/api/payment-api.md
```

**优点**：
- ✅ 清晰的文档结构
- ✅ 主文件很小
- ✅ 按需深入阅读

---

## 💡 最佳实践

### 1. 信息分层

```
第1层：CLAUDE.md（核心原则，必读）
  ↓
第2层：@docs/（详细文档，常用）
  ↓
第3层：外部链接（参考文档，偶尔）
```

**实施**：
- **CLAUDE.md**：写最重要的 10-20 条
- **@docs/**：详细说明，常用文档
- **外部链接**：补充材料，访问说明

---

### 2. 判断标准

**写内容前，问自己**：

✅ **这个信息每次对话都需要吗？**
- 是 → 写在 CLAUDE.md
- 否 → 用 `@` 引用

✅ **这个信息有多重要？**
- 非常重要 → 写在 CLAUDE.md
- 一般重要 → `@` 引用

✅ **这个内容有多长？**
- 短（<50 行）→ 可以直接写
- 中（50-200 行）→ 用 `@` 引用
- 长（>200 行）→ 必须用 `@` 引用

---

### 3. 检查清单

**修改 CLAUDE.md 前，确认**：

- [ ] 是否有项目概述？
- [ ] 是否有核心原则？
- [ ] 是否有快速开始？
- [ ] 是否使用了 `@` 引用？
- [ ] 外部链接是否有访问说明？
- [ ] 文件大小是否合理（建议 <500 行）？

---

## 🎯 实际案例

### 案例：优化前的 CLAUDE.md

**问题**：
```markdown
# 项目文档

## 小白友好模式
[200 行详细说明...]

## 编程经验教训
[300 行详细说明...]

## 文件组织约定
[150 行详细说明...]

## 全局项目配置
[100 行详细说明...]

总计：750 行
```

**问题**：
- ❌ 太长，每次都加载
- ❌ 浪费上下文
- ❌ 难以快速找到重点

---

### 优化后的 CLAUDE.md

**改进**：
```markdown
# 项目文档

## 小白友好模式
每次对话必须：
1. ✅ 解释术语
2. ✅ 说明原因
3. ✅ 演示操作
4. ✅ 提供类比

详细说明：@docs/explanation-standards.md

## 编程经验教训
核心原则：
- 根据实际场景选择方案
- 简单能用 > 完美但复杂
- 测试后再部署

经验总结：@knowledge-base/lessons-learned.md

## 文件组织约定
### 目录结构
```
d:\claude code -11\
├── docs/        # 文档
├── tests/       # 测试
└── uploads/     # 上传
```

### 基本规则
- 文档 → `docs/`
- 测试 → `tests/`
- 截图 → `test_screenshots/`

完整规范：@docs/file-organization.md

## 全局项目配置
默认位置：`d:\projects\`

创建命令：@docs/project-creation.md

总计：150 行（节省 80%）
```

**优点**：
- ✅ 核心信息突出
- ✅ 详细文档按需读取
- ✅ 节省 80% 上下文

---

## 📝 模板

### 小项目模板

```markdown
# 项目名称

## 概述
[1-2 句话描述]

## 快速开始
```bash
# 安装
npm install

# 运行
npm start
```

## 核心功能
1. 功能一
2. 功能二
3. 功能三

## 开发规范
- 使用 TypeScript
- 遵循 ESLint 规则
- 编写单元测试

## 常用命令
- `npm run dev` - 开发模式
- `npm test` - 运行测试
- `npm run build` - 构建
```

---

### 中型项目模板

```markdown
# 项目名称

## 概述
[简短描述]

## 快速开始
### 安装
```bash
npm install
```

### 运行
```bash
npm run dev
```

## 核心规范
### 编码标准
详细规范：@docs/coding-standards.md

### API 设计
API 指南：@docs/api-guidelines.md

### 测试规范
测试指南：@docs/testing-guide.md

## 项目结构
```
project/
├── src/
├── tests/
└── docs/
```

详细说明：@docs/project-structure.md

## 常见问题
故障排查：@docs/troubleshooting.md
```

---

## ✅ 总结

### 核心要点

1. **`@` 引用** → 按需读取，节省上下文
2. **核心内容** → 直接写在 CLAUDE.md
3. **外部链接** → 添加访问说明
4. **文件大小** → 控制在合理范围（<500 行）

### 判断标准

**好的 CLAUDE.md**：
- ✅ 有项目概述
- ✅ 有核心原则
- ✅ 有快速开始
- ✅ 使用 `@` 引用
- ✅ 外部链接有说明
- ✅ 大小适中

**不好的 CLAUDE.md**：
- ❌ 只有外部链接
- ❌ 内容过长（>1000 行）
- ❌ 没有核心信息
- ❌ 没有快速开始

---

**文档版本**：1.0
**最后更新**：2026-01-05
**适用范围**：Claude Code 项目

---

**记住**：好的 CLAUDE.md 能让 AI 更好地理解项目，提高工作效率！
