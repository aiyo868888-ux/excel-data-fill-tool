"""
数据填充工具 - Web应用 (高级版本)
支持多供应商管理、进度追踪、会话管理
"""

from flask import Flask, render_template, request, send_file, jsonify
import os
import sys
import traceback
from datetime import datetime
import pandas as pd
import tempfile
import uuid
import json
import threading
import time

# 导入核心数据填充类
import importlib.util
# 使用绝对路径避免编码问题（绿色版：数据填充工具.py 在根目录）
module_path = os.path.abspath(os.path.join(os.path.dirname(__file__), '数据填充工具.py'))
spec = importlib.util.spec_from_file_location("data_filler", module_path)
DataFillerModule = importlib.util.module_from_spec(spec)
spec.loader.exec_module(DataFillerModule)
DataFiller = DataFillerModule.DataFiller

app = Flask(__name__)
app.config['MAX_CONTENT_LENGTH'] = 50 * 1024 * 1024  # 50MB最大文件上传
app.config['UPLOAD_FOLDER'] = 'uploads'
app.config['OUTPUT_FOLDER'] = 'temp'
app.config['SESSION_FOLDER'] = 'sessions'

# 确保目录存在
os.makedirs(app.config['UPLOAD_FOLDER'], exist_ok=True)
os.makedirs(app.config['OUTPUT_FOLDER'], exist_ok=True)
os.makedirs(app.config['SESSION_FOLDER'], exist_ok=True)

# Whisper 相关目录
WHISPER_DIRS = [
    'uploads/whisper/audio',
    'uploads/whisper/converted',
    'temp/whisper/text',
    'temp/whisper/srt',
    'temp/whisper/vtt',
    'static'
]
for dir_path in WHISPER_DIRS:
    os.makedirs(dir_path, exist_ok=True)

# ==================== 配置管理 ====================

CONFIG_FILE = 'suppliers_config.json'

# 全局配置变量
GLOBAL_SUPPLIERS_CONFIG = {
    "suppliers": [
        {"name": "万邦蔬菜", "start_column": "AC", "end_column": "AH"},
        {"name": "康来福冻品", "start_column": "AI", "end_column": "AN"},
        {"name": "太宇肉", "start_column": "AO", "end_column": "AT"}
    ]
}

def load_suppliers_config():
    """从文件加载送货商配置"""
    global GLOBAL_SUPPLIERS_CONFIG
    if os.path.exists(CONFIG_FILE):
        try:
            with open(CONFIG_FILE, 'r', encoding='utf-8') as f:
                GLOBAL_SUPPLIERS_CONFIG = json.load(f)
            print(f"✅ 配置文件已加载: {CONFIG_FILE}")
        except Exception as e:
            print(f"⚠️  加载配置失败: {e}，使用默认配置")
    else:
        print(f"⚠️  配置文件不存在，创建默认配置: {CONFIG_FILE}")
        save_suppliers_config_to_file()

def save_suppliers_config_to_file():
    """保存送货商配置到文件"""
    global GLOBAL_SUPPLIERS_CONFIG
    try:
        with open(CONFIG_FILE, 'w', encoding='utf-8') as f:
            json.dump(GLOBAL_SUPPLIERS_CONFIG, f, ensure_ascii=False, indent=2)
        print(f"✅ 配置已保存: {CONFIG_FILE}")
        return True
    except Exception as e:
        print(f"❌ 保存配置失败: {e}")
        return False

def update_all_sessions_config():
    """更新所有活跃 session 的配置"""
    global GLOBAL_SUPPLIERS_CONFIG
    for session_id, session_data in sessions.items():
        session_data['suppliers_config'] = GLOBAL_SUPPLIERS_CONFIG.copy()
    print(f"✅ 已更新 {len(sessions)} 个 session 的配置")

# 启动时加载配置
load_suppliers_config()

# ==================== 会话管理 ====================

# 全局会话存储 (简单实现，生产环境应使用Redis)
sessions = {}


@app.route('/')
def index():
    """主页 - 使用高级模板"""
    return render_template('index.html')


