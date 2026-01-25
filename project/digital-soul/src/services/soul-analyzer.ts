/**
 * AI 驱动的分身画像分析服务
 * 从对话中提取价值观、性格特质、关注方向
 */

import { aiService } from './ai'
import type { DigitalSoul } from '@/core/models/DigitalSoul'
import type { Value, PersonalityTrait, FocusArea } from '@/core/models/DigitalSoul'
import type { ConversationMessage } from './storage'

/**
 * 分析结果接口
 */
interface AnalysisResult {
  values: Value[]
  personality: PersonalityTrait[]
  focusAreas: FocusArea[]
  confidence: number
}

/**
 * AI 原始响应格式
 */
interface RawAnalysisResponse {
  values: Array<{
    name: string
    description?: string
    priority: number
    confidence?: number
    examples?: string[]
  }>
  personality: Array<{
    name: string
    displayName?: string
    score: number
    description?: string
    confidence?: number
    indicators?: string[]
  }>
  focusAreas: Array<{
    topic: string
    category?: string
    intensity: number
    trending?: 'up' | 'down' | 'stable'
    reason?: string
  }>
  overallConfidence: number
}

/**
 * 分身分析器类
 */
export class SoulAnalyzer {
  // 只读常量
  readonly ANALYSIS_THRESHOLD = 5 // 每5条消息分析一次

  private cache: {
    timestamp: number
    messageCount: number
    result: AnalysisResult
  } | null = null

  private analyzePromise: Promise<AnalysisResult> | null = null

  /**
   * 构建 AI 分析 Prompt
   */
  private buildAnalysisPrompt(messages: ConversationMessage[]): string {
    // 最近10条消息
    const recent = messages.slice(-10)
    const conversationText = recent
      .map(m => `${m.role === 'user' ? '用户' : 'AI'}: ${m.content}`)
      .join('\n')

    return `你是专业的心理分析师。请分析以下对话内容,提取用户的特征信息。

对话记录:
${conversationText}

请按以下 JSON 格式返回分析结果:
{
  "values": [
    {
      "name": "价值观名称(如:创新、稳定、效率、协作)",
      "description": "简短描述",
      "priority": 0.8,
      "confidence": 0.7,
      "examples": ["对话中的具体例子1", "例子2"]
    }
  ],
  "personality": [
    {
      "name": "openness|conscientiousness|extraversion|agreeableness|neuroticism",
      "displayName": "开放性/尽责性/外向性/宜人性/神经质",
      "score": 0.7,
      "description": "基于对话的判断依据",
      "confidence": 0.6,
      "indicators": ["观察到的行为指标1", "指标2"]
    }
  ],
  "focusAreas": [
    {
      "topic": "关注主题(如:技术、管理、学习、健康)",
      "category": "work|personal|learning",
      "intensity": 0.8,
      "trending": "up",
      "reason": "判断理由"
    }
  ],
  "overallConfidence": 0.7
}

要求:
1. 价值观提取 3-5 个核心价值观,按优先级排序
2. 性格特质基于五大人格模型,每个维度给出 0-1 的评分
3. 关注方向识别 3-5 个主要关注点
4. confidence 字段反映分析的可靠程度 (0-1)
5. 只返回 JSON,不要其他文字
6. 确保返回的是有效的 JSON 格式`
  }

  /**
   * 执行分析
   */
  async analyze(
    soul: DigitalSoul,
    newMessages: ConversationMessage[]
  ): Promise<AnalysisResult> {
    if (newMessages.length < this.ANALYSIS_THRESHOLD) {
      throw new Error(`消息数量不足,需要至少 ${this.ANALYSIS_THRESHOLD} 条消息才能分析`)
    }

    // 检查缓存 (5分钟内相同消息数量)
    const cacheKey = `${newMessages.length}-${newMessages[newMessages.length - 1].timestamp}`
    if (this.cache &&
      Date.now() - this.cache.timestamp < 300000 &&
      this.cache.messageCount === newMessages.length) {
      console.log('[Analyzer] 使用缓存的分析结果')
      return this.cache.result
    }

    // 如果正在分析,等待完成
    if (this.analyzePromise) {
      console.log('[Analyzer] 等待正在进行的分析...')
      return await this.analyzePromise
    }

    this.analyzePromise = this._analyze(soul, newMessages)
    const result = await this.analyzePromise
    this.analyzePromise = null

    // 缓存结果
    this.cache = {
      timestamp: Date.now(),
      messageCount: newMessages.length,
      result
    }

    return result
  }

  /**
   * 内部分析实现
   */
  private async _analyze(
    soul: DigitalSoul,
    newMessages: ConversationMessage[]
  ): Promise<AnalysisResult> {
    const prompt = this.buildAnalysisPrompt(newMessages)

    try {
      console.log('[Analyzer] 开始分析,消息数量:', newMessages.length)

      const response = await aiService.chat([
        {
          role: 'system',
          content: '你是专业的心理分析师,擅长从对话中提取人格特征。请严格按照 JSON 格式返回结果。'
        },
        { role: 'user', content: prompt }
      ], {
        temperature: 0.3, // 低温度保证稳定输出
        maxTokens: 2000
      })

      console.log('[Analyzer] AI 响应长度:', response.content.length)

      // 解析 JSON 响应 (多重容错)
      let jsonMatch = response.content.match(/\{[\s\S]*\}/)
      if (!jsonMatch) {
        // 尝试清理 Markdown 代码块
        const cleaned = response.content
          .replace(/```json\n?/g, '')
          .replace(/```\n?/g, '')
          .trim()
        jsonMatch = cleaned.match(/\{[\s\S]*\}/)
      }

      if (!jsonMatch) {
        throw new Error('AI 返回格式错误,无法解析 JSON')
      }

      const raw: RawAnalysisResponse = JSON.parse(jsonMatch[0])

      console.log('[Analyzer] 解析成功:',
        '价值观:', raw.values?.length || 0,
        '性格:', raw.personality?.length || 0,
        '关注方向:', raw.focusAreas?.length || 0
      )

      // 转换为标准格式
      return this.normalizeResult(raw, newMessages)
    } catch (error: any) {
      console.error('[Analyzer] 分析失败:', error)
      throw new Error(`分析失败: ${error.message}`)
    }
  }

