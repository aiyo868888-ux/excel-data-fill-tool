// imageToExcel 前端交互脚本

document.addEventListener('DOMContentLoaded', function() {
    const form = document.getElementById('uploadForm');
    const excelFile = document.getElementById('excelFile');
    const imageFiles = document.getElementById('imageFiles');
    const dataFile = document.getElementById('dataFile');
    const customData = document.getElementById('customData');
    const submitBtn = document.getElementById('submitBtn');

    // 文件显示函数
    function showFileInfo(inputId, infoId, multiple = false) {
        const input = document.getElementById(inputId);
        const info = document.getElementById(infoId);
        const files = input.files;

        if (files.length > 0) {
            info.innerHTML = '';
            info.classList.add('show');

            if (multiple) {
                const title = document.createElement('div');
                title.innerHTML = `<strong>已选择 ${files.length} 个文件:</strong>`;
                info.appendChild(title);

                Array.from(files).forEach((file, index) => {
                    const item = document.createElement('div');
                    item.className = 'file-info-item';
                    item.innerHTML = `
                        <span>${index + 1}. ${file.name}</span>
                        <span style="color: #666; font-size: 0.9em;">(${(file.size / 1024).toFixed(1)} KB)</span>
                        <button type="button" class="remove-btn" onclick="removeFile('${inputId}', ${index})">删除</button>
                    `;
                    info.appendChild(item);
                });
            } else {
                const item = document.createElement('div');
                item.className = 'file-info-item';
                item.innerHTML = `
                    <span>${files[0].name}</span>
                    <span style="color: #666; font-size: 0.9em;">(${(files[0].size / 1024).toFixed(1)} KB)</span>
                    <button type="button" class="remove-btn" onclick="clearFile('${inputId}')">删除</button>
                `;
                info.appendChild(item);
            }
        } else {
            info.classList.remove('show');
        }
    }

    // 全局函数：删除文件
    window.clearFile = function(inputId) {
        const input = document.getElementById(inputId);
        input.value = '';
        showFileInfo(inputId, inputId.replace('File', 'FileInfo'));
    };

    window.removeFile = function(inputId, index) {
        const input = document.getElementById(inputId);
        const dt = new DataTransfer();
        const files = Array.from(input.files);

        files.splice(index, 1);
        files.forEach(file => dt.items.add(file));

        input.files = dt.files;
        showFileInfo(inputId, inputId.replace('File', 'FileInfo'), true);
    };

    // 文件选择监听
    excelFile.addEventListener('change', () => showFileInfo('excelFile', 'excelFileInfo'));
    imageFiles.addEventListener('change', () => showFileInfo('imageFiles', 'imageFileInfo', true));
    dataFile.addEventListener('change', () => showFileInfo('dataFile', 'dataFileInfo'));

    // 拖拽上传支持
    function setupDragDrop(dropZoneId, inputId) {
        const dropZone = document.getElementById(dropZoneId);
        const input = document.getElementById(inputId);

        ['dragenter', 'dragover', 'dragleave', 'drop'].forEach(eventName => {
            dropZone.addEventListener(eventName, preventDefaults, false);
        });

        function preventDefaults(e) {
            e.preventDefault();
            e.stopPropagation();
        }

        dropZone.addEventListener('dragover', () => {
            dropZone.classList.add('dragover');
        });

        dropZone.addEventListener('dragleave', () => {
            dropZone.classList.remove('dragover');
        });

        dropZone.addEventListener('drop', (e) => {
            dropZone.classList.remove('dragover');
            const files = e.dataTransfer.files;

            if (input.multiple) {
                input.files = files;
            } else {
                input.files = files;
            }

            // 触发change事件
            const event = new Event('change');
            input.dispatchEvent(event);
        });
    }

    setupDragDrop('excelDropZone', 'excelFile');
    setupDragDrop('imageDropZone', 'imageFiles');

    // JSON验证
    customData.addEventListener('input', function() {
        const validator = document.getElementById('jsonValidator');
        const value = this.value.trim();

        if (value === '') {
            validator.style.display = 'none';
            return;
        }

        try {
            JSON.parse(value);
            validator.textContent = '✓ JSON格式正确';
            validator.className = 'json-validator valid';
        } catch (e) {
            validator.textContent = '✗ JSON格式错误: ' + e.message;
            validator.className = 'json-validator invalid';
        }
    });

    // 表单提交
    form.addEventListener('submit', async function(e) {
        e.preventDefault();

        // 验证
        if (!excelFile.files || excelFile.files.length === 0) {
            alert('请上传Excel文件');
            return;
        }

        if (imageFiles.files.length === 0) {
            const confirm = window.confirm('未上传图片，是否继续？');
            if (!confirm) return;
        }

        // 禁用提交按钮
        submitBtn.disabled = true;
        submitBtn.innerHTML = '<span class="btn-icon">⏳</span> 处理中...';

        // 显示加载状态
        const resultSection = document.getElementById('resultSection');
        const resultContent = document.getElementById('resultContent');
        resultSection.style.display = 'block';
        resultContent.innerHTML = `
            <div class="loading">
                <div class="spinner"></div>
                <p>正在处理，请稍候...</p>
            </div>
        `;

        // 滚动到结果区域
        resultSection.scrollIntoView({ behavior: 'smooth' });

        try {
            const formData = new FormData(form);

            const response = await fetch('/api/process', {
                method: 'POST',
                body: formData
            });

            const result = await response.json();

            if (result.success) {
                // 成功
                resultContent.innerHTML = `
                    <div class="success-message">
                        <div class="success-icon">✅</div>
                        <h3 class="success-title">${result.message}</h3>
                        <div class="success-details">
                            <ul>
                                <li>📄 Excel文件: ${result.data.excel_file}</li>
                                <li>🖼️ 图片数量: ${result.data.image_count}</li>
                                ${result.data.has_custom_data ? '<li>✏️ 使用了自定义数据</li>' : ''}
                                ${result.data.has_data_file ? '<li>📊 使用了数据文件</li>' : ''}
                            </ul>
                        </div>
                        <a href="/download/${result.output_file}" class="btn-download" download>
                            📥 下载生成的Excel文件
                        </a>
                    </div>
                `;
            } else {
                // 失败
                resultContent.innerHTML = `
                    <div class="error-message">
                        <h3>❌ 处理失败</h3>
                        <p>${result.error}</p>
                    </div>
                `;
            }
        } catch (error) {
            resultContent.innerHTML = `
                <div class="error-message">
                    <h3>❌ 网络错误</h3>
                    <p>请检查网络连接后重试</p>
                    <p style="font-size: 0.9em; color: #666;">错误详情: ${error.message}</p>
                </div>
            `;
        } finally {
            // 恢复提交按钮
            submitBtn.disabled = false;
            submitBtn.innerHTML = '<span class="btn-icon">⚡</span> 开始处理';
        }
    });

    // 页面加载时检查技能状态
    fetch('/api/health')
        .then(res => res.json())
        .then(data => {
            if (!data.skill_available) {
                const resultSection = document.getElementById('resultSection');
                const resultContent = document.getElementById('resultContent');
                resultSection.style.display = 'block';
                resultContent.innerHTML = `
                    <div class="error-message">
                        <h3>⚠️ 技能不可用</h3>
                        <p>image-table-excel技能无法加载，请检查路径配置</p>
                    </div>
                `;
            }
        })
        .catch(err => console.error('健康检查失败:', err));
});
