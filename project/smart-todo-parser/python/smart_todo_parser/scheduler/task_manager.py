"""任务管理器 - 任务存储和状态管理"""
from datetime import datetime as dt
from typing import List, Dict, Any, Optional
import json
from enum import Enum


class TaskStatus(Enum):
    """任务状态"""
    PENDING = "pending"      # 待办
    COMPLETED = "completed"  # 已完成
    OVERDUE = "overdue"      # 已过期
    CANCELLED = "cancelled"  # 已取消


class Task:
    """任务对象"""

    def __init__(
        self,
        id: str,
        task: str,
        datetime: Optional[str] = None,
        priority: str = "normal",
        tags: List[str] = None,
        status: TaskStatus = TaskStatus.PENDING,
        created_at: Optional[str] = None,
        completed_at: Optional[str] = None
    ):
        self.id = id
        self.task = task
        self.datetime = datetime
        self.priority = priority
        self.tags = tags or []
        self.status = status
        self.created_at = created_at or dt.now().strftime("%Y-%m-%d %H:%M:%S")
        self.completed_at = completed_at
        self.reminder_set = False

    def to_dict(self) -> Dict[str, Any]:
        """转换为字典"""
        return {
            "id": self.id,
            "task": self.task,
            "datetime": self.datetime,
            "priority": self.priority,
            "tags": self.tags,
            "status": self.status.value,
            "created_at": self.created_at,
            "completed_at": self.completed_at
        }

    @classmethod
    def from_dict(cls, data: Dict[str, Any]) -> 'Task':
        """从字典创建任务"""
        return cls(
            id=data["id"],
            task=data["task"],
            datetime=data.get("datetime"),
            priority=data.get("priority", "normal"),
            tags=data.get("tags", []),
            status=TaskStatus(data.get("status", "pending")),
            created_at=data.get("created_at"),
            completed_at=data.get("completed_at")
        )

    def is_due(self) -> bool:
        """检查任务是否到期"""
        if not self.datetime:
            return False
        due_time = dt.strptime(self.datetime, "%Y-%m-%d %H:%M:%S")
        return dt.now() >= due_time

    def is_overdue(self) -> bool:
        """检查任务是否已过期"""
        return self.is_due() and self.status == TaskStatus.PENDING


class TaskManager:
    """任务管理器 - 存储和管理任务"""

    def __init__(self, storage_path: Optional[str] = None):
        """
        初始化任务管理器

        Args:
            storage_path: 存储文件路径（JSON 格式）
        """
        self.storage_path = storage_path or "tasks.json"
        self.tasks: Dict[str, Task] = {}
        self.load()

    def add_task(self, parsed_result: Dict[str, Any]) -> Task:
        """
        添加任务

        Args:
            parsed_result: 解析器返回的结果

        Returns:
            Task 对象
        """
        task_id = self._generate_id()
        task = Task(
            id=task_id,
            task=parsed_result["task"],
            datetime=parsed_result.get("datetime"),
            priority=parsed_result.get("priority", "normal"),
            tags=parsed_result.get("tags", [])
        )
        self.tasks[task_id] = task
        self.save()
        return task

    def get_task(self, task_id: str) -> Optional[Task]:
        """获取任务"""
        return self.tasks.get(task_id)

    def list_tasks(
        self,
        status: Optional[TaskStatus] = None,
        priority: Optional[str] = None,
        tags: Optional[List[str]] = None
    ) -> List[Task]:
        """
        列出任务（支持过滤）

        Args:
            status: 任务状态过滤
            priority: 优先级过滤
            tags: 标签过滤

        Returns:
            任务列表
        """
        tasks = list(self.tasks.values())

        # 状态过滤
        if status:
            tasks = [t for t in tasks if t.status == status]

        # 优先级过滤
        if priority:
            tasks = [t for t in tasks if t.priority == priority]

        # 标签过滤
        if tags:
            tasks = [t for t in tasks if any(tag in t.tags for tag in tags)]

        # 排序：按时间和优先级
        tasks = self._sort_tasks(tasks)

        return tasks

    def update_task(self, task_id: str, **kwargs) -> Optional[Task]:
        """更新任务"""
        task = self.tasks.get(task_id)
        if not task:
            return None

        for key, value in kwargs.items():
            if hasattr(task, key):
                setattr(task, key, value)

        self.save()
        return task

    def complete_task(self, task_id: str) -> Optional[Task]:
        """完成任务"""
        task = self.tasks.get(task_id)
        if not task:
            return None

        task.status = TaskStatus.COMPLETED
        task.completed_at = dt.now().strftime("%Y-%m-%d %H:%M:%S")
        self.save()
        return task

    def cancel_task(self, task_id: str) -> Optional[Task]:
        """取消任务"""
        task = self.tasks.get(task_id)
        if not task:
            return None

        task.status = TaskStatus.CANCELLED
        self.save()
        return task

    def delete_task(self, task_id: str) -> bool:
        """删除任务"""
        if task_id in self.tasks:
            del self.tasks[task_id]
            self.save()
            return True
        return False

    def update_overdue_tasks(self) -> List[Task]:
        """更新过期任务状态"""
        overdue_tasks = []
        for task in self.tasks.values():
            if task.is_overdue():
                task.status = TaskStatus.OVERDUE
                overdue_tasks.append(task)
        self.save()
        return overdue_tasks

    def get_due_tasks(self, minutes_before: int = 0) -> List[Task]:
        """
        获取即将到期的任务

        Args:
            minutes_before: 提前多少分钟

        Returns:
            即将到期的任务列表
        """
        now = dt.now()
        due_tasks = []

        for task in self.tasks.values():
            if not task.datetime or task.status != TaskStatus.PENDING:
                continue

            due_time = dt.strptime(task.datetime, "%Y-%m-%d %H:%M:%S")
            time_delta = (due_time - now).total_seconds() / 60

            if minutes_before == 0:
                # 已到期
                if time_delta <= 0:
                    due_tasks.append(task)
            else:
                # 提前提醒
                if 0 <= time_delta <= minutes_before:
                    due_tasks.append(task)

        return due_tasks

    def save(self):
        """保存到文件"""
        data = {
            "tasks": [task.to_dict() for task in self.tasks.values()]
        }
        with open(self.storage_path, 'w', encoding='utf-8') as f:
            json.dump(data, f, ensure_ascii=False, indent=2)

    def load(self):
        """从文件加载"""
        try:
            with open(self.storage_path, 'r', encoding='utf-8') as f:
                data = json.load(f)
                self.tasks = {
                    task_data["id"]: Task.from_dict(task_data)
                    for task_data in data.get("tasks", [])
                }
        except FileNotFoundError:
            self.tasks = {}

    def _generate_id(self) -> str:
        """生成任务 ID"""
        return f"task_{dt.now().timestamp()}"

    def _sort_tasks(self, tasks: List[Task]) -> List[Task]:
        """排序任务（按时间和优先级）"""
        priority_order = {"high": 0, "medium": 1, "normal": 2, "low": 3}

        def sort_key(task):
            # 有时间的任务优先
            has_time = 0 if task.datetime else 1
            # 按优先级
            priority = priority_order.get(task.priority, 99)
            # 按时间
            time = task.datetime or ""

            return (has_time, priority, time)

        return sorted(tasks, key=sort_key)
