package com.jishi.clipboard.network.gist

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

/**
 * Gist Token 管理器
 * 使用 EncryptedSharedPreferences 安全存储敏感 Token
 */
class GistTokenManager(context: Context) {

    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val sharedPreferences: SharedPreferences = EncryptedSharedPreferences.create(
        context,
        PREF_NAME,
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    companion object {
        private const val PREF_NAME = "gist_tokens"
        private const val KEY_GITHUB_TOKEN = "github_token"
        private const val KEY_GITEE_TOKEN = "gitee_token"
        private const val KEY_GITHUB_GIST_ID = "github_gist_id"
        private const val KEY_GITEE_GIST_ID = "gitee_gist_id"
        private const val KEY_LAST_SYNC_TIME = "last_sync_time"
        private const val KEY_DEVICE_ID = "device_id"
    }

    // GitHub Token 相关
    fun saveGitHubToken(token: String) {
        sharedPreferences.edit { putString(KEY_GITHUB_TOKEN, token) }
    }

    fun getGitHubToken(): String? {
        return sharedPreferences.getString(KEY_GITHUB_TOKEN, null)
    }

    fun clearGitHubToken() {
        sharedPreferences.edit { remove(KEY_GITHUB_TOKEN) }
    }

    // Gitee Token 相关
    fun saveGiteeToken(token: String) {
        sharedPreferences.edit { putString(KEY_GITEE_TOKEN, token) }
    }

    fun getGiteeToken(): String? {
        return sharedPreferences.getString(KEY_GITEE_TOKEN, null)
    }

    fun clearGiteeToken() {
        sharedPreferences.edit { remove(KEY_GITEE_TOKEN) }
    }

    // Gist ID 相关
    fun saveGitHubGistId(gistId: String) {
        sharedPreferences.edit { putString(KEY_GITHUB_GIST_ID, gistId) }
    }

    fun getGitHubGistId(): String? {
        return sharedPreferences.getString(KEY_GITHUB_GIST_ID, null)
    }

    fun clearGitHubGistId() {
        sharedPreferences.edit { remove(KEY_GITHUB_GIST_ID) }
    }

    fun saveGiteeGistId(gistId: String) {
        sharedPreferences.edit { putString(KEY_GITEE_GIST_ID, gistId) }
    }

    fun getGiteeGistId(): String? {
        return sharedPreferences.getString(KEY_GITEE_GIST_ID, null)
    }

    fun clearGiteeGistId() {
        sharedPreferences.edit { remove(KEY_GITEE_GIST_ID) }
    }

    // 同步时间相关
    fun saveLastSyncTime(timestamp: Long) {
        sharedPreferences.edit { putLong(KEY_LAST_SYNC_TIME, timestamp) }
    }

    fun getLastSyncTime(): Long {
        return sharedPreferences.getLong(KEY_LAST_SYNC_TIME, 0L)
    }

    // 设备 ID 相关
    fun getDeviceId(): String {
        var deviceId = sharedPreferences.getString(KEY_DEVICE_ID, null)
        if (deviceId == null) {
            deviceId = generateDeviceId()
            sharedPreferences.edit { putString(KEY_DEVICE_ID, deviceId) }
        }
        return deviceId
    }

    private fun generateDeviceId(): String {
        return "android_${System.currentTimeMillis()}"
    }

    // 工具方法
    fun hasValidGitHubToken(): Boolean {
        return getGitHubToken().isNullOrEmpty().not()
    }

    fun hasValidGiteeToken(): Boolean {
        return getGiteeToken().isNullOrEmpty().not()
    }

    fun isConfigured(): Boolean {
        return hasValidGitHubToken() || hasValidGiteeToken()
    }

    fun clearAll() {
        sharedPreferences.edit {
            remove(KEY_GITHUB_TOKEN)
            remove(KEY_GITEE_TOKEN)
            remove(KEY_GITHUB_GIST_ID)
            remove(KEY_GITEE_GIST_ID)
            remove(KEY_LAST_SYNC_TIME)
        }
    }
}
