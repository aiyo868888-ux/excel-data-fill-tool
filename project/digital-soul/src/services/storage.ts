/**
 * 浏览器环境数据持久化服务
 * 使用 localStorage 存储对话历史和分身数据
 */

import type { DigitalSoul } from '@/core/models/DigitalSoul'
import { serializeSoul, deserializeSoul, createDefaultSoul } from '@/core/models/DigitalSoul'

/**
 * 对话消息接口
 */
export interface ConversationMessage {
  id: string
  conversationId: string
  role: 'user' | 'assistant' | 'system'
  content: string
  timestamp: number
}

/**
 * 本地存储服务类
 */
class LocalStorageService {
  private readonly SOUL_KEY = 'digital-soul-data'
  private readonly MESSAGES_KEY = 'conversation-history'
  private readonly MAX_MESSAGES = 500 // 最大消息数量
  private readonly CLEANUP_THRESHOLD = 300 // 清理阈值

  /**
   * 保存分身数据
   */
  saveSoul(soul: DigitalSoul): void {
    try {
      const serialized = serializeSoul(soul)
      localStorage.setItem(this.SOUL_KEY, serialized)
      console.log('[Storage] 分身数据已保存, 版本:', soul.version)
    } catch (error: any) {
      if (error.name === 'QuotaExceededError') {
        console.warn('[Storage] localStorage 已满,尝试清理后重试')
        this.cleanup()
        // 重试一次
        const serialized = serializeSoul(soul)
        localStorage.setItem(this.SOUL_KEY, serialized)
      } else {
        throw error
      }
    }
  }

  /**
   * 加载分身数据
   */
  loadSoul(): DigitalSoul | null {
    try {
      const data = localStorage.getItem(this.SOUL_KEY)
      if (!data) {
        console.log('[Storage] 未找到分身数据')
        return null
      }
      const soul = deserializeSoul(data)
      console.log('[Storage] 分身数据已加载, 版本:', soul.version)
      return soul
    } catch (error) {
      console.error('[Storage] 加载分身数据失败:', error)
      return null
    }
  }

  /**
   * 添加单条消息
   */
  addMessage(msg: ConversationMessage): void {
    try {
      const history = this.getMessages()
      history.push(msg)

      // 限制消息数量
      if (history.length > this.MAX_MESSAGES) {
        const removed = history.length - this.MAX_MESSAGES
        console.warn(`[Storage] 消息数量超限,删除最早的 ${removed} 条消息`)
        history.splice(0, removed)
      }

      localStorage.setItem(this.MESSAGES_KEY, JSON.stringify(history))
      console.log('[Storage] 消息已保存, 总数:', history.length)
    } catch (error: any) {
      if (error.name === 'QuotaExceededError') {
        console.warn('[Storage] localStorage 已满,执行清理')
        this.cleanup()
        // 重试
        const history = this.getMessages()
        history.push(msg)
        localStorage.setItem(this.MESSAGES_KEY, JSON.stringify(history))
      } else {
        throw error
      }
    }
  }

  /**
   * 获取消息历史
   */
  getMessages(conversationId?: string): ConversationMessage[] {
    try {
      const data = localStorage.getItem(this.MESSAGES_KEY)
      if (!data) {
        return []
      }
      const all: ConversationMessage[] = JSON.parse(data)

      if (conversationId) {
        return all.filter(m => m.conversationId === conversationId)
      }
      return all
    } catch (error) {
      console.error('[Storage] 获取消息历史失败:', error)
      return []
    }
  }

  /**
   * 获取未分析的消息
   * @param since 时间戳,只返回此时间之后的消息
   */
  getUnanalyzedMessages(since: number): ConversationMessage[] {
    const all = this.getMessages()
    return all.filter(m => m.timestamp > since)
  }

  /**
   * 清理旧消息
   * 保留最近的 300 条消息
   */
  cleanup(): void {
    try {
      const history = this.getMessages()
      if (history.length <= this.CLEANUP_THRESHOLD) {
        return
      }

      const removed = history.length - this.CLEANUP_THRESHOLD
      const trimmed = history.slice(-this.CLEANUP_THRESHOLD)

      localStorage.setItem(this.MESSAGES_KEY, JSON.stringify(trimmed))
      console.log(`[Storage] 清理完成,删除 ${removed} 条旧消息,保留 ${trimmed.length} 条`)
    } catch (error) {
      console.error('[Storage] 清理失败:', error)
    }
  }

  /**
   * 清空所有数据
   */
  clearAll(): void {
    localStorage.removeItem(this.SOUL_KEY)
    localStorage.removeItem(this.MESSAGES_KEY)
    console.log('[Storage] 所有数据已清空')
  }

  /**
   * 获取存储统计信息
   */
  getStats(): {
    soulSize: number
    messagesSize: number
    messagesCount: number
    totalSize: number
  } {
    const soulData = localStorage.getItem(this.SOUL_KEY) || ''
    const messagesData = localStorage.getItem(this.MESSAGES_KEY) || ''

    return {
      soulSize: new Blob([soulData]).size,
      messagesSize: new Blob([messagesData]).size,
      messagesCount: this.getMessages().length,
      totalSize: new Blob([soulData + messagesData]).size
    }
  }
}

// 导出单例
export const storageService = new LocalStorageService()
