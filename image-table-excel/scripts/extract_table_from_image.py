"""
从图片中提取表格数据
支持 paddleocr 和 tesseract 两种OCR引擎
"""

import os
# 禁用oneDNN以解决兼容性问题
os.environ['USE_ONEDNN'] = '0'
os.environ['DISABLE_MODEL_SOURCE_CHECK'] = 'True'

import argparse
import json
import sys
from pathlib import Path
from typing import List, Dict, Any, Optional
import re


class TableExtractor:
    """表格提取器基类"""

    def extract(self, image_path: str, columns: Optional[List[str]] = None) -> List[Dict[str, Any]]:
        """提取表格数据"""
        raise NotImplementedError


class PaddleOCRExtractor(TableExtractor):
    """使用PaddleOCR提取表格"""

    def __init__(self):
        try:
            from paddleocr import PaddleOCR
        except ImportError as e:
            print(f"错误: 无法导入PaddleOCR - {e}")
            print("安装命令: pip install paddleocr paddlepaddle")
            sys.exit(1)

        try:
            # 使用最简单的初始化，避免参数兼容性问题
            self.ocr = PaddleOCR(lang='ch')
        except Exception as e:
            print(f"错误: PaddleOCR初始化失败 - {e}")
            import traceback
            traceback.print_exc()
            sys.exit(1)

    def extract(self, image_path: str, columns: Optional[List[str]] = None) -> List[Dict[str, Any]]:
        """提取表格数据"""
        # PaddleOCR 2.x使用ocr()方法
        result = self.ocr.ocr(image_path, cls=True)

        if not result or not result[0]:
            return []

        # 解析OCR结果为表格结构
        table_data = self._parse_to_table(result[0])

        # 过滤指定列
        if columns:
            table_data = self._filter_columns(table_data, columns)

        return table_data

    def _parse_to_table(self, ocr_result: list) -> List[Dict[str, Any]]:
        """将OCR结果解析为表格结构"""
        # 简化实现：按y坐标分组识别行
        lines = {}
        for line in ocr_result:
            box, (text, confidence) = line
            y_center = (box[0][1] + box[2][1]) / 2
            x_center = (box[0][0] + box[2][0]) / 2

            # 按y坐标归类到行
            row_key = int(y_center / 30)  # 假设行高约30像素
            if row_key not in lines:
                lines[row_key] = []
            lines[row_key].append((x_center, text))

        # 按x坐标排序，构建表格
        table = []
        for row_key in sorted(lines.keys()):
            row_data = sorted(lines[row_key], key=lambda x: x[0])
            table.append({"cells": [text for _, text in row_data]})

        return table

    def _filter_columns(self, table_data: List[Dict], columns: List[str]) -> List[Dict]:
        """过滤指定列"""
        # 判断columns是索引还是列名
        if all(isinstance(c, str) and c.isdigit() for c in columns):
            # 列索引
            col_indices = [int(c) for c in columns]
            filtered = []
            for row in table_data:
                cells = row.get("cells", [])
                filtered_row = {
                    "cells": [cells[i] if i < len(cells) else "" for i in col_indices]
                }
                filtered.append(filtered_row)
            return filtered
        else:
            # 列名（假设第一行是表头）
            if not table_data:
                return []
            header = table_data[0].get("cells", [])
            col_indices = []
            for col_name in columns:
                if col_name in header:
                    col_indices.append(header.index(col_name))

            if not col_indices:
                return table_data  # 未找到匹配列，返回全部

            filtered = [{"cells": [table_data[0]["cells"][i] for i in col_indices]}]
            for row in table_data[1:]:
                cells = row.get("cells", [])
                filtered_row = {
                    "cells": [cells[i] if i < len(cells) else "" for i in col_indices]
                }
                filtered.append(filtered_row)
            return filtered


class TesseractExtractor(TableExtractor):
    """使用Tesseract提取表格"""

    def __init__(self):
        try:
            import pytesseract
            from PIL import Image
            self.pytesseract = pytesseract
            self.Image = Image
        except ImportError:
            print("错误: 需要安装 pytesseract 和 Pillow")
            print("安装命令: pip install pytesseract pillow")
            print("同时需要安装 Tesseract-OCR 软件")
            sys.exit(1)

    def extract(self, image_path: str, columns: Optional[List[str]] = None) -> List[Dict[str, Any]]:
        """提取表格数据"""
        image = self.Image.open(image_path)

        # 使用表格模式识别
        text = self.pytesseract.image_to_string(
            image,
            lang='chi_sim+eng',
            config='--psm 6'  # 假设单列文本
        )

        # 解析文本为表格
        lines = [line.strip() for line in text.split('\n') if line.strip()]
        table_data = []
        for line in lines:
            # 简单按空格分割
            cells = [cell.strip() for cell in line.split() if cell.strip()]
            if cells:
                table_data.append({"cells": cells})

        return table_data


def main():
    parser = argparse.ArgumentParser(description='从图片中提取表格数据')
    parser.add_argument('image_path', help='图片文件路径')
    parser.add_argument('--columns', help='指定列（索引:0,2,4 或 列名:姓名,金额）')
    parser.add_argument('--ocr-engine', choices=['paddleocr', 'tesseract'], default='paddleocr',
                        help='OCR引擎（默认: paddleocr）')
    parser.add_argument('--output', default='extracted_data.json', help='输出JSON文件路径')

    args = parser.parse_args()

    # 检查图片文件
    image_path = Path(args.image_path)
    if not image_path.exists():
        print(f"错误: 图片文件不存在: {args.image_path}")
        sys.exit(1)

    # 解析列参数
    columns = None
    if args.columns:
        columns = [c.strip() for c in args.columns.split(',')]

    # 选择OCR引擎
    if args.ocr_engine == 'paddleocr':
        extractor = PaddleOCRExtractor()
    else:
        extractor = TesseractExtractor()

    # 提取表格
    print(f"正在识别图片: {args.image_path}")
    table_data = extractor.extract(str(image_path), columns)

    if not table_data:
        print("警告: 未识别到表格数据")
        table_data = []

    # 保存结果
    output_path = Path(args.output)
    with open(output_path, 'w', encoding='utf-8') as f:
        json.dump(table_data, f, ensure_ascii=False, indent=2)

    print(f"识别完成，共 {len(table_data)} 行")
    print(f"数据已保存到: {output_path}")


def extract_table_from_image(image_path: str, columns: Optional[List[str]] = None) -> List[Dict[str, Any]]:
    """
    从图片中提取表格数据的便捷函数

    Args:
        image_path: 图片文件路径
        columns: 可选，指定要提取的列名列表

    Returns:
        包含表格数据的字典列表，每个字典代表一行
    """
    try:
        extractor = PaddleOCRExtractor()
        table_data = extractor.extract(image_path, columns)
        return table_data if table_data else []
    except Exception as e:
        print(f"OCR识别失败: {e}")
        return []


if __name__ == '__main__':
    main()
