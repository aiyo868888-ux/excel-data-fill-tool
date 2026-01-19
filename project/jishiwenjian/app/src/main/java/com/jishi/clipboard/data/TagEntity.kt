package com.jishi.clipboard.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tags")
data class TagEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val clipboardId: Long,
    val color: String = "#4ECDC4",
    val createdAt: Long = System.currentTimeMillis()
)
