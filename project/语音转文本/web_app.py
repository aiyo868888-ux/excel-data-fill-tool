"""
语音转文字 - Web应用
基于 OpenAI Whisper 的音频转文字工具
"""

from flask import Flask, render_template, request, send_file, jsonify
import os
import sys
import traceback
from datetime import datetime
import threading
import time
import uuid
import json

# 添加项目根目录到 Python 路径
sys.path.insert(0, os.path.dirname(__file__))

app = Flask(__name__,
            template_folder='templates',
            static_folder='static')
app.config['MAX_CONTENT_LENGTH'] = 500 * 1024 * 1024  # 500MB

# 使用绝对路径
BASE_DIR = os.path.dirname(os.path.abspath(__file__))
app.config['UPLOAD_FOLDER'] = os.path.join(BASE_DIR, '../../uploads/whisper/audio')
app.config['OUTPUT_FOLDER'] = os.path.join(BASE_DIR, '../../temp/whisper')

print(f"[INFO] BASE_DIR: {BASE_DIR}")
print(f"[INFO] UPLOAD_FOLDER: {app.config['UPLOAD_FOLDER']}")
print(f"[INFO] OUTPUT_FOLDER: {app.config['OUTPUT_FOLDER']}")

# 确保目录存在
os.makedirs(app.config['UPLOAD_FOLDER'], exist_ok=True)
os.makedirs(os.path.join(app.config['OUTPUT_FOLDER'], 'text'), exist_ok=True)
os.makedirs(os.path.join(app.config['OUTPUT_FOLDER'], 'srt'), exist_ok=True)
os.makedirs(os.path.join(app.config['OUTPUT_FOLDER'], 'vtt'), exist_ok=True)
os.makedirs(os.path.join(BASE_DIR, '../../uploads/whisper/converted'), exist_ok=True)

# ==================== Whisper 任务管理 ====================

whisper_tasks = {}

# Whisper 配置
app.config['WHISPER_MAX_FILE_SIZE'] = 500 * 1024 * 1024  # 500MB
app.config['WHISPER_ALLOWED_EXTENSIONS'] = {
    'mp3', 'wav', 'm4a', 'mp4', 'ogg', 'flac', 'aac', 'wma'
}
app.config['WHISPER_DEFAULT_MODEL'] = 'base'
app.config['WHISPER_MAX_CONCURRENT_TASKS'] = 3


def run_whisper_task(task_id, audio_path, model_size, output_formats):
    """
    后台执行 Whisper 转录任务
    在独立线程中运行，避免阻塞主线程
    """
    try:
        from whisper_service import WhisperTranscriber

        print(f"[INFO] 任务 {task_id} 开始处理")
        print(f"[INFO] 音频路径: {audio_path}")

        # 检查文件是否存在
        if not os.path.exists(audio_path):
            raise Exception(f"音频文件不存在: {audio_path}")

        # 更新状态：处理中
        whisper_tasks[task_id].update({
            'status': 'processing',
            'progress': 0,
            'message': '任务初始化...'
        })

        # 创建转录器
        print(f"[INFO] 创建转录器，模型: {model_size}")
        transcriber = WhisperTranscriber(model_size)

        # 定义进度回调
        def progress_callback(progress, message):
            whisper_tasks[task_id].update({
                'progress': progress,
                'message': message
            })

        # 执行转录
        result = transcriber.transcribe(
            audio_path,
            output_formats=output_formats,
            progress_callback=progress_callback
        )

        # 更新状态：完成
        whisper_tasks[task_id].update({
            'status': 'completed',
            'progress': 100,
            'message': '转换完成！',
            'result': result
        })

    except Exception as e:
        # 更新状态：失败
        whisper_tasks[task_id].update({
            'status': 'failed',
            'message': f'转换失败：{str(e)}',
            'error': str(e)
        })
        traceback.print_exc()


@app.route('/')
def index():
    """主页 - 重定向到 Whisper 页面"""
    return render_template('whisper.html')


@app.route('/whisper')
def whisper_page():
    """Whisper 语音识别页面"""
    return render_template('whisper.html')


@app.route('/static/whisper.js')
def whisper_static():
    """Whisper 静态文件"""
    return send_file(os.path.join('static', 'whisper.js'), mimetype='application/javascript')


