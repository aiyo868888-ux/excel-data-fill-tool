<template>
  <div class="home-view">
      <div class="conversation-container">
        <el-card class="chat-card">
          <template #header>
            <div class="card-header">
              <span>对话</span>
              <el-button type="primary" size="small" :icon="Plus" @click="startNewConversation">
                新对话
              </el-button>
            </div>
          </template>

          <div class="messages-container" ref="messagesContainer">
            <el-empty v-if="messages.length === 0" description="开始新的对话" />

            <div v-for="message in messages" :key="message.id" class="message-item">
              <div :class="['message', message.role]">
                <div class="message-avatar">
                  <el-icon v-if="message.role === 'user'"><User /></el-icon>
                  <el-icon v-else><ChatDotRound /></el-icon>
                </div>
                <div class="message-content">
                  <div class="message-text">{{ message.content }}</div>
                  <div class="message-time">{{ formatTime(message.timestamp) }}</div>
                </div>
              </div>
            </div>
          </div>

          <div class="input-container">
            <el-input
              v-model="inputMessage"
              type="textarea"
              :rows="3"
              placeholder="输入消息..."
              @keydown.ctrl.enter="sendMessage"
            />
            <div class="input-actions">
              <el-button type="primary" :icon="Position" @click="sendMessage" :disabled="!inputMessage">
                发送 (Ctrl+Enter)
              </el-button>
            </div>
          </div>
        </el-card>
      </div>
</div>
</template>

