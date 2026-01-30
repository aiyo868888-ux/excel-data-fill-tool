package com.jishi.clipboard.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import org.json.JSONArray
import org.json.JSONException
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

/**
 * 图片处理工具类
 * 负责图片的压缩、存储、JSON 转换等功能
 */
object ImageUtils {

    /**
     * 将图片路径列表转换为 JSON 字符串
     */
    fun convertImagesToJson(images: List<String>?): String? {
        if (images.isNullOrEmpty()) return null
        val jsonArray = JSONArray()
        images.forEach { jsonArray.put(it) }
        return jsonArray.toString()
    }

    /**
     * 将 JSON 字符串解析为图片路径列表
     */
    fun parseImagesFromJson(json: String?): List<String> {
        if (json.isNullOrEmpty()) return emptyList()
        val images = mutableListOf<String>()
        try {
            val jsonArray = JSONArray(json)
            for (i in 0 until jsonArray.length()) {
                images.add(jsonArray.getString(i))
            }
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        return images
    }

    /**
     * 保存图片到应用私有目录
     * @param context 上下文
     * @param uri 图片 URI
     * @param maxWidth 最大宽度（用于压缩）
     * @param maxHeight 最大高度（用于压缩）
     * @return 保存后的文件路径，失败返回 null
     */
    fun saveImageToLocal(
        context: Context,
        uri: Uri,
        maxWidth: Int = 1080,
        maxHeight: Int = 1920
    ): String? {
        try {
            // 读取原始图片
            val inputStream = context.contentResolver.openInputStream(uri) ?: return null
            val originalBitmap = BitmapFactory.decodeStream(inputStream)
            inputStream.close()

            // 压缩图片
            val compressedBitmap = compressBitmap(originalBitmap, maxWidth, maxHeight)

            // 生成唯一文件名
            val imageDir = File(context.filesDir, "images")
            if (!imageDir.exists()) {
                imageDir.mkdirs()
            }
            val fileName = "${UUID.randomUUID()}.jpg"
            val imageFile = File(imageDir, fileName)

            // 保存图片
            FileOutputStream(imageFile).use { outputStream ->
                compressedBitmap.compress(Bitmap.CompressFormat.JPEG, 85, outputStream)
            }

            // 回收 Bitmap
            if (compressedBitmap != originalBitmap) {
                compressedBitmap.recycle()
            }
            originalBitmap.recycle()

            return imageFile.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    /**
     * 压缩 Bitmap
     */
    private fun compressBitmap(bitmap: Bitmap, maxWidth: Int, maxHeight: Int): Bitmap {
        val width = bitmap.width
        val height = bitmap.height

        // 如果图片已经足够小，不需要压缩
        if (width <= maxWidth && height <= maxHeight) {
            return bitmap
        }

        // 计算缩放比例
        val scaleWidth = maxWidth.toFloat() / width
        val scaleHeight = maxHeight.toFloat() / height
        val scale = minOf(scaleWidth, scaleHeight)

        val newWidth = (width * scale).toInt()
        val newHeight = (height * scale).toInt()

        return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
    }

    /**
     * 删除图片文件
     * @param imagePath 图片路径
     * @return 是否删除成功
     */
    fun deleteImage(imagePath: String): Boolean {
        return try {
            val file = File(imagePath)
            if (file.exists()) {
                file.delete()
            } else {
                false
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * 删除多张图片
     * @param images 图片路径列表
     */
    fun deleteImages(images: List<String>?) {
        images?.forEach { deleteImage(it) }
    }

    /**
     * 获取图片文件大小（KB）
     */
    fun getImageSize(imagePath: String): Long {
        return try {
            val file = File(imagePath)
            if (file.exists()) {
                file.length() / 1024 // 返回 KB
            } else {
                0
            }
        } catch (e: Exception) {
            0
        }
    }

    /**
     * 检查图片文件是否存在
     */
    fun isImageExists(imagePath: String): Boolean {
        return try {
            File(imagePath).exists()
        } catch (e: Exception) {
            false
        }
    }
}