@app.route('/api/session/create', methods=['POST'])
def create_session():
    """创建新会话"""
    try:
        session_id = str(uuid.uuid4())
        sessions[session_id] = {
            'id': session_id,
            'created_at': datetime.now().isoformat(),
            'report_path': None,
            'report_password': None,
            'filler': None,
            'supplier_files': [],
            'fill_history': {},
            'suppliers_config': GLOBAL_SUPPLIERS_CONFIG.copy()  # 使用全局配置的深拷贝
        }

        return jsonify({
            'success': True,
            'session_id': session_id
        })
    except Exception as e:
        traceback.print_exc()
        return jsonify({'success': False, 'error': str(e)}), 500


def get_session(session_id):
    """获取会话"""
    return sessions.get(session_id)


def update_session(session_id, data):
    """更新会话数据"""
    if session_id in sessions:
        sessions[session_id].update(data)


@app.route('/api/upload-report', methods=['POST'])
def upload_report():
    """上传报表文件"""
    try:
        session_id = request.headers.get('X-Session-ID')
        if not session_id or session_id not in sessions:
            return jsonify({'success': False, 'error': '无效的会话'}), 400

        if 'report' not in request.files:
            return jsonify({'success': False, 'error': '未上传报表文件'}), 400

        file = request.files['report']
        if file.filename == '':
            return jsonify({'success': False, 'error': '未选择文件'}), 400

        # 检查文件扩展名
        if not (file.filename.endswith('.xlsx') or file.filename.endswith('.xls')):
            return jsonify({'success': False, 'error': '只支持 .xlsx 或 .xls 格式的文件'}), 400

        # 保存文件
        timestamp = datetime.now().strftime('%Y%m%d_%H%M%S')
        ext = '.xlsx' if file.filename.endswith('.xlsx') else '.xls'
        filename = f"report_{timestamp}{ext}"
        filepath = os.path.join(app.config['UPLOAD_FOLDER'], filename)
        file.save(filepath)

        # 验证文件
        if not os.path.exists(filepath) or os.path.getsize(filepath) == 0:
            return jsonify({'success': False, 'error': '文件保存失败'}), 400

        # 获取密码
        password = request.form.get('password', '')

        # 创建填充工具实例
        filler = DataFiller(filepath, password if password else None)
        if not filler.load_report():
            try:
                os.unlink(filepath)
            except:
                pass
            return jsonify({'success': False, 'error': '报表加载失败，请检查文件或密码'}), 400

        # 获取所有数字工作表（日期）
        dates = filler.get_all_numeric_sheets()

        # 获取供应商配置（使用最新的全局配置）
        suppliersConfig = GLOBAL_SUPPLIERS_CONFIG.copy()
        print(f"📊 准备返回 {len(suppliersConfig.get('suppliers', []))} 个送货商配置")

        # 同时更新session的配置
        session = get_session(session_id)
        session['suppliers_config'] = suppliersConfig

        # 统计已填充数据
        supplierData = filler.count_supplier_data_in_columns(
            suppliersConfig['suppliers'],
            dates
        )

        # 更新会话
        update_session(session_id, {
            'report_path': filepath,
            'report_password': password,
            'filler': filler,
            'dates': dates,
            'supplier_data': supplierData
        })

        return jsonify({
            'success': True,
            'message': f'报表已加载: {file.filename}',
            'dates': dates,
            'sheets': filler.wb.sheetnames,
            'suppliersConfig': suppliersConfig,
            'supplierData': supplierData
        })

    except Exception as e:
        traceback.print_exc()
        return jsonify({'success': False, 'error': f'上传失败: {str(e)}'}), 500


@app.route('/api/upload-suppliers', methods=['POST'])
def upload_suppliers():
    """上传送货商文件"""
    try:
        session_id = request.headers.get('X-Session-ID')
        if not session_id or session_id not in sessions:
            return jsonify({'success': False, 'error': '无效的会话'}), 400

        session = get_session(session_id)
        if not session.get('filler'):
            return jsonify({'success': False, 'error': '请先上传报表'}), 400

        if 'files' not in request.files:
            return jsonify({'success': False, 'error': '未上传送货商文件'}), 400

        files = request.files.getlist('files')
        if not files or files[0].filename == '':
            return jsonify({'success': False, 'error': '未选择文件'}), 400

        # 保存第一个文件
        file = files[0]
        timestamp = datetime.now().strftime('%Y%m%d_%H%M%S')
        filename = f"supplier_{timestamp}.xlsx"
        filepath = os.path.join(app.config['UPLOAD_FOLDER'], filename)
        file.save(filepath)

        # 读取送货商文件获取日期
        filler = DataFiller.__new__(DataFiller)
        filler.wb = None
        filler.supplier_data = {}
        filler._temp_decrypted_file = None

        filler.read_supplier_file(filepath)
        dates = list(filler.supplier_data.keys())

        # 清理临时对象
        try:
            filler.cleanup()
        except:
            pass

        # 保存文件信息到会话
        session['supplier_files'].append(filepath)

        return jsonify({
            'success': True,
            'dates': dates,
            'file_info': {
                'filename': file.filename,
                'filepath': filepath,
                'dates_count': len(dates)
            }
        })

    except Exception as e:
        traceback.print_exc()
        return jsonify({'success': False, 'error': f'上传失败: {str(e)}'}), 500


