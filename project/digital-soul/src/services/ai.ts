/**
 * AI 服务 - OpenAI API 集成
 */

/**
 * 生成智谱 AI 的 JWT Token
 * 使用 Web Crypto API 实现 HMAC-SHA256 签名
 */
async function generateZhipuJWT(apiKey: string): Promise<string> {
  try {
    const [id, secret] = apiKey.split('.')
    if (!id || !secret) {
      console.warn('[智谱AI] API Key 格式错误,直接使用')
      return apiKey
    }

    // JWT Header
    const header = {
      alg: 'HS256',
      sign_type: 'SIGN'
    }

    // JWT Payload (1小时有效期)
    const now = Date.now()
    const payload = {
      api_key: id,
      exp: now + 3600 * 1000,
      timestamp: now
    }

    // Base64URL 编码函数
    const base64UrlEncode = (str: string): string => {
      return btoa(str)
        .replace(/\+/g, '-')
        .replace(/\//g, '_')
        .replace(/=/g, '')
    }

    // 编码 header 和 payload
    const encodedHeader = base64UrlEncode(JSON.stringify(header))
    const encodedPayload = base64UrlEncode(JSON.stringify(payload))

    // 生成签名
    const data = `${encodedHeader}.${encodedPayload}`
    const encoder = new TextEncoder()
    const keyData = encoder.encode(secret)
    const messageData = encoder.encode(data)

    // 使用 Web Crypto API 生成 HMAC-SHA256
    const cryptoKey = await crypto.subtle.importKey(
      'raw',
      keyData,
      { name: 'HMAC', hash: 'SHA-256' },
      false,
      ['sign']
    )

    const signature = await crypto.subtle.sign(
      'HMAC',
      cryptoKey,
      messageData
    )

    // 转换签名为 Base64URL
    const signatureArray = Array.from(new Uint8Array(signature))
    const signatureString = btoa(String.fromCharCode.apply(null, signatureArray))
      .replace(/\+/g, '-')
      .replace(/\//g, '_')
      .replace(/=/g, '')

    return `${encodedHeader}.${encodedPayload}.${signatureString}`
  } catch (error) {
    console.error('[智谱AI] JWT 生成失败:', error)
    // 失败时返回原始 API Key
    return apiKey
  }
}

export interface ChatMessage {
  role: 'system' | 'user' | 'assistant'
  content: string
}

export interface ChatOptions {
  model?: string
  temperature?: number
  maxTokens?: number
  stream?: boolean
}

export interface ChatResponse {
  content: string
  usage?: {
    promptTokens: number
    completionTokens: number
    totalTokens: number
  }
}

/**
 * AI 服务类
 */
export class AIService {
  private apiKey: string = ''
  private baseURL: string = 'https://api.openai.com/v1'
  private model: string = 'gpt-3.5-turbo'
  private provider: string = 'openai'

  constructor() {
    // 从 localStorage 读取配置
    this.loadConfig()
  }

  /**
   * 加载配置
   */
  private loadConfig() {
    const config = localStorage.getItem('ai-config')
    if (config) {
      const parsed = JSON.parse(config)
      this.apiKey = parsed.apiKey || ''
      this.baseURL = parsed.baseURL || this.baseURL
      this.model = parsed.model || this.model
      this.provider = parsed.provider || 'openai'
    }
  }

  /**
   * 保存配置
   */
  saveConfig(config: { apiKey: string; baseURL?: string; model?: string; provider?: string }) {
    this.apiKey = config.apiKey
    if (config.baseURL) this.baseURL = config.baseURL
    if (config.model) this.model = config.model
    if (config.provider) this.provider = config.provider

    localStorage.setItem('ai-config', JSON.stringify({
      apiKey: this.apiKey,
      baseURL: this.baseURL,
      model: this.model,
      provider: this.provider
    }))
  }

  /**
   * 检查是否已配置
   */
  isConfigured(): boolean {
    return !!this.apiKey
  }

  /**
   * 发送聊天请求
   */
  async chat(messages: ChatMessage[], options: ChatOptions = {}): Promise<ChatResponse> {
    if (!this.apiKey) {
      throw new Error('API Key 未配置，请先在设置中配置 API Key')
    }

    const model = options.model || this.model
    const temperature = options.temperature ?? 0.7
    const maxTokens = options.maxTokens ?? 2000

    // 准备 Authorization header
    let authToken: string
    if (this.provider === 'zhipu') {
      // 智谱 AI 需要生成 JWT Token
      authToken = await generateZhipuJWT(this.apiKey)
    } else {
      // OpenAI 直接使用 API Key
      authToken = this.apiKey
    }

    const headers: Record<string, string> = {
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${authToken}`
    }

    try {
      console.log(`[AI] 发送请求: provider=${this.provider}, model=${model}, baseURL=${this.baseURL}`)

      const response = await fetch(`${this.baseURL}/chat/completions`, {
        method: 'POST',
        headers,
        body: JSON.stringify({
          model,
          messages,
          temperature,
          max_tokens: maxTokens
        })
      })

      if (!response.ok) {
        const errorText = await response.text()
        console.error('[AI] 请求失败:', response.status, errorText)

        let errorMessage = 'API 请求失败'
        try {
          const error = await response.json()
          errorMessage = error.error?.message || error.message || errorMessage
        } catch {
          errorMessage = `HTTP ${response.status}: ${errorText}`
        }

        throw new Error(errorMessage)
      }

      const data = await response.json()

      console.log('[AI] 请求成功, tokens:', data.usage?.total_tokens || 0)

      return {
        content: data.choices[0]?.message?.content || '',
        usage: {
          promptTokens: data.usage?.prompt_tokens || 0,
          completionTokens: data.usage?.completion_tokens || 0,
          totalTokens: data.usage?.total_tokens || 0
        }
      }
    } catch (error: any) {
      console.error('[AI] 请求异常:', error)
      throw new Error(`AI 请求失败: ${error.message}`)
    }
  }

  /**
   * 流式聊天（返回异步生成器）
   */
  async *chatStream(messages: ChatMessage[], options: ChatOptions = {}): AsyncGenerator<string> {
    if (!this.apiKey) {
      throw new Error('API Key 未配置，请先在设置中配置 API Key')
    }

    const model = options.model || this.model
    const temperature = options.temperature ?? 0.7
    const maxTokens = options.maxTokens ?? 2000

    const response = await fetch(`${this.baseURL}/chat/completions`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${this.apiKey}`
      },
      body: JSON.stringify({
        model,
        messages,
        temperature,
        max_tokens: maxTokens,
        stream: true
      })
    })

    if (!response.ok) {
      const error = await response.json()
      throw new Error(error.error?.message || 'API 请求失败')
    }

    const reader = response.body?.getReader()
    const decoder = new TextDecoder()

    if (!reader) {
      throw new Error('无法读取响应流')
    }

    try {
      while (true) {
        const { done, value } = await reader.read()
        if (done) break

        const chunk = decoder.decode(value)
        const lines = chunk.split('\n').filter(line => line.trim() !== '')

        for (const line of lines) {
          if (line.startsWith('data: ')) {
            const data = line.slice(6)
            if (data === '[DONE]') return

            try {
              const parsed = JSON.parse(data)
              const content = parsed.choices[0]?.delta?.content
              if (content) {
                yield content
              }
            } catch (e) {
              // 忽略解析错误
            }
          }
        }
      }
    } finally {
      reader.releaseLock()
    }
  }
}

// 导出单例
export const aiService = new AIService()
