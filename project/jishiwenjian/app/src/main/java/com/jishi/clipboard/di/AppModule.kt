package com.jishi.clipboard.di

import android.content.Context
import com.jishi.clipboard.data.ClipboardDao
import com.jishi.clipboard.data.ClipboardDatabase
import com.jishi.clipboard.data.ReminderDao
import com.jishi.clipboard.repository.ClipboardRepository
import com.jishi.clipboard.repository.ReminderRepository
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
    fun provideClipboardRepository(dao: ClipboardDao): ClipboardRepository {
        return ClipboardRepository(dao)
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
}
