"""
调试行结构 - 理解copy_data_to_handover的行逻辑
"""
from 数据填充工具 import DataFiller
import openpyxl

def debug_row_structure():
    """调试交接单的行结构"""
    print("=" * 70)
    print("调试交接单行结构")
    print("=" * 70)

    # 使用现有的交接单文件
    test_file = "d:/claude code -11/project/数据填充/temp/金融岛报表_交接单_20260109_144153.xlsx"

    # 先查看供应商数据范围
    import os
    if not os.path.exists(test_file):
        print(f"❌ 文件不存在: {test_file}")
        return

    filler = DataFiller(test_file)
    filler.load_report()  # 需要先加载报表
    ws = filler.wb['1']  # 使用第一个工作表

    # 查看几个送货商的数据范围
    suppliers = [
        ("菜1", 29, 34),   # AC-AH
        ("菜2", 35, 40),   # AL-AQ
    ]

    for name, start_col, end_col in suppliers:
        print(f"\n{'=' * 70}")
        print(f"送货商: {name} (列{start_col}-{end_col})")
        print("=" * 70)

        # 获取数据范围
        data_range = filler.get_supplier_data_range(ws, start_col, end_col)
        if data_range[0] is None:
            print("  ⚠️  没有数据")
            continue

        start_row, end_row = data_range
        num_rows = end_row - start_row + 1

        print(f"\n📊 数据范围: 第{start_row}行 - 第{end_row}行 (共{num_rows}行)")

        # 显示每一行的内容
        print(f"\n📝 数据内容:")
        for row_offset in range(num_rows):
            source_row = start_row + row_offset
            print(f"\n  row_offset={row_offset} (源第{source_row}行):")

            # 显示前3列的内容
            for col_offset in range(min(3, end_col - start_col + 1)):
                col = start_col + col_offset
                cell = ws.cell(row=source_row, column=col)
                col_letter = openpyxl.utils.get_column_letter(col)
                value = str(cell.value)[:30] if cell.value else "(空)"
                print(f"    {col_letter}列(偏移{col_offset}): {value}")

        print(f"\n🔍 行类型分析:")
        print(f"   row_offset=0: 第1行（送货商信息）→ 应该不填金额")
        print(f"   row_offset=1: 第2行（表头）→ 应该填'金额'")
        print(f"   row_offset=2到倒数第3行: 数据行 → 应该填公式 =数量*单价")
        print(f"   row_offset={num_rows-2}: 倒数第2行（合计）→ 应该填=SUM(...)")
        print(f"   row_offset={num_rows-1}: 最后一行 → 应该不填金额")

    print("\n" + "=" * 70)
    print("调试完成")
    print("=" * 70)

if __name__ == '__main__':
    debug_row_structure()
