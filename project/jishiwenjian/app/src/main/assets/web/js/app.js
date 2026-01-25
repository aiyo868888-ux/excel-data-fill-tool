// Web同步客户端
class WebSyncClient {
    constructor() {
        this.serverUrl = window.location.origin;
        this.token = null;
        this.ws = null;
    }

    // 配对设备
    async pairDevice(token) {
        try {
            const response = await fetch(`${this.serverUrl}/api/pair`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({ token })
            });

            const data = await response.json();
            return data.success;
        } catch (error) {
            console.error('配对失败:', error);
            return false;
        }
    }

    // 连接WebSocket
    connectWebSocket(token) {
        const wsProtocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:';
        const wsUrl = `${wsProtocol}//${window.location.host}/clipboard?token=${token}`;

        this.ws = new WebSocket(wsUrl);

        this.ws.onopen = () => {
            console.log('WebSocket已连接');
            showNotification('✅ 已连接到手机');
            updateConnectionStatus('🟢 已连接');
        };

        this.ws.onmessage = async (event) => {
            try {
                const data = JSON.parse(event.data);
                console.log('收到消息:', data);

                if (data.type === 'connected') {
                    showNotification('✅ WebSocket连接成功');
                } else if (data.type === 'clipboard_push') {
                    await this.handleClipboardPush(data.data);
                }
            } catch (error) {
                console.error('处理消息失败:', error);
            }
        };

        this.ws.onclose = () => {
            console.log('WebSocket已断开');
            updateConnectionStatus('🔴 连接已断开');
            showNotification('❌ 连接已断开');
        };

        this.ws.onerror = (error) => {
            console.error('WebSocket错误:', error);
        };
    }

    // 处理剪贴板推送
    async handleClipboardPush(data) {
        if (!data || !data.content) {
            console.warn('无效的剪贴板数据');
            return;
        }

        try {
            await writeToClipboard(data.content);
            showNotification(`✅ 已复制: ${data.content.substring(0, 30)}${data.content.length > 30 ? '...' : ''}`);
            console.log('剪贴板内容已复制:', data.content);
        } catch (error) {
            console.error('复制到剪贴板失败:', error);
            showNotification('❌ 复制失败');
        }
    }
}

// 写入剪贴板
async function writeToClipboard(text) {
    if (navigator.clipboard && navigator.clipboard.writeText) {
        // 使用Clipboard API
        await navigator.clipboard.writeText(text);
    } else {
        // 降级方案：使用document.execCommand
        const textarea = document.createElement('textarea');
        textarea.value = text;
        textarea.style.position = 'fixed';
        textarea.style.opacity = '0';
        document.body.appendChild(textarea);
        textarea.select();
        const success = document.execCommand('copy');
        document.body.removeChild(textarea);

        if (!success) {
            throw new Error('execCommand failed');
        }
    }
}

// 显示通知
function showNotification(message) {
    const toast = document.createElement('div');
    toast.className = 'toast';
    toast.textContent = message;
    document.body.appendChild(toast);

    setTimeout(() => {
        toast.style.opacity = '0';
        setTimeout(() => toast.remove(), 300);
    }, 3000);
}

// 更新连接状态
function updateConnectionStatus(status) {
    const statusElement = document.getElementById('connectionStatus');
    if (statusElement) {
        statusElement.textContent = status;
    }
}

// 页面加载完成后初始化
document.addEventListener('DOMContentLoaded', () => {
    const client = new WebSyncClient();
    const pairingCodeInput = document.getElementById('pairingCode');
    const pairBtn = document.getElementById('pairBtn');
    const pairError = document.getElementById('pairError');
    const goDashboardBtn = document.getElementById('goDashboardBtn');

    // 配对按钮点击事件
    pairBtn.addEventListener('click', async () => {
        const code = pairingCodeInput.value.trim();

        if (!code || code.length !== 6 || !/^\d{6}$/.test(code)) {
            pairError.textContent = '请输入6位数字配对码';
            pairError.classList.add('show');
            return;
        }

        pairError.classList.remove('show');
        pairBtn.disabled = true;
        pairBtn.textContent = '配对中...';

        const success = await client.pairDevice(code);

        if (success) {
            // 配对成功，连接WebSocket
            client.connectWebSocket(code);

            // 切换到成功界面
            document.getElementById('step-pair').classList.add('hidden');
            document.getElementById('step-success').classList.remove('hidden');

            // 隐藏证书警告
            document.getElementById('cert-warning').classList.add('hidden');
        } else {
            pairError.textContent = '配对码无效或已过期，请重试';
            pairError.classList.add('show');
            pairBtn.disabled = false;
            pairBtn.textContent = '配对';
        }
    });

    // 配对码输入框事件
    pairingCodeInput.addEventListener('input', (e) => {
        // 只允许数字
        e.target.value = e.target.value.replace(/\D/g, '');
    });

    pairingCodeInput.addEventListener('keypress', (e) => {
        if (e.key === 'Enter') {
            pairBtn.click();
        }
    });

    // 跳转到控制台按钮
    goDashboardBtn.addEventListener('click', () => {
        showNotification('📊 控制台功能开发中...');
        // TODO: 实现控制台页面
    });
});
