package com.jishi.clipboard.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [ClipboardEntity::class, TagEntity::class, TagDefinition::class, Reminder::class, TodoEntity::class],
    version = 6,
    exportSchema = false
)
abstract class ClipboardDatabase : RoomDatabase() {

    abstract fun clipboardDao(): ClipboardDao
    abstract fun tagDao(): TagDao
    abstract fun clipboardTagRelationDao(): ClipboardTagRelationDao
    abstract fun reminderDao(): ReminderDao
    abstract fun todoDao(): TodoDao

    companion object {
        private const val DATABASE_NAME = "jishi_clipboard.db"

        // 迁移策略：版本 4 → 5（添加 TodoEntity）
        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // 创建 todos 表
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS todos (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        task TEXT NOT NULL,
                        rawContent TEXT NOT NULL,
                        dueTimestamp INTEGER,
                        originalTimeText TEXT,
                        status TEXT NOT NULL DEFAULT 'PENDING',
                        tags TEXT NOT NULL DEFAULT '',
                        createdAt INTEGER NOT NULL,
                        updatedAt INTEGER NOT NULL,
                        completedAt INTEGER
                    )
                """)
            }
        }

        // 迁移策略：版本 5 → 6（添加层级标签支持）
        val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // 添加 parentId 和 level 字段
                database.execSQL("ALTER TABLE tag_definitions ADD COLUMN parentId INTEGER")
                database.execSQL("ALTER TABLE tag_definitions ADD COLUMN level INTEGER NOT NULL DEFAULT 0")

                // 创建默认根标签：待办、灵感、启发
                database.execSQL("""
                    INSERT INTO tag_definitions (name, color, displayOrder, useCount, parentId, level, createdAt)
                    VALUES
                        ('待办', '#FF6B6B', 1, 0, NULL, 0, ${System.currentTimeMillis()}),
                        ('灵感', '#4ECDC4', 2, 0, NULL, 0, ${System.currentTimeMillis()}),
                        ('启发', '#45B7D1', 3, 0, NULL, 0, ${System.currentTimeMillis()})
                """)

                // 创建默认子标签
                // 待办 -> 工作、生活
                database.execSQL("""
                    INSERT INTO tag_definitions (name, color, displayOrder, useCount, parentId, level, createdAt)
                    VALUES
                        ('工作', '#FFA07A', 1, 0, (SELECT id FROM tag_definitions WHERE name = '待办' LIMIT 1), 1, ${System.currentTimeMillis()}),
                        ('生活', '#98D8C8', 2, 0, (SELECT id FROM tag_definitions WHERE name = '待办' LIMIT 1), 1, ${System.currentTimeMillis()})
                """)

                // 启发 -> 读书
                database.execSQL("""
                    INSERT INTO tag_definitions (name, color, displayOrder, useCount, parentId, level, createdAt)
                    VALUES
                        ('读书', '#F39C12', 1, 0, (SELECT id FROM tag_definitions WHERE name = '启发' LIMIT 1), 1, ${System.currentTimeMillis()})
                """)
            }
        }

        @Volatile
        private var INSTANCE: ClipboardDatabase? = null

        fun getDatabase(context: Context): ClipboardDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ClipboardDatabase::class.java,
                    DATABASE_NAME
                )
                .addMigrations(MIGRATION_4_5, MIGRATION_5_6)
                // 生产环境：移除 fallbackToDestructiveMigration
                // 如需数据丢失容忍度，可添加 .fallbackToDestructiveMigrationOnDowngrade()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