@app.route('/api/check-supplier-data', methods=['POST'])
def check_supplier_data():
    """检查数据冲突"""
    try:
        session_id = request.headers.get('X-Session-ID')
        if not session_id or session_id not in sessions:
            return jsonify({'success': False, 'error': '无效的会话'}), 400

        session = get_session(session_id)
        data = request.json
        column_range = data.get('column_range')
        dates = data.get('dates', [])

        # 检查数据冲突
        conflict_dates = []
        if 'supplier_data' in session and column_range in session['supplier_data']:
            existing_dates = session['supplier_data'][column_range].keys()
            for date in dates:
                if date in existing_dates:
                    conflict_dates.append(date)

        return jsonify({
            'success': True,
            'conflict_dates': conflict_dates
        })

    except Exception as e:
        traceback.print_exc()
        return jsonify({'success': False, 'error': str(e)}), 500


@app.route('/api/fill-supplier-data', methods=['POST'])
def fill_supplier_data():
    """填充供应商数据"""
    try:
        session_id = request.headers.get('X-Session-ID')
        if not session_id or session_id not in sessions:
            return jsonify({'success': False, 'error': '无效的会话'}), 400

        session = get_session(session_id)
        filler = session.get('filler')
        if not filler:
            return jsonify({'success': False, 'error': '请先上传报表'}), 400

        data = request.json
        column_range = data.get('column_range')
        dates = data.get('dates', [])

        # 解析列范围
        if '-' in column_range:
            start_col, end_col = column_range.split('-')
        else:
            start_col = end_col = column_range

        print(f"🔍 开始填充: column_range={column_range}, dates={dates}")
        print(f"🔍 supplier_files: {session.get('supplier_files', [])}")

        # ✅ 修复：清空现有数据，避免多次填充时数据累积
        filler.supplier_data = {}

        # ✅ 修复：只读取最近上传的一个文件（最后一个）
        # 因为每个送货商上传自己的文件，填充时只应该使用当前送货商的文件
        supplier_files = session.get('supplier_files', [])
        if not supplier_files:
            print("❌ 没有上传的送货商文件")
            return jsonify({'success': False, 'error': '请先上传送货商文件'}), 400

        latest_file = supplier_files[-1]  # 获取最后一个（最近上传的）
        print(f"📂 读取送货商文件: {latest_file}")

        # 检查文件是否存在
        if not os.path.exists(latest_file):
            print(f"❌ 文件不存在: {latest_file}")
            return jsonify({'success': False, 'error': f'文件不存在: {latest_file}'}), 400

        filler.read_supplier_file(latest_file)
        print(f"✅ 文件读取成功，supplier_data keys: {list(filler.supplier_data.keys())}")

        # 执行智能填充
        filler.fill_data_smart(
            column_range=column_range,
            dates=dates,
            col_start=start_col,
            col_end=end_col
        )
        print("✅ 智能填充完成")

        # 保存结果
        timestamp = datetime.now().strftime('%Y%m%d_%H%M%S')
        output_filename = f"金融岛报表_已填充_{timestamp}.xlsx"
        output_path = os.path.join(app.config['OUTPUT_FOLDER'], output_filename)
        filler.save_report(output_path)
        print(f"✅ 报表已保存: {output_path}")

        # 更新填充历史
        supplierData = filler.count_supplier_data_in_columns(
            session['suppliers_config']['suppliers'],
            session.get('dates', [])
        )
        session['supplier_data'] = supplierData
        session['last_output_file'] = output_filename
        print(f"✅ 填充历史: {supplierData}")

        return jsonify({
            'success': True,
            'message': '填充完成！',
            'fillHistory': supplierData,
            'output_file': output_filename
        })

    except Exception as e:
        print(f"❌ 填充失败: {str(e)}")
        traceback.print_exc()
        return jsonify({'success': False, 'error': f'填充失败: {str(e)}'}), 500


