package com.jishi.clipboard.di

import android.content.Context
import com.jishi.clipboard.data.ClipboardDao
import com.jishi.clipboard.data.ClipboardDatabase
import com.jishi.clipboard.data.ClipboardTagRelationDao
import com.jishi.clipboard.data.ReminderDao
import com.jishi.clipboard.data.TagDao
import com.jishi.clipboard.data.TodoDao
import com.jishi.clipboard.repository.ClipboardRepository
import com.jishi.clipboard.repository.ReminderRepository
import com.jishi.clipboard.repository.TagRepository
import com.jishi.clipboard.repository.TodoRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideClipboardDatabase(@ApplicationContext context: Context): ClipboardDatabase {
        return ClipboardDatabase.getDatabase(context)
    }

    @Provides
    @Singleton
    fun provideClipboardDao(database: ClipboardDatabase): ClipboardDao {
        return database.clipboardDao()
    }

    @Provides
    @Singleton
    fun provideTagDao(database: ClipboardDatabase): TagDao {
        return database.tagDao()
    }

    @Provides
    @Singleton
    fun provideClipboardTagRelationDao(database: ClipboardDatabase): ClipboardTagRelationDao {
        return database.clipboardTagRelationDao()
    }

    @Provides
    @Singleton
    fun provideClipboardRepository(
        clipboardDao: ClipboardDao,
        tagDao: TagDao,
        relationDao: ClipboardTagRelationDao
    ): ClipboardRepository {
        return ClipboardRepository(clipboardDao, tagDao, relationDao)
    }

    @Provides
    @Singleton
    fun provideTagRepository(
        tagDao: TagDao,
        relationDao: ClipboardTagRelationDao
    ): TagRepository {
        return TagRepository(tagDao, relationDao)
    }

    @Provides
    @Singleton
    fun provideReminderDao(database: ClipboardDatabase): ReminderDao {
        return database.reminderDao()
    }

    @Provides
    @Singleton
    fun provideReminderRepository(dao: ReminderDao): ReminderRepository {
        return ReminderRepository(dao)
    }

    @Provides
    @Singleton
    fun provideTodoDao(database: ClipboardDatabase): TodoDao {
        return database.todoDao()
    }

    @Provides
    @Singleton
    fun provideTodoRepository(todoDao: TodoDao): TodoRepository {
        return TodoRepository(todoDao)
    }

    @Provides
    @Singleton
    fun provideReminderScheduler(
        @ApplicationContext context: Context
    ): com.jishi.clipboard.reminder.ReminderScheduler {
        return com.jishi.clipboard.reminder.ReminderScheduler(context)
    }

    @Provides
    @Singleton
    fun provideWebServerManager(
        @ApplicationContext context: Context,
        repository: ClipboardRepository
    ): com.jishi.clipboard.network.server.WebServerManager {
        return com.jishi.clipboard.network.server.WebServerManager(context, repository)
    }
}
