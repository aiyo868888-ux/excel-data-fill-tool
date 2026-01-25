#!/usr/bin/env python3
"""
Smart Todo Parser - 快速开始示例
"""
from smart_todo_parser import TodoParser, TaskManager, SimpleReminder
from datetime import datetime


def demo_basic_parsing():
    """基础解析示例"""
    print("=" * 50)
    print("示例 1: 基础时间解析")
    print("=" * 50)

    parser = TodoParser()

    examples = [
        "明天下午3点开会",
        "2025年3月15日下午2点面试",
        "后天交报告",
        "月底前完成项目",
    ]

    for text in examples:
        result = parser.parse(text)
        print(f"\n输入：{text}")
        print(f"  任务：{result['task']}")
        if result['datetime']:
            print(f"  时间：{result['datetime']}")
        print(f"  类型：{result['type']}")


def demo_priority_and_tags():
    """优先级和标签示例"""
    print("\n" + "=" * 50)
    print("示例 2: 优先级和标签识别")
    print("=" * 50)

    parser = TodoParser()

    examples = [
        "明天紧急处理bug #工作",
        "下周重要会议 #工作 #重要",
        "有空时整理文档 #个人",
        "今天提交代码",
    ]

    for text in examples:
        result = parser.parse(text)
        print(f"\n输入：{text}")
        print(f"  任务：{result['task']}")
        print(f"  优先级：{result['priority']}")
        if result['tags']:
            print(f"  标签：{', '.join(result['tags'])}")


def demo_task_manager():
    """任务管理示例"""
    print("\n" + "=" * 50)
    print("示例 3: 任务管理")
    print("=" * 50)

    parser = TodoParser()
    task_manager = TaskManager(storage_path="demo_tasks.json")

    # 添加任务
    todos = [
        "明天下午3点开会 #工作",
        "后天早上9点面试 #求职",
        "下周三提交报告 #重要",
    ]

    print("\n添加任务：")
    for todo_text in todos:
        result = parser.parse(todo_text)
        if result['success']:
            task = task_manager.add_task(result)
            print(f"  ✓ {task.task} (ID: {task.id[:15]}...)")

    # 列出所有任务
    print("\n所有任务：")
    tasks = task_manager.list_tasks()
    for task in tasks:
        status = "✓" if task.status == "completed" else "○"
        print(f"  {status} 【{task.priority}】{task.task}")
        if task.datetime:
            print(f"     时间：{task.datetime}")


def demo_reminder():
    """提醒器示例"""
    print("\n" + "=" * 50)
    print("示例 4: 提醒器")
    print("=" * 50)

    parser = TodoParser()
    task_manager = TaskManager(storage_path="demo_tasks.json")
    reminder = SimpleReminder(task_manager)

    # 手动检查一次
    due_tasks = reminder.check_now()
    if due_tasks:
        print("\n即将到期的任务：")
        for task in due_tasks:
            print(f"  🔔 {task.task}")
            if task.datetime:
                print(f"     时间：{task.datetime}")
    else:
        print("\n无到期任务")


def demo_batch_processing():
    """批量处理示例"""
    print("\n" + "=" * 50)
    print("示例 5: 批量处理")
    print("=" * 50)

    parser = TodoParser()

    # 批量解析
    texts = [
        "明天开会",
        "后天交报告",
        "下周三面试",
        "月底前完成",
    ]

    results = parser.parse_batch(texts)

    print("批量解析结果：")
    for i, result in enumerate(results, 1):
        print(f"\n{i}. {result['raw_text']}")
        print(f"   任务：{result['task']}")
        if result['datetime']:
            print(f"   时间：{result['datetime']}")


def demo_fixed_date():
    """固定基准日期示例（用于测试）"""
    print("\n" + "=" * 50)
    print("示例 6: 固定基准日期（测试用）")
    print("=" * 50)

    # 设置基准日期为 2025年1月20日
    base_date = datetime(2025, 1, 20, 10, 0, 0)
    parser = TodoParser(base_date=base_date)

    examples = [
        ("明天开会", "2025-01-21 09:00:00"),
        ("后天交报告", "2025-01-22 09:00:00"),
        ("下周三面试", "2025-01-29 00:00:00"),
    ]

    print("基准日期：2025-01-20 10:00:00\n")
    for text, expected in examples:
        result = parser.parse(text)
        print(f"输入：{text}")
        print(f"  解析：{result['datetime']}")
        print(f"  期望：{expected}")
        print(f"  匹配：{'✓' if result['datetime'] == expected else '✗'}")


def main():
    """运行所有示例"""
    print("\n" + "🚀" * 25)
    print("\nSmart Todo Parser - 智能待办事项解析器")
    print("快速开始示例\n")

    try:
        demo_basic_parsing()
        demo_priority_and_tags()
        demo_task_manager()
        demo_reminder()
        demo_batch_processing()
        demo_fixed_date()

        print("\n" + "=" * 50)
        print("✅ 所有示例运行完成！")
        print("=" * 50)
        print("\n下一步：")
        print("  1. 查看 docs/EXAMPLES.md 了解更多用法")
        print("  2. 查看 docs/API.md 了解 API 详情")
        print("  3. 运行 pytest 运行测试")

    except Exception as e:
        print(f"\n❌ 错误：{e}")
        import traceback
        traceback.print_exc()


if __name__ == "__main__":
    main()