@app.route('/api/paste-handover', methods=['POST'])
def paste_handover():
    """生成交接单数据"""
    try:
        session_id = request.headers.get('X-Session-ID')
        if not session_id or session_id not in sessions:
            return jsonify({'success': False, 'error': '无效的会话'}), 400

        session = get_session(session_id)
        filler = session.get('filler')
        if not filler:
            return jsonify({'success': False, 'error': '请先上传报表'}), 400

        # ✅ 获取表首表尾配置
        data = request.json or {}
        header_footer_config = data.get('headerFooterConfig', {})

        # 🔍 调试日志：打印表首表尾配置
        print(f"🔍 交接单表首表尾配置: {header_footer_config}")
        if 'header' in header_footer_config:
            print(f"   表首: {header_footer_config['header']}")
        if 'footer' in header_footer_config:
            print(f"   表尾: {header_footer_config['footer']}")

        # 生成交接单数据（传递表首表尾配置）
        filler.compile_handover_from_suppliers(
            session['suppliers_config'],
            header_footer_config=header_footer_config
        )

        # 保存结果
        timestamp = datetime.now().strftime('%Y%m%d_%H%M%S')
        output_filename = f"金融岛报表_交接单_{timestamp}.xlsx"
        output_path = os.path.join(app.config['OUTPUT_FOLDER'], output_filename)
        filler.save_report(output_path)

        # 更新填充历史
        supplierData = filler.count_supplier_data_in_columns(
            session['suppliers_config']['suppliers'],
            session.get('dates', [])
        )
        session['supplier_data'] = supplierData
        session['last_output_file'] = output_filename

        return jsonify({
            'success': True,
            'message': '交接单数据生成完成！',
            'fillHistory': supplierData,
            'output_file': output_filename
        })

    except Exception as e:
        traceback.print_exc()
        return jsonify({'success': False, 'error': f'操作失败: {str(e)}'}), 500


@app.route('/api/paste-stockin', methods=['POST'])
def paste_stockin():
    """生成入库单数据"""
    try:
        session_id = request.headers.get('X-Session-ID')
        if not session_id or session_id not in sessions:
            return jsonify({'success': False, 'error': '无效的会话'}), 400

        session = get_session(session_id)
        filler = session.get('filler')
        if not filler:
            return jsonify({'success': False, 'error': '请先上传报表'}), 400

        # ✅ 获取表首表尾配置
        data = request.json or {}
        header_footer_config = data.get('headerFooterConfig', {})

        # 🔍 调试日志：打印表首表尾配置
        print(f"🔍 入库单表首表尾配置: {header_footer_config}")
        if 'header' in header_footer_config:
            print(f"   表首: {header_footer_config['header']}")
        if 'footer' in header_footer_config:
            print(f"   表尾: {header_footer_config['footer']}")

        # 生成入库单数据（传递表首表尾配置）
        filler.compile_stock_in_from_suppliers(
            session['suppliers_config'],
            header_footer_config=header_footer_config
        )

        # 保存结果
        timestamp = datetime.now().strftime('%Y%m%d_%H%M%S')
        output_filename = f"金融岛报表_入库单_{timestamp}.xlsx"
        output_path = os.path.join(app.config['OUTPUT_FOLDER'], output_filename)
        filler.save_report(output_path)

        # 更新填充历史
        supplierData = filler.count_supplier_data_in_columns(
            session['suppliers_config']['suppliers'],
            session.get('dates', [])
        )
        session['supplier_data'] = supplierData
        session['last_output_file'] = output_filename

        return jsonify({
            'success': True,
            'message': '入库单数据生成完成！',
            'fillHistory': supplierData,
            'output_file': output_filename
        })

    except Exception as e:
        traceback.print_exc()
        return jsonify({'success': False, 'error': f'操作失败: {str(e)}'}), 500


