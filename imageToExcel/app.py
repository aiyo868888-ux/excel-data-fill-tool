"""
imageToExcel - 图片表格识别并填入Excel的Web应用
使用Flask提供Web界面，调用image-table-excel技能
"""
import os
# 必须在导入PaddleOCR之前设置环境变量
os.environ['USE_ONEDNN'] = '0'
os.environ['DISABLE_MODEL_SOURCE_CHECK'] = 'True'

import json
import shutil
from pathlib import Path
from flask import Flask, render_template, request, send_file, jsonify
from werkzeug.utils import secure_filename
import sys

# 添加技能路径
sys.path.insert(0, r'd:\claude code -11\image-table-excel\scripts')

# 延迟导入标志
OCR_IMPORTED = False

try:
    from map_and_insert import process_and_insert
    SKILL_AVAILABLE = True
    print("map_and_insert 模块导入成功")
except ImportError as e:
    SKILL_AVAILABLE = False
    print(f"警告: 无法导入image-table-excel技能 - {e}")

app = Flask(__name__)
app.config['MAX_CONTENT_LENGTH'] = 16 * 1024 * 1024  # 16MB最大文件大小
app.config['UPLOAD_FOLDER'] = Path(__file__).parent / 'uploads'
app.config['OUTPUT_FOLDER'] = Path(__file__).parent / 'outputs'

# 确保目录存在
app.config['UPLOAD_FOLDER'].mkdir(exist_ok=True)
app.config['OUTPUT_FOLDER'].mkdir(exist_ok=True)

ALLOWED_EXTENSIONS = {'xlsx', 'xls', 'png', 'jpg', 'jpeg', 'gif', 'bmp'}


def allowed_file(filename):
    """检查文件扩展名是否允许"""
    return '.' in filename and \
           filename.rsplit('.', 1)[1].lower() in ALLOWED_EXTENSIONS


@app.route('/')
def index():
    """主页"""
    return render_template('index.html')


