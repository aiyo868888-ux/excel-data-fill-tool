package com.jishi.clipboard.network.security

import android.util.Log
import java.util.concurrent.ConcurrentHashMap

/**
 * 配对Token管理器
 * 负责生成和验证6位数字配对码
 */
object TokenManager {
    private const val TAG = "TokenManager"
    private const val TOKEN_VALID_DURATION = 5 * 60 * 1000 // 5分钟有效期

    // 存储活跃的Token及其创建时间
    private val tokens = ConcurrentHashMap<String, Long>()

    /**
     * 生成6位随机数字配对码（线程安全）
     * 使用putIfAbsent确保原子性，避免重复
     */
    fun generateToken(): String {
        while (true) {
            val token = (100000..999999).random().toString()
            val timestamp = System.currentTimeMillis()
            // 原子操作：只有当token不存在时才插入
            if (tokens.putIfAbsent(token, timestamp) == null) {
                Log.d(TAG, "生成新Token: $token")
                return token
            }
            // 如果token已存在，重新生成
        }
    }

    /**
     * 验证Token是否有效
     * @param token 要验证的配对码
     * @return true如果Token有效且未过期
     */
    fun validateToken(token: String?): Boolean {
        if (token == null) {
            Log.w(TAG, "Token为null，验证失败")
            return false
        }

        val timestamp = tokens[token] ?: run {
            Log.w(TAG, "Token不存在: $token")
            return false
        }

        val currentTime = System.currentTimeMillis()
        val elapsedTime = currentTime - timestamp

        return if (elapsedTime > TOKEN_VALID_DURATION) {
            Log.w(TAG, "Token已过期: $token (已存在${elapsedTime / 1000}秒)")
            tokens.remove(token)
            false
        } else {
            Log.d(TAG, "Token验证成功: $token (剩余${(TOKEN_VALID_DURATION - elapsedTime) / 1000}秒)")
            true
        }
    }

    /**
     * 撤销Token（主动断开连接时调用）
     */
    fun revokeToken(token: String) {
        tokens.remove(token)
        Log.d(TAG, "撤销Token: $token")
    }

    /**
     * 清理所有过期的Token
     * 建议定期调用以释放内存
     */
    fun cleanupExpiredTokens() {
        val currentTime = System.currentTimeMillis()
        val expiredTokens = tokens.filterValues { timestamp ->
            (currentTime - timestamp) > TOKEN_VALID_DURATION
        }.keys

        expiredTokens.forEach { tokens.remove(it) }

        if (expiredTokens.isNotEmpty()) {
            Log.d(TAG, "清理了${expiredTokens.size}个过期Token")
        }
    }

    /**
     * 获取当前活跃的Token数量
     */
    fun getActiveTokenCount(): Int = tokens.size
}
