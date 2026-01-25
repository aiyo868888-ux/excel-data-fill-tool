"""提醒调度器 - 任务到期提醒"""
import time
import threading
from datetime import datetime, timedelta
from typing import List, Callable, Optional, Dict, Any
from .task_manager import TaskManager, Task


class Reminder:
    """提醒调度器"""

    def __init__(
        self,
        task_manager: TaskManager,
        check_interval: int = 60,
        reminder_before: int = 15
    ):
        """
        初始化提醒器

        Args:
            task_manager: 任务管理器
            check_interval: 检查间隔（秒）
            reminder_before: 提前提醒时间（分钟）
        """
        self.task_manager = task_manager
        self.check_interval = check_interval
        self.reminder_before = reminder_before
        self.running = False
        self.thread: Optional[threading.Thread] = None
        self.callbacks: List[Callable[[Task], None]] = []

    def add_callback(self, callback: Callable[[Task], None]):
        """
        添加提醒回调函数

        Args:
            callback: 回调函数，接收 Task 对象
        """
        self.callbacks.append(callback)

    def start(self):
        """启动提醒器"""
        if self.running:
            return

        self.running = True
        self.thread = threading.Thread(target=self._run_loop, daemon=True)
        self.thread.start()
        print(f"提醒器已启动，检查间隔：{self.check_interval}秒")

    def stop(self):
        """停止提醒器"""
        self.running = False
        if self.thread:
            self.thread.join()
        print("提醒器已停止")

    def _run_loop(self):
        """主循环"""
        while self.running:
            try:
                # 检查到期任务
                due_tasks = self._check_due_tasks()

                # 触发回调
                for task in due_tasks:
                    self._trigger_reminders(task)

                # 更新过期任务状态
                self.task_manager.update_overdue_tasks()

            except Exception as e:
                print(f"提醒器错误：{e}")

            # 等待下次检查
            time.sleep(self.check_interval)

    def _check_due_tasks(self) -> List[Task]:
        """检查到期任务"""
        # 获取已到期任务
        overdue = self.task_manager.get_due_tasks(minutes_before=0)

        # 获取即将到期任务（提前提醒）
        upcoming = self.task_manager.get_due_tasks(minutes_before=self.reminder_before)

        # 去重
        all_tasks = overdue + upcoming
        unique_tasks = {task.id: task for task in all_tasks}

        # 过滤已提醒的任务
        due_tasks = [
            task for task in unique_tasks.values()
            if not task.reminder_set
        ]

        return due_tasks

    def _trigger_reminders(self, task: Task):
        """触发提醒"""
        print(f"\n🔔 提醒：{task.task}")
        if task.datetime:
            print(f"   时间：{task.datetime}")
        print(f"   优先级：{task.priority}")

        # 标记已提醒
        task.reminder_set = True

        # 调用回调函数
        for callback in self.callbacks:
            try:
                callback(task)
            except Exception as e:
                print(f"回调函数错误：{e}")

    def check_now(self) -> List[Task]:
        """立即检查一次（手动触发）"""
        return self._check_due_tasks()

    def get_upcoming_tasks(self, hours: int = 24) -> List[Task]:
        """
        获取即将到期的任务

        Args:
            hours: 未来多少小时内

        Returns:
            任务列表
        """
        now = datetime.now()
        end_time = now + timedelta(hours=hours)

        upcoming_tasks = []
        for task in self.task_manager.tasks.values():
            if not task.datetime or task.status != "pending":
                continue

            due_time = datetime.strptime(task.datetime, "%Y-%m-%d %H:%M:%S")
            if now <= due_time <= end_time:
                upcoming_tasks.append(task)

        return sorted(
            upcoming_tasks,
            key=lambda t: t.datetime or ""
        )


class SimpleReminder(Reminder):
    """简化版提醒器（用于测试和简单场景）"""

    def __init__(self, task_manager: TaskManager):
        super().__init__(task_manager, check_interval=60, reminder_before=15)
        self.notifications: List[Dict[str, Any]] = []

    def _trigger_reminders(self, task: Task):
        """触发提醒（保存到通知列表）"""
        notification = {
            "task_id": task.id,
            "task": task.task,
            "datetime": task.datetime,
            "priority": task.priority,
            "triggered_at": datetime.now().strftime("%Y-%m-%d %H:%M:%S")
        }
        self.notifications.append(notification)

        print(f"\n🔔 提醒：{task.task}")
        if task.datetime:
            print(f"   时间：{task.datetime}")
        print(f"   优先级：{task.priority}")

        task.reminder_set = True

    def get_notifications(self) -> List[Dict[str, Any]]:
        """获取所有通知"""
        return self.notifications

    def clear_notifications(self):
        """清空通知"""
        self.notifications.clear()