@app.route('/api/process', methods=['POST'])
def process_files():
    """处理上传的文件"""

    # 检查技能是否可用
    if not SKILL_AVAILABLE:
        return jsonify({
            'success': False,
            'error': 'image-table-excel技能不可用，请确保路径正确'
        }), 500

    try:
        # 获取表单数据
        custom_data = request.form.get('custom_data', '')
        excel_file = request.files.get('excel_file')
        image_files = request.files.getlist('image_files')

        # 验证必需文件
        if not excel_file:
            return jsonify({
                'success': False,
                'error': '请上传Excel文件'
            }), 400

        # 验证Excel文件扩展名
        if excel_file.filename == '':
            return jsonify({
                'success': False,
                'error': '请选择Excel文件'
            }), 400

        filename = excel_file.filename.lower()
        if not (filename.endswith('.xlsx') or filename.endswith('.xls')):
            return jsonify({
                'success': False,
                'error': f'不支持的文件格式: {filename}<br>支持的格式: .xlsx, .xls<br>提示: 请使用"另存为"将文件保存为.xlsx格式'
            }), 400

        # 保存Excel文件（保留原文件名，但移除不安全字符）
        original_filename = excel_file.filename
        # 只保留扩展名安全检查，保留中文文件名
        if original_filename.endswith('.xlsx') or original_filename.endswith('.xls'):
            excel_filename = original_filename
        else:
            # 如果扩展名不对，使用secure_filename
            excel_filename = secure_filename(original_filename)

        excel_path = app.config['UPLOAD_FOLDER'] / excel_filename
        excel_file.save(excel_path)

        # 验证Excel文件是否有效
        try:
            from openpyxl import load_workbook
            test_wb = load_workbook(excel_path, read_only=True)
            test_wb.close()
        except Exception as e:
            # 清理无效文件
            try:
                excel_path.unlink()
            except:
                pass

            error_msg = f'Excel文件验证失败<br><br>可能的原因:<br>1. 文件已损坏<br>2. 文件实际上是其他格式（如CSV、TXT）改了扩展名<br>3. 文件版本太旧（Excel 95及更早版本）<br>4. 文件受密码保护<br><br>建议：用Excel或WPS打开文件，然后"另存为".xlsx格式'
            return jsonify({
                'success': False,
                'error': error_msg
            }), 400

        # 保存图片文件（保留原文件名）
        image_paths = []
        for img in image_files:
            if img and img.filename and allowed_file(img.filename):
                # 保留原文件名（包含中文）
                img_filename = img.filename
                img_path = app.config['UPLOAD_FOLDER'] / img_filename
                img.save(img_path)
                image_paths.append(str(img_path))

        # 解析自定义数据
        user_custom = None
        if custom_data:
            try:
                user_custom = json.loads(custom_data)
            except json.JSONDecodeError:
                return jsonify({
                    'success': False,
                    'error': '自定义数据格式错误，应为JSON格式'
                }), 400

        # 检查是否有数据文件（可选）
        data_file = request.files.get('data_file')
        data_file_path = None
        if data_file and data_file.filename and allowed_file(data_file.filename):
            data_filename = secure_filename(data_file.filename)
            data_file_path = app.config['UPLOAD_FOLDER'] / data_filename
            data_file.save(data_file_path)

        # 如果没有提供数据文件但有图片，使用OCR提取
        extracted_data_file = None
        if not data_file_path and image_paths and SKILL_AVAILABLE:
            print(f"[OCR] 正在从图片提取表格数据...")
            try:
                import subprocess

                # 使用子进程调用OCR脚本，确保环境变量正确传递
                extract_script = Path(r'd:\claude code -11\image-table-excel\scripts\extract_table_from_image.py')
                extracted_data_file = app.config['UPLOAD_FOLDER'] / 'extracted_data.json'

                # 对第一张图片进行OCR
                img_path = image_paths[0]
                print(f"[OCR] 处理图片: {img_path}")

                # 设置环境变量
                env = os.environ.copy()
                env['USE_ONEDNN'] = '0'
                env['DISABLE_MODEL_SOURCE_CHECK'] = 'True'

                # 调用OCR脚本
                cmd = [sys.executable, str(extract_script), str(img_path), '--output', str(extracted_data_file)]
                result = subprocess.run(cmd, capture_output=True, text=True, encoding='utf-8', errors='ignore',
                                      env=env, timeout=120)

                if result.returncode == 0 and extracted_data_file.exists():
                    # 读取提取的数据
                    with open(extracted_data_file, 'r', encoding='utf-8') as f:
                        all_extracted_data = json.load(f)
                    print(f"[OCR] 提取到 {len(all_extracted_data)} 行数据")
                else:
                    print(f"[OCR] 提取失败，返回码: {result.returncode}")
                    if result.stderr:
                        print(f"[OCR] 错误: {result.stderr[:500]}")
                    all_extracted_data = []
                    extracted_data_file = None

            except subprocess.TimeoutExpired:
                print("[OCR] 识别超时（>120秒）")
                extracted_data_file = None
            except Exception as e:
                print(f"[OCR] 提取失败: {e}")
                import traceback
                traceback.print_exc()
                extracted_data_file = None

        # 处理并插入
        output_path = process_and_insert(
            excel_path=str(excel_path),
            image_paths=image_paths if image_paths else None,
            data_file=str(extracted_data_file) if extracted_data_file else str(data_file_path) if data_file_path else None,
            user_custom=user_custom
        )

        # 生成输出文件名
        output_filename = Path(output_path).name

        return jsonify({
            'success': True,
            'message': '处理成功！',
            'output_file': output_filename,
            'download_url': f'/download/{output_filename}',
            'data': {
                'excel_file': excel_filename,
                'image_count': len(image_paths),
                'has_custom_data': bool(user_custom),
                'has_data_file': bool(data_file_path)
            }
        })

    except Exception as e:
        import traceback
        error_detail = str(e)

        # 提供更友好的错误信息
        if "openpyxl does not support file format" in error_detail:
            error_msg = "上传的文件不是有效的Excel格式。请确保文件扩展名是.xlsx或.xls，并且可以用Excel正常打开。"
        elif "JSONDecodeError" in error_detail:
            error_msg = "自定义数据格式错误，请检查JSON语法是否正确。"
        elif "File" in error_detail:
            error_msg = "文件读取错误，请检查文件是否损坏。"
        else:
            error_msg = f"处理失败: {error_detail}"

        # 记录详细错误日志
        print(f"Error processing request: {error_detail}")
        traceback.print_exc()

        return jsonify({
            'success': False,
            'error': error_msg
        }), 500


@app.route('/download/<filename>')
def download_file(filename):
    """下载生成的文件"""
    try:
        # 先在outputs文件夹查找
        output_path = app.config['OUTPUT_FOLDER'] / filename
        if not output_path.exists():
            # 如果不在outputs，在uploads查找
            output_path = app.config['UPLOAD_FOLDER'] / filename

        if output_path.exists():
            return send_file(output_path, as_attachment=True)
        else:
            return jsonify({'error': '文件不存在'}), 404
    except Exception as e:
        return jsonify({'error': str(e)}), 500


@app.route('/api/health')
def health_check():
    """健康检查"""
    return jsonify({
        'status': 'ok',
        'skill_available': SKILL_AVAILABLE
    })


if __name__ == '__main__':
    print("=" * 60)
    print("imageToExcel Web App")
    print("=" * 60)
    skill_status = "Available" if SKILL_AVAILABLE else "Not Available"
    print(f"Skill Status: {skill_status}")
    print(f"Upload Folder: {app.config['UPLOAD_FOLDER']}")
    print(f"Output Folder: {app.config['OUTPUT_FOLDER']}")
    print("\nStarting server...")
    print("Access URL: http://localhost:5000")
    print("Press Ctrl+C to stop")
    print("=" * 60)

    # 禁用debug模式以避免PaddleOCR重载问题
    app.run(debug=False, host='0.0.0.0', port=5000)
