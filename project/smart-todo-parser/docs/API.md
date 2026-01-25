# API 参考文档

## 模块：smart_todo_parser

### TodoParser

主解析器类，用于解析自然语言待办事项。

#### 初始化

```python
TodoParser(
    language: str = 'zh-CN',
    base_date: Optional[datetime] = None,
    timezone: str = 'Asia/Shanghai'
)
```

**参数：**
- `language`: 语言设置（默认 'zh-CN'）
- `base_date`: 基准日期，用于相对时间计算（默认当前时间）
- `timezone`: 时区（默认 'Asia/Shanghai'）

**示例：**
```python
from smart_todo_parser import TodoParser
from datetime import datetime

parser = TodoParser(language='zh-CN')

# 设置基准日期（用于测试）
base_date = datetime(2025, 1, 20, 10, 0, 0)
parser = TodoParser(base_date=base_date)
```

#### 方法：parse()

解析单条文本。

```python
parse(text: str) -> Dict[str, Any]
```

**参数：**
- `text`: 输入文本

**返回：**
```python
{
    "task": str,              # 任务描述
    "datetime": str | None,   # 格式化的时间 (YYYY-MM-DD HH:MM:SS)
    "priority": str,          # 优先级 (high | medium | normal | low)
    "tags": List[str],        # 标签列表
    "type": str | None,       # 时间类型 (absolute | relative | fuzzy)
    "raw_text": str,          # 原始文本
    "success": bool           # 是否解析成功
}
```

**示例：**
```python
result = parser.parse("明天下午3点开会 #工作")
# {
#     'task': '开会',
#     'datetime': '2025-01-21 15:00:00',
#     'priority': 'normal',
#     'tags': ['工作'],
#     'type': 'relative',
#     'raw_text': '明天下午3点开会 #工作',
#     'success': True
# }
```

#### 方法：parse_batch()

批量解析文本。

```python
parse_batch(texts: List[str]) -> List[Dict[str, Any]]
```

**参数：**
- `texts`: 输入文本列表

**返回：** 解析结果列表

**示例：**
```python
texts = ["明天开会", "后天交报告"]
results = parser.parse_batch(texts)
```

---

## 模块：smart_todo_parser.scheduler

### TaskManager

任务管理器，负责任务的存储和状态管理。

#### 初始化

```python
TaskManager(storage_path: Optional[str] = None)
```

**参数：**
- `storage_path`: 存储文件路径（默认 'tasks.json'）

**示例：**
```python
from smart_todo_parser import TaskManager

task_manager = TaskManager(storage_path="my_tasks.json")
```

#### 方法：add_task()

添加任务。

```python
add_task(parsed_result: Dict[str, Any]) -> Task
```

**参数：**
- `parsed_result`: 解析器返回的结果

**返回：** Task 对象

**示例：**
```python
result = parser.parse("明天下午3点开会")
task = task_manager.add_task(result)
print(task.id)  # task_1234567890.123
```

#### 方法：get_task()

获取单个任务。

```python
get_task(task_id: str) -> Optional[Task]
```

**参数：**
- `task_id`: 任务 ID

**返回：** Task 对象或 None

#### 方法：list_tasks()

列出任务（支持过滤）。

```python
list_tasks(
    status: Optional[TaskStatus] = None,
    priority: Optional[str] = None,
    tags: Optional[List[str]] = None
) -> List[Task]
```

**参数：**
- `status`: 任务状态（TaskStatus.PENDING | COMPLETED | OVERDUE | CANCELLED）
- `priority`: 优先级过滤（high | medium | normal | low）
- `tags`: 标签过滤

**返回：** 任务列表

**示例：**
```python
# 获取所有待办任务
tasks = task_manager.list_tasks(status=TaskStatus.PENDING)

# 获取高优先级工作任务
tasks = task_manager.list_tasks(priority="high", tags=["工作"])
```

#### 方法：update_task()

更新任务。

```python
update_task(task_id: str, **kwargs) -> Optional[Task]
```

**参数：**
- `task_id`: 任务 ID
- `**kwargs`: 要更新的字段

**返回：** 更新后的 Task 对象或 None

**示例：**
```python
task = task_manager.update_task("task_id", priority="high")
```

#### 方法：complete_task()

完成任务。

```python
complete_task(task_id: str) -> Optional[Task]
```

**示例：**
```python
task = task_manager.complete_task("task_id")
```

#### 方法：cancel_task()

取消任务。

```python
cancel_task(task_id: str) -> Optional[Task]
```

#### 方法：delete_task()

删除任务。

```python
delete_task(task_id: str) -> bool
```

**返回：** 是否成功删除

#### 方法：save()

手动保存到文件。

```python
save()
```

#### 方法：load()

从文件加载。

```python
load()
```

---

### Task

任务对象。

#### 属性

