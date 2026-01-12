"""
音效生成工具 - Web应用
提供图形化界面生成各种环境音效
"""

from flask import Flask, render_template, request, send_file, jsonify
import os
import sys
import traceback
import uuid
import threading
import json
from datetime import datetime

# 添加项目根目录到 Python 路径
sys.path.insert(0, os.path.dirname(__file__))

from sound_generator import SoundGenerator

app = Flask(__name__,
            template_folder='templates',
            static_folder='static')
app.config['MAX_CONTENT_LENGTH'] = 100 * 1024 * 1024  # 100MB

# 使用绝对路径
BASE_DIR = os.path.dirname(os.path.abspath(__file__))
app.config['OUTPUT_FOLDER'] = os.path.join(BASE_DIR, 'output')
app.config['TEMP_FOLDER'] = os.path.join(BASE_DIR, '../../temp/soundfx')

print(f"[INFO] BASE_DIR: {BASE_DIR}")
print(f"[INFO] OUTPUT_FOLDER: {app.config['OUTPUT_FOLDER']}")
print(f"[INFO] TEMP_FOLDER: {app.config['TEMP_FOLDER']}")

# 确保目录存在
os.makedirs(app.config['OUTPUT_FOLDER'], exist_ok=True)
os.makedirs(app.config['TEMP_FOLDER'], exist_ok=True)

# 音效生成器
sound_gen = SoundGenerator()

# 生成任务管理
generation_tasks = {}

# 音效类型配置
SOUND_TYPES = {
    'white_noise': {
        'name': '白噪声',
        'description': '基础白噪声，可用于睡眠、专注等场景',
        'params': {
            'duration': {'default': 10, 'min': 1, 'max': 300, 'unit': '秒'},
            'volume': {'default': 70, 'min': 0, 'max': 100, 'unit': '%'},
            'fade_in': {'default': 0.5, 'min': 0, 'max': 5, 'unit': '秒'},
            'fade_out': {'default': 0.5, 'min': 0, 'max': 5, 'unit': '秒'}
        }
    },
    'pink_noise': {
        'name': '粉红噪声',
        'description': '粉红噪声，声音更柔和，适合放松',
        'params': {
            'duration': {'default': 10, 'min': 1, 'max': 300, 'unit': '秒'},
            'volume': {'default': 70, 'min': 0, 'max': 100, 'unit': '%'},
            'fade_in': {'default': 0.5, 'min': 0, 'max': 5, 'unit': '秒'},
            'fade_out': {'default': 0.5, 'min': 0, 'max': 5, 'unit': '秒'}
        }
    },
    'brown_noise': {
        'name': '布朗噪声',
        'description': '低频噪声，声音深沉，适合深度睡眠',
        'params': {
            'duration': {'default': 10, 'min': 1, 'max': 300, 'unit': '秒'},
            'volume': {'default': 70, 'min': 0, 'max': 100, 'unit': '%'},
            'fade_in': {'default': 0.5, 'min': 0, 'max': 5, 'unit': '秒'},
            'fade_out': {'default': 0.5, 'min': 0, 'max': 5, 'unit': '秒'}
        }
    },
    'rain': {
        'name': '雨声',
        'description': '雨滴声，清新自然',
        'params': {
            'duration': {'default': 30, 'min': 1, 'max': 300, 'unit': '秒'},
            'volume': {'default': 70, 'min': 0, 'max': 100, 'unit': '%'},
            'intensity': {'default': 50, 'min': 0, 'max': 100, 'unit': '%'},
            'fade_in': {'default': 1.0, 'min': 0, 'max': 5, 'unit': '秒'},
            'fade_out': {'default': 1.0, 'min': 0, 'max': 5, 'unit': '秒'}
        }
    },
    'wind': {
        'name': '风声',
        'description': '风吹过的声音，舒缓放松',
        'params': {
            'duration': {'default': 30, 'min': 1, 'max': 300, 'unit': '秒'},
            'volume': {'default': 70, 'min': 0, 'max': 100, 'unit': '%'},
            'speed': {'default': 1.0, 'min': 0.5, 'max': 2.0, 'unit': '倍'},
            'fade_in': {'default': 1.0, 'min': 0, 'max': 5, 'unit': '秒'},
            'fade_out': {'default': 1.0, 'min': 0, 'max': 5, 'unit': '秒'}
        }
    },
    'thunder': {
        'name': '雷声',
        'description': '雷鸣声，震撼有力',
        'params': {
            'duration': {'default': 5, 'min': 1, 'max': 30, 'unit': '秒'},
            'volume': {'default': 90, 'min': 0, 'max': 100, 'unit': '%'},
            'fade_in': {'default': 0.1, 'min': 0, 'max': 2, 'unit': '秒'},
            'fade_out': {'default': 2.0, 'min': 0, 'max': 10, 'unit': '秒'}
        }
    },
    'waves': {
        'name': '海浪',
        'description': '海浪拍岸声，宁静悠远',
        'params': {
            'duration': {'default': 30, 'min': 1, 'max': 300, 'unit': '秒'},
            'volume': {'default': 70, 'min': 0, 'max': 100, 'unit': '%'},
            'frequency': {'default': 0.1, 'min': 0.05, 'max': 0.2, 'step': 0.01, 'unit': 'Hz'},
            'fade_in': {'default': 1.0, 'min': 0, 'max': 5, 'unit': '秒'},
            'fade_out': {'default': 1.0, 'min': 0, 'max': 5, 'unit': '秒'}
        }
    },
    'forest': {
        'name': '森林环境',
        'description': '森林环境音（风声+鸟鸣）',
        'params': {
            'duration': {'default': 30, 'min': 1, 'max': 300, 'unit': '秒'},
            'volume': {'default': 60, 'min': 0, 'max': 100, 'unit': '%'},
            'fade_in': {'default': 1.0, 'min': 0, 'max': 5, 'unit': '秒'},
            'fade_out': {'default': 1.0, 'min': 0, 'max': 5, 'unit': '秒'}
        }
    },
    'door_open': {
        'name': '开门声',
        'description': '门吱呀打开的声音',
        'params': {
            'duration': {'default': 2, 'min': 0.5, 'max': 5, 'unit': '秒'},
            'volume': {'default': 70, 'min': 0, 'max': 100, 'unit': '%'}
        }
    },
    'door_close': {
        'name': '关门声',
        'description': '门关闭的撞击声',
        'params': {
            'duration': {'default': 1, 'min': 0.3, 'max': 3, 'unit': '秒'},
            'volume': {'default': 80, 'min': 0, 'max': 100, 'unit': '%'}
        }
    },
    'dog_bark': {
        'name': '狗叫声',
        'description': '狗汪汪叫，中型犬',
        'params': {
            'duration': {'default': 1, 'min': 0.5, 'max': 3, 'unit': '秒'},
            'volume': {'default': 80, 'min': 0, 'max': 100, 'unit': '%'}
        }
    },
    'footsteps': {
        'name': '脚步声',
        'description': '在木地板上行走的脚步声',
        'params': {
            'duration': {'default': 5, 'min': 1, 'max': 30, 'unit': '秒'},
            'volume': {'default': 60, 'min': 0, 'max': 100, 'unit': '%'},
            'step_rate': {'default': 1.0, 'min': 0.5, 'max': 2.0, 'step': 0.1, 'unit': '步/秒'}
        }
    }
}


