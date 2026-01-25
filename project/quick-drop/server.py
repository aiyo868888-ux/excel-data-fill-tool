"""
QuickDrop - 极简局域网文件传输工具
手机上传文件到电脑，自动按日期归档
"""

from flask import Flask, request, jsonify, send_from_directory
from datetime import datetime
import os
import socket
import json

app = Flask(__name__, static_folder='static', static_url_path='')

# 配置
BASE_DIR = os.path.dirname(os.path.abspath(__file__))
UPLOAD_DIR = os.path.join(BASE_DIR, 'uploads')
CONFIG_FILE = os.path.join(BASE_DIR, 'config.json')

# 默认配置
DEFAULT_CONFIG = {
    'port': 8899,
    'max_file_size': 100,  # MB
    'auto_open_folder': False
}

# 加载配置
if os.path.exists(CONFIG_FILE):
    with open(CONFIG_FILE, 'r', encoding='utf-8') as f:
        config = json.load(f)
else:
    config = DEFAULT_CONFIG
    with open(CONFIG_FILE, 'w', encoding='utf-8') as f:
        json.dump(config, f, ensure_ascii=False, indent=2)

app.config['MAX_CONTENT_LENGTH'] = config['max_file_size'] * 1024 * 1024
PORT = config['port']


def get_local_ip():
    """获取本机局域网IP"""
    try:
        s = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
        s.connect(('8.8.8.8', 80))
        ip = s.getsockname()[0]
        s.close()
        return ip
    except:
        return '127.0.0.1'


def get_date_folder():
    """获取今日日期文件夹路径"""
    today = datetime.now().strftime('%Y-%m-%d')
    folder_path = os.path.join(UPLOAD_DIR, today)
    os.makedirs(folder_path, exist_ok=True)
    return folder_path, today


@app.route('/')
def index():
    """返回上传页面"""
    return send_from_directory(app.static_folder, 'index.html')


@app.route('/upload', methods=['POST'])
def upload_file():
    """处理文件上传"""
    try:
        if 'file' not in request.files:
            return jsonify({'success': False, 'message': '没有文件'}), 400

        file = request.files['file']
        if file.filename == '':
            return jsonify({'success': False, 'message': '文件名为空'}), 400

        # 获取日期文件夹
        date_folder, date_str = get_date_folder()

        # 保存文件（处理重名）
        base_name = file.filename
        name, ext = os.path.splitext(base_name)
        counter = 1
        file_path = os.path.join(date_folder, base_name)

        while os.path.exists(file_path):
            new_name = f"{name}_{counter}{ext}"
            file_path = os.path.join(date_folder, new_name)
            counter += 1

        file.save(file_path)

        # 返回文件信息
        return jsonify({
            'success': True,
            'message': '上传成功',
            'data': {
                'filename': os.path.basename(file_path),
                'path': file_path,
                'date_folder': date_str,
                'size': os.path.getsize(file_path)
            }
        })

    except Exception as e:
        return jsonify({'success': False, 'message': str(e)}), 500


@app.route('/info')
def server_info():
    """返回服务器信息"""
    local_ip = get_local_ip()
    return jsonify({
        'ip': local_ip,
        'port': PORT,
        'url': f'http://{local_ip}:{PORT}'
    })


if __name__ == '__main__':
    local_ip = get_local_ip()

    print(f"""
╔══════════════════════════════════════════════════════════════╗
║                       QuickDrop 启动成功                      ║
╠══════════════════════════════════════════════════════════════╣
║  电脑访问:   http://localhost:{PORT}                            ║
║  手机访问:   http://{local_ip}:{PORT}                          ║
╠══════════════════════════════════════════════════════════════╣
║  文件保存位置: {UPLOAD_DIR}                              ║
║  按 Ctrl+C 停止服务                                           ║
╚══════════════════════════════════════════════════════════════╝
    """)

    # 确保上传目录存在
    os.makedirs(UPLOAD_DIR, exist_ok=True)

    app.run(host='0.0.0.0', port=PORT, debug=False)
