<template>
  <div class="tools-view">
    <el-row :gutter="24">
      <el-col :span="24">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>工具箱</span>
            </div>
          </template>

          <el-row :gutter="16">
            <el-col :xs="24" :sm="12" :md="8" v-for="tool in tools" :key="tool.id">
              <el-card class="tool-card" @click="handleToolClick(tool)">
                <div class="tool-icon">
                  <el-icon :size="40">
                    <component :is="tool.icon" />
                  </el-icon>
                </div>
                <div class="tool-info">
                  <h3>{{ tool.name }}</h3>
                  <p>{{ tool.description }}</p>
                </div>
                <div class="tool-action">
                  <el-button type="primary" link>使用</el-button>
                </div>
              </el-card>
            </el-col>
          </el-row>
        </el-card>
      </el-col>
    </el-row>

    <!-- 提示词生成对话框 -->
    <el-dialog v-model="promptDialogVisible" :title="currentTool?.name" width="700px">
      <el-form :model="promptForm" label-width="100px">
        <el-form-item label="任务描述">
          <el-input
            v-model="promptForm.task"
            type="textarea"
            :rows="3"
            placeholder="描述你想要完成的任务..."
          />
        </el-form-item>
        <el-form-item label="风格要求">
          <el-select v-model="promptForm.style" placeholder="选择风格" style="width: 100%">
            <el-option label="专业正式" value="formal" />
            <el-option label="简洁明了" value="concise" />
            <el-option label="详细说明" value="detailed" />
            <el-option label="创意独特" value="creative" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="promptDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleGeneratePrompt" :loading="generating">
          生成提示词
        </el-button>
      </template>
    </el-dialog>

    <!-- 生成结果对话框 -->
    <el-dialog v-model="resultDialogVisible" title="生成结果" width="700px">
      <el-input
        v-model="generatedResult"
        type="textarea"
        :rows="10"
        readonly
      />
      <template #footer>
        <el-button @click="resultDialogVisible = false">关闭</el-button>
        <el-button type="primary" @click="handleCopyResult">复制</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import { Document, MagicStick, ChatLineRound, Edit, TrendCharts } from '@element-plus/icons-vue'

const tools = ref([
  {
    id: 'prompt-gen',
    name: '提示词生成',
    description: '根据你的需求生成优化的 AI 提示词',
    icon: MagicStick,
    category: 'writing'
  },
  {
    id: 'text-improve',
    name: '文本优化',
    description: '优化和改进你的文本内容',
    icon: Edit,
    category: 'writing'
  },
  {
    id: 'summary',
    name: '内容摘要',
    description: '快速生成文章或文档的摘要',
    icon: Document,
    category: 'writing'
  },
  {
    id: 'idea-brainstorm',
    name: '创意风暴',
    description: '激发创意和灵感',
    icon: TrendCharts,
    category: 'creative'
  },
  {
    id: 'role-play',
    name: '角色扮演',
    description: '模拟不同角色的对话',
    icon: ChatLineRound,
    category: 'simulation'
  },
  {
    id: 'code-helper',
    name: '代码助手',
    description: '编程相关的问题解答',
    icon: Document,
    category: 'tech'
  }
])

const promptDialogVisible = ref(false)
const resultDialogVisible = ref(false)
const currentTool = ref<any>(null)
const generating = ref(false)
const promptForm = ref({
  task: '',
  style: 'formal'
})
const generatedResult = ref('')

const handleToolClick = (tool: any) => {
  currentTool.value = tool
  promptForm.value = {
    task: '',
    style: 'formal'
  }
  promptDialogVisible.value = true
}

const handleGeneratePrompt = async () => {
  if (!promptForm.value.task) {
    ElMessage.warning('请描述任务')
    return
  }

  generating.value = true

  try {
    const { aiService } = await import('@/services/ai')
    const prompt = `你是一个专业的提示词工程师。请根据以下信息生成一个优化的 AI 提示词：

任务：${promptForm.value.task}
风格：${promptForm.value.style}

请生成一个结构清晰、效果好的提示词。`

    const response = await aiService.chat([
      { role: 'user', content: prompt }
    ])

    generatedResult.value = response.content
    promptDialogVisible.value = false
    resultDialogVisible.value = true
  } catch (error: any) {
    ElMessage.error(`生成失败: ${error.message}`)
  } finally {
    generating.value = false
  }
}

const handleCopyResult = () => {
  navigator.clipboard.writeText(generatedResult.value)
  ElMessage.success('已复制到剪贴板')
}
</script>

<style scoped>
.tools-view {
  padding: 24px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.tool-card {
  cursor: pointer;
  transition: all 0.3s;
  margin-bottom: 16px;
}

.tool-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.tool-icon {
  text-align: center;
  padding: 16px 0;
  color: #409eff;
}

.tool-info {
  padding: 0 16px 16px;
}

.tool-info h3 {
  margin: 0 0 8px 0;
  font-size: 18px;
  color: #303133;
}

.tool-info p {
  margin: 0;
  font-size: 14px;
  color: #909399;
  line-height: 1.5;
}

.tool-action {
  text-align: right;
  padding: 0 16px 16px;
}
</style>
