package com.jishi.clipboard.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "clipboards")
data class ClipboardEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val content: String,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
