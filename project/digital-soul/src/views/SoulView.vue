<template>
  <div class="soul-view">
      <el-row :gutter="24">
        <el-col :span="24">
          <el-card>
            <template #header>
              <div class="card-header">
                <span>数字分身画像</span>
                <div>
                  <el-button
                    type="primary"
                    size="small"
                    @click="handleReanalyze"
                    :disabled="messagesCount < soulAnalyzer.ANALYSIS_THRESHOLD"
                  >
                    重新分析
                  </el-button>
                  <el-button type="primary" size="small" @click="handleRefresh">刷新</el-button>
                </div>
              </div>
            </template>

            <el-empty v-if="!soul" description="暂无分身数据" />

            <!-- 数据来源提示 -->
            <el-alert
              v-if="soul && soul.metadata.dataSources.conversations === 0"
              type="info"
              :closable="false"
              style="margin-bottom: 16px"
            >
              开始对话后,系统将自动分析并更新你的分身画像 (每5条消息分析一次)
            </el-alert>

            <!-- 数据来源统计 -->
            <el-alert
              v-if="soul && soul.metadata.dataSources.conversations > 0"
              type="success"
              :closable="false"
              style="margin-bottom: 16px"
            >
              基于对话分析 | 数据源: {{ soul.metadata.dataSources.conversations.toFixed(1) }} | 版本: v{{ soul.version }}
            </el-alert>

            <div v-if="soul">
              <el-descriptions :column="2" border>
                <el-descriptions-item label="ID">{{ soul.id }}</el-descriptions-item>
                <el-descriptions-item label="版本">v{{ soul.version }}</el-descriptions-item>
                <el-descriptions-item label="创建时间">
                  {{ formatDate(soul.createdAt) }}
                </el-descriptions-item>
                <el-descriptions-item label="更新时间">
                  {{ formatDate(soul.updatedAt) }}
                </el-descriptions-item>
              </el-descriptions>

              <el-divider />

              <el-tabs v-model="activeTab">
                <el-tab-pane label="核心特征" name="core">
                  <el-row :gutter="24">
                    <!-- 基本信息 -->
                    <el-col :span="24">
                      <el-card class="info-card" shadow="never">
                        <template #header>
                          <div class="card-title">基本信息</div>
                        </template>
                        <el-descriptions :column="3" border>
                          <el-descriptions-item label="用户ID">{{ soul.userId }}</el-descriptions-item>
                          <el-descriptions-item label="分身ID">{{ soul.id.slice(-8) }}</el-descriptions-item>
                          <el-descriptions-item label="版本">
                            <el-tag type="success">v{{ soul.version }}</el-tag>
                          </el-descriptions-item>
                          <el-descriptions-item label="创建时间">{{ formatDate(soul.createdAt) }}</el-descriptions-item>
                          <el-descriptions-item label="更新时间">{{ formatDate(soul.updatedAt) }}</el-descriptions-item>
                          <el-descriptions-item label="数据源">
                            <el-tag v-if="soul.metadata.dataSources.conversations > 0" type="primary">
                              AI 分析 ({{ soul.metadata.dataSources.conversations.toFixed(1) }})
                            </el-tag>
                            <el-tag v-else type="info">默认数据</el-tag>
                          </el-descriptions-item>
                        </el-descriptions>
                      </el-card>
                    </el-col>

                    <!-- 价值观 -->
                    <el-col :span="24">
                      <el-card class="info-card" shadow="never">
                        <template #header>
                          <div class="card-title">价值观 ({{ soul.foundation.values.length }})</div>
                        </template>
                        <div v-if="soul.foundation.values.length === 0">
                          <el-empty description="暂无价值观数据,开始对话后将自动分析" />
                        </div>
                        <div v-else class="values-grid">
                          <div
                            v-for="value in soul.foundation.values"
                            :key="value.id"
                            class="value-card"
                            :style="{ borderLeftColor: getColorByPriority(value.priority) }"
                          >
                            <div class="value-header">
                              <span class="value-name" :style="{ fontSize: 14 + value.priority * 3 + 'px' }">
                                {{ value.name }}
                              </span>
                              <el-tag size="small" :type="getPriorityType(value.priority)">
                                {{ (value.priority * 100).toFixed(0) }}%
                              </el-tag>
                            </div>
                            <div class="value-desc">{{ value.description }}</div>
                            <div v-if="value.examples && value.examples.length > 0" class="value-examples">
                              <div class="examples-label">案例:</div>
                              <div v-for="(example, idx) in value.examples.slice(0, 2)" :key="idx" class="example-item">
                                "{{ example }}"
                              </div>
                            </div>
                          </div>
                        </div>
                      </el-card>
                    </el-col>

                    <!-- 性格特质 -->
                    <el-col :span="24">
                      <el-card class="info-card" shadow="never">
                        <template #header>
                          <div class="card-title">性格特质 ({{ soul.foundation.personality.length }})</div>
                        </template>
                        <div v-if="soul.foundation.personality.length === 0">
                          <el-empty description="暂无性格数据,开始对话后将自动分析" />
                        </div>
                        <div v-else class="personality-grid">
                          <div
                            v-for="trait in soul.foundation.personality"
                            :key="trait.id"
                            class="personality-card"
                          >
                            <div class="personality-header">
                              <span class="personality-name">{{ trait.displayName }}</span>
                              <span class="personality-score">{{ (trait.score * 100).toFixed(0) }}%</span>
                            </div>
                            <el-progress
                              :percentage="trait.score * 100"
                              :color="getScoreColor(trait.score)"
                              :show-text="false"
                            />
                            <div class="personality-desc">{{ trait.description }}</div>
                            <div v-if="trait.indicators && trait.indicators.length > 0" class="personality-indicators">
                              <div v-for="(indicator, idx) in trait.indicators.slice(0, 3)" :key="idx" class="indicator-tag">
                                {{ indicator }}
                              </div>
                            </div>
                          </div>
                        </div>
                      </el-card>
                    </el-col>

                    <!-- 关注方向 -->
                    <el-col :span="24">
                      <el-card class="info-card" shadow="never">
                        <template #header>
                          <div class="card-title">关注方向 ({{ soul.memories.focusAreas.length }})</div>
                        </template>
                        <div v-if="soul.memories.focusAreas.length === 0">
                          <el-empty description="暂无关注方向数据,开始对话后将自动分析" />
                        </div>
                        <div v-else>
                          <el-table :data="soul.memories.focusAreas" style="width: 100%">
                            <el-table-column prop="topic" label="主题" width="150">
                              <template #default="scope">
                                <el-tag>{{ scope.row.topic }}</el-tag>
                              </template>
                            </el-table-column>
                            <el-table-column prop="category" label="分类" width="120">
                              <template #default="scope">
                                <el-tag :type="getCategoryType(scope.row.category)" size="small">
                                  {{ getCategoryLabel(scope.row.category) }}
                                </el-tag>
                              </template>
                            </el-table-column>
                            <el-table-column label="关注强度">
                              <template #default="scope">
                                <el-progress
                                  :percentage="scope.row.intensity * 100"
                                  :color="getScoreColor(scope.row.intensity)"
                                  :show-text="true"
                                />
                              </template>
                            </el-table-column>
                            <el-table-column label="趋势" width="100">
                              <template #default="scope">
                                <el-tag v-if="scope.row.trending === 'up'" type="success">
                                  <el-icon><Top /></el-icon> 上升
                                </el-tag>
                                <el-tag v-else-if="scope.row.trending === 'down'" type="danger">
                                  <el-icon><Bottom /></el-icon> 下降
                                </el-tag>
                                <el-tag v-else type="info">
                                  <el-icon><Minus /></el-icon> 稳定
                                </el-tag>
                              </template>
                            </el-table-column>
                            <el-table-column prop="since" label="起始时间">
                              <template #default="scope">
                                {{ formatDate(scope.row.since) }}
                              </template>
                            </el-table-column>
                          </el-table>
                        </div>
                      </el-card>
                    </el-col>
                  </el-row>
                </el-tab-pane>

                <el-tab-pane label="评估指标" name="metrics">
                  <div>
                    <el-row :gutter="16">
                      <el-col :span="6">
                        <el-statistic title="稳定性" :value="soul.metadata.metrics.stability.score" :precision="2" />
                      </el-col>
                      <el-col :span="6">
                        <el-statistic title="一致性" :value="soul.metadata.metrics.consistency.score" :precision="2" />
                      </el-col>
                      <el-col :span="6">
                        <el-statistic title="完整性" :value="soul.metadata.metrics.completeness.score" :precision="2" />
                      </el-col>
                      <el-col :span="6">
                        <el-statistic title="准确性" :value="soul.metadata.metrics.accuracy.score" :precision="2" />
                      </el-col>
                    </el-row>
                  </div>
                </el-tab-pane>
              </el-tabs>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Top, Bottom, Minus } from '@element-plus/icons-vue'
