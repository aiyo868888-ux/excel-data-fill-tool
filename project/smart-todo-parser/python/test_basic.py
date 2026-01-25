#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""基础功能测试"""
from smart_todo_parser import TodoParser, TaskManager, SimpleReminder
from datetime import datetime

def test_basic_parse():
    """测试基础解析"""
    print("=" * 50)
    print("测试 1: 基础时间解析")
    print("=" * 50)

    parser = TodoParser()

    # 测试用例
    tests = [
        ("明天下午3点开会", "relative"),
        ("2025年3月15日下午2点面试", "absolute"),
        ("后天交报告", "relative"),
        ("月底前完成项目", "fuzzy"),
    ]

    for text, expected_type in tests:
        result = parser.parse(text)
        status = "PASS" if result['success'] and result['type'] == expected_type else "FAIL"
        print(f"\n[{status}] {text}")
        print(f"  Task: {result['task']}")
        print(f"  Time: {result['datetime']}")
        print(f"  Type: {result['type']} (expected: {expected_type})")

def test_priority_and_tags():
    """测试优先级和标签"""
    print("\n" + "=" * 50)
    print("测试 2: 优先级和标签")
    print("=" * 50)

    parser = TodoParser()

    tests = [
        ("明天紧急处理bug #工作", "high", ["工作"]),
        ("下周重要会议 #工作 #重要", "medium", ["工作", "重要"]),
        ("有空时整理文档 #个人", "low", ["个人"]),
        ("今天提交代码", "high", []),
    ]

    for text, expected_priority, expected_tags in tests:
        result = parser.parse(text)
        priority_match = result['priority'] == expected_priority
        tags_match = set(result['tags']) == set(expected_tags)
        status = "PASS" if priority_match and tags_match else "FAIL"

        print(f"\n[{status}] {text}")
        print(f"  Priority: {result['priority']} (expected: {expected_priority})")
        print(f"  Tags: {result['tags']} (expected: {expected_tags})")

def test_fixed_date():
    """测试固定基准日期"""
    print("\n" + "=" * 50)
    print("测试 3: 固定基准日期")
    print("=" * 50)

    base_date = datetime(2025, 1, 20, 10, 0, 0)
    parser = TodoParser(base_date=base_date)

    tests = [
        ("明天开会", "2025-01-21 09:00:00"),
        ("后天交报告", "2025-01-22 09:00:00"),
        ("下周三面试", "2025-01-29 00:00:00"),
    ]

    for text, expected_time in tests:
        result = parser.parse(text)
        status = "PASS" if result['datetime'] == expected_time else "FAIL"
        print(f"\n[{status}] {text}")
        print(f"  Parsed: {result['datetime']}")
        print(f"  Expected: {expected_time}")

def test_task_manager():
    """测试任务管理"""
    print("\n" + "=" * 50)
    print("测试 4: 任务管理")
    print("=" * 50)

    parser = TodoParser()
    task_manager = TaskManager(storage_path="test_tasks.json")

    # 添加任务
    result = parser.parse("明天下午3点开会 #工作")
    task = task_manager.add_task(result)
    print(f"\n[ADD] Task ID: {task.id}")
    print(f"  Task: {task.task}")
    print(f"  Time: {task.datetime}")
    print(f"  Priority: {task.priority}")
    print(f"  Tags: {task.tags}")

    # 获取任务
    retrieved = task_manager.get_task(task.id)
    status = "PASS" if retrieved and retrieved.task == task.task else "FAIL"
    print(f"\n[{status}] Retrieved: {retrieved.task if retrieved else 'None'}")

    # 列出任务
    tasks = task_manager.list_tasks()
    print(f"\n[LIST] Total tasks: {len(tasks)}")

    # 完成任务
    completed = task_manager.complete_task(task.id)
    status = "PASS" if completed and completed.status.value == "completed" else "FAIL"
    print(f"\n[{status}] Completed: {completed.status.value if completed else 'None'}")

    # 删除任务
    deleted = task_manager.delete_task(task.id)
    status = "PASS" if deleted else "FAIL"
    print(f"\n[{status}] Deleted: {deleted}")

def test_batch():
    """测试批量解析"""
    print("\n" + "=" * 50)
    print("测试 5: 批量解析")
    print("=" * 50)

    parser = TodoParser()

    texts = [
        "明天开会",
        "后天交报告",
        "下周三面试",
        "月底前完成",
    ]

    results = parser.parse_batch(texts)
    status = "PASS" if len(results) == len(texts) else "FAIL"
    print(f"\n[{status}] Processed {len(results)}/{len(texts)} items")

    for i, result in enumerate(results, 1):
        print(f"\n{i}. {result['raw_text']}")
        print(f"   Task: {result['task']}")
        print(f"   Time: {result['datetime']}")

def main():
    """运行所有测试"""
    print("\n" + "=" * 60)
    print("Smart Todo Parser - 功能测试")
    print("=" * 60)

    try:
        test_basic_parse()
        test_priority_and_tags()
        test_fixed_date()
        test_task_manager()
        test_batch()

        print("\n" + "=" * 60)
        print("All tests completed!")
        print("=" * 60)

    except Exception as e:
        print(f"\nERROR: {e}")
        import traceback
        traceback.print_exc()

if __name__ == "__main__":
    main()
