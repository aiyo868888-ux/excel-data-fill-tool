# 提醒调度系统使用指南

## 1. 基础使用

```python
from smart_todo_parser import TodoParser, TaskManager, SimpleReminder

# 初始化
parser = TodoParser()
task_manager = TaskManager(storage_path="my_tasks.json")
reminder = SimpleReminder(task_manager)

# 解析并添加任务
result = parser.parse("明天下午3点开会 #工作")
task = task_manager.add_task(result)

# 检查即将到期的任务
due_tasks = reminder.check_now()
for task in due_tasks:
    print(f"提醒：{task.task} - {task.datetime}")
```

## 2. 完整工作流

```python
from smart_todo_parser import TodoParser, TaskManager, Reminder
import time

# 1. 创建解析器和任务管理器
parser = TodoParser()
task_manager = TaskManager(storage_path="tasks.json")

# 2. 批量添加任务
todos = [
    "明天下午3点开会 #工作",
    "后天早上9点面试 #求职",
    "下周三提交报告 #重要"
]

for todo_text in todos:
    result = parser.parse(todo_text)
    if result['success']:
        task = task_manager.add_task(result)
        print(f"✓ 已添加：{task.task}")

# 3. 创建提醒器
reminder = Reminder(task_manager, check_interval=60, reminder_before=15)

# 4. 添加提醒回调
def my_callback(task):
    print(f"\n🔔 任务提醒：{task.task}")
    print(f"   时间：{task.datetime}")
    print(f"   优先级：{task.priority}")

reminder.add_callback(my_callback)

# 5. 启动提醒器
reminder.start()

# 6. 模拟运行
try:
    while True:
        time.sleep(60)
except KeyboardInterrupt:
    reminder.stop()
```

## 3. 任务管理操作

### 查看所有任务

```python
# 获取所有待办任务
pending_tasks = task_manager.list_tasks(status=TaskStatus.PENDING)

for task in pending_tasks:
    print(f"【{task.priority}】{task.task}")
    if task.datetime:
        print(f"  时间：{task.datetime}")
    if task.tags:
        print(f"  标签：{', '.join(task.tags)}")
```

### 过滤任务

```python
# 按优先级过滤
high_priority_tasks = task_manager.list_tasks(priority="high")

# 按标签过滤
work_tasks = task_manager.list_tasks(tags=["工作"])

# 组合过滤
urgent_work_tasks = task_manager.list_tasks(
    priority="high",
    tags=["工作"]
)
```

### 更新任务状态

```python
# 完成任务
task = task_manager.complete_task("task_id")
print(f"✓ 已完成：{task.task}")

# 取消任务
task = task_manager.cancel_task("task_id")
print(f"✗ 已取消：{task.task}")

# 删除任务
success = task_manager.delete_task("task_id")
if success:
    print("已删除任务")
```

## 4. 提醒器高级配置

### 自定义检查间隔

```python
# 每30秒检查一次
reminder = Reminder(
    task_manager,
    check_interval=30,  # 秒
    reminder_before=15  # 提前15分钟提醒
)
```

### 多回调支持

```python
# 添加多个回调函数
reminder.add_callback(lambda task: print(f"通知1：{task.task}"))
reminder.add_callback(lambda task: print(f"通知2：{task.task}"))
reminder.add_callback(lambda task: send_email(task))  # 发送邮件
reminder.add_callback(lambda task: send_sms(task))    # 发送短信
```

### 自定义提醒逻辑

```python
class CustomReminder(Reminder):
    def _trigger_reminders(self, task):
        # 自定义提醒逻辑
        if task.priority == "high":
            # 高优先级任务：电话通知
            self.call_phone(task)
        elif task.priority == "medium":
            # 中等优先级：邮件通知
            self.send_email(task)
        else:
            # 普通任务：仅记录
            print(f"提醒：{task.task}")

reminder = CustomReminder(task_manager)
```

## 5. 查看即将到期的任务

```python
# 未来24小时内到期的任务
upcoming = reminder.get_upcoming_tasks(hours=24)

print("📅 即将到期：")
for task in upcoming:
    print(f"  {task.task} - {task.datetime}")
```

## 6. 持久化存储

### 自动保存

```python
# 任务管理器会自动保存到文件
task_manager = TaskManager(storage_path="my_tasks.json")

# 每次修改都会自动保存
task = task_manager.add_task(result)  # 自动保存
task_manager.complete_task(task.id)   # 自动保存
```

### 手动保存和加载

```python
# 手动保存
task_manager.save()

# 手动加载
task_manager.load()
```

### 查看存储的文件

```python
import json

with open("my_tasks.json", "r", encoding="utf-8") as f:
    data = json.load(f)
    for task_data in data["tasks"]:
        print(task_data)
```

## 7. 集成到 Web 应用

### Flask 示例

