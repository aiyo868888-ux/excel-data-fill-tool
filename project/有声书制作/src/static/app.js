/**
 * SonicLab - 音效生成工具前端交互
 */

// 全局状态
let soundTypes = {};
let currentSoundType = null;
let currentTaskId = null;
let currentFilename = null;
let generatedFiles = [];

// 页面加载时初始化
document.addEventListener('DOMContentLoaded', async () => {
    await loadSoundTypes();
});

// 加载音效类型
async function loadSoundTypes() {
    try {
        const response = await fetch('/api/sound_types');
        const data = await response.json();
        if (data.success) {
            soundTypes = data.sound_types;
            renderSoundTypeGrid();
        }
    } catch (error) {
        console.error('加载音效类型失败:', error);
        showError('无法加载音效类型，请刷新页面重试');
    }
}

// 渲染音效类型网格
function renderSoundTypeGrid() {
    const grid = document.getElementById('soundTypeGrid');

    grid.innerHTML = '';

    Object.entries(soundTypes).forEach(([type, info]) => {
        const card = document.createElement('div');
        card.className = 'sound-card';
        card.onclick = () => selectSoundType(type);

        card.innerHTML = `
            <div class="sound-icon">${getSoundIcon(type)}</div>
            <div class="sound-name">${info.name}</div>
            <div class="sound-desc">${info.description}</div>
        `;

        grid.appendChild(card);
    });
}

// 获取音效图标
function getSoundIcon(type) {
    const icons = {
        'white_noise': '📻',
        'pink_noise': '🌸',
        'brown_noise': '🟤',
        'rain': '🌧️',
        'wind': '💨',
        'thunder': '⛈️',
        'waves': '🌊',
        'forest': '🌲',
        'door_open': '🚪',
        'door_close': '🚪',
        'dog_bark': '🐕',
        'footsteps': '👣'
    };
    return icons[type] || '🎵';
}

// 选择音效类型
function selectSoundType(type) {
    currentSoundType = type;

    // 更新UI状态
    document.querySelectorAll('.sound-card').forEach(card => {
        card.classList.remove('active');
    });
    event.currentTarget.classList.add('active');

    // 更新工作流程
    updateWorkflow(2);

    // 渲染参数
    renderParams();

    // 启用生成按钮
    document.getElementById('generateBtn').disabled = false;
    document.getElementById('currentSoundName').textContent = soundTypes[type].name;
}

// 更新工作流程指示器
function updateWorkflow(step) {
    for (let i = 1; i <= 3; i++) {
        const stepEl = document.getElementById(`step${i}`);
        if (stepEl) {
            if (i <= step) {
                stepEl.classList.add('active');
            } else {
                stepEl.classList.remove('active');
            }
        }
    }
}

// 渲染参数控件
function renderParams() {
    const container = document.getElementById('paramsContainer');
    const info = soundTypes[currentSoundType];

    if (!info) return;

    container.innerHTML = '';

    Object.entries(info.params).forEach(([paramName, config]) => {
        const row = document.createElement('div');
        row.className = 'param-row';

        const defaultValue = config.default;
        const step = config.step || (paramName === 'duration' || paramName === 'fade_in' || paramName === 'fade_out' ? 0.1 : 1);

        row.innerHTML = `
            <div class="param-label">${getParamLabel(paramName)}</div>
            <input
                type="range"
                class="param-slider"
                id="${paramName}Slider"
                min="${config.min}"
                max="${config.max}"
                step="${step}"
                value="${defaultValue}"
                oninput="updateParamValue('${paramName}', this.value)"
            >
            <div class="param-value mono" id="${paramName}Value">${defaultValue}${getUnitSymbol(config.unit)}</div>
        `;
        container.appendChild(row);
    });
}

// 获取参数标签
function getParamLabel(paramName) {
    const labels = {
        'duration': '时长',
        'volume': '音量',
        'intensity': '强度',
        'speed': '速度',
        'frequency': '频率',
        'fade_in': '淡入',
        'fade_out': '淡出'
    };
    return labels[paramName] || paramName;
}

// 获取单位符号
function getUnitSymbol(unit) {
    const symbols = {
        '秒': 's',
        '%': '%',
        '倍': 'x',
        'Hz': 'Hz'
    };
    return symbols[unit] || unit;
}

// 更新参数值显示
function updateParamValue(paramName, value) {
    const info = soundTypes[currentSoundType];
    const unit = info.params[paramName].unit;
    const symbol = getUnitSymbol(unit);

    document.getElementById(`${paramName}Value`).textContent = value + symbol;
}

// 获取当前参数
function getCurrentParams() {
    const info = soundTypes[currentSoundType];
    const params = {};

    Object.keys(info.params).forEach(paramName => {
        const slider = document.getElementById(`${paramName}Slider`);
        if (slider) {
            params[paramName] = parseFloat(slider.value);
        }
    });

    return params;
}

