/**
 * SQLite 数据库管理器
 */

import Database from 'better-sqlite3'
import path from 'path'
import fs from 'fs'

export class SQLiteDatabaseManager {
  private db: Database.Database | null = null
  private dbPath: string

  constructor(dbPath: string = './data/soul.db') {
    this.dbPath = dbPath
  }

  /**
   * 初始化数据库连接
   */
  connect(): void {
    if (this.db) {
      return
    }

    // 确保数据目录存在
    const dir = path.dirname(this.dbPath)
    if (!fs.existsSync(dir)) {
      fs.mkdirSync(dir, { recursive: true })
    }

    this.db = new Database(this.dbPath)
    this.db.pragma('journal_mode = WAL')
    this.db.pragma('foreign_keys = ON')

    console.log(`[SQLite] Connected to database: ${this.dbPath}`)
  }

  /**
   * 关闭数据库连接
   */
  close(): void {
    if (this.db) {
      this.db.close()
      this.db = null
      console.log('[SQLite] Database connection closed')
    }
  }

  /**
   * 初始化数据库表结构
   */
  initializeSchema(): void {
    if (!this.db) {
      throw new Error('Database not connected')
    }

    const schemaPath = path.join(__dirname, 'schema.sql')
    const schema = fs.readFileSync(schemaPath, 'utf-8')

    // 执行 SQL 脚本
    this.db.exec(schema)

    console.log('[SQLite] Database schema initialized')
  }

  /**
   * 获取数据库实例
   */
  getDatabase(): Database.Database {
    if (!this.db) {
      throw new Error('Database not connected')
    }
    return this.db
  }

  /**
   * 执行查询（只读）
   */
  query<T>(sql: string, params: any[] = []): T[] {
    if (!this.db) {
      throw new Error('Database not connected')
    }

    const stmt = this.db.prepare(sql)
    return stmt.all(...params) as T[]
  }

  /**
   * 执行查询（单行）
   */
  queryOne<T>(sql: string, params: any[] = []): T | undefined {
    if (!this.db) {
      throw new Error('Database not connected')
    }

    const stmt = this.db.prepare(sql)
    return stmt.get(...params) as T | undefined
  }

  /**
   * 执行更新/插入/删除
   */
  execute(sql: string, params: any[] = []): Database.RunResult {
    if (!this.db) {
      throw new Error('Database not connected')
    }

    const stmt = this.db.prepare(sql)
    return stmt.run(...params)
  }

  /**
   * 开始事务
   */
  beginTransaction(): void {
    if (!this.db) {
      throw new Error('Database not connected')
    }
    this.db.exec('BEGIN TRANSACTION')
  }

  /**
   * 提交事务
   */
  commit(): void {
    if (!this.db) {
      throw new Error('Database not connected')
    }
    this.db.exec('COMMIT')
  }

  /**
   * 回滚事务
   */
  rollback(): void {
    if (!this.db) {
      throw new Error('Database not connected')
    }
    this.db.exec('ROLLBACK')
  }

  /**
   * 执行事务中的多个操作
   */
  transaction<T>(fn: () => T): T {
    if (!this.db) {
      throw new Error('Database not connected')
    }

    const txn = this.db.transaction(fn)
    return txn()
  }

  /**
   * 备份数据库
   */
  backup(backupPath: string): void {
    if (!this.db) {
      throw new Error('Database not connected')
    }

    // 确保备份目录存在
    const dir = path.dirname(backupPath)
    if (!fs.existsSync(dir)) {
      fs.mkdirSync(dir, { recursive: true })
    }

    // 复制数据库文件
    fs.copyFileSync(this.dbPath, backupPath)
    console.log(`[SQLite] Database backed up to: ${backupPath}`)
  }

  /**
   * 获取数据库统计信息
   */
  getStats(): {
    pageSize: number
    pageCount: number
    size: number
    tables: Array<{ name: string; rows: number }>
  } {
    if (!this.db) {
      throw new Error('Database not connected')
    }

    const pageSize = this.db.pragma('page_size', { simple: true }) as number
    const pageCount = this.db.pragma('page_count', { simple: true }) as number

    const tables = this.query<any>(
      "SELECT name FROM sqlite_master WHERE type='table' AND name NOT LIKE 'sqlite_%'"
    )

    const tableStats = tables.map((t: any) => {
      const count = this.queryOne<{ count: number }>(`SELECT COUNT(*) as count FROM ${t.name}`)
      return {
        name: t.name,
        rows: count?.count || 0
      }
    })

    return {
      pageSize,
      pageCount,
      size: pageSize * pageCount,
      tables: tableStats
    }
  }
}

// 单例模式
let manager: SQLiteDatabaseManager | null = null

export function getDatabaseManager(dbPath?: string): SQLiteDatabaseManager {
  if (!manager) {
    manager = new SQLiteDatabaseManager(dbPath)
  }
  return manager
}

export default getDatabaseManager
