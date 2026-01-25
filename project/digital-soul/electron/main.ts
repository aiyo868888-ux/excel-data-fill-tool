/**
 * Electron 主进程入口
 */

import { app, BrowserWindow, ipcMain } from 'electron'
import path from 'path'
import { getDatabaseManager } from './database/sqlite-manager'

let mainWindow: BrowserWindow | null = null

/**
 * 创建主窗口
 */
function createWindow(): void {
  mainWindow = new BrowserWindow({
    width: 1400,
    height: 900,
    minWidth: 1000,
    minHeight: 600,
    webPreferences: {
      nodeIntegration: false,
      contextIsolation: true,
      preload: path.join(__dirname, '../build/preload.js')
    },
    backgroundColor: '#f5f5f5',
    titleBarStyle: 'default'
  })

  // 开发环境加载 Vite 开发服务器
  if (process.env.NODE_ENV === 'development') {
    mainWindow.loadURL('http://localhost:5173')
    mainWindow.webContents.openDevTools()
  } else {
    // 生产环境加载打包后的文件
    mainWindow.loadFile(path.join(__dirname, '../dist/index.html'))
  }

  mainWindow.on('closed', () => {
    mainWindow = null
  })
}

/**
 * App 事件监听
 */

// 应用启动时创建窗口
app.on('ready', () => {
  createWindow()

  // 初始化数据库
  const dbManager = getDatabaseManager()
  dbManager.connect()
  dbManager.initializeSchema()

  console.log('[Electron] App ready')
})

// 所有窗口关闭时退出应用（macOS 除外）
app.on('window-all-closed', () => {
  if (process.platform !== 'darwin') {
    app.quit()
  }
})

// macOS 点击 Dock 图标时重新创建窗口
app.on('activate', () => {
  if (mainWindow === null) {
    createWindow()
  }
})

// 应用退出前关闭数据库连接
app.on('before-quit', () => {
  const dbManager = getDatabaseManager()
  dbManager.close()
})

/**
 * IPC 通信处理
 */

// 获取分身数据
ipcMain.handle('soul:get', async (event, soulId: string) => {
  const dbManager = getDatabaseManager()
  const row = dbManager.queryOne<any>(
    'SELECT * FROM digital_souls WHERE id = ?',
    [soulId]
  )

  if (!row) {
    return null
  }

  // 解析 JSON 字段
  return {
    ...row,
    foundation: JSON.parse(row.foundation),
    memories: JSON.parse(row.memories),
    metadata: JSON.parse(row.metadata)
  }
})

// 保存/更新分身数据
ipcMain.handle('soul:save', async (event, soulData: any) => {
  const dbManager = getDatabaseManager()

  const now = Date.now()
  const sql = `
    INSERT INTO digital_souls (id, user_id, version, created_at, updated_at, foundation, memories, metadata)
    VALUES (?, ?, ?, ?, ?, ?, ?, ?)
    ON CONFLICT(id) DO UPDATE SET
      version = excluded.version,
      updated_at = excluded.updated_at,
      foundation = excluded.foundation,
      memories = excluded.memories,
      metadata = excluded.metadata
  `

  dbManager.execute(sql, [
    soulData.id,
    soulData.userId,
    soulData.version,
    soulData.createdAt || now,
    now,
    JSON.stringify(soulData.foundation),
    JSON.stringify(soulData.memories),
    JSON.stringify(soulData.metadata)
  ])

  return { success: true }
})

// 获取所有对话列表
ipcMain.handle('conversations:list', async (event, soulId: string) => {
  const dbManager = getDatabaseManager()
  const conversations = dbManager.query<any>(
    'SELECT * FROM conversations WHERE soul_id = ? ORDER BY started_at DESC',
    [soulId]
  )

  return conversations
})

// 创建新对话
ipcMain.handle('conversations:create', async (event, conversationData: any) => {
  const dbManager = getDatabaseManager()

  const id = `conv-${Date.now()}`
  const now = Date.now()

  dbManager.execute(
    `INSERT INTO conversations (id, soul_id, started_at, title, message_count)
     VALUES (?, ?, ?, ?, ?)`,
    [id, conversationData.soulId, now, conversationData.title || '新对话', 0]
  )

  return { id, ...conversationData, started_at: now, message_count: 0 }
})

