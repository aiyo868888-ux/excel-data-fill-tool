package com.jishi.clipboard.network.server

import android.content.Context
import android.util.Log
import java.io.File
import java.security.KeyStore
import java.security.SecureRandom
import java.security.cert.Certificate
import java.security.cert.X509Certificate
import java.util.Date
import javax.security.auth.x500.X500Principal
import java.math.BigInteger
import java.security.KeyPairGenerator
import java.security.PrivateKey
import java.security.PublicKey
import java.security.Signature
import java.security.cert.CertificateFactory
import java.io.ByteArrayInputStream

/**
 * SSL证书生成和管理工具（简化版）
 * 使用KeyTool预生成证书或使用HTTP模式
 */
object SslCertificateHelper {
    private const val TAG = "SslCertificateHelper"
    private const val KEYSTORE_FILE = "jishiclipboard.jks"
    private const val KEYSTORE_PASSWORD = "changeit"
    private const val KEY_ALIAS = "jishiclipboard"

    /**
     * 加载或创建KeyStore
     * 注意：首次使用时需要手动生成证书或使用HTTP模式
     */
    fun loadKeyStore(context: Context): KeyStore {
        val keyStoreFile = File(context.filesDir, KEYSTORE_FILE)
        val keyStore = KeyStore.getInstance("JKS")

        return if (keyStoreFile.exists()) {
            keyStoreFile.inputStream().use { fis ->
                keyStore.load(fis, KEYSTORE_PASSWORD.toCharArray())
            }
            Log.d(TAG, "加载现有KeyStore")
            keyStore
        } else {
            Log.w(TAG, "证书文件不存在，将使用HTTP模式")
            Log.w(TAG, "请运行: keytool -genkey -alias jishiclipboard -keyalg RSA -keystore jishiclipboard.jks -storepass changeit -validity 365")
            // 创建空KeyStore
            keyStore.load(null, null)
            keyStore
        }
    }

    /**
     * 检查证书是否存在
     */
    fun certificateExists(context: Context): Boolean {
        val keyStoreFile = File(context.filesDir, KEYSTORE_FILE)
        return keyStoreFile.exists()
    }

    /**
     * 删除证书
     */
    fun resetCertificate(context: Context): Boolean {
        return try {
            val keyStoreFile = File(context.filesDir, KEYSTORE_FILE)
            if (keyStoreFile.exists()) {
                keyStoreFile.delete()
                Log.d(TAG, "证书已删除")
            }
            true
        } catch (e: Exception) {
            Log.e(TAG, "删除证书失败", e)
            false
        }
    }

    /**
     * 获取证书信息
     */
    fun getCertificateInfo(context: Context): String {
        return try {
            val keyStore = loadKeyStore(context)
            val cert = keyStore.getCertificate(KEY_ALIAS) as? X509Certificate
            if (cert != null) {
                """
                    主体: ${cert.subjectDN}
                    颁发者: ${cert.issuerDN}
                    有效期: ${cert.notBefore} 到 ${cert.notAfter}
                """.trimIndent()
            } else {
                "未找到证书，请使用HTTP模式"
            }
        } catch (e: Exception) {
            "获取证书信息失败: ${e.message}"
        }
    }
}
