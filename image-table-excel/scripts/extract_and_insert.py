"""
一步完成：从图片提取表格并填入Excel
"""

import argparse
import sys
from pathlib import Path
from extract_table_from_image import TableExtractor, PaddleOCRExtractor, TesseractExtractor
from update_excel_with_data import ExcelUpdater


def extract_column_number(column_str: str) -> int:
    """将列字母转换为数字（A=1, B=2, ..., Z=26, AA=27, etc.）"""
    result = 0
    for char in column_str.upper():
        if 'A' <= char <= 'Z':
            result = result * 26 + (ord(char) - ord('A') + 1)
        else:
            raise ValueError(f"Invalid column letter: {char}")
    return result


def main():
    parser = argparse.ArgumentParser(
        description='从图片提取表格数据并填入Excel（一步完成）',
        epilog='示例: python extract_and_insert.py table.png data.xlsx --columns "姓名,金额,日期" --insert-row 5'
    )

    parser.add_argument('image_path', help='图片文件路径')
    parser.add_argument('excel_path', help='Excel文件路径')
    parser.add_argument('--columns', help='指定提取的列（索引:0,2,4 或 列名:姓名,金额）')
    parser.add_argument('--ocr-engine', choices=['paddleocr', 'tesseract'], default='paddleocr',
                        help='OCR引擎（默认: paddleocr）')
    parser.add_argument('--target-sheet', help='目标工作表名称（默认: 活动工作表）')
    parser.add_argument('--insert-row', type=int, default=1,
                        help='插入起始行号（默认: 1，即追加到现有数据后）')
    parser.add_argument('--append', action='store_true',
                        help='追加到现有数据后（自动检测最后数据行）')
    parser.add_argument('--no-blank-row', action='store_true',
                        help='不在原有内容和新内容间插入空行')
    parser.add_argument('--paste-image', action='store_true',
                        help='将图片粘贴到Excel中')
    parser.add_argument('--image-column', default='A',
                        help='图片粘贴列（默认: A列）')

    args = parser.parse_args()

    # 检查文件
    image_path = Path(args.image_path)
    excel_path = Path(args.excel_path)

    if not image_path.exists():
        print(f"错误: 图片文件不存在: {args.image_path}")
        sys.exit(1)

    if not excel_path.exists():
        print(f"错误: Excel文件不存在: {args.excel_path}")
        sys.exit(1)

    # 解析列参数
    columns = None
    if args.columns:
        columns = [c.strip() for c in args.columns.split(',')]

    print("=" * 60)
    print("图片表格识别并填入Excel工具")
    print("=" * 60)

    # 步骤1: 提取表格
    print(f"\n[步骤 1/2] 正在识别图片: {args.image_path}")

    if args.ocr_engine == 'paddleocr':
        extractor = PaddleOCRExtractor()
    else:
        extractor = TesseractExtractor()

    table_data = extractor.extract(str(image_path), columns)

    if not table_data:
        print("错误: 未识别到表格数据")
        sys.exit(1)

    print(f"✓ 识别成功，共 {len(table_data)} 行")

    # 显示前几行数据预览
    preview_rows = min(3, len(table_data))
    print("\n数据预览:")
    for i, row in enumerate(table_data[:preview_rows], 1):
        cells = row.get("cells", [])
        print(f"  行 {i}: {' | '.join(cells)}")
    if len(table_data) > preview_rows:
        print(f"  ... (还有 {len(table_data) - preview_rows} 行)")

    # 步骤2: 更新Excel
    print(f"\n[步骤 2/2] 正在更新Excel: {args.excel_path}")

    updater = ExcelUpdater(str(excel_path))

    # 确定插入行
    insert_row = args.insert_row
    if args.append:
        # 自动检测最后数据行
        ws = updater.wb.active
        insert_row = ws.max_row + 1
        print(f"✓ 追加模式：将从第 {insert_row} 行开始插入")

    # 准备图片路径
    image_path_for_paste = str(image_path) if args.paste_image else None

    # 更新Excel
    updater.update(
        data=table_data,
        insert_row=insert_row,
        target_sheet=args.target_sheet,
        insert_blank_row=not args.no_blank_row,
        image_path=image_path_for_paste,
        image_column=args.image_column
    )

    print("\n" + "=" * 60)
    print("✓ 完成！")
    print("=" * 60)
    print(f"\n统计:")
    print(f"  - 识别行数: {len(table_data)}")
    print(f"  - 插入位置: 第 {insert_row} 行")
    print(f"  - 目标文件: {excel_path}")
    if args.paste_image:
        print(f"  - 图片位置: {args.image_column}列")


if __name__ == '__main__':
    main()