// 保存消息
ipcMain.handle('messages:save', async (event, messageData: any) => {
  const dbManager = getDatabaseManager()

  const id = `msg-${Date.now()}-${Math.random().toString(36).substr(2, 9)}`
  const now = Date.now()

  dbManager.execute(
    `INSERT INTO messages (id, conversation_id, role, content, timestamp, metadata)
     VALUES (?, ?, ?, ?, ?, ?)`,
    [
      id,
      messageData.conversationId,
      messageData.role,
      messageData.content,
      now,
      messageData.metadata ? JSON.stringify(messageData.metadata) : null
    ]
  )

  // 更新对话的消息计数
  dbManager.execute(
    'UPDATE conversations SET message_count = message_count + 1 WHERE id = ?',
    [messageData.conversationId]
  )

  return { id, ...messageData, timestamp: now }
})

// 获取对话的所有消息
ipcMain.handle('messages:list', async (event, conversationId: string) => {
  const dbManager = getDatabaseManager()
  const messages = dbManager.query<any>(
    'SELECT * FROM messages WHERE conversation_id = ? ORDER BY timestamp ASC',
    [conversationId]
  )

  return messages.map(msg => ({
    ...msg,
    metadata: msg.metadata ? JSON.parse(msg.metadata) : null
  }))
})

// 保存记忆片段
ipcMain.handle('fragments:save', async (event, fragmentData: any) => {
  const dbManager = getDatabaseManager()

  const id = fragmentData.id || `fragment-${Date.now()}-${Math.random().toString(36).substr(2, 9)}`
  const now = Date.now()

  dbManager.execute(
    `INSERT INTO memory_fragments (id, soul_id, timestamp, type, context, user_action, extraction, feedback, processed, validated, tags)
     VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
     ON CONFLICT(id) DO UPDATE SET
       timestamp = excluded.timestamp,
       type = excluded.type,
       context = excluded.context,
       user_action = excluded.user_action,
       extraction = excluded.extraction,
       feedback = excluded.feedback,
       processed = excluded.processed,
       validated = excluded.validated,
       tags = excluded.tags`,
    [
      id,
      fragmentData.soulId,
      fragmentData.timestamp || now,
      fragmentData.type,
      JSON.stringify(fragmentData.context),
      JSON.stringify(fragmentData.userAction),
      JSON.stringify(fragmentData.extraction),
      fragmentData.feedback ? JSON.stringify(fragmentData.feedback) : null,
      fragmentData.processed || false,
      fragmentData.validated || false,
      JSON.stringify(fragmentData.tags || [])
    ]
  )

  return { id, ...fragmentData }
})

// 获取记忆片段列表
ipcMain.handle('fragments:list', async (event, soulId: string, filter?: any) => {
  const dbManager = getDatabaseManager()

  let sql = 'SELECT * FROM memory_fragments WHERE soul_id = ?'
  const params: any[] = [soulId]

  if (filter?.type) {
    sql += ' AND type = ?'
    params.push(filter.type)
  }

  if (filter?.startDate) {
    sql += ' AND timestamp >= ?'
    params.push(filter.startDate)
  }

  if (filter?.endDate) {
    sql += ' AND timestamp <= ?'
    params.push(filter.endDate)
  }

  sql += ' ORDER BY timestamp DESC'

  if (filter?.limit) {
    sql += ' LIMIT ?'
    params.push(filter.limit)
  }

  const fragments = dbManager.query<any>(sql, params)

  return fragments.map(f => ({
    ...f,
    context: JSON.parse(f.context),
    userAction: JSON.parse(f.user_action),
    extraction: JSON.parse(f.extraction),
    feedback: f.feedback ? JSON.parse(f.feedback) : null,
    tags: f.tags ? JSON.parse(f.tags) : []
  }))
})

// 获取数据库统计信息
ipcMain.handle('db:stats', async () => {
  const dbManager = getDatabaseManager()
  return dbManager.getStats()
})

// 备份数据库
ipcMain.handle('db:backup', async (event, backupPath: string) => {
  const dbManager = getDatabaseManager()
  dbManager.backup(backupPath)
  return { success: true, path: backupPath }
})

console.log('[Electron] Main process loaded')