@app.route('/api/paste-request', methods=['POST'])
def paste_request():
    """生成需求单数据"""
    try:
        session_id = request.headers.get('X-Session-ID')
        if not session_id or session_id not in sessions:
            return jsonify({'success': False, 'error': '无效的会话'}), 400

        session = get_session(session_id)
        filler = session.get('filler')
        if not filler:
            return jsonify({'success': False, 'error': '请先上传报表'}), 400

        # ✅ 获取表首表尾配置
        data = request.json or {}
        header_footer_config = data.get('headerFooterConfig', {})

        # 🔍 调试日志：打印表首表尾配置
        print(f"🔍 需求单表首表尾配置: {header_footer_config}")
        if 'header' in header_footer_config:
            print(f"   表首: {header_footer_config['header']}")
        if 'footer' in header_footer_config:
            print(f"   表尾: {header_footer_config['footer']}")

        # 生成需求单表首表尾（传递表首表尾配置）
        filler.add_request_header_footer(header_footer_config=header_footer_config)

        # 保存结果
        timestamp = datetime.now().strftime('%Y%m%d_%H%M%S')
        output_filename = f"金融岛报表_需求单_{timestamp}.xlsx"
        output_path = os.path.join(app.config['OUTPUT_FOLDER'], output_filename)
        filler.save_report(output_path)

        return jsonify({
            'success': True,
            'message': '需求单表首表尾添加完成！',
            'output_file': output_filename
        })

    except Exception as e:
        traceback.print_exc()
        return jsonify({'success': False, 'error': f'操作失败: {str(e)}'}), 500


@app.route('/api/delete-columns', methods=['POST'])
def delete_columns():
    """删除指定列"""
    try:
        session_id = request.headers.get('X-Session-ID')
        if not session_id or session_id not in sessions:
            return jsonify({'success': False, 'error': '无效的会话'}), 400

        session = get_session(session_id)
        filler = session.get('filler')
        if not filler:
            return jsonify({'success': False, 'error': '请先上传报表'}), 400

        data = request.json
        startColumn = data.get('startColumn')

        # 删除列
        filler.delete_columns_after(start_column_letter=startColumn)

        # 保存结果
        timestamp = datetime.now().strftime('%Y%m%d_%H%M%S')
        output_filename = f"金融岛报表_已删除列_{timestamp}.xlsx"
        output_path = os.path.join(app.config['OUTPUT_FOLDER'], output_filename)
        filler.save_report(output_path)

        return jsonify({
            'success': True,
            'message': f'已从 {startColumn} 列开始删除数据',
            'output_file': output_filename
        })

    except Exception as e:
        traceback.print_exc()
        return jsonify({'success': False, 'error': f'操作失败: {str(e)}'}), 500


@app.route('/api/fill-history', methods=['DELETE'])
def clear_fill_history():
    """清空填充历史"""
    try:
        session_id = request.headers.get('X-Session-ID')
        if not session_id or session_id not in sessions:
            return jsonify({'success': False, 'error': '无效的会话'}), 400

        session = get_session(session_id)
        filler = session.get('filler')
        if not filler:
            return jsonify({'success': False, 'error': '请先上传报表'}), 400

        # 清空所有供应商列的数据
        suppliers = session['suppliers_config']['suppliers']
        for supplier in suppliers:
            filler._clear_all_data_in_column_range(
                supplier['start_column'],
                supplier['end_column']
            )

        # 清空填充历史
        session['supplier_data'] = {}

        # 保存结果
        timestamp = datetime.now().strftime('%Y%m%d_%H%M%S')
        output_filename = f"金融岛报表_已清空_{timestamp}.xlsx"
        output_path = os.path.join(app.config['OUTPUT_FOLDER'], output_filename)
        filler.save_report(output_path)

        return jsonify({
            'success': True,
            'message': '填充历史已清空',
            'output_file': output_filename
        })

    except Exception as e:
        traceback.print_exc()
        return jsonify({'success': False, 'error': f'操作失败: {str(e)}'}), 500


@app.route('/api/download/<filename>')
def download_file(filename):
    """下载文件"""
    try:
        filepath = os.path.join(app.config['OUTPUT_FOLDER'], filename)
        if os.path.exists(filepath):
            return send_file(filepath, as_attachment=True, download_name=filename)
        else:
            return jsonify({'success': False, 'error': '文件不存在'}), 404
    except Exception as e:
        traceback.print_exc()
        return jsonify({'success': False, 'error': f'下载失败: {str(e)}'}), 500


