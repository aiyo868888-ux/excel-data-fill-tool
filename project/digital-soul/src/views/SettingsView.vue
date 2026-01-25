<template>
  <div class="settings-view">
    <el-row :gutter="24">
      <el-col :span="24">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>AI 配置</span>
            </div>
          </template>

          <el-form :model="form" label-width="120px" style="max-width: 600px">
            <el-form-item label="API Key">
              <el-input
                v-model="form.apiKey"
                type="password"
                placeholder="输入 OpenAI API Key"
                show-password
              />
              <div class="form-tip">
                获取 API Key: <a href="https://platform.openai.com/api-keys" target="_blank">OpenAI Platform</a>
              </div>
            </el-form-item>

            <el-form-item label="Base URL">
              <el-input
                v-model="form.baseURL"
                placeholder="默认: https://api.openai.com/v1"
              />
              <div class="form-tip">
                如果使用代理或其他兼容服务，请修改此地址
              </div>
            </el-form-item>

            <el-form-item label="服务提供商">
              <el-select v-model="form.provider" placeholder="选择提供商" style="width: 100%" @change="handleProviderChange">
                <el-option label="OpenAI" value="openai" />
                <el-option label="智谱 AI" value="zhipu" />
                <el-option label="自定义" value="custom" />
              </el-select>
              <div class="form-tip">
                选择服务提供商会自动配置 Base URL 和模型列表
              </div>
            </el-form-item>

            <el-form-item label="模型">
              <el-select v-model="form.model" placeholder="选择模型" style="width: 100%">
                <el-option v-for="model in availableModels" :key="model.value" :label="model.label" :value="model.value" />
              </el-select>
            </el-form-item>

            <el-form-item>
              <el-button type="primary" @click="handleSave" :loading="saving">
                保存配置
              </el-button>
              <el-button @click="handleTest" :disabled="!form.apiKey">
                测试连接
              </el-button>
            </el-form-item>
          </el-form>

          <el-divider />

          <el-alert
            v-if="saveSuccess"
            title="配置已保存"
            type="success"
            :closable="false"
            style="margin-bottom: 16px"
          />

          <div v-if="testResult" class="test-result">
            <el-descriptions title="测试结果" :column="1" border>
              <el-descriptions-item label="状态">
                <el-tag :type="testResult.success ? 'success' : 'danger'">
                  {{ testResult.success ? '成功' : '失败' }}
                </el-tag>
              </el-descriptions-item>
              <el-descriptions-item v-if="testResult.success" label="响应时间">
                {{ testResult.latency }}ms
              </el-descriptions-item>
              <el-descriptions-item v-if="testResult.error" label="错误信息">
                {{ testResult.error }}
              </el-descriptions-item>
            </el-descriptions>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { aiService } from '@/services/ai'

const form = ref({
  apiKey: '',
  baseURL: 'https://api.openai.com/v1',
  model: 'gpt-3.5-turbo',
  provider: 'openai'
})

const saving = ref(false)
const saveSuccess = ref(false)
const testResult = ref<any>(null)

// 模型列表配置
const modelConfigs = {
  openai: [
    { label: 'gpt-3.5-turbo (推荐)', value: 'gpt-3.5-turbo' },
    { label: 'gpt-4', value: 'gpt-4' },
    { label: 'gpt-4-turbo', value: 'gpt-4-turbo' },
    { label: 'gpt-4o', value: 'gpt-4o' }
  ],
  zhipu: [
    { label: 'glm-4-flash (免费)', value: 'glm-4-flash' },
    { label: 'glm-4-air', value: 'glm-4-air' },
    { label: 'glm-4-plus', value: 'glm-4-plus' },
    { label: 'glm-4', value: 'glm-4' },
    { label: 'glm-3-turbo', value: 'glm-3-turbo' }
  ],
  custom: []
}

const availableModels = ref(modelConfigs.openai)

onMounted(() => {
  // 加载已保存的配置
  const config = localStorage.getItem('ai-config')
  if (config) {
    const parsed = JSON.parse(config)
    form.value = {
      apiKey: parsed.apiKey || '',
      baseURL: parsed.baseURL || 'https://api.openai.com/v1',
      model: parsed.model || 'gpt-3.5-turbo',
      provider: parsed.provider || 'openai'
    }
    // 更新模型列表
    handleProviderChange(form.value.provider)
  }
})

// 服务提供商切换
const handleProviderChange = (provider: string) => {
  switch (provider) {
    case 'openai':
      form.value.baseURL = 'https://api.openai.com/v1'
      form.value.model = 'gpt-3.5-turbo'
      availableModels.value = modelConfigs.openai
      break
    case 'zhipu':
      form.value.baseURL = 'https://open.bigmodel.cn/api/paas/v4'
      form.value.model = 'glm-4-flash'
      availableModels.value = modelConfigs.zhipu
      break
    case 'custom':
      availableModels.value = modelConfigs.custom
      break
  }
}

const handleSave = async () => {
  if (!form.value.apiKey) {
    ElMessage.warning('请输入 API Key')
    return
  }

  saving.value = true
  try {
    aiService.saveConfig(form.value)
    saveSuccess.value = true
    ElMessage.success('配置已保存')
    setTimeout(() => {
      saveSuccess.value = false
    }, 3000)
  } catch (error) {
    ElMessage.error('保存失败')
  } finally {
    saving.value = false
  }
}

const handleTest = async () => {
  if (!form.value.apiKey) {
    ElMessage.warning('请先输入 API Key')
    return
  }

  // 先保存配置
  aiService.saveConfig(form.value)

  testResult.value = null
  ElMessage.info('正在测试连接...')

  const startTime = Date.now()

  try {
    await aiService.chat([
      { role: 'user', content: '你好' }
    ])

    testResult.value = {
      success: true,
      latency: Date.now() - startTime
    }
    ElMessage.success('连接测试成功')
  } catch (error: any) {
    testResult.value = {
      success: false,
      error: error.message
    }
    ElMessage.error('连接测试失败')
  }
}
</script>

<style scoped>
.settings-view {
  padding: 24px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.form-tip {
  font-size: 12px;
  color: #999;
  margin-top: 4px;
}

.form-tip a {
  color: #409eff;
  text-decoration: none;
}

.form-tip a:hover {
  text-decoration: underline;
}

.test-result {
  margin-top: 16px;
}
</style>
