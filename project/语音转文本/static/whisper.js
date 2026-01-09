/**
 * Whisper 语音识别前端交互逻辑
 */

let currentTaskId = null;
let pollInterval = null;

// 初始化上传区域
function initUploadZone() {
    const uploadZone = document.getElementById('uploadZone');
    const audioInput = document.getElementById('audioInput');

    // 点击上传
    uploadZone.addEventListener('click', () => audioInput.click());

    // 文件选择
    audioInput.addEventListener('change', (e) => {
        if (e.target.files.length > 0) {
            handleFileUpload(e.target.files[0]);
        }
    });

    // 拖拽事件
    uploadZone.addEventListener('dragover', (e) => {
        e.preventDefault();
        uploadZone.classList.add('dragover');
    });

    uploadZone.addEventListener('dragleave', () => {
        uploadZone.classList.remove('dragover');
    });

    uploadZone.addEventListener('drop', (e) => {
        e.preventDefault();
        uploadZone.classList.remove('dragover');

        if (e.dataTransfer.files.length > 0) {
            handleFileUpload(e.dataTransfer.files[0]);
        }
    });
}

// 处理文件上传
async function handleFileUpload(file) {
    // 验证文件
    const validTypes = ['audio/', 'video/'];
    if (!validTypes.some(type => file.type.startsWith(type))) {
        showError('请上传音频或视频文件');
        return;
    }

    if (file.size > 500 * 1024 * 1024) { // 500MB
        showError('文件大小不能超过 500MB');
        return;
    }

    // 获取配置
    const modelSize = document.getElementById('modelSelect').value;
    const outputFormats = [];
    if (document.getElementById('formatTxt').checked) outputFormats.push('txt');
    if (document.getElementById('formatSrt').checked) outputFormats.push('srt');
    if (document.getElementById('formatVtt').checked) outputFormats.push('vtt');

    if (outputFormats.length === 0) {
        showError('请至少选择一种输出格式');
        return;
    }

    // 显示进度
    showProgress();
    addLog(`📤 上传文件：${file.name} (${formatFileSize(file.size)})`);
    addLog(`⚙️ 配置：模型=${modelSize}, 格式=${outputFormats.join(', ')}`);

    // 上传文件
    const formData = new FormData();
    formData.append('audio', file);
    formData.append('model_size', modelSize);
    formData.append('output_formats', JSON.stringify(outputFormats));

    try {
        const response = await fetch('/api/whisper/upload', {
            method: 'POST',
            body: formData
        });

        const result = await response.json();

        if (result.success) {
            currentTaskId = result.task_id;
            addLog(`✅ 任务创建成功：${currentTaskId}`);

            // 开始轮询状态
            startPolling();
        } else {
            showError(result.error || '上传失败');
        }
    } catch (error) {
        showError('网络错误：' + error.message);
    }
}

// 轮询任务状态
function startPolling() {
    if (pollInterval) {
        clearInterval(pollInterval);
    }

    pollInterval = setInterval(async () => {
        try {
            const response = await fetch(`/api/whisper/status/${currentTaskId}`);
            const status = await response.json();

            // 更新进度
            updateProgress(status.progress, status.message);
            addLog(status.message);

            // 检查状态
            if (status.status === 'completed') {
                clearInterval(pollInterval);
                showResult(status.result);
                addLog('🎉 全部完成！');
            } else if (status.status === 'failed') {
                clearInterval(pollInterval);
                showError(status.error || '转换失败');
            }
        } catch (error) {
            console.error('轮询错误:', error);
        }
    }, 2000); // 每 2 秒查询一次
}

// 更新进度显示
function updateProgress(percent, message) {
    const progressBar = document.getElementById('progressBar');
    const progressPercent = document.getElementById('progressPercent');
    const progressMessage = document.getElementById('progressMessage');

    progressBar.style.width = percent + '%';
    progressBar.textContent = percent + '%';
    progressPercent.textContent = percent + '%';
    progressMessage.textContent = message;
}

