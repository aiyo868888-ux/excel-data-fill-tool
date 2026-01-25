/**
 * 记忆片段数据模型
 */

import { Value, PersonalityTrait, ThinkingPattern } from './DigitalSoul'

// ============================================
// 枚举类型
// ============================================

export enum FragmentType {
  INTERACTION = 'interaction', // 用户交互
  DECISION = 'decision', // 决策记录
  BEHAVIOR = 'behavior', // 行为观察
  EXPRESSION = 'expression', // 观点表达
  REFLECTION = 'reflection' // 自我反思
}

// ============================================
// 情绪检测
// ============================================

export interface Emotion {
  type: string // 'joy' | 'anger' | 'sadness' | 'fear' | 'surprise' | 'disgust' | 'neutral'
  intensity: number // 0-1
  confidence: number
  triggers?: string[]
}

// ============================================
// 上下文信息
// ============================================

export interface FragmentContext {
  scenario: string // 场景描述
  participants: string[] // 参与者
  environment: string // 环境因素
  timeConstraints?: string // 时间限制
  resources?: string[] // 可用资源
}

// ============================================
// 用户行为
// ============================================

export interface UserAction {
  choice: string // 用户的决定
  alternatives: string[] // 备选方案
  reasoning?: string // 用户解释
  timeSpent: number // 决策时长（毫秒）
  hesitationLevel?: number // 犹豫程度 0-1
  confidence?: number // 决策置信度 0-1
}

// ============================================
// AI 提取结果
// ============================================

export interface ExtractionResult {
  values: Value[] // 提取的价值观
  traits: PersonalityTrait[] // 性格特质
  patterns: ThinkingPattern[] // 思维模式
  emotions: Emotion[] // 情绪反应
  confidence: number // AI 置信度
  modelUsed: string // 使用的模型
  timestamp: Date
}

// ============================================
// 用户反馈
// ============================================

export interface FragmentFeedback {
  accuracy: number // 准确性评分（1-5）
  corrections: string[] // 修正内容
  userNotes?: string // 用户备注
  timestamp: Date
}

// ============================================
// 记忆片段核心模型
// ============================================

export interface MemoryFragment {
  id: string
  soulId: string
  timestamp: Date
  type: FragmentType

  context: FragmentContext
  userAction: UserAction
  extraction: ExtractionResult

  feedback?: FragmentFeedback

  // 元数据
  processed: boolean // 是否已处理
  validated: boolean // 是否已验证
  tags: string[] // 标签
  relatedFragments: string[] // 关联的记忆片段 ID
}

// ============================================
// 工厂函数
// ============================================

export function createMemoryFragment(
  soulId: string,
  type: FragmentType,
  context: FragmentContext,
  userAction: UserAction
): MemoryFragment {
  return {
    id: `fragment-${Date.now()}-${Math.random().toString(36).substr(2, 9)}`,
    soulId,
    timestamp: new Date(),
    type,
    context,
    userAction,
    extraction: {
      values: [],
      traits: [],
      patterns: [],
      emotions: [],
      confidence: 0,
      modelUsed: '',
      timestamp: new Date()
    },
    processed: false,
    validated: false,
    tags: [],
    relatedFragments: []
  }
}

// ============================================
// 查询和过滤
// ============================================

export interface FragmentFilter {
  type?: FragmentType
  startDate?: Date
  endDate?: Date
  minConfidence?: number
  tags?: string[]
  hasFeedback?: boolean
  validated?: boolean
}

export function filterFragments(
  fragments: MemoryFragment[],
  filter: FragmentFilter
): MemoryFragment[] {
  return fragments.filter(f => {
    if (filter.type && f.type !== filter.type) return false
    if (filter.startDate && f.timestamp < filter.startDate) return false
    if (filter.endDate && f.timestamp > filter.endDate) return false
    if (filter.minConfidence && f.extraction.confidence < filter.minConfidence) return false
    if (filter.tags && !filter.tags.some(t => f.tags.includes(t))) return false
    if (filter.hasFeedback !== undefined && (f.feedback === undefined) !== filter.hasFeedback) return false
    if (filter.validated !== undefined && f.validated !== filter.validated) return false
    return true
  })
}

// ============================================
// 统计和分析
// ============================================

export interface FragmentStatistics {
  total: number
  byType: Map<FragmentType, number>
  averageConfidence: number
  validatedCount: number
  withFeedbackCount: number
  topTags: Array<{ tag: string; count: number }>
}

export function calculateFragmentStats(fragments: MemoryFragment[]): FragmentStatistics {
  const byType = new Map<FragmentType, number>()
  const tagCounts = new Map<string, number>()

  let totalConfidence = 0
  let validatedCount = 0
  let withFeedbackCount = 0

  fragments.forEach(f => {
    // 按类型统计
    byType.set(f.type, (byType.get(f.type) || 0) + 1)

    // 置信度总和
    totalConfidence += f.extraction.confidence

    // 已验证
    if (f.validated) validatedCount++

    // 有反馈
    if (f.feedback) withFeedbackCount++

    // 标签统计
    f.tags.forEach(tag => {
      tagCounts.set(tag, (tagCounts.get(tag) || 0) + 1)
    })
  })

  // Top tags
  const topTags = Array.from(tagCounts.entries())
    .map(([tag, count]) => ({ tag, count }))
    .sort((a, b) => b.count - a.count)
    .slice(0, 10)

  return {
    total: fragments.length,
    byType,
    averageConfidence: fragments.length > 0 ? totalConfidence / fragments.length : 0,
    validatedCount,
    withFeedbackCount,
    topTags
  }
}
