"""
数据填充工具 - Web应用 (高级版本)
支持多供应商管理、进度追踪、会话管理
"""

# ========== 关键：必须在任何导入之前修复 sys.path ==========
import sys
import os

# 修复 numpy 源代码目录检查问题
# 这是 PyInstaller 打包后 numpy 报错的根本原因
if hasattr(sys, '_MEIPASS'):
    # PyInstaller 打包环境：完全重置 sys.path
    # 只保留 _MEIPASS，删除其他所有路径
    sys.path = [sys._MEIPASS]
else:
    # 开发环境：移除空字符串和当前目录引用
    if '' in sys.path:
        sys.path.remove('')
    if '.' in sys.path:
        sys.path.remove('.')
# ========== sys.path 修复完成 ==========

from flask import Flask, render_template, request, send_file, jsonify
import traceback
from datetime import datetime
import pandas as pd
import tempfile
import uuid
import json
import threading
import time

# ==================== 获取资源路径 ====================
def get_resource_path():
    """
    获取资源文件路径（支持 PyInstaller 打包）
    Returns:
        资源文件所在目录的绝对路径
    """
    # 优先从环境变量读取（由 exe 启动器设置）
    if 'FLASK_RESOURCE_DIR' in os.environ:
        return os.environ['FLASK_RESOURCE_DIR']

    # PyInstaller 打包后的临时目录
    if hasattr(sys, '_MEIPASS'):
        return sys._MEIPASS

    # 开发环境：使用当前文件所在目录
    return os.path.abspath(os.path.dirname(__file__))

# 获取资源目录
RESOURCE_DIR = get_resource_path()

# 导入核心数据填充类
import importlib.util
# 使用资源目录查找数据填充工具模块
module_path = os.path.join(RESOURCE_DIR, '数据填充工具.py')
spec = importlib.util.spec_from_file_location("data_filler", module_path)
DataFillerModule = importlib.util.module_from_spec(spec)
spec.loader.exec_module(DataFillerModule)
DataFiller = DataFillerModule.DataFiller

# 初始化 Flask 应用，指定 templates 和 static 路径
app = Flask(__name__,
            template_folder=os.path.join(RESOURCE_DIR, 'templates'),
            static_folder=os.path.join(RESOURCE_DIR, 'static'))

app.config['MAX_CONTENT_LENGTH'] = 50 * 1024 * 1024  # 50MB最大文件上传

# 使用绝对路径
BASE_DIR = os.path.dirname(os.path.abspath(__file__))
app.config['UPLOAD_FOLDER'] = os.path.join(BASE_DIR, 'uploads')
app.config['OUTPUT_FOLDER'] = os.path.join(BASE_DIR, 'temp')
app.config['SESSION_FOLDER'] = os.path.join(BASE_DIR, 'sessions')

# 确保目录存在（在项目目录下）
os.makedirs(app.config['UPLOAD_FOLDER'], exist_ok=True)
os.makedirs(app.config['OUTPUT_FOLDER'], exist_ok=True)
os.makedirs(app.config['SESSION_FOLDER'], exist_ok=True)

# 打印路径信息（便于调试）
print(f"Flask 应用初始化完成:")
print(f"  资源目录: {RESOURCE_DIR}")
print(f"  Templates: {app.template_folder}")
print(f"  Static: {app.static_folder}")
print(f"  项目目录: {BASE_DIR}")
print(f"  Upload: {app.config['UPLOAD_FOLDER']}")
print(f"  Output: {app.config['OUTPUT_FOLDER']}")

# ==================== 配置管理 ====================

# 配置文件路径
CONFIG_FILE = os.path.join(BASE_DIR, 'suppliers_config.json')

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
            print(f"⚠️  加载配置失败: {e}")
    else:
        print(f"⚠️  配置文件不存在，使用默认配置")

def save_suppliers_config_to_file():
    """保存送货商配置到文件（保存到工作目录）"""
    global GLOBAL_SUPPLIERS_CONFIG
    # 保存到工作目录
    config_file = CONFIG_FILE_WORKING
    try:
        with open(config_file, 'w', encoding='utf-8') as f:
            json.dump(GLOBAL_SUPPLIERS_CONFIG, f, ensure_ascii=False, indent=2)
        print(f"✅ 配置已保存: {config_file}")
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
        print(f"[DEBUG] upload-suppliers called, session_id: {session_id}")
        print(f"[DEBUG] sessions keys: {list(sessions.keys())[:5]}")

        if not session_id or session_id not in sessions:
            print(f"[DEBUG] Session validation failed")
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


@app.route('/api/download')
@app.route('/api/download/<path:filename>')
def download_file(filename=None):
    """下载文件（如果不指定文件名，则下载最新的文件）"""
    try:
        output_folder = app.config['OUTPUT_FOLDER']

        print(f"[DEBUG] 下载请求: filename={filename}")

        # 如果没有指定文件名，获取最新的文件
        if filename is None:
            try:
                files = [f for f in os.listdir(output_folder) if f.endswith('.xlsx')]
                if files:
                    # 按修改时间排序，获取最新的文件
                    files_with_time = [
                        (f, os.path.getmtime(os.path.join(output_folder, f)))
                        for f in files
                    ]
                    files_with_time.sort(key=lambda x: x[1], reverse=True)
                    filename = files_with_time[0][0]
                    print(f"[DEBUG] 自动选择最新文件: {filename}")
                else:
                    return jsonify({'success': False, 'error': '没有可下载的文件'}), 404
            except Exception as e:
                return jsonify({'success': False, 'error': f'获取文件列表失败: {str(e)}'}), 500

        filepath = os.path.join(output_folder, filename)

        print(f"[DEBUG] 下载文件:")
        print(f"  文件名: {filename}")
        print(f"  OUTPUT_FOLDER: {output_folder}")
        print(f"  完整路径: {filepath}")
        print(f"  文件存在: {os.path.exists(filepath)}")

        if os.path.exists(filepath):
            return send_file(filepath, as_attachment=True, download_name=filename)
        else:
            # 列出目录中的文件，帮助调试
            try:
                files = os.listdir(output_folder)
                print(f"  目录中的文件: {files[:10]}")
            except:
                pass
            return jsonify({'success': False, 'error': f'文件不存在: {filename}'}), 404
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