```python
task.id: str                    # 任务 ID
task.task: str                  # 任务描述
task.datetime: str | None       # 格式化的时间
task.priority: str              # 优先级
task.tags: List[str]            # 标签列表
task.status: TaskStatus         # 任务状态
task.created_at: str            # 创建时间
task.completed_at: str | None   # 完成时间
```

#### 方法：to_dict()

转换为字典。

```python
to_dict() -> Dict[str, Any]
```

#### 方法：is_due()

检查任务是否到期。

```python
is_due() -> bool
```

#### 方法：is_overdue()

检查任务是否已过期。

```python
is_overdue() -> bool
```

---

### TaskStatus (枚举)

任务状态枚举。

```python
TaskStatus.PENDING      # 待办
TaskStatus.COMPLETED    # 已完成
TaskStatus.OVERDUE      # 已过期
TaskStatus.CANCELLED    # 已取消
```

---

### Reminder

提醒调度器。

#### 初始化

```python
Reminder(
    task_manager: TaskManager,
    check_interval: int = 60,
    reminder_before: int = 15
)
```

**参数：**
- `task_manager`: 任务管理器实例
- `check_interval`: 检查间隔，秒（默认 60）
- `reminder_before`: 提前提醒时间，分钟（默认 15）

**示例：**
```python
from smart_todo_parser import Reminder

reminder = Reminder(
    task_manager,
    check_interval=60,    # 每60秒检查一次
    reminder_before=15    # 提前15分钟提醒
)
```

#### 方法：start()

启动提醒器（后台线程）。

```python
start()
```

**示例：**
```python
reminder.start()
```

#### 方法：stop()

停止提醒器。

```python
stop()
```

#### 方法：add_callback()

添加提醒回调函数。

```python
add_callback(callback: Callable[[Task], None])
```

**参数：**
- `callback`: 回调函数，接收 Task 对象

**示例：**
```python
def my_reminder(task):
    print(f"提醒：{task.task}")
    send_email(task)  # 发送邮件

reminder.add_callback(my_reminder)
```

#### 方法：check_now()

立即检查一次（手动触发）。

```python
check_now() -> List[Task]
```

**返回：** 到期任务列表

#### 方法：get_upcoming_tasks()

获取即将到期的任务。

```python
get_upcoming_tasks(hours: int = 24) -> List[Task]
```

**参数：**
- `hours`: 未来多少小时内（默认 24）

**返回：** 任务列表

**示例：**
```python
# 获取未来24小时内的任务
tasks = reminder.get_upcoming_tasks(hours=24)
```

---

### SimpleReminder

简化版提醒器（用于测试和简单场景）。

继承自 `Reminder`，额外提供：

#### 方法：get_notifications()

获取所有通知。

```python
get_notifications() -> List[Dict[str, Any]]
```

**返回：**
```python
[
    {
        "task_id": str,
        "task": str,
        "datetime": str,
        "priority": str,
        "triggered_at": str
    }
]
```

#### 方法：clear_notifications()

清空通知。

```python
clear_notifications()
```

---

## 异常处理

所有函数都会妥善处理异常，不会抛出未捕获的异常。

### 示例

```python
# 解析失败
result = parser.parse("")
# result["success"] == False

# 任务不存在
task = task_manager.get_task("invalid_id")
# 返回 None

# 文件损坏
task_manager.load()
# 自动使用空任务列表，不抛异常
```

---

## 性能指标

- **解析速度：** <10ms/条（简单时间）
- **批量解析：** ~100条/秒
- **内存占用：** <50MB
- **文件存储：** JSON 格式，自动压缩

---

## 扩展 API

### 自定义时间模式

```python
from smart_todo_parser.config.patterns import TIME_PATTERNS

# 添加自定义模式
TIME_PATTERNS['custom'] = [
    r'大后天',  # 3天后
]

parser = TodoParser()
result = parser.parse("大后天开会")
```

### 自定义优先级关键词

```python
from smart_todo_parser.config.patterns import PRIORITY_KEYWORDS

# 添加自定义关键词
PRIORITY_KEYWORDS['high'].append('加急')
PRIORITY_KEYWORDS['high'].append('重要')

parser = TodoParser()
result = parser.parse("明天加急处理")
# result["priority"] == "high"
```

---

## 最佳实践

1. **使用批量解析提高性能**
```python
# 好的做法
results = parser.parse_batch(texts)

# 避免
for text in texts:
    result = parser.parse(text)
```

2. **定期清理已完成任务**
```python
cleanup_tasks = task_manager.list_tasks(status=TaskStatus.COMPLETED)
for task in cleanup_tasks:
    task_manager.delete_task(task.id)
```

3. **设置合理的检查间隔**
```python
# 生产环境：5分钟
reminder = Reminder(task_manager, check_interval=300)

# 开发环境：10秒
reminder = Reminder(task_manager, check_interval=10)
```

4. **使用回调处理提醒**
```python
def send_notification(task):
    if task.priority == "high":
        send_sms(task)
    else:
        send_email(task)

reminder.add_callback(send_notification)
```
