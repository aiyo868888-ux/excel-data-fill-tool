# AI 功能使用指南

## 🎉 恭喜！AI 功能已添加

你的 Digital Soul 现在支持真实的 AI 对话功能了！

## 🚀 快速开始

### 1️⃣ 获取 OpenAI API Key

1. 访问 [OpenAI Platform](https://platform.openai.com/api-keys)
2. 登录你的账号（如果没有账号，先注册）
3. 点击 "Create new secret key"
4. 复制生成的 API Key（格式：`sk-...`）

**⚠️ 重要提醒：**
- API Key 只会显示一次，请妥善保存
- 不要分享你的 API Key
- 建议设置使用限额，避免意外扣费

### 2️⃣ 配置 API Key

1. 打开应用：http://localhost:5177
2. 点击左侧菜单"设置"
3. 在设置页面填写：
   - **API Key**: 粘贴你的 OpenAI API Key
   - **Base URL**: 保持默认（如果使用代理或兼容服务，可修改）
   - **模型**: 选择你要使用的模型
     - `gpt-3.5-turbo` - 快速经济（推荐）
     - `gpt-4` - 更强大的推理
     - `gpt-4-turbo` - 平衡性能
     - `gpt-4o` - 最新模型

4. 点击"保存配置"按钮

### 3️⃣ 测试连接

在设置页面点击"测试连接"按钮：
- ✅ 成功：显示响应时间
- ❌ 失败：显示错误信息

**常见问题：**
- "API Key 未配置" - 请先保存配置
- "Incorrect API key provided" - 检查 API Key 是否正确
- "Insufficient quota" - 账户余额不足，需要充值

### 4️⃣ 开始对话

1. 点击左侧菜单"对话"
2. 在输入框中输入你的问题
3. 点击"发送"或按 `Ctrl+Enter`
4. 等待 AI 回复（会显示"正在思考..."）

## 💡 使用技巧

### 对话功能
- **上下文记忆**: 系统会记住最近 10 条消息作为上下文
- **新对话**: 点击"新对话"按钮清空历史
- **快捷发送**: `Ctrl+Enter` 快速发送消息

### 模型选择建议
| 模型 | 速度 | 成本 | 适用场景 |
|------|------|------|----------|
| gpt-3.5-turbo | ⚡⚡⚡ | 💰 | 日常对话、快速问答 |
| gpt-4 | ⚡ | 💰💰💰 | 复杂推理、专业问题 |
| gpt-4-turbo | ⚡⚡ | 💰💰 | 平衡性能和成本 |
| gpt-4o | ⚡⚡⚡ | 💰💰 | 最新功能、多模态 |

### 节省成本
- 优先使用 `gpt-3.5-turbo`，成本低速度快
- 定期清理对话历史，减少 token 消耗
- 在 OpenAI 设置中设置使用限额

## 🔧 高级配置

### 使用代理或兼容服务

如果你使用代理或其他 OpenAI 兼容服务（如 Azure OpenAI）：

1. 在设置页面修改"Base URL"
2. 例如：`https://your-proxy.com/v1`
3. 或 Azure：`https://your-resource.openai.azure.com/openai/deployments/your-deployment`

### 本地模型支持

支持使用本地运行的模型（如 Ollama）：

1. 安装 Ollama
2. 运行 `ollama run qwen2`
3. 设置 Base URL: `http://localhost:11434/v1`
4. 模型选择: `qwen2`

## 📊 功能说明

### 已实现
- ✅ OpenAI API 集成
- ✅ 多模型支持
- ✅ 对话历史记忆
- ✅ 配置持久化（localStorage）
- ✅ 连接测试
- ✅ 错误处理
- ✅ 打字动画效果

### 开发中
- 🚧 流式输出
- 🚧 多轮对话优化
- 🚧 记忆提取和存储
- 🚧 个性化设置

## ❓ 常见问题

**Q: API Key 安全吗？**
A: API Key 保存在浏览器的 localStorage 中，仅在本地使用。建议定期更换 API Key。

**Q: 会消耗多少费用？**
A: 使用 gpt-3.5-turbo，1000 条消息约 $0.1-0.5。建议在 OpenAI 设置限额。

**Q: 可以使用其他 AI 服务吗？**
A: 支持 OpenAI 兼容的 API，如 Azure OpenAI、本地模型等。

**Q: 数据会保存在哪里？**
A: 当前版本数据保存在浏览器 localStorage，刷新后重置。完整版本需要 Electron 环境。

**Q: 如何查看 API 使用情况？**
A: 访问 [OpenAI Dashboard](https://platform.openai.com/usage) 查看详细使用统计。

## 🎯 下一步

1. **配置 API Key** - 在设置页面添加你的 OpenAI API Key
2. **测试连接** - 确保配置正确
3. **开始对话** - 体验真实的 AI 对话功能
4. **探索功能** - 尝试不同的模型和配置

## 📞 获取帮助

- OpenAI 文档: https://platform.openai.com/docs
- API 参考: https://platform.openai.com/docs/api-reference
- 定价: https://openai.com/pricing

---

**开始你的 AI 对话之旅吧！** 🚀