// 显示结果
function showResult(result) {
    document.getElementById('progressContainer').classList.add('hidden');
    document.getElementById('resultContainer').classList.remove('hidden');

    // 显示文本预览
    if (result.text) {
        const preview = document.getElementById('textPreview');
        const text = result.text;
        const previewText = text.length > 500 ? text.substring(0, 500) + '...' : text;
        preview.textContent = previewText;
    }

    // 显示下载按钮
    if (result.srt_path) {
        const btn = document.getElementById('downloadSrt');
        btn.classList.remove('hidden');
        const filename = result.srt_path.split('/').pop();
        btn.onclick = () => downloadFile(filename);
    }

    if (result.vtt_path) {
        const btn = document.getElementById('downloadVtt');
        btn.classList.remove('hidden');
        const filename = result.vtt_path.split('/').pop();
        btn.onclick = () => downloadFile(filename);
    }

    // TXT 下载（从 text 生成）
    if (result.text) {
        const btn = document.getElementById('downloadTxt');
        btn.classList.remove('hidden');
        btn.onclick = () => downloadTextAsFile(result.text);
    }
}

// 下载文件
async function downloadFile(filename) {
    try {
        const response = await fetch(`/api/whisper/download/${filename}`);

        if (!response.ok) {
            throw new Error('下载失败');
        }

        const blob = await response.blob();
        const url = window.URL.createObjectURL(blob);
        const link = document.createElement('a');
        link.href = url;
        link.download = filename;
        link.click();
        window.URL.revokeObjectURL(url);
    } catch (error) {
        console.error('下载错误:', error);
        alert('下载失败：' + error.message);
    }
}

// 下载文本
function downloadTextAsFile(text) {
    const blob = new Blob([text], { type: 'text/plain;charset=utf-8' });
    const url = URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = url;
    link.download = `transcript_${Date.now()}.txt`;
    link.click();
    URL.revokeObjectURL(url);
}

// 显示进度
function showProgress() {
    document.getElementById('uploadZone').classList.add('hidden');
    document.getElementById('progressContainer').classList.remove('hidden');
    document.getElementById('resultContainer').classList.add('hidden');
    document.getElementById('errorContainer').classList.add('hidden');
}

// 显示错误
function showError(message) {
    document.getElementById('uploadZone').classList.remove('hidden');
    document.getElementById('progressContainer').classList.add('hidden');
    document.getElementById('resultContainer').classList.add('hidden');

    document.getElementById('errorContainer').classList.remove('hidden');
    document.getElementById('errorMessage').textContent = message;
}

// 重置 UI
function resetUI() {
    if (pollInterval) {
        clearInterval(pollInterval);
        pollInterval = null;
    }

    currentTaskId = null;

    document.getElementById('uploadZone').classList.remove('hidden');
    document.getElementById('progressContainer').classList.add('hidden');
    document.getElementById('resultContainer').classList.add('hidden');
    document.getElementById('errorContainer').classList.add('hidden');

    document.getElementById('progressBar').style.width = '0%';
    document.getElementById('logContent').innerHTML = '';
    document.getElementById('audioInput').value = '';

    // 重置下载按钮
    document.getElementById('downloadTxt').classList.add('hidden');
    document.getElementById('downloadSrt').classList.add('hidden');
    document.getElementById('downloadVtt').classList.add('hidden');
}

// 添加日志
function addLog(message) {
    const logContent = document.getElementById('logContent');
    const time = new Date().toLocaleTimeString();
    const logEntry = document.createElement('div');
    logEntry.textContent = `[${time}] ${message}`;
    logContent.appendChild(logEntry);
    logContent.scrollTop = logContent.scrollHeight;
}

// 格式化文件大小
function formatFileSize(bytes) {
    if (bytes < 1024) return bytes + ' B';
    if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(1) + ' KB';
    return (bytes / (1024 * 1024)).toFixed(1) + ' MB';
}

// 初始化
document.addEventListener('DOMContentLoaded', () => {
    initUploadZone();
});