  /**
   * 标准化 AI 返回结果
   */
  private normalizeResult(
    raw: RawAnalysisResponse,
    messages: ConversationMessage[]
  ): AnalysisResult {
    const now = new Date()

    // 处理价值观
    const values: Value[] = (raw.values || []).map((v, i) => ({
      id: `value-${Date.now()}-${i}`,
      name: v.name || '未命名',
      description: v.description || '',
      priority: Math.min(1, Math.max(0, v.priority || 0.5)),
      confidence: Math.min(1, Math.max(0, v.confidence || 0.5)),
      examples: v.examples || [],
      lastValidated: now
    }))

    // 处理性格特质
    const personality: PersonalityTrait[] = (raw.personality || []).map((p, i) => ({
      id: `trait-${Date.now()}-${i}`,
      name: p.name || 'unknown',
      displayName: p.displayName || p.name,
      score: Math.min(1, Math.max(0, p.score || 0.5)),
      description: p.description || '',
      confidence: Math.min(1, Math.max(0, p.confidence || 0.5)),
      indicators: p.indicators || []
    }))

    // 处理关注方向
    const focusAreas: FocusArea[] = (raw.focusAreas || []).map((f, i) => ({
      id: `focus-${Date.now()}-${i}`,
      topic: f.topic || '未命名',
      category: f.category || 'personal',
      intensity: Math.min(1, Math.max(0, f.intensity || 0.5)),
      trending: f.trending || 'stable',
      since: now
    }))

    return {
      values,
      personality,
      focusAreas,
      confidence: Math.min(1, Math.max(0, raw.overallConfidence || 0.5))
    }
  }

  /**
   * 增量更新分身数据
   */
  updateSoul(soul: DigitalSoul, analysis: AnalysisResult): DigitalSoul {
    const updated = { ...soul }

    // 1. 合并价值观 (去重,按优先级排序,保留前10个)
    const existingValueNames = new Set(soul.foundation.values.map(v => v.name))
    const newValues = analysis.values.filter(v => !existingValueNames.has(v.name))
    updated.foundation.values = [...soul.foundation.values, ...newValues]
    updated.foundation.values.sort((a, b) => b.priority - a.priority)
    if (updated.foundation.values.length > 10) {
      updated.foundation.values = updated.foundation.values.slice(0, 10)
    }

    // 2. 更新性格特质 (加权平均)
    const personalityMap = new Map(soul.foundation.personality.map(p => [p.name, p]))
    analysis.personality.forEach(p => {
      const existing = personalityMap.get(p.name)
      if (existing) {
        // 加权平均: 新数据权重 0.3
        existing.score = existing.score * 0.7 + p.score * 0.3
        existing.confidence = Math.min(1, existing.confidence + 0.05)
        existing.description = p.description
        // 合并 indicators
        const allIndicators = new Set([...existing.indicators, ...p.indicators])
        existing.indicators = Array.from(allIndicators)
      } else {
        personalityMap.set(p.name, p)
      }
    })
    updated.foundation.personality = Array.from(personalityMap.values())

    // 3. 合并关注方向 (加权平均,保留前15个)
    const focusMap = new Map(soul.memories.focusAreas.map(f => [f.topic, f]))
    analysis.focusAreas.forEach(f => {
      const existing = focusMap.get(f.topic)
      if (existing) {
        existing.intensity = Math.min(1, existing.intensity * 0.8 + f.intensity * 0.2)
        existing.trending = f.intensity > existing.intensity ? 'up' :
          f.intensity < existing.intensity ? 'down' : 'stable'
      } else {
        focusMap.set(f.topic, f)
      }
    })
    updated.memories.focusAreas = Array.from(focusMap.values())
    updated.memories.focusAreas.sort((a, b) => b.intensity - a.intensity)
    if (updated.memories.focusAreas.length > 15) {
      updated.memories.focusAreas = updated.memories.focusAreas.slice(0, 15)
    }

    // 4. 更新元数据
    updated.version += 1
    updated.updatedAt = new Date()
    updated.metadata.lastUpdate = new Date()
    updated.metadata.dataSources.conversations += analysis.confidence

    // 5. 记录版本历史 (最多20条)
    updated.metadata.evolutionHistory.push({
      version: updated.version,
      timestamp: new Date(),
      changes: [
        `新增 ${newValues.length} 个价值观`,
        `更新 ${analysis.personality.length} 个性格特质`,
        `识别 ${analysis.focusAreas.length} 个关注方向`
      ],
      confidence: analysis.confidence
    })
    if (updated.metadata.evolutionHistory.length > 20) {
      updated.metadata.evolutionHistory = updated.metadata.evolutionHistory.slice(-20)
    }

    console.log('[Analyzer] 分身更新完成, 版本:', updated.version,
      '价值观:', updated.foundation.values.length,
      '性格:', updated.foundation.personality.length,
      '关注:', updated.memories.focusAreas.length)

    return updated
  }
}

// 导出单例
export const soulAnalyzer = new SoulAnalyzer()