# ==================== 配置管理路由 ====================

@app.route('/config')
def config_page():
    """配置管理页面"""
    return render_template('config.html')


@app.route('/api/suppliers-config', methods=['GET'])
def get_suppliers_config():
    """获取送货商配置"""
    try:
        return jsonify(GLOBAL_SUPPLIERS_CONFIG)
    except Exception as e:
        traceback.print_exc()
        return jsonify({'success': False, 'error': str(e)}), 500


@app.route('/api/suppliers-config', methods=['POST'])
def save_suppliers_config_api():
    """保存送货商配置"""
    try:
        data = request.json

        if not data or 'suppliers' not in data:
            return jsonify({'success': False, 'error': '无效的配置数据'}), 400

        # 更新全局配置
        global GLOBAL_SUPPLIERS_CONFIG
        GLOBAL_SUPPLIERS_CONFIG = data

        # 保存到文件
        if save_suppliers_config_to_file():
            # 更新所有活跃 session 的配置
            update_all_sessions_config()

            return jsonify({
                'success': True,
                'message': '配置已保存'
            })
        else:
            return jsonify({
                'success': False,
                'message': '保存配置文件失败'
            }), 500

    except Exception as e:
        traceback.print_exc()
        return jsonify({'success': False, 'error': str(e)}), 500


# ==================== Whisper 语音识别功能 ====================

# Whisper 任务状态管理
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

        # 更新状态：处理中
        whisper_tasks[task_id].update({
            'status': 'processing',
            'progress': 0,
            'message': '任务初始化...'
        })

        # 创建转录器
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


@app.route('/whisper')
def whisper_page():
    """Whisper 语音识别页面"""
    return render_template('whisper.html')


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

        # 保存文件
        filename = f"{uuid.uuid4()}_{file.filename}"
        audio_path = os.path.join('uploads/whisper/audio', filename)
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
            'audio_file': filename
        }

        # 创建并启动线程
        thread = threading.Thread(
            target=run_whisper_task,
            args=(task_id, audio_path, model_size, output_formats)
        )
        thread.daemon = True  # 守护线程，主程序退出时自动结束
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
        # 安全检查：确保文件路径在允许的目录内
        if '..' in filename or filename.startswith('/'):
            return jsonify({'success': False, 'error': '非法的文件路径'}), 400

        # 检查文件是否在 whisper 输出目录中
        allowed_dirs = ['temp/whisper/text', 'temp/whisper/srt', 'temp/whisper/vtt']
        filepath = None
        for dir_path in allowed_dirs:
            potential_path = os.path.join(dir_path, filename)
            if os.path.exists(potential_path):
                filepath = potential_path
                break

        if not filepath or not os.path.exists(filepath):
            return jsonify({'success': False, 'error': '文件不存在'}), 404

        return send_file(filepath, as_attachment=True, download_name=filename)

    except Exception as e:
        traceback.print_exc()
        return jsonify({'success': False, 'error': f'下载失败: {str(e)}'}), 500


def init_whisper():
    """初始化 Whisper（后台预加载模型）"""
    def load_models():
        time.sleep(2)  # 等待应用启动
        try:
            from whisper_service import WhisperTranscriber
            print("⏳ 后台预加载 Whisper 模型...")
            WhisperTranscriber.preload_models(['base'])
        except Exception as e:
            print(f"⚠️  Whisper 模型预加载失败：{e}")

    thread = threading.Thread(target=load_models, daemon=True)
    thread.start()


# 启动时预加载 Whisper 模型
init_whisper()


if __name__ == '__main__':
    import webbrowser
    import threading

    def open_browser():
        time.sleep(2)  # 等待服务器启动
        webbrowser.open('http://localhost:8888')

    # 在后台线程打开浏览器
    browser_thread = threading.Thread(target=open_browser, daemon=True)
    browser_thread.start()

    print("=" * 70)
    print("   数据填充工具 - Web应用 (高级版)")
    print("   访问地址: http://localhost:8888")
    print("=" * 70)
    app.run(host='0.0.0.0', port=8888, debug=False, use_reloader=False)
