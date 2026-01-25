package com.fleetingnotes

import android.app.Application

/**
 * 闪念笔记 Application 类
 */
class FleetingNotesApp : Application() {

    lateinit var noteRepository: com.fleetingnotes.domain.repository.NoteRepository

    override fun onCreate() {
        super.onCreate()
        // 初始化服务定位器
        ServiceLocator.initialize(this)
        noteRepository = ServiceLocator.noteRepository
    }
}

/**
 * 服务定位器 - 简单的依赖注入
 */
object ServiceLocator {
    lateinit var noteRepository: com.fleetingnotes.domain.repository.NoteRepository
        private set

    fun initialize(context: android.content.Context) {
        val jsonFileStorage = com.fleetingnotes.data.local.JsonFileStorage(context)
        noteRepository = com.fleetingnotes.data.repository.NoteRepositoryImpl(jsonFileStorage)
    }
}
