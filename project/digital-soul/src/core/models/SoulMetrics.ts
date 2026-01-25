/**
 * 评估指标数据模型
 */

import { DigitalSoul } from './DigitalSoul'
import { MemoryFragment, FragmentStatistics } from './MemoryFragment'

// ============================================
// 稳定性评分
// ============================================

export interface StabilityDetails {
  valueStability: number // 价值观稳定性
  traitStability: number // 特质稳定性
  patternStability: number // 模式稳定性
}

export interface StabilityScore {
  score: number // 0-1，总稳定性
  details: {
    valueStability: number
    traitStability: number
    patternStability: number
  }
}

export function calculateStability(soul: DigitalSoul): StabilityScore {
  const history = soul.metadata.evolutionHistory
  const recentVersions = history.slice(-10)

  if (recentVersions.length < 2) {
    return {
      score: 0,
      details: {
        valueStability: 0,
        traitStability: 0,
        patternStability: 0
      }
    }
  }

  // 计算特征变化的方差（简化版）
  const valueStability = computeVariance(
    recentVersions.map(v => v.confidence)
  )

  const traitStability = valueStability // 简化，实际应该分别计算
  const patternStability = valueStability // 简化

  const score = 1 - (valueStability + traitStability + patternStability) / 3

  return {
    score,
    details: {
      valueStability: 1 - valueStability,
      traitStability: 1 - traitStability,
      patternStability: 1 - patternStability
    }
  }
}

// ============================================
// 一致性评分
// ============================================

export interface Validation {
  context: string
  predicted: string
  actual: string
  match: boolean
}

export interface ConsistencyScore {
  score: number // 0-1
  validationCases: number
  matchRate: number
  validations: Validation[]
}

export async function calculateConsistency(
  soul: DigitalSoul,
  fragments: MemoryFragment[]
): Promise<ConsistencyScore> {
  const validations: Validation[] = []
  let matchCount = 0

  // 简化版：检查已验证的片段
  const validatedFragments = fragments.filter(f => f.validated && f.feedback)

  validatedFragments.forEach(f => {
    if (!f.feedback) return

    const match = f.feedback.accuracy >= 3 // 3分以上认为一致

    validations.push({
      context: f.context.scenario,
      predicted: 'predicted', // 实际应该从预测记录中获取
      actual: f.userAction.choice,
      match
    })

    if (match) matchCount++
  })

  return {
    score: validations.length > 0 ? matchCount / validations.length : 0,
    validationCases: validations.length,
    matchRate: validations.length > 0 ? matchCount / validations.length : 0,
    validations
  }
}

// ============================================
// 完整性评分
// ============================================

export interface CompletenessScore {
  score: number // 0-1
  coverage: Map<string, number> // 各维度覆盖度
  gaps: string[] // 未知领域
  totalDimensions: number
  coveredDimensions: number
}

export function calculateCompleteness(soul: DigitalSoul): CompletenessScore {
  const coverage = new Map<string, number>()

  // 各维度覆盖度计算
  const dimensions = [
    { name: 'values', coverage: soul.foundation.values.length > 0 ? 1 : 0 },
    { name: 'personality', coverage: soul.foundation.personality.length > 0 ? 1 : 0 },
    { name: 'thinkingPatterns', coverage: soul.foundation.thinkingPatterns.length > 0 ? 1 : 0 },
    { name: 'decisionPrinciples', coverage: soul.foundation.decisionPrinciples.length > 0 ? 1 : 0 },
    { name: 'interactions', coverage: soul.memories.interactions.length > 0 ? 1 : 0 },
    { name: 'behaviors', coverage: soul.memories.behaviors.length > 0 ? 1 : 0 },
    { name: 'focusAreas', coverage: soul.memories.focusAreas.length > 0 ? 1 : 0 }
  ]

  dimensions.forEach(d => {
    coverage.set(d.name, d.coverage)
  })

  const totalCoverage = Array.from(coverage.values()).reduce((sum, v) => sum + v, 0)
  const score = totalCoverage / dimensions.length

  // 识别空白领域
  const gaps = dimensions.filter(d => d.coverage === 0).map(d => d.name)

  return {
    score,
    coverage,
    gaps,
    totalDimensions: dimensions.length,
    coveredDimensions: dimensions.filter(d => d.coverage > 0).length
  }
}

// ============================================
// 准确性评分
// ============================================

