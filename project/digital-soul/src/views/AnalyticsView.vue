<template>
  <div class="analytics-view">
    <el-row :gutter="24">
      <el-col :span="24">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>数据分析</span>
              <el-button type="primary" @click="loadData">刷新数据</el-button>
            </div>
          </template>

          <el-row :gutter="16">
            <el-col :xs="24" :sm="12" :md="6">
              <el-statistic title="总对话数" :value="stats.totalConversations" />
            </el-col>
            <el-col :xs="24" :sm="12" :md="6">
              <el-statistic title="总消息数" :value="stats.totalMessages" />
            </el-col>
            <el-col :xs="24" :sm="12" :md="6">
              <el-statistic title="AI 调用次数" :value="stats.aiCalls" />
            </el-col>
            <el-col :xs="24" :sm="12" :md="6">
              <el-statistic title="使用天数" :value="stats.daysUsed" suffix="天" />
            </el-col>
          </el-row>

          <el-divider />

          <el-row :gutter="24">
            <el-col :span="12">
              <el-card>
                <template #header>
                  <span>对话趋势</span>
                </template>
                <div class="chart-placeholder">
                  <el-empty description="图表功能开发中" :image-size="100" />
                </div>
              </el-card>
            </el-col>
            <el-col :span="12">
              <el-card>
                <template #header>
                  <span>话题分布</span>
                </template>
                <div class="topic-list">
                  <div v-for="topic in topics" :key="topic.name" class="topic-item">
                    <div class="topic-name">{{ topic.name }}</div>
                    <el-progress :percentage="topic.percentage" :color="topic.color" />
                  </div>
                </div>
              </el-card>
            </el-col>
          </el-row>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { storageService } from '@/services/storage'

const stats = ref({
  totalConversations: 0,
  totalMessages: 0,
  aiCalls: 0,
  daysUsed: 0
})

// 从真实对话数据计算话题分布
const topics = computed(() => {
  const messages = storageService.getMessages()
  const soul = storageService.loadSoul()

  if (!soul || soul.memories.focusAreas.length === 0) {
    return [
      { name: '暂无数据', percentage: 0, color: '#909399' }
    ]
  }

  // 从分身画像的关注方向生成话题分布
  const totalIntensity = soul.memories.focusAreas.reduce((sum, f) => sum + f.intensity, 0)

  return soul.memories.focusAreas.slice(0, 5).map((focus, index) => {
    const colors = ['#409eff', '#67c23a', '#e6a23c', '#f56c6c', '#909399']
    return {
      name: focus.topic,
      percentage: Math.round((focus.intensity / totalIntensity) * 100),
      color: colors[index % colors.length]
    }
  })
})

const loadData = () => {
  try {
    const messages = storageService.getMessages()
    const soul = storageService.loadSoul()

    // 按日期分组统计
    const dailyStats = messages.reduce((acc, msg) => {
      const date = new Date(msg.timestamp).toLocaleDateString('zh-CN')
      acc[date] = (acc[date] || 0) + 1
      return acc
    }, {} as Record<string, number>)

    const uniqueDays = Object.keys(dailyStats).length

    stats.value = {
      totalConversations: soul?.metadata.evolutionHistory.length || 0,
      totalMessages: messages.length,
      aiCalls: messages.filter(m => m.role === 'assistant').length,
      daysUsed: uniqueDays || 1
    }

    console.log('[Analytics] 数据加载成功:', stats.value)
    ElMessage.success('数据已刷新')
  } catch (error) {
    console.error('[Analytics] 加载失败:', error)
    ElMessage.error('数据加载失败')
  }
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.analytics-view {
  padding: 24px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.chart-placeholder {
  height: 200px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.topic-list {
  padding: 16px 0;
}

.topic-item {
  margin-bottom: 16px;
}

.topic-name {
  display: flex;
  justify-content: space-between;
  margin-bottom: 8px;
  font-size: 14px;
  color: #606266;
}
</style>