// 生成音效
async function generateSound() {
    if (!currentSoundType) {
        showError('请先选择一个音效类型');
        return;
    }

    const btn = document.getElementById('generateBtn');
    const progressContainer = document.getElementById('progressContainer');
    const downloadContainer = document.getElementById('downloadContainer');

    // 禁用按钮
    btn.disabled = true;
    btn.innerHTML = '<span>⏳</span><span>生成中...</span>';

    // 隐藏成功状态，显示进度
    downloadContainer.classList.remove('active');
    progressContainer.classList.add('active');

    // 更新工作流程
    updateWorkflow(3);

    try {
        const response = await fetch('/api/generate', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
                sound_type: currentSoundType,
                params: getCurrentParams()
            })
        });

        const data = await response.json();

        if (data.success) {
            currentTaskId = data.task_id;
            pollTaskStatus();
        } else {
            throw new Error(data.error);
        }
    } catch (error) {
        console.error('生成失败:', error);
        showError('生成失败: ' + error.message);
        resetGenerateButton();
        progressContainer.classList.remove('active');
    }
}

// 轮询任务状态
function pollTaskStatus() {
    if (!currentTaskId) return;

    const interval = setInterval(async () => {
        try {
            const response = await fetch(`/api/status/${currentTaskId}`);
            const data = await response.json();

            if (data.success) {
                // 更新进度
                updateProgress(data.progress, data.message);

                // 检查是否完成
                if (data.status === 'completed' && data.result) {
                    clearInterval(interval);
                    onGenerationComplete(data.result);
                } else if (data.status === 'failed') {
                    clearInterval(interval);
                    throw new Error(data.error || '生成失败');
                }
            }
        } catch (error) {
            clearInterval(interval);
            console.error('查询状态失败:', error);
            showError('生成失败: ' + error.message);
            resetGenerateButton();
        }
    }, 500);
}

// 更新进度条
function updateProgress(progress, message) {
    document.getElementById('progressBar').style.width = progress + '%';
    document.getElementById('progressPercent').textContent = progress + '%';
    document.getElementById('progressMessage').textContent = message;
}

// 生成完成
function onGenerationComplete(result) {
    const progressContainer = document.getElementById('progressContainer');
    const downloadContainer = document.getElementById('downloadContainer');
    const previewBtn = document.getElementById('previewBtn');

    // 隐藏进度
    progressContainer.classList.remove('active');

    // 显示成功状态
    currentFilename = result.filename;
    downloadContainer.classList.add('active');
    document.getElementById('downloadFileName').textContent = result.filename;

    // 启用试听
    previewBtn.disabled = false;
    previewBtn.onclick = () => previewSound(`/api/download/${result.filename}`);

    // 添加到文件列表
    addToFileList(result.filename);

    // 重置生成按钮
    resetGenerateButton();
}

// 重置生成按钮
function resetGenerateButton() {
    const btn = document.getElementById('generateBtn');
    btn.disabled = false;
    btn.innerHTML = '<span>🎬</span><span>生成音效</span>';
}

// 试听音效
function previewSound(url) {
    if (!url) {
        showError('请先生成音效');
        return;
    }

    const audio = new Audio(url);
    audio.play();
}

// 下载当前文件
function downloadCurrentFile() {
    if (currentFilename) {
        window.location.href = `/api/download/${currentFilename}`;
    }
}

// 重置生成器
function resetGenerator() {
    // 隐藏成功状态
    document.getElementById('downloadContainer').classList.remove('active');

    // 禁用试听
    document.getElementById('previewBtn').disabled = true;

    // 重置工作流程
    updateWorkflow(2);

    currentFilename = null;
}

// 添加到文件列表
function addToFileList(filename) {
    if (!generatedFiles.includes(filename)) {
        generatedFiles.push(filename);
        renderFileList();
    }
}

// 渲染文件列表
function renderFileList() {
    const container = document.getElementById('fileList');
    const countEl = document.getElementById('fileCount');

    countEl.textContent = generatedFiles.length;

    if (generatedFiles.length === 0) {
        container.innerHTML = '<p style="text-align: center; color: var(--text-muted); font-size: 0.875rem;">暂无文件</p>';
        return;
    }

    container.innerHTML = generatedFiles.map(filename => `
        <div class="file-item">
            <span class="file-name">${filename}</span>
            <div class="file-actions">
                <button class="icon-btn" onclick="previewSound('/api/download/${filename}')" title="试听">
                    ▶️
                </button>
                <button class="icon-btn" onclick="window.location.href='/api/download/${filename}'" title="下载">
                    📥
                </button>
            </div>
        </div>
    `).join('');
}

// 显示错误信息
function showError(message) {
    alert(message); // 可以改进为更好的错误显示方式
}
