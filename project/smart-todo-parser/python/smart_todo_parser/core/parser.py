"""主解析器 - 整合时间和任务提取"""
from datetime import datetime
from typing import Dict, Any, List, Optional

from .time_extractor import TimeExtractor
from .task_extractor import TaskExtractor


class TodoParser:
    """智能待办事项解析器"""

    def __init__(
        self,
        language: str = 'zh-CN',
        base_date: Optional[datetime] = None,
        timezone: str = 'Asia/Shanghai'
    ):
        """
        初始化解析器

        Args:
            language: 语言设置（'zh-CN' | 'en-US'）
            base_date: 基准日期（用于相对时间计算）
            timezone: 时区
        """
        self.language = language
        self.timezone = timezone
        self.base_date = base_date or datetime.now()

        self.time_extractor = TimeExtractor(base_date, timezone)
        self.task_extractor = TaskExtractor()

    def parse(self, text: str) -> Dict[str, Any]:
        """
        解析待办事项文本

        Args:
            text: 输入文本，如"明天下午3点开会，准备PPT #工作"

        Returns:
            {
                "task": "开会，准备PPT",
                "datetime": "2025-01-21 15:00:00",
                "priority": "high",
                "tags": ["工作"],
                "type": "relative",
                "raw_text": "..."
            }
        """
        result = {
            "task": "",
            "datetime": None,
            "priority": "normal",
            "tags": [],
            "type": None,
            "raw_text": text,
            "success": False
        }

        # 1. 提取时间
        time_info = self.time_extractor.extract(text)

        # 2. 提取任务
        task_info = self.task_extractor.extract(text, time_info)

        # 3. 合并结果
        result["task"] = task_info["task"]
        result["priority"] = task_info["priority"]
        result["tags"] = task_info["tags"]
        result["raw_text"] = text

        if time_info.get("datetime"):
            result["datetime"] = time_info["datetime"].strftime("%Y-%m-%d %H:%M:%S")
            result["type"] = time_info["type"]
            result["success"] = True
        else:
            # 即使没有时间，也算解析成功（无时间限制的任务）
            result["success"] = True if result["task"] else False

        return result

    def parse_batch(self, texts: List[str]) -> List[Dict[str, Any]]:
        """批量解析"""
        return [self.parse(text) for text in texts]

    def set_base_date(self, date: datetime):
        """设置基准日期（用于测试）"""
        self.base_date = date
        self.time_extractor.base_date = date
        self.time_extractor.fuzzy_parser.base_date = date

    def __repr__(self):
        return f"<TodoParser language={self.language} timezone={self.timezone}>"