export interface PredictionRecord {
  context: string
  predicted: string
  actual: string
  wasCorrect: boolean
  confidence: number
  timestamp: Date
}

export interface AccuracyScore {
  score: number // 0-1
  totalSimulations: number
  correctPredictions: number
  trend: number[] // 历史趋势
  recentPredictions: PredictionRecord[]
}

export function trackAccuracy(
  soul: DigitalSoul,
  newPredictions?: PredictionRecord[]
): AccuracyScore {
  // 从演进历史中提取预测记录（简化版）
  const predictions: PredictionRecord[] = []

  // 如果有新预测，添加进去
  if (newPredictions) {
    predictions.push(...newPredictions)
  }

  const correctPredictions = predictions.filter(p => p.wasCorrect).length
  const score = predictions.length > 0 ? correctPredictions / predictions.length : 0

  // 计算趋势（最近20次预测）
  const recentPredictions = predictions.slice(-20)
  const trend = recentPredictions.map((_, i) => {
    const slice = recentPredictions.slice(0, i + 1)
    return slice.length > 0 ? slice.filter(p => p.wasCorrect).length / slice.length : 0
  })

  return {
    score,
    totalSimulations: predictions.length,
    correctPredictions,
    trend,
    recentPredictions: recentPredictions.slice(-10) // 保留最近10条
  }
}

// ============================================
// 综合指标
// ============================================

export interface SoulMetrics {
  stability: StabilityScore
  consistency: ConsistencyScore
  completeness: CompletenessScore
  accuracy: AccuracyScore

  lastUpdated: Date
  overallScore: number // 综合得分
}

export function calculateOverallMetrics(
  soul: DigitalSoul,
  fragments: MemoryFragment[]
): SoulMetrics {
  const stability = calculateStability(soul)

  // 一致性需要异步计算，这里先给个默认值
  const consistency: ConsistencyScore = {
    score: soul.metadata.metrics.consistency.score,
    validationCases: soul.metadata.metrics.consistency.validationCases,
    matchRate: soul.metadata.metrics.consistency.matchRate,
    validations: soul.metadata.metrics.consistency.validations
  }

  const completeness = calculateCompleteness(soul)

  const accuracy = trackAccuracy(soul)

  // 综合得分（加权平均）
  const weights = {
    stability: 0.3,
    consistency: 0.3,
    completeness: 0.2,
    accuracy: 0.2
  }

  const overallScore =
    stability.score * weights.stability +
    consistency.score * weights.consistency +
    completeness.score * weights.completeness +
    accuracy.score * weights.accuracy

  return {
    stability,
    consistency,
    completeness,
    accuracy,
    lastUpdated: new Date(),
    overallScore
  }
}

// ============================================
// 辅助函数
// ============================================

function computeVariance(values: number[]): number {
  if (values.length === 0) return 0

  const mean = values.reduce((sum, v) => sum + v, 0) / values.length
  const squaredDiffs = values.map(v => Math.pow(v - mean, 2))
  return squaredDiffs.reduce((sum, d) => sum + d, 0) / values.length
}

// ============================================
// 指标比较
// ============================================

export interface MetricsComparison {
  stability: { old: number; new: number; change: number }
  consistency: { old: number; new: number; change: number }
  completeness: { old: number; new: number; change: number }
  accuracy: { old: number; new: number; change: number }
  overall: { old: number; new: number; change: number }
}

export function compareMetrics(
  oldMetrics: SoulMetrics,
  newMetrics: SoulMetrics
): MetricsComparison {
  return {
    stability: {
      old: oldMetrics.stability.score,
      new: newMetrics.stability.score,
      change: newMetrics.stability.score - oldMetrics.stability.score
    },
    consistency: {
      old: oldMetrics.consistency.score,
      new: newMetrics.consistency.score,
      change: newMetrics.consistency.score - oldMetrics.consistency.score
    },
    completeness: {
      old: oldMetrics.completeness.score,
      new: newMetrics.completeness.score,
      change: newMetrics.completeness.score - oldMetrics.completeness.score
    },
    accuracy: {
      old: oldMetrics.accuracy.score,
      new: newMetrics.accuracy.score,
      change: newMetrics.accuracy.score - oldMetrics.accuracy.score
    },
    overall: {
      old: oldMetrics.overallScore,
      new: newMetrics.overallScore,
      change: newMetrics.overallScore - oldMetrics.overallScore
    }
  }
}