import type { DigitalSoul } from '@/core/models/DigitalSoul'
import { createDefaultSoul } from '@/core/models/DigitalSoul'
import { storageService } from '@/services/storage'
import { soulAnalyzer } from '@/services/soul-analyzer'

const soul = ref<DigitalSoul | null>(null)
const activeTab = ref('core')
const messagesCount = ref(0)
const isBrowser = ref(typeof window !== 'undefined' && !(window as any).electronAPI)

// 辅助函数
const getColorByPriority = (priority: number) => {
  if (priority >= 0.8) return '#67c23a'
  if (priority >= 0.6) return '#409eff'
  if (priority >= 0.4) return '#e6a23c'
  return '#909399'
}

const getPriorityType = (priority: number) => {
  if (priority >= 0.8) return 'success'
  if (priority >= 0.6) return 'primary'
  if (priority >= 0.4) return 'warning'
  return 'info'
}

const getScoreColor = (score: number) => {
  if (score >= 0.7) return '#67c23a'
  if (score >= 0.5) return '#409eff'
  if (score >= 0.3) return '#e6a23c'
  return '#f56c6c'
}

const getCategoryLabel = (category: string) => {
  const labels: Record<string, string> = {
    work: '工作',
    personal: '个人',
    learning: '学习',
    health: '健康',
    finance: '财务',
    relationship: '关系',
    creativity: '创意'
  }
  return labels[category] || category
}

