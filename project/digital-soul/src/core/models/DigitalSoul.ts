/**
 * 数字分身核心数据模型
 */

// ============================================
// 基础类型定义
// ============================================

export interface Value {
  id: string
  name: string
  description: string
  priority: number // 0-1，越大越重要
  confidence: number // 0-1，AI的置信度
  examples: string[] // 具体案例
  lastValidated: Date
}

export interface PersonalityTrait {
  id: string
  name: string // 'openness' | 'conscientiousness' | 'extraversion' | 'agreeableness' | 'neuroticism'
  displayName: string
  score: number // 0-1
  description: string
  confidence: number
  indicators: string[] // 行为指标
}

export interface ThinkingPattern {
  id: string
  name: string
  description: string
  frequency: number // 出现频率
  contexts: string[] // 适用场景
  examples: string[]
}

export interface DecisionPrinciple {
  id: string
  name: string
  description: string
  priority: number
  applicationCount: number // 应用次数
  successRate: number // 成功率
}

export interface MentalModel {
  id: string
  name: string
  description: string
  source: string // 来源
  usage: string[] // 使用场景
}

// ============================================
// 基础画像（相对稳定，慢速演变）
// ============================================

export interface SoulFoundation {
  values: Value[] // 价值观（优先级排序）
  personality: PersonalityTrait[] // 性格特征（五大人格特质）
  thinkingPatterns: ThinkingPattern[] // 思维模式
  decisionPrinciples: DecisionPrinciple[] // 决策原则
  mentalModels: MentalModel[] // 心智模型
}

// ============================================
// 动态记忆（持续更新）
// ============================================

export interface Interaction {
  id: string
  timestamp: Date
  scenario: string
  outcome: string
  userChoice: string
  alternatives: string[]
  reasoning?: string
}

export interface Behavior {
  id: string
  type: string
  description: string
  frequency: number
  lastOccurrence: Date
  triggers: string[]
}

export interface FocusArea {
  id: string
  topic: string
  category: string // 'work' | 'personal' | 'learning' | etc.
  intensity: number // 0-1，关注强度
  trending: 'up' | 'down' | 'stable'
  since: Date
}

export interface WorkHabits {
  peakHours: number[] // 高效时段（0-23）
  breakPreferences: string
  collaborationStyle: string
  environment: string
  tools: string[]
}

export interface LanguageStyle {
  formality: number // 0-1，0=非正式，1=正式
  verbosity: number // 0-1，0=简洁，1=详细
  tone: string[] // 'friendly' | 'professional' | 'casual' | etc.
  commonPhrases: string[]
  emojiUsage: 'never' | 'rarely' | 'sometimes' | 'frequently'
}

export interface SoulMemories {
  interactions: Interaction[]
  behaviors: Behavior[]
  focusAreas: FocusArea[]
  workHabits: WorkHabits
  languageStyle: LanguageStyle
}

// ============================================
// 元数据
// ============================================

export interface SoulVersion {
  version: number
  timestamp: Date
  changes: string[] // 变更描述
  confidence: number
  snapshotId?: string
}

export interface DataSourceStats {
  conversations: number
  manualEntries: number
  imports: number
  lastSync?: Date
}

// ============================================
// 评估指标（定义在 SoulMetrics.ts）
// ============================================

export interface SoulMetadata {
  confidenceScores: Map<string, number>
  evolutionHistory: SoulVersion[]
  lastUpdate: Date
  dataSources: DataSourceStats
  metrics: {
    stability: {
      score: number
      details: {
        valueStability: number
        traitStability: number
        patternStability: number
      }
    }
    consistency: {
      score: number
      validationCases: number
      matchRate: number
      validations: any[]
    }
    completeness: {
      score: number
      coverage: Map<string, number>
      gaps: string[]
    }
    accuracy: {
      score: number
      totalSimulations: number
      correctPredictions: number
      trend: number[]
    }
  }
}

// ============================================
// 数字分身核心模型
// ============================================

export interface DigitalSoul {
  id: string
  userId: string
  version: number
  createdAt: Date
  updatedAt: Date

  foundation: SoulFoundation
  memories: SoulMemories
  metadata: SoulMetadata
}

// ============================================
// 工厂函数
// ============================================

export function createDefaultSoul(userId: string = 'default'): DigitalSoul {
  const now = new Date()

  return {
    id: `soul-${userId}-${Date.now()}`,
    userId,
    version: 1,
    createdAt: now,
    updatedAt: now,

    foundation: {
      values: [],
      personality: [],
      thinkingPatterns: [],
      decisionPrinciples: [],
      mentalModels: []
    },

    memories: {
      interactions: [],
      behaviors: [],
      focusAreas: [],
      workHabits: {
        peakHours: [9, 10, 11, 14, 15, 16],
        breakPreferences: 'pomodoro',
        collaborationStyle: 'async',
        environment: 'quiet',
        tools: []
      },
      languageStyle: {
        formality: 0.5,
        verbosity: 0.5,
        tone: ['professional'],
        commonPhrases: [],
        emojiUsage: 'rarely'
      }
    },

    metadata: {
      confidenceScores: new Map(),
      evolutionHistory: [],
      lastUpdate: now,
      dataSources: {
        conversations: 0,
        manualEntries: 0,
        imports: 0
      },
      metrics: {
        stability: {
          score: 0,
          details: {
            valueStability: 0,
            traitStability: 0,
            patternStability: 0
          }
        },
        consistency: {
          score: 0,
          validationCases: 0,
          matchRate: 0,
          validations: []
        },
        completeness: {
          score: 0,
          coverage: new Map(),
          gaps: []
        },
        accuracy: {
          score: 0,
          totalSimulations: 0,
          correctPredictions: 0,
          trend: []
        }
      }
    }
  }
}

// ============================================
// 序列化/反序列化
// ============================================

export function serializeSoul(soul: DigitalSoul): string {
  return JSON.stringify(soul, (key, value) => {
    if (value instanceof Map) {
      return { __type: 'Map', value: Array.from(value.entries()) }
    }
    if (value instanceof Date) {
      return { __type: 'Date', value: value.toISOString() }
    }
    return value
  })
}

export function deserializeSoul(json: string): DigitalSoul {
  return JSON.parse(json, (key, value) => {
    if (value && value.__type === 'Map') {
      return new Map(value.value)
    }
    if (value && value.__type === 'Date') {
      return new Date(value.value)
    }
    return value
  })
}
