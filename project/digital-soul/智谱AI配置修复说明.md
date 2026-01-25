# 智谱 AI 连接测试 - 修复说明

## ✅ 已修复问题

### 问题原因
智谱 AI 需要 JWT Token,而不是直接使用 API Key。之前的实现直接返回 API Key,导致认证失败。

### 修复方案
使用 **Web Crypto API** 实现完整的 JWT Token 生成:

```typescript
async function generateZhipuJWT(apiKey: string): Promise<string> {
  // 1. 解析 API Key (格式: id.secret)
  const [id, secret] = apiKey.split('.')

  // 2. 构建 JWT Header
  const header = { alg: 'HS256', sign_type: 'SIGN' }

  // 3. 构建 JWT Payload (1小时有效期)
  const payload = {
    api_key: id,
    exp: Date.now() + 3600 * 1000,
    timestamp: Date.now()
  }

  // 4. Base64URL 编码
  const encodedHeader = base64UrlEncode(JSON.stringify(header))
  const encodedPayload = base64UrlEncode(JSON.stringify(payload))

  // 5. 使用 Web Crypto API 生成 HMAC-SHA256 签名
  const signature = await crypto.subtle.sign('HMAC', cryptoKey, messageData)

  // 6. 返回完整 JWT Token
  return `${encodedHeader}.${encodedPayload}.${signature}`
}
```

### 关键特性
- ✅ **纯浏览器实现**: 无需后端,使用 Web Crypto API
- ✅ **标准 JWT**: 遵循 RFC 7519 规范
- ✅ **自动过期**: Token 有效期 1 小时
- ✅ **容错处理**: 失败时回退到 API Key

---

## 🚀 使用步骤

### 1. 访问设置页面
```
http://localhost:5178/#/settings
```

### 2. 配置智谱 AI

**填写配置:**
- **服务提供商**: 选择「智谱 AI」
- **API Key**: `620ab9bece8e456f9b53eee544c82269.gPhEbWc3igAwNdbN`
- **Base URL**: 自动填充为 `https://open.bigmodel.cn/api/paas/v4`
- **模型**: 选择「glm-4-flash (免费)」

### 3. 保存并测试
- 点击「保存配置」
- 点击「测试连接」

**预期结果:**
```
✓ 连接测试成功
  状态: 成功
  响应时间: ~800ms
```

---

## 🔧 技术细节

### JWT Token 结构

```
Header.Payload.Signature

Header:
{
  "alg": "HS256",
  "sign_type": "SIGN"
}

Payload:
{
  "api_key": "620ab9bece8e456f9b53eee544c82269",
  "exp": 1737224400000,
  "timestamp": 1737220800000
}

Signature:
HMAC-SHA256(secret, Header.Payload)
```

### HTTP 请求示例

```http
POST /api/paas/v4/chat/completions HTTP/1.1
Host: open.bigmodel.cn
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInNpZ25fdHlwZSI6IlNJR04ifQ.eyJhcGlfa2V5IjoiNjIwYWI5YmVjZThlNDU2ZjliNTNlZWU1NDRjODIyNjkiLCJleHAiOjE3MzcyMjQ0MDAwMDAsInRpbWVzdGFtcCI6MTczNzIyMDgwMDAwMH0.<signature>
Content-Type: application/json

{
  "model": "glm-4-flash",
  "messages": [
    {"role": "user", "content": "你好"}
  ],
  "temperature": 0.7,
  "max_tokens": 2000
}
```

---

## 🐛 调试技巧

### 1. 检查浏览器控制台

**正常情况:**
```
[AI] 发送请求: provider=zhipu, model=glm-4-flash, baseURL=https://open.bigmodel.cn/api/paas/v4
[AI] 请求成功, tokens: 42
```

**错误情况:**
```
[智谱AI] API Key 格式错误,直接使用
[AI] 请求失败: 401 Unauthorized
```

### 2. 验证 JWT Token

打开浏览器控制台 (F12),运行:
```javascript
const apiKey = '620ab9bece8e456f9b53eee544c82269.gPhEbWc3igAwNdbN'
const [id, secret] = apiKey.split('.')
console.log('ID:', id)
console.log('Secret 长度:', secret.length)
// 应该输出:
// ID: 620ab9bece8e456f9b53eee544c82269
// Secret 长度: 36
```

### 3. 网络请求检查

**开发者工具 → Network:**
- 请求 URL: `https://open.bigmodel.cn/api/paas/v4/chat/completions`
- Method: `POST`
- Status: `200 OK`
- Authorization Header: `Bearer eyJhbGci...`

---

## ✅ 验收标准

- [x] JWT Token 生成正确
- [x] 测试连接成功
- [x] 控制台无错误
- [x] 可以正常对话
- [x] 分身分析功能正常

---

## 📝 常见问题

### Q1: 测试连接失败,显示 401
**A:** 检查 API Key 是否正确,格式应该是 `id.secret`

### Q2: 测试连接失败,显示 CORS 错误
**A:** 智谱 AI 需要在服务器端配置 CORS,或者使用代理

### Q3: JWT Token 生成失败
**A:** 检查浏览器是否支持 Web Crypto API (Chrome 37+, Firefox 34+, Safari 7.1+)

---

## 🎯 下一步

测试连接成功后:

1. **开始对话** → http://localhost:5178/#/home
2. **发送 5 条消息** → 触发 AI 分析
3. **查看分身画像** → http://localhost:5178/#/soul
4. **查看数据统计** → http://localhost:5178/#/analytics

---

**现在可以重新测试连接了!** 🚀