const getCategoryType = (category: string) => {
  const types: Record<string, any> = {
    work: 'primary',
    learning: 'success',
    personal: 'warning',
    health: 'danger',
    finance: 'info',
    relationship: '',
    creativity: ''
  }
  return types[category] || ''
}

onMounted(async () => {
  await loadSoul()
  updateMessagesCount()
})

const loadSoul = async () => {
  try {
    if (isBrowser.value) {
      // 浏览器环境：从 localStorage 加载真实数据
      let loaded = storageService.loadSoul()

      if (!loaded) {
        // 首次使用,创建默认分身
        loaded = createDefaultSoul('browser-user')
        storageService.saveSoul(loaded)
        console.log('[SoulView] 创建默认分身')
      }

      soul.value = loaded
      console.log('[SoulView] 分身加载成功, 版本:', loaded.version)
    } else {
      // Electron 环境：调用 API
      const data = await (window as any).electronAPI.soul.get('default')
      soul.value = data
    }
  } catch (error) {
    console.error('[SoulView] 加载失败:', error)
    ElMessage.error('加载分身数据失败')
  }
}

const handleRefresh = () => {
  loadSoul()
}

// 重新分析
const handleReanalyze = async () => {
  if (!soul.value) return

  const messages = storageService.getMessages()
  if (messages.length < soulAnalyzer.ANALYSIS_THRESHOLD) {
    ElMessage.warning(`需要至少 ${soulAnalyzer.ANALYSIS_THRESHOLD} 条消息才能分析`)
    return
  }

  try {
    ElMessage.info('正在重新分析...')

    // 重置分身到初始状态
    const baseSoul = createDefaultSoul(soul.value.userId)
    const analysis = await soulAnalyzer.analyze(baseSoul, messages)
    const updated = soulAnalyzer.updateSoul(baseSoul, analysis)

    storageService.saveSoul(updated)
    soul.value = updated

    ElMessage.success('重新分析完成')
  } catch (error: any) {
    console.error('[SoulView] 分析失败:', error)
    ElMessage.error(`分析失败: ${error.message}`)
  }
}

// 更新消息计数
const updateMessagesCount = () => {
  messagesCount.value = storageService.getMessages().length
  console.log('[SoulView] 消息总数:', messagesCount.value)
}

const formatDate = (date: Date | string) => {
  return new Date(date).toLocaleString('zh-CN')
}
</script>

<style scoped>
.soul-view {
  height: 100%;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

/* 信息卡片 */
.info-card {
  margin-bottom: 24px;
}

.info-card:last-child {
  margin-bottom: 0;
}

.card-title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

/* 价值观网格 */
.values-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 16px;
}

.value-card {
  padding: 16px;
  border: 1px solid #ebeef5;
  border-radius: 8px;
  border-left: 4px solid #409eff;
  background: #fafafa;
  transition: all 0.3s;
}

.value-card:hover {
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  transform: translateY(-2px);
}

.value-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.value-name {
  font-weight: 600;
  color: #303133;
}

.value-desc {
  color: #606266;
  font-size: 13px;
  line-height: 1.6;
  margin-bottom: 8px;
}

.value-examples {
  margin-top: 12px;
  padding-top: 12px;
  border-top: 1px dashed #dcdfe6;
}

.examples-label {
  font-size: 12px;
  color: #909399;
  margin-bottom: 6px;
}

.example-item {
  font-size: 12px;
  color: #606266;
  font-style: italic;
  margin-bottom: 4px;
}

/* 性格特质网格 */
.personality-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(250px, 1fr));
  gap: 16px;
}

.personality-card {
  padding: 16px;
  border: 1px solid #ebeef5;
  border-radius: 8px;
  background: #fafafa;
  transition: all 0.3s;
}

.personality-card:hover {
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.personality-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.personality-name {
  font-weight: 600;
  font-size: 14px;
  color: #303133;
}

.personality-score {
  font-size: 18px;
  font-weight: bold;
  color: #409eff;
}

.personality-desc {
  color: #606266;
  font-size: 13px;
  line-height: 1.6;
  margin: 8px 0;
}

.personality-indicators {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  margin-top: 8px;
}

.indicator-tag {
  padding: 2px 8px;
  background: #ecf5ff;
  border: 1px solid #d9ecff;
  border-radius: 4px;
  font-size: 11px;
  color: #409eff;
}
</style>
