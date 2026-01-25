/**
 * 模式数据模型
 */

// ============================================
// 枚举类型
// ============================================

export enum PatternType {
  VALUE_BASED = 'value_based', // 价值观驱动
  DECISION_STYLE = 'decision_style', // 决策风格
  COMMUNICATION = 'communication', // 沟通模式
  WORKFLOW = 'workflow', // 工作流程
  EMOTIONAL_RESPONSE = 'emotional_response', // 情感反应
  LEARNING = 'learning', // 学习模式
  PROBLEM_SOLVING = 'problem_solving' // 问题解决
}

// ============================================
// 模式实例
// ============================================

export interface PatternInstance {
  id: string
  timestamp: Date
  context: string
  outcome: string
  confidence: number
  fragmentId: string // 关联的记忆片段
}

// ============================================
// 预测能力
// ============================================

export interface PredictionCapability {
  accuracy: number // 预测准确率
  sampleSize: number // 样本数量
  lastUpdated: Date
  correctPredictions: number
}

// ============================================
// 模式核心模型
// ============================================

export interface Pattern {
  id: string
  soulId: string
  type: PatternType
  name: string
  description: string
  confidence: number // 0-1，模式可信度
  frequency: number // 出现频率
  firstSeen: Date
  lastSeen: Date

  instances: PatternInstance[] // 具体案例
  relatedPatterns: string[] // 关联模式 ID

  prediction?: PredictionCapability // 预测能力

  // 元数据
  tags: string[]
  verified: boolean
  strength: 'weak' | 'moderate' | 'strong' // 模式强度
}

// ============================================
// 工厂函数
// ============================================

export function createPattern(
  soulId: string,
  type: PatternType,
  name: string,
  description: string
): Pattern {
  const now = new Date()

  return {
    id: `pattern-${Date.now()}-${Math.random().toString(36).substr(2, 9)}`,
    soulId,
    type,
    name,
    description,
    confidence: 0,
    frequency: 1,
    firstSeen: now,
    lastSeen: now,
    instances: [],
    relatedPatterns: [],
    tags: [],
    verified: false,
    strength: 'weak'
  }
}

// ============================================
// 模式匹配
// ============================================

export interface PatternMatch {
  pattern: Pattern
  similarity: number // 0-1
  confidence: number
  reasons: string[]
}

export function matchPattern(
  fragment: string,
  patterns: Pattern[]
): PatternMatch[] {
  // 简单的关键词匹配（实际应用中可以使用更复杂的算法）
  return patterns
    .map(p => {
      const keywords = p.name.toLowerCase().split(/\s+/)
      const fragmentLower = fragment.toLowerCase()

      const matchedKeywords = keywords.filter(kw => fragmentLower.includes(kw))
      const similarity = matchedKeywords.length / keywords.length

      return {
        pattern: p,
        similarity,
        confidence: similarity * p.confidence,
        reasons: matchedKeywords.map(kw => `包含关键词: "${kw}"`)
      }
    })
    .filter(m => m.similarity > 0.3) // 至少30%相似度
    .sort((a, b) => b.confidence - a.confidence)
}

// ============================================
// 模式强度计算
// ============================================

export function calculatePatternStrength(pattern: Pattern): 'weak' | 'moderate' | 'strong' {
  if (pattern.frequency < 3 || pattern.confidence < 0.4) {
    return 'weak'
  } else if (pattern.frequency < 7 || pattern.confidence < 0.7) {
    return 'moderate'
  } else {
    return 'strong'
  }
}

// ============================================
// 模式更新
// ============================================

export function addPatternInstance(
  pattern: Pattern,
  instance: PatternInstance
): Pattern {
  const updated = { ...pattern }

  updated.instances = [...updated.instances, instance]
  updated.frequency = updated.instances.length
  updated.lastSeen = instance.timestamp

  // 重新计算置信度（基于实例数量和一致性）
  const avgConfidence =
    updated.instances.reduce((sum, i) => sum + i.confidence, 0) / updated.instances.length
  updated.confidence = Math.min(avgConfidence * (1 + Math.log10(updated.frequency) / 10), 1)

  // 更新强度
  updated.strength = calculatePatternStrength(updated)

  return updated
}

// ============================================
// 模式关系
// ============================================

export function findRelatedPatterns(
  pattern: Pattern,
  allPatterns: Pattern[],
  threshold: number = 0.5
): string[] {
  const related: string[] = []

  allPatterns.forEach(other => {
    if (other.id === pattern.id) return

    // 基于标签相似度
    const commonTags = pattern.tags.filter(t => other.tags.includes(t))
    const tagSimilarity = commonTags.length / Math.max(pattern.tags.length, other.tags.length)

    // 基于类型
    const typeMatch = pattern.type === other.type ? 0.3 : 0

    const totalSimilarity = tagSimilarity + typeMatch

    if (totalSimilarity >= threshold) {
      related.push(other.id)
    }
  })

  return related
}

// ============================================
// 模式验证
// ============================================

export function verifyPattern(
  pattern: Pattern,
  newInstances: PatternInstance[],
  minAccuracy: number = 0.7
): { verified: boolean; accuracy: number } {
  if (pattern.instances.length < 5) {
    return { verified: false, accuracy: pattern.confidence }
  }

  // 基于预测准确性验证
  const correctPredictions = newInstances.filter(instance => {
    // 简化验证：检查置信度是否足够高
    return instance.confidence >= minAccuracy
  }).length

  const accuracy = newInstances.length > 0 ? correctPredictions / newInstances.length : pattern.confidence

  return {
    verified: accuracy >= minAccuracy,
    accuracy
  }
}

// ============================================
// 模式演化
// ============================================

export interface PatternEvolution {
  patternId: string
  oldStrength: string
  newStrength: string
  oldConfidence: number
  newConfidence: number
  oldFrequency: number
  newFrequency: number
  timestamp: Date
}

export function trackPatternEvolution(
  oldPattern: Pattern,
  newPattern: Pattern
): PatternEvolution {
  return {
    patternId: oldPattern.id,
    oldStrength: oldPattern.strength,
    newStrength: newPattern.strength,
    oldConfidence: oldPattern.confidence,
    newConfidence: newPattern.confidence,
    oldFrequency: oldPattern.frequency,
    newFrequency: newPattern.frequency,
    timestamp: new Date()
  }
}
