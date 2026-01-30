package com.jishi.clipboard.data

import android.content.Context
import android.os.Environment
import com.google.gson.Gson
import com.jishi.clipboard.data.json.DailyData
import com.jishi.clipboard.data.json.InsightItem
import com.jishi.clipboard.data.json.InspirationItem
import com.jishi.clipboard.data.json.TodoItem
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * JSON 文件管理器
 * 负责所有 JSON 文件的读写操作
 */
object JsonFileManager {
    private const val DIR_NAME = "JiShiJi"
    private val gson = Gson()

    /**
     * 获取存储目录（使用应用外部存储，不需要特殊权限）
     */
    fun getStorageDir(context: Context): File {
        // 使用应用的外部文件目录，不需要 MANAGE_EXTERNAL_STORAGE 权限
        val appExternalDir = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
        val dir = File(appExternalDir, DIR_NAME)

        android.util.Log.d("JsonFileManager", "应用外部目录: ${appExternalDir?.absolutePath}")
        android.util.Log.d("JsonFileManager", "JiShiJi 目录: ${dir.absolutePath}")
        android.util.Log.d("JsonFileManager", "目录存在: ${dir.exists()}")

        if (!dir.exists()) {
            val created = dir.mkdirs()
            android.util.Log.d("JsonFileManager", "创建目录: $created")
        }

        return dir
    }

    /**
     * 获取指定日期的文件
     */
    fun getFileForDate(context: Context, date: String): File {
        return File(getStorageDir(context), "$date.json")
    }

    /**
     * 读取指定日期的数据
     */
    fun readDailyData(context: Context, date: String): DailyData {
        val file = getFileForDate(context, date)
        android.util.Log.d("JsonFileManager", "读取文件: ${file.absolutePath}, 存在: ${file.exists()}")

        if (!file.exists()) {
            android.util.Log.d("JsonFileManager", "文件不存在，返回空数据")
            return DailyData(date = date)
        }

        return try {
            val json = file.readText()
            android.util.Log.d("JsonFileManager", "读取到数据，长度: ${json.length}")
            val data = gson.fromJson(json, DailyData::class.java)
            android.util.Log.d("JsonFileManager", "解析成功 - 灵感:${data.灵感.size}, 启发:${data.启发.size}, 待办:${data.待办.size}")
            data
        } catch (e: Exception) {
            android.util.Log.e("JsonFileManager", "解析失败: ${e.message}", e)
            e.printStackTrace()
            DailyData(date = date)
        }
    }

    /**
     * 保存指定日期的数据
     */
    fun saveDailyData(context: Context, data: DailyData) {
        try {
            val file = getFileForDate(context, data.date)
            val json = gson.toJson(data)
            android.util.Log.d("JsonFileManager", "保存到文件: ${file.absolutePath}")
            file.writeText(json)
            android.util.Log.d("JsonFileManager", "保存成功 - 灵感:${data.灵感.size}, 启发:${data.启发.size}, 待办:${data.待办.size}")
        } catch (e: Exception) {
            android.util.Log.e("JsonFileManager", "保存失败: ${e.message}", e)
            e.printStackTrace()
        }
    }

    /**
     * 添加灵感项
     */
    fun addInspiration(context: Context, item: InspirationItem) {
        android.util.Log.d("JsonFileManager", "添加灵感: ${item.content}")
        val date = getCurrentDate()
        val data = readDailyData(context, date)
        val updatedList = data.灵感 + item
        val updatedData = data.copy(灵感 = updatedList)
        saveDailyData(context, updatedData)
        android.util.Log.d("JsonFileManager", "灵感保存成功，当前数量: ${updatedList.size}")
    }

    /**
     * 添加启发项
     */
    fun addInsight(context: Context, item: InsightItem) {
        val date = getCurrentDate()
        val data = readDailyData(context, date)
        val updatedList = data.启发 + item
        val updatedData = data.copy(启发 = updatedList)
        saveDailyData(context, updatedData)
    }

    /**
     * 添加待办项
     */
    fun addTodo(context: Context, item: TodoItem) {
        val date = getCurrentDate()
        val data = readDailyData(context, date)
        val updatedList = data.待办 + item
        val updatedData = data.copy(待办 = updatedList)
        saveDailyData(context, updatedData)
    }

    /**
     * 更新待办项状态
     */
    fun updateTodoStatus(context: Context, itemId: String, newStatus: String) {
        val date = getCurrentDate()
        val data = readDailyData(context, date)
        val updatedList = data.待办.map { item ->
            if (item.id == itemId) {
                item.copy(
                    status = newStatus,
                    updatedAt = System.currentTimeMillis()
                )
            } else {
                item
            }
        }
        val updatedData = data.copy(待办 = updatedList)
        saveDailyData(context, updatedData)
    }

    /**
     * 删除灵感项
     */
    fun deleteInspiration(context: Context, itemId: String) {
        val date = getCurrentDate()
        val data = readDailyData(context, date)
        val updatedList = data.灵感.filter { it.id != itemId }
        val updatedData = data.copy(灵感 = updatedList)
        saveDailyData(context, updatedData)
    }

    /**
     * 删除启发项
     */
    fun deleteInsight(context: Context, itemId: String) {
        val date = getCurrentDate()
        val data = readDailyData(context, date)
        val updatedList = data.启发.filter { it.id != itemId }
        val updatedData = data.copy(启发 = updatedList)
        saveDailyData(context, updatedData)
    }

    /**
     * 删除待办项
     */
    fun deleteTodo(context: Context, itemId: String) {
        val date = getCurrentDate()
        val data = readDailyData(context, date)
        val updatedList = data.待办.filter { it.id != itemId }
        val updatedData = data.copy(待办 = updatedList)
        saveDailyData(context, updatedData)
    }

    /**
     * 获取当前日期字符串（YYYY-MM-DD）
     */
    fun getCurrentDate(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return sdf.format(Date())
    }
}
