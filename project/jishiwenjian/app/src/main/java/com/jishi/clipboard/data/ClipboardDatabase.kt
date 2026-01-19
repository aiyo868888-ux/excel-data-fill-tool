package com.jishi.clipboard.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [ClipboardEntity::class, TagEntity::class, Reminder::class],
    version = 3,
    exportSchema = false
)
abstract class ClipboardDatabase : RoomDatabase() {

    abstract fun clipboardDao(): ClipboardDao
    abstract fun reminderDao(): ReminderDao

    companion object {
        private const val DATABASE_NAME = "jishi_clipboard.db"

        @Volatile
        private var INSTANCE: ClipboardDatabase? = null

        fun getDatabase(context: Context): ClipboardDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ClipboardDatabase::class.java,
                    DATABASE_NAME
                )
                .fallbackToDestructiveMigration() // 开发阶段使用破坏性迁移
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