def run_generation_task(task_id, sound_type, params):
    """
    后台执行音效生成任务
    """
    try:
        print(f"[INFO] 任务 {task_id} 开始处理: {sound_type}")
        print(f"[INFO] 参数: {params}")

        # 更新状态：处理中
        generation_tasks[task_id].update({
            'status': 'processing',
            'progress': 10,
            'message': '正在生成音效...'
        })

        # 根据类型生成音效
        volume = params.get('volume', 70) / 100.0
        fade_in = params.get('fade_in', 0.5)
        fade_out = params.get('fade_out', 0.5)
        duration = params.get('duration', 10)

        if sound_type == 'white_noise':
            audio = sound_gen.generate_white_noise(duration, volume, fade_in, fade_out)
        elif sound_type == 'pink_noise':
            audio = sound_gen.generate_pink_noise(duration, volume, fade_in, fade_out)
        elif sound_type == 'brown_noise':
            audio = sound_gen.generate_brown_noise(duration, volume, fade_in, fade_out)
        elif sound_type == 'rain':
            intensity = params.get('intensity', 50) / 100.0
            audio = sound_gen.generate_rain(duration, volume, intensity, fade_in, fade_out)
        elif sound_type == 'wind':
            speed = params.get('speed', 1.0)
            audio = sound_gen.generate_wind(duration, volume, speed, fade_in, fade_out)
        elif sound_type == 'thunder':
            audio = sound_gen.generate_thunder(duration, volume, fade_in, fade_out)
        elif sound_type == 'waves':
            frequency = params.get('frequency', 0.1)
            audio = sound_gen.generate_waves(duration, volume, frequency, fade_in, fade_out)
        elif sound_type == 'forest':
            audio = sound_gen.generate_forest(duration, volume, fade_in, fade_out)
        elif sound_type == 'door_open':
            audio = sound_gen.generate_door_open(duration, volume)
        elif sound_type == 'door_close':
            audio = sound_gen.generate_door_close(duration, volume)
        elif sound_type == 'dog_bark':
            audio = sound_gen.generate_dog_bark(duration, volume)
        elif sound_type == 'footsteps':
            step_rate = params.get('step_rate', 1.0)
            audio = sound_gen.generate_footsteps(duration, volume, 'wood', step_rate)
        else:
            raise ValueError(f'不支持的音效类型: {sound_type}')

        generation_tasks[task_id].update({
            'progress': 70,
            'message': '正在保存文件...'
        })

        # 保存文件
        filename = f"{sound_type}_{uuid.uuid4().hex[:8]}.wav"
        output_path = os.path.join(app.config['OUTPUT_FOLDER'], filename)
        sound_gen.save_wav(audio, output_path)

        # 更新状态：完成
        generation_tasks[task_id].update({
            'status': 'completed',
            'progress': 100,
            'message': '生成完成！',
            'result': {
                'filename': filename,
                'path': output_path
            }
        })

        print(f"[INFO] 任务 {task_id} 完成: {filename}")

    except Exception as e:
        # 更新状态：失败
        generation_tasks[task_id].update({
            'status': 'failed',
            'message': f'生成失败：{str(e)}',
            'error': str(e)
        })
        traceback.print_exc()


