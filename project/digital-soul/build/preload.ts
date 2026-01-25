/**
 * Electron 预加载脚本
 */

import { contextBridge, ipcRenderer } from 'electron'

/**
 * 暴露给渲染进程的 API
 */
contextBridge.exposeInMainWorld('electronAPI', {
  // 分身相关
  soul: {
    get: (soulId: string) => ipcRenderer.invoke('soul:get', soulId),
    save: (soulData: any) => ipcRenderer.invoke('soul:save', soulData)
  },

  // 对话相关
  conversations: {
    list: (soulId: string) => ipcRenderer.invoke('conversations:list', soulId),
    create: (data: any) => ipcRenderer.invoke('conversations:create', data)
  },

  // 消息相关
  messages: {
    save: (data: any) => ipcRenderer.invoke('messages:save', data),
    list: (conversationId: string) => ipcRenderer.invoke('messages:list', conversationId)
  },

  // 记忆片段相关
  fragments: {
    save: (data: any) => ipcRenderer.invoke('fragments:save', data),
    list: (soulId: string, filter?: any) => ipcRenderer.invoke('fragments:list', soulId, filter)
  },

  // 数据库相关
  db: {
    stats: () => ipcRenderer.invoke('db:stats'),
    backup: (path: string) => ipcRenderer.invoke('db:backup', path)
  }
})

/**
 * TypeScript 类型声明
 */
declare global {
  interface Window {
    electronAPI: {
      soul: {
        get: (soulId: string) => Promise<any>
        save: (soulData: any) => Promise<any>
      }
      conversations: {
        list: (soulId: string) => Promise<any[]>
        create: (data: any) => Promise<any>
      }
      messages: {
        save: (data: any) => Promise<any>
        list: (conversationId: string) => Promise<any[]>
      }
      fragments: {
        save: (data: any) => Promise<any>
        list: (soulId: string, filter?: any) => Promise<any[]>
      }
      db: {
        stats: () => Promise<any>
        backup: (path: string) => Promise<any>
      }
    }
  }
}

export {}
