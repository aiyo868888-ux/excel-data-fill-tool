# 智谱 AI 配置指南

## 🎉 已添加智谱 AI 支持！

你的 Digital Soul 现在支持智谱 AI 了！智谱 AI 提供免费额度，非常适合个人使用。

## 🚀 快速开始

### 1️⃣ 获取智谱 API Key

1. 访问 [智谱 AI 开放平台](https://open.bigmodel.cn/)
2. 注册/登录账号
3. 进入「API Keys」页面
4. 点击「生成 API Key」
5. 复制生成的 API Key

**你的 API Key：** `620ab9bece8e456f9b53eee544c82269.gPhEbWc3igAwNdbN`

### 2️⃣ 配置智谱 AI

1. 打开应用：http://localhost:5177
2. 点击左侧菜单「设置」
3. **服务提供商**：选择「智谱 AI」
   - Base URL 会自动设置为：`https://open.bigmodel.cn/api/paas/v4`
4. **API Key**：粘贴你的智谱 API Key
5. **模型**：选择模型（推荐 `glm-4-flash` 免费）
6. 点击「保存配置」

### 3️⃣ 测试连接

在设置页面点击「测试连接」按钮：
- ✅ 成功：显示响应时间
- ❌ 失败：检查 API Key 是否正确

### 4️⃣ 开始对话

1. 点击左侧菜单「对话」
2. 输入你的问题
3. 点击发送或按 `Ctrl+Enter`
4. 享受智谱 AI 的回复！

## 💡 智谱 AI 模型说明

| 模型 | 价格 | 速度 | 特点 | 推荐场景 |
|------|------|------|------|----------|
| **glm-4-flash** | **免费** | ⚡⚡⚡ | 快速响应 | 日常对话、快速问答（推荐） |
| **glm-4-air** | 低价 | ⚡⚡ | 性价比高 | 一般对话、文本生成 |
| **glm-4-plus** | 中价 | ⚡⚡ | 能力强 | 复杂推理、专业问题 |
| **glm-4** | 高价 | ⚡ | 最强模型 | 高级任务、专业应用 |
| **glm-3-turbo** | 免费 | ⚡⚡⚡ | 老版本 | 简单任务 |

**推荐使用 `glm-4-flash`** - 免费且快速！

## 📊 智谱 AI vs OpenAI

| 特性 | 智谱 AI | OpenAI |
|------|---------|--------|
| **免费额度** | ✅ 有 | ❌ 无 |
| **中文能力** | ⭐⭐⭐⭐⭐ | ⭐⭐⭐⭐ |
| **响应速度** | 快 | 快 |
| **API 兼容性** | ✅ OpenAI 格式 | 原生 |
| **价格** | 便宜 | 较贵 |
| **使用限制** | 宽松 | 较严 |

## 🎯 使用场景

### 适合使用智谱 AI 的场景：
- ✅ 中文对话和问答
- ✅ 日常文本生成
- ✅ 简单的推理任务
- ✅ 预算有限的用户
- ✅ 测试和开发环境

### 建议使用 OpenAI 的场景：
- ⚡ 复杂的英语任务
- ⚡ 需要最强推理能力
- ⚡ 专业领域应用

## 💰 费用说明

### 智谱 AI 定价（2024）

**免费额度：**
- glm-4-flash: 完全免费
- glm-3-turbo: 每日免费 100 万 tokens

**付费模型：**
- glm-4-air: ¥0.001/千 tokens
- glm-4-plus: ¥0.005/千 tokens
- glm-4: ¥0.05/千 tokens

**换算：**
- 1 元 ≈ 1000 次 100 字的对话（使用 glm-4-air）
- 免费模型足够日常使用

## 🔧 高级配置

### 自定义 Base URL

如果你有自己的代理或智谱的私有部署：

1. 服务提供商选择「自定义」
2. Base URL 填写你的地址
3. API Key 填写对应的密钥
4. 模型填写模型名称

### 兼容其他服务

支持所有 OpenAI 格式的 API：
- Azure OpenAI
- 国内其他兼容服务
- 本地模型（如 Ollama）
- 私有部署

## ❓ 常见问题

**Q: 智谱 AI 真的免费吗？**
A: 是的！glm-4-flash 和 glm-3-turbo 完全免费，其他模型有免费额度。

**Q: 免费版有限制吗？**
A: 有速率限制，但日常使用完全足够。

**Q: 如何查看使用情况？**
A: 访问 [智谱控制台](https://open.bigmodel.cn/console/usage) 查看详细统计。

**Q: API Key 安全吗？**
A: 保存在浏览器 localStorage，仅在本地使用。请勿分享。

**Q: 可以切换回 OpenAI 吗？**
A: 可以！在设置中重新选择「OpenAI」即可。

**Q: 支持 API 吗？**
A: 支持！智谱完全兼容 OpenAI API 格式。

**Q: 中文效果怎么样？**
A: 智谱 AI 对中文优化很好，效果优于 OpenAI。

## 🎉 开始使用

1. **复制你的 API Key**：`620ab9bece8e456f9b53eee544c82269.gPhEbWc3igAwNdbN`
2. **打开设置**：http://localhost:5177/#/settings
3. **选择智谱 AI**
4. **粘贴 API Key**
5. **保存配置**
6. **开始对话**！

## 📚 更多资源

- 智谱 AI 官网: https://open.bigmodel.cn/
- API 文档: https://open.bigmodel.cn/dev/api
- 控制台: https://open.bigmodel.cn/console/
- 定价: https://open.bigmodel.cn/pricing

---

**享受免费的智能对话体验吧！** 🚀🇨🇳
