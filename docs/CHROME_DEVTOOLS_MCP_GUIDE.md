# Chrome DevTools MCP 安装完成指南

## ✅ 安装状态

您的系统已成功配置 Chrome DevTools MCP 服务器！

### 已完成的步骤
- ✅ Node.js v24.10.0 已安装
- ✅ npm 11.6.1 可用
- ✅ Chrome v143 已安装
- ✅ MCP 配置文件已创建: `C:\Users\15085\.claude\settings.json`
- ✅ Chrome DevTools MCP 服务器已配置

---

## 🚀 下一步操作

### 1. 重启 Claude Code

**重要**：必须重启 Claude Code 才能加载新的 MCP 服务器配置。

```bash
# 关闭当前 Claude Code 会话，然后重新打开
claude
```

### 2. 验证 MCP 服务器

重启后，在 Claude Code 中运行：

```bash
/mcp
```

您应该看到 `chrome-devtools` 服务器在列表中。

### 3. 开始使用

现在您可以直接在对话中使用 Chrome 自动化功能！

---

## 💡 使用示例

### 示例 1：打开网页并截图
```
打开 https://www.example.com 并截图给我看
```

### 示例 2：测试本地 Web 应用
```
打开 http://localhost:3000，检查页面标题，然后点击登录按钮
```

### 示例 3：性能分析
```
分析 https://developers.chrome.com 的性能，找出加载瓶颈
```

### 示例 4：检查控制台错误
```
打开 http://localhost:8080，读取控制台日志并告诉我是否有错误
```

### 示例 5：填写表单
```
打开表单页面，填写姓名、邮箱字段，然后提交
```

---

## 🎯 可用的 MCP 工具

Chrome DevTools MCP 提供以下工具类别：

### 输入自动化（8个工具）
- `click` - 点击元素
- `drag` - 拖拽元素
- `fill` - 填写输入框
- `fill_form` - 批量填写表单
- `handle_dialog` - 处理对话框
- `hover` - 鼠标悬停
- `press_key` - 按键
- `upload_file` - 上传文件

### 导航自动化（6个工具）
- `close_page` - 关闭页面
- `list_pages` - 列出所有页面
- `navigate_page` - 导航到 URL
- `new_page` - 打开新页面
- `select_page` - 选择页面
- `wait_for` - 等待条件

### 调试工具（5个工具）
- `evaluate_script` - 执行 JavaScript
- `get_console_message` - 获取控制台消息
- `list_console_messages` - 列出控制台消息
- `take_screenshot` - 截图
- `take_snapshot` - 获取 DOM 快照

### 性能分析（3个工具）
- `performance_start_trace` - 开始性能追踪
- `performance_stop_trace` - 停止性能追踪
- `performance_analyze_insight` - 分析性能数据

### 网络工具（2个工具）
- `get_network_request` - 获取网络请求
- `list_network_requests` - 列出网络请求

---

## ⚙️ 高级配置

### 自定义 Chrome 路径

如果需要使用特定的 Chrome 可执行文件，可以修改配置：

```json
{
  "mcpServers": {
    "chrome-devtools": {
      "command": "npx",
      "args": [
        "-y",
        "chrome-devtools-mcp@latest",
        "--executable-path=C:\\Users\\15085\\AppData\\Local\\Google\\Chrome\\Application\\chrome.exe"
      ],
      "disabled": false
    }
  }
}
```

### 启用无头模式

如果您不需要看到浏览器界面：

```json
{
  "mcpServers": {
    "chrome-devtools": {
      "command": "npx",
      "args": [
        "-y",
        "chrome-devtools-mcp@latest",
        "--headless=true"
      ],
      "disabled": false
    }
  }
}
```

### 设置视口大小

```json
{
  "mcpServers": {
    "chrome-devtools": {
      "command": "npx",
      "args": [
        "-y",
        "chrome-devtools-mcp@latest",
        "--viewport=1920x1080"
      ],
      "disabled": false
    }
  }
}
```

---

## 🔍 故障排查

### 问题 1：MCP 服务器未显示

**解决方案**：
1. 确认已重启 Claude Code
2. 检查配置文件路径是否正确
3. 运行 `/mcp` 查看服务器状态

### 问题 2：Chrome 无法启动

**解决方案**：
1. 确认 Chrome 已正确安装
2. 检查是否有其他 Chrome 实例正在运行
3. 尝试手动运行：`npx -y chrome-devtools-mcp@latest`

### 问题 3：功能不工作

**解决方案**：
1. 确保 Node.js 和 npm 版本符合要求
2. 检查网络连接（首次需要下载包）
3. 查看 Claude Code 的错误日志

---

## 📊 与原生集成对比

| 特性 | Chrome DevTools MCP | 原生集成（需付费） |
|------|---------------------|-------------------|
| 成本 | ✅ 免费 | ❌ 需要 Pro 订阅 |
| 登录状态 | ❌ 独立实例 | ✅ 共享登录状态 |
| 浏览器扩展 | ❌ 无扩展 | ✅ 保留扩展 |
| DevTools 功能 | ✅ 完整支持 | ✅ 完整支持 |
| 性能分析 | ✅ 专业级 | ✅ 专业级 |
| 自动化测试 | ✅ 强大 | ✅ 强大 |
| 适用场景 | 开发/测试 | 日常自动化 |

---

## 🎓 推荐工作流程

### Web 开发流程
1. 在终端开发代码
2. 使用 MCP 打开本地服务器（如 localhost:3000）
3. 测试 UI 交互
4. 读取控制台日志调试
5. 发现问题直接修改代码
6. 刷新浏览器验证修复

### 数据抓取流程
1. 打开目标网页
2. 使用 `take_snapshot` 获取 DOM 结构
3. 使用 `evaluate_script` 提取数据
4. 保存到本地文件
5. 处理和分析数据

---

## 🔗 相关资源

- [Chrome DevTools MCP 官方文档](https://github.com/ChromeDevTools/chrome-devtools-mcp)
- [Claude Code 文档](https://code.claude.com/docs)
- [MCP 协议规范](https://modelcontextprotocol.io)

---

## 💡 最佳实践

1. **首次使用**：从简单的任务开始，如打开网页和截图
2. **调试技巧**：善用 `list_console_messages` 查看错误
3. **性能优化**：使用性能分析工具找出瓶颈
4. **自动化测试**：编写重复性的测试流程
5. **数据提取**：结合 `evaluate_script` 提取结构化数据

---

## ⚠️ 注意事项

1. **独立浏览器实例**：MCP 会启动新的 Chrome 实例，不共享您的登录状态
2. **资源占用**：首次运行需要下载包，可能需要一些时间
3. **网络依赖**：首次使用需要网络连接下载 chrome-devtools-mcp
4. **权限管理**：浏览器可以访问任何网站，请注意安全

---

## 🚀 立即开始

重启 Claude Code 后，试试这个命令：

```
打开 https://www.baidu.com，截图给我看看
```

或者更复杂的任务：

```
打开 https://github.com，搜索 "claude-code"，然后告诉我前三个结果
```

祝您使用愉快！🎉
