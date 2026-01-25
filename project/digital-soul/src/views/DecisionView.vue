<template>
  <div class="decision-view">
    <el-row :gutter="24">
      <el-col :span="24">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>决策模拟</span>
              <el-button type="primary" @click="handleNewSimulation">新建模拟</el-button>
            </div>
          </template>

          <el-empty v-if="simulations.length === 0" description="暂无决策模拟">
            <el-button type="primary" @click="handleNewSimulation">创建第一个模拟</el-button>
          </el-empty>

          <div v-else>
            <el-timeline>
              <el-timeline-item
                v-for="sim in simulations"
                :key="sim.id"
                :timestamp="formatDate(sim.timestamp)"
                placement="top"
              >
                <el-card>
                  <h4>{{ sim.scenario }}</h4>
                  <p class="scenario-desc">{{ sim.description }}</p>
                  <div class="decision-details">
                    <el-descriptions :column="2" size="small" border>
                      <el-descriptions-item label="决策风格">
                        <el-tag size="small">{{ sim.decisionStyle }}</el-tag>
                      </el-descriptions-item>
                      <el-descriptions-item label="置信度">
                        <el-progress
                          :percentage="sim.confidence * 100"
                          :color="getConfidenceColor(sim.confidence)"
                          :show-text="false"
                          style="width: 100px"
                        />
                        <span style="margin-left: 8px">{{ (sim.confidence * 100).toFixed(0) }}%</span>
                      </el-descriptions-item>
                    </el-descriptions>
                  </div>
                  <div class="suggestions">
                    <div class="suggestion-title">建议方案：</div>
                    <ul>
                      <li v-for="(suggestion, idx) in sim.suggestions" :key="idx">
                        {{ suggestion }}
                      </li>
                    </ul>
                  </div>
                </el-card>
              </el-timeline-item>
            </el-timeline>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 新建模拟对话框 -->
    <el-dialog v-model="dialogVisible" title="新建决策模拟" width="600px">
      <el-form :model="form" label-width="100px">
        <el-form-item label="场景描述">
          <el-input
            v-model="form.scenario"
            placeholder="例如：是否接受这份工作offer"
            maxlength="100"
            show-word-limit
          />
        </el-form-item>
        <el-form-item label="详细情况">
          <el-input
            v-model="form.description"
            type="textarea"
            :rows="4"
            placeholder="详细描述决策背景、选项、考虑因素等..."
          />
        </el-form-item>
        <el-form-item label="决策风格">
          <el-select v-model="form.decisionStyle" placeholder="选择风格" style="width: 100%">
            <el-option label="理性分析" value="rational" />
            <el-option label="情感导向" value="emotional" />
            <el-option label="风险规避" value="risk-averse" />
            <el-option label="冒险探索" value="adventurous" />
            <el-option label="平衡型" value="balanced" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitting">
          提交分析
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { ElMessage } from 'element-plus'

interface Simulation {
  id: string
  scenario: string
  description: string
  decisionStyle: string
  confidence: number
  suggestions: string[]
  timestamp: number
}

const simulations = ref<Simulation[]>([])
const dialogVisible = ref(false)
const submitting = ref(false)

const form = ref({
  scenario: '',
  description: '',
  decisionStyle: 'balanced'
})

const handleNewSimulation = () => {
  form.value = {
    scenario: '',
    description: '',
    decisionStyle: 'balanced'
  }
  dialogVisible.value = true
}

const handleSubmit = async () => {
  if (!form.value.scenario || !form.value.description) {
    ElMessage.warning('请填写完整信息')
    return
  }

  submitting.value = true

  try {
    // 调用 AI 分析
    const { aiService } = await import('@/services/ai')
    const prompt = `作为决策顾问，请分析以下决策场景：

场景：${form.value.scenario}
详细情况：${form.value.description}
决策风格：${form.value.decisionStyle}

请提供：
1. 置信度评估（0-1之间的数值）
2. 3-5个具体的建议方案

请以JSON格式回复：
{
  "confidence": 0.8,
  "suggestions": ["建议1", "建议2", "建议3"]
}`

    const response = await aiService.chat([
      { role: 'user', content: prompt }
    ])

    // 解析 AI 回复
    let analysis: any
    try {
      const jsonMatch = response.content.match(/\{[\s\S]*\}/)
      if (jsonMatch) {
        analysis = JSON.parse(jsonMatch[0])
      } else {
        throw new Error('无法解析AI回复')
      }
    } catch (e) {
      // 如果解析失败，使用默认值
      analysis = {
        confidence: 0.7,
        suggestions: [response.content.slice(0, 100) + '...']
      }
    }

    const simulation: Simulation = {
      id: `sim-${Date.now()}`,
      scenario: form.value.scenario,
      description: form.value.description,
      decisionStyle: form.value.decisionStyle,
      confidence: analysis.confidence || 0.7,
      suggestions: analysis.suggestions || ['建议1：仔细权衡利弊', '建议2：考虑长期影响', '建议3：咨询他人意见'],
      timestamp: Date.now()
    }

    simulations.value.unshift(simulation)
    dialogVisible.value = false
    ElMessage.success('决策分析完成')
  } catch (error: any) {
    ElMessage.error(`分析失败: ${error.message}`)
  } finally {
    submitting.value = false
  }
}

const formatDate = (timestamp: number) => {
  return new Date(timestamp).toLocaleString('zh-CN')
}

const getConfidenceColor = (confidence: number) => {
  if (confidence >= 0.8) return '#67c23a'
  if (confidence >= 0.5) return '#e6a23c'
  return '#f56c6c'
}
</script>

<style scoped>
.decision-view {
  padding: 24px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.scenario-desc {
  color: #666;
  margin: 8px 0 12px 0;
}

.decision-details {
  margin: 12px 0;
}

.suggestions {
  margin-top: 12px;
  padding: 12px;
  background: #f5f7fa;
  border-radius: 4px;
}

.suggestion-title {
  font-weight: 500;
  margin-bottom: 8px;
  color: #303133;
}

.suggestions ul {
  margin: 0;
  padding-left: 20px;
}

.suggestions li {
  margin: 4px 0;
  color: #606266;
  line-height: 1.6;
}
</style>