@app.route('/')
def index():
    """主页"""
    return render_template('index.html', sound_types=SOUND_TYPES)


@app.route('/api/sound_types')
def get_sound_types():
    """获取所有音效类型"""
    return jsonify({
        'success': True,
        'sound_types': SOUND_TYPES
    })


@app.route('/api/generate', methods=['POST'])
def generate_sound():
    """生成音效"""
    try:
        data = request.get_json()
        sound_type = data.get('sound_type')
        params = data.get('params', {})

        # 验证音效类型
        if sound_type not in SOUND_TYPES:
            return jsonify({
                'success': False,
                'error': f'不支持的音效类型: {sound_type}'
            }), 400

        # 创建任务
        task_id = str(uuid.uuid4())

        # 初始化任务状态
        generation_tasks[task_id] = {
            'status': 'pending',
            'progress': 0,
            'message': '任务已创建...',
            'result': None,
            'error': None,
            'created_at': datetime.now().isoformat(),
            'sound_type': sound_type
        }

        # 创建并启动线程
        print(f"[INFO] 创建生成任务 {task_id}，类型: {sound_type}")
        thread = threading.Thread(
            target=run_generation_task,
            args=(task_id, sound_type, params)
        )
        thread.daemon = True
        thread.start()

        return jsonify({
            'success': True,
            'task_id': task_id,
            'message': '任务已创建，正在处理...'
        })

    except Exception as e:
        traceback.print_exc()
        return jsonify({'success': False, 'error': f'创建任务失败: {str(e)}'}), 500


@app.route('/api/status/<task_id>')
def get_task_status(task_id):
    """查询任务状态"""
    if task_id not in generation_tasks:
        return jsonify({'success': False, 'error': '任务不存在'}), 404

    task = generation_tasks[task_id]

    return jsonify({
        'success': True,
        'status': task['status'],
        'progress': task['progress'],
        'message': task['message'],
        'result': task.get('result'),
        'error': task.get('error')
    })


@app.route('/api/download/<filename>')
def download_sound(filename):
    """下载生成的音效文件"""
    try:
        # 安全检查
        if '..' in filename or filename.startswith('/'):
            return jsonify({'success': False, 'error': '非法的文件路径'}), 400

        filepath = os.path.join(app.config['OUTPUT_FOLDER'], filename)

        if not os.path.exists(filepath):
            return jsonify({'success': False, 'error': '文件不存在'}), 404

        return send_file(filepath, as_attachment=True, download_name=filename)

    except Exception as e:
        traceback.print_exc()
        return jsonify({'success': False, 'error': f'下载失败: {str(e)}'}), 500


@app.route('/api/mix', methods=['POST'])
def mix_sounds():
    """混合多个音效"""
    try:
        data = request.get_json()
        sound_files = data.get('sound_files', [])
        volumes = data.get('volumes', [])

        if len(sound_files) != len(volumes):
            return jsonify({
                'success': False,
                'error': '音效文件数量和音量数量必须相同'
            }), 400

        if not sound_files:
            return jsonify({
                'success': False,
                'error': '至少需要一个音效文件'
            }), 400

        # 构建完整路径
        file_paths = [os.path.join(app.config['OUTPUT_FOLDER'], f) for f in sound_files]

        # 验证文件存在
        for fp in file_paths:
            if not os.path.exists(fp):
                return jsonify({
                    'success': False,
                    'error': f'文件不存在: {os.path.basename(fp)}'
                }), 404

        # 生成输出文件名
        output_filename = f"mixed_{uuid.uuid4().hex[:8]}.wav"
        output_path = os.path.join(app.config['OUTPUT_FOLDER'], output_filename)

        # 混合音效
        volumes_normalized = [v / 100.0 for v in volumes]
        sound_gen.mix_sounds(file_paths, volumes_normalized, output_path)

        return jsonify({
            'success': True,
            'filename': output_filename,
            'message': '混合完成！'
        })

    except Exception as e:
        traceback.print_exc()
        return jsonify({'success': False, 'error': f'混合失败: {str(e)}'}), 500


if __name__ == '__main__':
    print("=" * 70)
    print("   音效生成工具 - Web应用")
    print("   访问地址: http://localhost:5003")
    print("=" * 70)
    app.run(host='0.0.0.0', port=5003, debug=False, use_reloader=False)