@app.route('/api/whisper/upload', methods=['POST'])
def upload_audio():
    """上传音频文件并启动转录任务"""
    try:
        # 检查文件
        if 'audio' not in request.files:
            return jsonify({'success': False, 'error': '未上传文件'}), 400

        file = request.files['audio']
        if file.filename == '':
            return jsonify({'success': False, 'error': '未选择文件'}), 400

        # 获取参数
        model_size = request.form.get('model_size', app.config['WHISPER_DEFAULT_MODEL'])
        output_formats = json.loads(request.form.get('output_formats', '["txt", "srt"]'))

        # 验证文件格式
        file_ext = file.filename.rsplit('.', 1)[1].lower() if '.' in file.filename else ''
        if file_ext not in app.config['WHISPER_ALLOWED_EXTENSIONS']:
            return jsonify({
                'success': False,
                'error': f'不支持的文件格式。支持格式：{", ".join(app.config["WHISPER_ALLOWED_EXTENSIONS"])}'
            }), 400

        # 检查并发任务数
        active_count = sum(1 for t in whisper_tasks.values() if t['status'] == 'processing')
        if active_count >= app.config['WHISPER_MAX_CONCURRENT_TASKS']:
            return jsonify({
                'success': False,
                'error': f'当前有 {active_count} 个任务正在处理，请稍后再试'
            }), 503

        # 保存文件（处理中文文件名）
        import uuid
        from werkzeug.utils import secure_filename

        # 使用 secure_filename 处理文件名，但保留中文
        original_filename = file.filename
        file_ext = original_filename.rsplit('.', 1)[1].lower() if '.' in original_filename else ''
        safe_filename = f"{uuid.uuid4()}.{file_ext}"

        audio_path = os.path.join(app.config['UPLOAD_FOLDER'], safe_filename)
        file.save(audio_path)

        # 创建任务
        task_id = str(uuid.uuid4())

        # 初始化任务状态
        whisper_tasks[task_id] = {
            'status': 'pending',
            'progress': 0,
            'message': '任务已创建，等待处理...',
            'result': None,
            'error': None,
            'created_at': datetime.now().isoformat(),
            'audio_file': safe_filename
        }

        # 创建并启动线程
        print(f"[INFO] 创建转录任务 {task_id}，文件: {safe_filename}, 路径: {audio_path}")
        thread = threading.Thread(
            target=run_whisper_task,
            args=(task_id, audio_path, model_size, output_formats)
        )
        thread.daemon = True
        thread.start()

        return jsonify({
            'success': True,
            'task_id': task_id,
            'message': '文件上传成功，正在处理...'
        })

    except Exception as e:
        traceback.print_exc()
        return jsonify({'success': False, 'error': f'上传失败: {str(e)}'}), 500


@app.route('/api/whisper/status/<task_id>')
def get_task_status(task_id):
    """查询转录任务状态"""
    if task_id not in whisper_tasks:
        return jsonify({'success': False, 'error': '任务不存在'}), 404

    task = whisper_tasks[task_id]

    return jsonify({
        'success': True,
        'status': task['status'],
        'progress': task['progress'],
        'message': task['message'],
        'result': task.get('result'),
        'error': task.get('error')
    })


@app.route('/api/whisper/download/<path:filename>')
def download_whisper_result(filename):
    """下载转录结果文件"""
    try:
        # 安全检查
        if '..' in filename or filename.startswith('/'):
            return jsonify({'success': False, 'error': '非法的文件路径'}), 400

        # 检查文件
        allowed_dirs = [
            os.path.join(app.config['OUTPUT_FOLDER'], 'text'),
            os.path.join(app.config['OUTPUT_FOLDER'], 'srt'),
            os.path.join(app.config['OUTPUT_FOLDER'], 'vtt')
        ]

        filepath = None
        for dir_path in allowed_dirs:
            potential_path = os.path.join(dir_path, filename)
            if os.path.exists(potential_path):
                filepath = potential_path
                break

        if not filepath:
            return jsonify({'success': False, 'error': '文件不存在'}), 404

        return send_file(filepath, as_attachment=True, download_name=filename)

    except Exception as e:
        traceback.print_exc()
        return jsonify({'success': False, 'error': f'下载失败: {str(e)}'}), 500


def init_whisper():
    """初始化 Whisper（后台预加载模型）"""
    def load_models():
        time.sleep(2)
        try:
            from whisper_service import WhisperTranscriber
            print("[INFO] 后台预加载 Whisper 模型...")
            WhisperTranscriber.preload_models(['base'])
        except Exception as e:
            print(f"[WARN] Whisper 模型预加载失败：{e}")

    thread = threading.Thread(target=load_models, daemon=True)
    thread.start()


# 启动时预加载 Whisper 模型
init_whisper()


if __name__ == '__main__':
    print("=" * 70)
    print("   语音转文字 - Web应用")
    print("   访问地址: http://localhost:5002")
    print("=" * 70)
    # 关闭调试模式和自动重载，避免任务被中断
    app.run(host='0.0.0.0', port=5002, debug=False, use_reloader=False)
