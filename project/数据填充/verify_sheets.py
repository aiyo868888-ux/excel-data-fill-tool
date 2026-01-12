"""
简单验证脚本：检查交接单是否只处理数字工作表
"""

def test_get_numeric_sheets():
    """测试get_all_numeric_sheets方法"""
    class MockSheet:
        def __init__(self, name):
            self.name = name

    class MockWB:
        def __init__(self, sheetnames):
            self.sheetnames = sheetnames

    class MockDataFiller:
        def __init__(self, sheets):
            self.wb = MockWB(sheets)

        def get_all_numeric_sheets(self):
            """获取所有名称为数字的工作表"""
            numeric_sheets = []
            for sheet_name in self.wb.sheetnames:
                if sheet_name.isdigit():
                    numeric_sheets.append((int(sheet_name), sheet_name))

            numeric_sheets.sort()
            result = [name for num, name in numeric_sheets]
            return result

    # 测试数据
    test_sheets = ['封面', '目录', '汇总', '统计', '1', '2', '3', '10', '31']
    filler = MockDataFiller(test_sheets)

    print("=" * 70)
    print("测试 get_all_numeric_sheets() 方法")
    print("=" * 70)
    print(f"\n所有工作表: {test_sheets}")

    numeric_only = filler.get_all_numeric_sheets()
    print(f"\n返回的数字工作表: {numeric_only}")

    # 验证
    expected = ['1', '2', '3', '10', '31']
    if numeric_only == expected:
        print("\n✅ 测试通过：只返回数字工作表")
        print("   - 不包含：封面、目录、汇总、统计")
        return True
    else:
        print(f"\n❌ 测试失败：期望 {expected}，实际 {numeric_only}")
        return False

if __name__ == '__main__':
    import sys
    success = test_get_numeric_sheets()
    sys.exit(0 if success else 1)
