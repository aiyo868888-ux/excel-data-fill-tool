# 我的 MCP 服务器清单

## 📋 更新时间
2025-01-04

---

## ✅ 全局用户配置 (settings.json)

### 1. **chrome-devtools** ✨ 新安装
- **类型**：官方 Chrome DevTools MCP
- **功能**：Chrome 浏览器自动化、调试、性能分析
- **工具数**：26+ 专业工具
- **状态**：✅ 已启用
- **位置**：`C:\Users\15085\.claude\settings.json`

**主要功能**：
- 🌐 打开/关闭/切换网页
- 🖱️ 点击、填表、拖拽、悬停
- 📸 截图、DOM 快照
- 🐛 控制台日志读取
- 📊 性能追踪和分析
- 🌐 网络请求监控
- ⚡ Puppeteer 自动化

**使用示例**：
```
打开 https://www.example.com 并截图
```

---

## 📁 项目级配置

### 项目 1：`D:\claude code`

#### 2. **github** 🐙
- **类型**：GitHub 集成
- **功能**：管理 GitHub 仓库、Issues、PR
- **状态**：✅ 已配置

**主要功能**：
- 查看仓库信息
- 创建/查看 Issues
- 管理 Pull Requests
- 查看代码文件

#### 3. **exa-web-search** 🔍
- **类型**：Exa 网络搜索
- **功能**：AI 驱动的网络搜索
- **状态**：✅ 已配置

#### 4. **playwright** 🎭
- **类型**：Playwright 浏览器自动化
- **功能**：跨浏览器测试和自动化
- **状态**：✅ 已配置

**注意**：与 chrome-devtools 类似，但支持多浏览器（Chrome、Firefox、Safari）

#### 5. **duckduckgo-web-search** 🦆
- **类型**：DuckDuckGo 搜索
- **功能**：隐私友好的网络搜索
- **状态**：✅ 已配置

#### 6. **notion** 📝
- **类型**：Notion 集成
- **功能**：读写 Notion 数据库和页面
- **状态**：✅ 已配置（已配置 API 密钥）

**配置**：
- API Key：已配置
- Page ID：已配置

**使用场景**：
- 读取 Notion 数据库
- 创建新页面
- 更新数据库记录

#### 7. **firecrawl** 🔥
- **类型**：Firecrawl 网页抓取
- **功能**：高级网页内容提取
- **状态**：✅ 已配置（已配置 API 密钥）

**功能**：
- 抓取整个网站
- 提取结构化数据
- 处理动态内容

---

### 项目 2：`c:\trae`

#### 8. **sequential-thinking** 🧠
- **类型**：顺序思考服务器
- **功能**：结构化思考和推理
- **状态**：✅ 已配置

#### 9. **playwright** 🎭
- **类型**：Playwright 浏览器自动化
- **功能**：同上
- **状态**：✅ 已配置

---

## 📊 统计总结

| 类别 | 数量 |
|------|------|
| **全局服务器** | 1 个 |
| **项目 D:\claude code** | 6 个 |
| **项目 c:\trae** | 2 个 |
| **总计** | 9 个 |

---

## 🎯 按功能分类

### 🔍 搜索类（3个）
- **exa-web-search** - AI 搜索
- **duckduckgo-web-search** - 隐私搜索
- **firecrawl** - 网页抓取

### 🌐 浏览器自动化（2个）
- **chrome-devtools** - Chrome 专用
- **playwright** - 跨浏览器

### 📝 生产力工具（2个）
- **github** - GitHub 管理
- **notion** - Notion 集成

### 🧠 AI 辅助（1个）
- **sequential-thinking** - 结构化思考

---

## 🚀 快速使用指南

### Chrome 浏览器自动化
```
打开 https://www.baidu.com 搜索"Claude Code"
```

### GitHub 集成
```
查看我的 GitHub 仓库列表
```

### Notion 操作
```
在 Notion 中创建新页面，标题是"今日任务"
```

### 网络搜索
```
搜索"2025年最新 AI 技术趋势"
```

### 网页抓取
```
抓取 https://example.com 的所有产品链接
```

---

## ⚙️ 配置文件位置

### 全局配置
- **用户配置**：`C:\Users\15085\.claude\settings.json`
  - chrome-devtools

- **全局配置**：`C:\Users\15085\.claude.json`
  - 其他项目级配置

### 项目配置
- **D:\claude code**：6 个 MCP 服务器
- **c:\trae**：2 个 MCP 服务器
- **D:\claude code -11**：0 个 MCP 服务器（当前项目）

---

## 💡 建议

### 当前项目 (D:\claude code -11)
您当前项目**没有配置任何 MCP 服务器**。

如果想使用以下功能，需要添加配置：
1. **chrome-devtools** - 浏览器自动化
2. **github** - GitHub 集成
3. **playwright** - 跨浏览器测试
4. **notion** - Notion 集成
5. **web-search** - 网络搜索

### 如何添加到当前项目？

创建或编辑 `D:\claude code -11\.claude\settings.json`：

```json
{
  "mcpServers": {
    "chrome-devtools": {
      "command": "npx",
      "args": ["-y", "chrome-devtools-mcp@latest"],
      "disabled": false
    }
  }
}
```

---

## 🔗 相关资源

- [Chrome DevTools MCP 文档](https://github.com/ChromeDevTools/chrome-devtools-mcp)
- [Playwright MCP 文档](https://github.com/Microsoft/playwright-mcp)
- [GitHub MCP 文档](https://github.com/modelcontextprotocol/servers/tree/main/src/github)
- [Notion MCP 文档](https://github.com/NotionX/notion-mcp-server)

---

**最后更新**：2025-01-04
**配置状态**：✅ 全局 chrome-devtools 已启用，其他在项目级配置中