```python
from flask import Flask, request, jsonify
from smart_todo_parser import TodoParser, TaskManager, Reminder

app = Flask(__name__)
parser = TodoParser()
task_manager = TaskManager(storage_path="tasks.json")
reminder = Reminder(task_manager)
reminder.start()

@app.route('/tasks', methods=['POST'])
def create_task():
    """创建任务"""
    data = request.json
    text = data.get('text', '')

    result = parser.parse(text)
    if result['success']:
        task = task_manager.add_task(result)
        return jsonify(task.to_dict())
    else:
        return jsonify({'error': '解析失败'}), 400

@app.route('/tasks', methods=['GET'])
def list_tasks():
    """列出任务"""
    tasks = task_manager.list_tasks()
    return jsonify([t.to_dict() for t in tasks])

@app.route('/tasks/<task_id>/complete', methods=['POST'])
def complete_task(task_id):
    """完成任务"""
    task = task_manager.complete_task(task_id)
    if task:
        return jsonify(task.to_dict())
    else:
        return jsonify({'error': '任务不存在'}), 404

@app.route('/tasks/upcoming', methods=['GET'])
def upcoming_tasks():
    """获取即将到期的任务"""
    hours = request.args.get('hours', 24, type=int)
    tasks = reminder.get_upcoming_tasks(hours=hours)
    return jsonify([t.to_dict() for t in tasks])

if __name__ == '__main__':
    try:
        app.run(debug=True)
    finally:
        reminder.stop()
```

## 8. 集成到命令行应用

```python
#!/usr/bin/env python3
import sys
import cmd
from smart_todo_parser import TodoParser, TaskManager, SimpleReminder

class TodoCLI(cmd.Cmd):
    """待办事项命令行界面"""
    prompt = "(todo) "
    intro = "智能待办事项管理系统（输入 help 查看命令）"

    def __init__(self):
        super().__init__()
        self.parser = TodoParser()
        self.task_manager = TaskManager(storage_path="todo.json")
        self.reminder = SimpleReminder(self.task_manager)

    def do_add(self, line):
        """添加任务：add 明天下午3点开会"""
        result = self.parser.parse(line)
        if result['success']:
            task = self.task_manager.add_task(result)
            print(f"✓ 已添加：{task.task}")
        else:
            print("✗ 解析失败")

    def do_list(self, line):
        """列出任务：list [all|pending|completed]"""
        tasks = self.task_manager.list_tasks()
        for task in tasks:
            status = "✓" if task.status == "completed" else "○"
            print(f"{status} 【{task.priority}】{task.task}")
            if task.datetime:
                print(f"   时间：{task.datetime}")

    def do_complete(self, task_id):
        """完成任务：complete <task_id>"""
        task = self.task_manager.complete_task(task_id)
        if task:
            print(f"✓ 已完成：{task.task}")
        else:
            print("✗ 任务不存在")

    def do_check(self, line):
        """检查提醒：check"""
        tasks = self.reminder.check_now()
        if tasks:
            for task in tasks:
                print(f"🔔 {task.task} - {task.datetime}")
        else:
            print("无到期任务")

    def do_exit(self, line):
        """退出程序"""
        return True

if __name__ == '__main__':
    TodoCLI().cmdloop()
```

## 9. 测试示例

```python
import pytest
from datetime import datetime
from smart_todo_parser import TodoParser, TaskManager

def test_task_manager():
    # 创建固定日期的解析器
    base_date = datetime(2025, 1, 20, 10, 0, 0)
    parser = TodoParser(base_date=base_date)
    task_manager = TaskManager(storage_path="test_tasks.json")

    # 添加任务
    result = parser.parse("明天下午3点开会")
    task = task_manager.add_task(result)

    assert task.task == "开会"
    assert task.status == "pending"

    # 完成任务
    completed_task = task_manager.complete_task(task.id)
    assert completed_task.status == "completed"

    # 清理
    import os
    os.remove("test_tasks.json")

if __name__ == '__main__':
    test_task_manager()
```

## 10. 性能优化建议

```python
# 1. 批量操作时暂停自动保存
task_manager.auto_save = False
for todo_text in many_todos:
    result = parser.parse(todo_text)
    task_manager.add_task(result)
task_manager.save()  # 手动保存一次

# 2. 使用数据库（大规模应用）
# 可以扩展 TaskManager 使用 SQLite 替代 JSON

# 3. 异步提醒处理
import asyncio
from smart_todo_parser import Reminder

class AsyncReminder(Reminder):
    async def _run_loop(self):
        while self.running:
            await self._check_and_notify()
            await asyncio.sleep(self.check_interval)
```

## 11. 最佳实践

1. **定期清理过期任务**
```python
# 每周清理一次已完成任务
cleanup_tasks = task_manager.list_tasks(status=TaskStatus.COMPLETED)
for task in cleanup_tasks:
    # 检查是否超过7天
    completed_time = datetime.strptime(task.completed_at, "%Y-%m-%d %H:%M:%S")
    if (datetime.now() - completed_time).days > 7:
        task_manager.delete_task(task.id)
```

2. **设置合理的检查间隔**
```python
# 生产环境：5分钟检查一次
reminder = Reminder(task_manager, check_interval=300)
```

3. **使用标签分类**
```python
# 为不同标签设置不同的提醒策略
work_tasks = task_manager.list_tasks(tags=["工作"])
personal_tasks = task_manager.list_tasks(tags=["个人"])
```

4. **任务去重**
```python
# 检查是否已存在相似任务
def is_duplicate(new_task, existing_tasks):
    for task in existing_tasks:
        if new_task.task == task.task:
            return True
    return False
```

## 12. 故障恢复

```python
# 异常处理
try:
    task_manager.load()
except json.JSONDecodeError:
    print("⚠️  数据文件损坏，使用空任务列表")
    task_manager.tasks = {}

# 备份
import shutil
shutil.copy("tasks.json", "tasks_backup.json")
```