<script setup lang="ts">
import { ref, onMounted, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import { Plus, Position, User, ChatDotRound } from '@element-plus/icons-vue'
import { storageService } from '@/services/storage'
import { soulAnalyzer } from '@/services/soul-analyzer'
import { createDefaultSoul } from '@/core/models/DigitalSoul'

const messages = ref<any[]>([])
const inputMessage = ref('')
const messagesContainer = ref<HTMLElement>()
const isBrowser = ref(typeof window !== 'undefined' && !(window as any).electronAPI)

let currentConversationId: string | null = null

onMounted(() => {
  startNewConversation()
})

const startNewConversation = async () => {
  try {
    if (isBrowser.value) {
      // 浏览器环境：使用模拟 ID
      currentConversationId = `conv-${Date.now()}`
      messages.value = []
      ElMessage.success('浏览器模式：新对话已创建')
    } else {
      // Electron 环境：调用 API
      const result = await (window as any).electronAPI.conversations.create({
        soulId: 'default',
        title: '新对话'
      })
      currentConversationId = result.id
      messages.value = []
    }
  } catch (error) {
    ElMessage.error('创建对话失败')
  }
}

const sendMessage = async () => {
  if (!inputMessage.value.trim() || !currentConversationId) {
    return
  }

  const userMessage = {
    role: 'user',
    content: inputMessage.value,
    timestamp: Date.now()
  }

  messages.value.push(userMessage)
  const userContent = inputMessage.value
  inputMessage.value = ''

  // 滚动到底部
  await nextTick()
  scrollToBottom()

  // 添加正在输入的消息占位
  const typingMessage = {
    role: 'assistant',
    content: '正在思考...',
    timestamp: Date.now(),
    typing: true
  }
  messages.value.push(typingMessage)

  await nextTick()
  scrollToBottom()

  try {
    // 准备对话历史
    const chatHistory = messages.value
      .filter(m => !m.typing)
      .slice(-10) // 只保留最近10条消息作为上下文
      .map(m => ({
        role: m.role === 'user' ? 'user' : 'assistant',
        content: m.content
      }))

    // 调用 AI 服务
    const { aiService } = await import('@/services/ai')
    const response = await aiService.chat(chatHistory)

    // 移除正在输入的消息
    messages.value = messages.value.filter(m => !m.typing)

    // 添加 AI 回复
    const aiMessage = {
      role: 'assistant',
      content: response.content,
      timestamp: Date.now()
    }

    messages.value.push(aiMessage)

    // 保存到 localStorage (浏览器模式)
    if (isBrowser.value) {
      storageService.addMessage({
        id: `msg-${Date.now()}-1`,
        conversationId: currentConversationId!,
        role: 'user',
        content: userContent,
        timestamp: userMessage.timestamp
      })

      storageService.addMessage({
        id: `msg-${Date.now()}-2`,
        conversationId: currentConversationId!,
        role: 'assistant',
        content: aiMessage.content,
        timestamp: aiMessage.timestamp
      })

      // 检查并触发分析
      await checkAndAnalyze()
    }

    // 保存到 Electron（如果可用）
    if (!isBrowser.value) {
      await (window as any).electronAPI.messages.save({
        conversationId: currentConversationId!,
        role: 'assistant',
        content: aiMessage.content,
        timestamp: aiMessage.timestamp
      })
    }
  } catch (error: any) {
    // 移除正在输入的消息
    messages.value = messages.value.filter(m => !m.typing)

    // 显示错误消息
    const errorMessage = {
      role: 'assistant',
      content: `错误: ${error.message}`,
      timestamp: Date.now(),
      error: true
    }
    messages.value.push(errorMessage)
  }

  await nextTick()
  scrollToBottom()
}

// 检查并触发分身分析
const checkAndAnalyze = async () => {
  const soul = storageService.loadSoul() || createDefaultSoul('browser-user')

  // 处理 lastUpdate 可能是字符串的情况
  const lastAnalyzed = soul.metadata.lastUpdate instanceof Date
    ? soul.metadata.lastUpdate.getTime()
    : new Date(soul.metadata.lastUpdate).getTime()

  const newMessages = storageService.getUnanalyzedMessages(lastAnalyzed)

  if (newMessages.length >= soulAnalyzer.ANALYSIS_THRESHOLD) {
    try {
      ElMessage.info('正在更新分身画像...')

      const analysis = await soulAnalyzer.analyze(soul, newMessages)
      const updated = soulAnalyzer.updateSoul(soul, analysis)

      storageService.saveSoul(updated)
      storageService.cleanup()

      ElMessage.success('分身画像已更新')
    } catch (error) {
      console.error('分析失败:', error)
      // 静默失败,不影响对话体验
    }
  }
}

const scrollToBottom = () => {
  if (messagesContainer.value) {
    messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight
  }
}

const formatTime = (timestamp: number) => {
  return new Date(timestamp).toLocaleTimeString('zh-CN', {
    hour: '2-digit',
    minute: '2-digit'
  })
}
</script>

<style scoped>
.home-view {
  height: 100%;
}

.conversation-container {
  height: 100%;
}

.chat-card {
  height: 100%;
  display: flex;
  flex-direction: column;
}

.chat-card :deep(.el-card__body) {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.messages-container {
  flex: 1;
  overflow-y: auto;
  padding: 16px;
  background: #fafafa;
  border-radius: 4px;
  margin-bottom: 16px;
}

.message-item {
  margin-bottom: 16px;
}

.message {
  display: flex;
  gap: 12px;
}

.message.user {
  flex-direction: row-reverse;
}

.message-avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  flex-shrink: 0;
}

.message.user .message-avatar {
  background: #1890ff;
  color: #fff;
}

.message.assistant .message-avatar {
  background: #52c41a;
  color: #fff;
}

.message-content {
  max-width: 70%;
}

.message-text {
  background: #fff;
  padding: 12px 16px;
  border-radius: 8px;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.05);
  line-height: 1.6;
}

.message.user .message-text {
  background: #1890ff;
  color: #fff;
}

.message.assistant.error .message-text {
  background: #fff;
  border-left: 4px solid #f56c6c;
  color: #f56c6c;
}

/* 打字动画 */
.message.typing .message-text {
  position: relative;
}

.message.typing .message-text::after {
  content: '...';
  animation: typing 1.5s infinite;
}

@keyframes typing {
  0%, 60%, 100% {
    content: '';
  }
  30% {
    content: '.';
  }
  60% {
    content: '..';
  }
  100% {
    content: '...';
  }
}

.message-time {
  font-size: 12px;
  color: #999;
  margin-top: 4px;
  text-align: right;
}

.input-container {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.input-actions {
  display: flex;
  justify-content: flex-end;
}
</style>
