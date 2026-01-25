package com.fleetingnotes.presentation.ui.components

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow

/**
 * 语音识别状态
 */
sealed class VoiceInputState {
    object Idle : VoiceInputState()
    object Initializing : VoiceInputState()
    object Listening : VoiceInputState()
    data class Processing(val text: String) : VoiceInputState()
    data class Error(val message: String) : VoiceInputState()
    data class Success(val text: String) : VoiceInputState()
}

/**
 * 语音输入按钮组件
 */
@Composable
fun VoiceInputButton(
    state: VoiceInputState = VoiceInputState.Idle,
    hasPermission: Boolean = true,
    onRequestPermission: () -> Unit = {},
    onStart: () -> Unit = {},
    onStop: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    // 语音按钮
    Box(
        modifier = modifier
            .size(56.dp)
            .background(
                color = when (state) {
                    is VoiceInputState.Listening -> Color.Red
                    else -> MaterialTheme.colorScheme.primary
                },
                shape = CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        IconButton(
            onClick = {
                when {
                    !hasPermission -> {
                        onRequestPermission()
                    }
                    state is VoiceInputState.Listening -> {
                        onStop()
                    }
                    else -> {
                        onStart()
                    }
                }
            }
        ) {
            Icon(
                imageVector = if (state is VoiceInputState.Listening) {
                    Icons.Default.Stop
                } else {
                    Icons.Default.Mic
                },
                contentDescription = if (state is VoiceInputState.Listening) "停止录音" else "开始录音",
                tint = Color.White,
                modifier = Modifier.size(28.dp)
            )
        }
    }
}

/**
 * 语音识别管理器
 */
@Composable
fun rememberVoiceInputManager(): VoiceInputManager {
    val context = LocalContext.current
    return remember { VoiceInputManager(context) }
}

/**
 * 语音识别管理器
 */
class VoiceInputManager(private val context: android.content.Context) {
    private var speechRecognizer: SpeechRecognizer? = null

    fun startListening(
        onResult: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        if (speechRecognizer == null) {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context)
        }

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "zh-CN")
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
        }

        speechRecognizer?.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {}
            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEndOfSpeech() {}
            override fun onError(error: Int) {
                val errorMessage = when (error) {
                    SpeechRecognizer.ERROR_AUDIO -> "音频错误"
                    SpeechRecognizer.ERROR_CLIENT -> "客户端错误"
                    SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "权限不足"
                    SpeechRecognizer.ERROR_NETWORK -> "网络错误"
                    SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "网络超时"
                    SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "识别器忙"
                    SpeechRecognizer.ERROR_NO_MATCH -> "无法识别"
                    SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "没有检测到语音"
                    else -> "未知错误"
                }
                onError(errorMessage)
            }

            override fun onResults(results: Bundle?) {
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                if (!matches.isNullOrEmpty()) {
                    onResult(matches[0])
                }
            }

            override fun onPartialResults(partialResults: Bundle?) {
                val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                if (!matches.isNullOrEmpty()) {
                    onResult(matches[0])
                }
            }

            override fun onEvent(eventType: Int, params: Bundle?) {}
        })

        speechRecognizer?.startListening(intent)
    }

    fun stopListening() {
        try {
            speechRecognizer?.stopListening()
        } catch (e: Exception) {
            // 忽略停止时的错误
        }
    }

    fun destroy() {
        speechRecognizer?.destroy()
        speechRecognizer = null
    }
}

/**
 * 语音输入完整组件
 */
@Composable
fun VoiceInput(
    onTextRecognized: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var voiceState by remember { mutableStateOf<VoiceInputState>(VoiceInputState.Idle) }
    val voiceManager = rememberVoiceInputManager()

    // 简化的权限检查 - 注意：实际使用时需要在 Activity/Fragment 中请求权限
    var hasPermission by remember {
        mutableStateOf(
            android.content.pm.PackageManager.PERMISSION_GRANTED ==
            context.checkSelfPermission(Manifest.permission.RECORD_AUDIO)
        )
    }

    // 监听生命周期，自动停止语音识别
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_PAUSE) {
                if (voiceState is VoiceInputState.Listening) {
                    voiceManager.stopListening()
                    voiceState = VoiceInputState.Idle
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            voiceManager.destroy()
        }
    }

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // 状态文本
        when (voiceState) {
            is VoiceInputState.Listening -> {
                Text(
                    text = "🎤 正在录音...",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Red
                )
            }
            is VoiceInputState.Processing -> {
                Text(
                    text = "处理中...",
                    style = MaterialTheme.typography.bodySmall
                )
            }
            is VoiceInputState.Error -> {
                Text(
                    text = "❌ ${(voiceState as VoiceInputState.Error).message}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Red
                )
            }
            is VoiceInputState.Idle -> {
                if (!hasPermission) {
                    Text(
                        text = "需要录音权限",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFFF9800)
                    )
                }
            }
            else -> {}
        }

        VoiceInputButton(
            state = voiceState,
            hasPermission = hasPermission,
            onRequestPermission = {
                // 注意：这里需要在 Activity 中使用 registerForActivityResult 请求权限
                // 这是一个简化版本，实际实现需要从 Activity 传递权限请求回调
            },
            onStart = {
                voiceState = VoiceInputState.Initializing
                voiceManager.startListening(
                    onResult = { text ->
                        voiceState = VoiceInputState.Processing(text)
                    },
                    onError = { error ->
                        voiceState = VoiceInputState.Error(error)
                    }
                )
                voiceState = VoiceInputState.Listening
            },
            onStop = {
                voiceManager.stopListening()
                if (voiceState is VoiceInputState.Processing) {
                    val text = (voiceState as VoiceInputState.Processing).text
                    if (text.isNotBlank()) {
                        onTextRecognized(text)
                    }
                }
                voiceState = VoiceInputState.Idle
            }
        )
    }
}
