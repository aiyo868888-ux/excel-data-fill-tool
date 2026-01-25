"""任务提取器 - 从文本中提取任务描述和元数据"""
import re
from typing import List, Dict, Any, Optional
from datetime import datetime

from ..config.patterns import PRIORITY_KEYWORDS, TAG_PATTERN


class TaskExtractor:
    """任务提取器 - 提取任务描述、优先级、标签"""

    def __init__(self):
        self.priority_keywords = PRIORITY_KEYWORDS

    def extract(self, text: str, time_info: Optional[Dict[str, Any]] = None) -> Dict[str, Any]:
        """
        从文本中提取任务信息

        Args:
            text: 输入文本，如"明天下午3点开会，准备PPT #工作"
            time_info: 已提取的时间信息（用于从文本中移除时间部分）

        Returns:
            {
                "task": "开会，准备PPT",
                "priority": "normal" | "high" | "low",
                "tags": ["工作"],
                "raw_text": "..."
            }
        """
        result = {
            "task": "",
            "priority": "normal",
            "tags": [],
            "raw_text": text
        }

        # 1. 移除时间部分，提取任务
        clean_text = self._remove_time_text(text, time_info)
        result["task"] = clean_text.strip()

        # 2. 提取标签
        tags = self._extract_tags(clean_text)
        result["tags"] = tags
        # 从任务中移除标签
        result["task"] = re.sub(TAG_PATTERN, '', result["task"]).strip()

        # 3. 识别优先级
        priority = self._detect_priority(clean_text, time_info)
        result["priority"] = priority

        # 4. 清理任务文本
        result["task"] = self._clean_task_text(result["task"])

        return result

    def _remove_time_text(self, text: str, time_info: Optional[Dict[str, Any]]) -> str:
        """从文本中移除时间部分"""
        if not time_info or not time_info.get("original_text"):
            return text

        time_text = time_info["original_text"]
        # 移除时间文本
        clean_text = text.replace(time_text, '')
        # 移除多余的标点符号和空格
        clean_text = re.sub(r'^[，,、\s]+', '', clean_text)  # 移除开头的标点
        clean_text = re.sub(r'[，,、\s]+$', '', clean_text)  # 移除结尾的标点

        return clean_text

    def _extract_tags(self, text: str) -> List[str]:
        """提取标签（#标签）"""
        matches = re.findall(TAG_PATTERN, text)
        return list(set(matches))  # 去重

    def _detect_priority(self, text: str, time_info: Optional[Dict[str, Any]]) -> str:
        """
        检测任务优先级

        Returns: "high" | "medium" | "low" | "normal"
        """
        # 检查紧急关键词
        for keyword in self.priority_keywords["high"]:
            if keyword in text:
                return "high"

        # 检查中等优先级关键词
        for keyword in self.priority_keywords["medium"]:
            if keyword in text:
                return "medium"

        # 检查低优先级关键词
        for keyword in self.priority_keywords["low"]:
            if keyword in text:
                return "low"

        # 根据时间判断优先级
        if time_info and time_info.get("datetime"):
            dt = time_info["datetime"]
            now = datetime.now()
            delta = dt - now

            # 24小时内 = 高优先级
            if delta.days < 1:
                return "high"
            # 3天内 = 中等优先级
            elif delta.days < 3:
                return "medium"

        return "normal"

    def _clean_task_text(self, text: str) -> str:
        """清理任务文本"""
        # 移除多余的空格
        text = re.sub(r'\s+', ' ', text)
        # 移除开头和结尾的标点
        text = text.strip('，,。.、;；')
        # 移除重复的逗号
        text = re.sub(r'[，,]+', '，', text)
        return text.strip()

    def parse_batch(self, texts: List[str]) -> List[Dict[str, Any]]:
        """批量提取任务"""
        return [self.extract(text) for text in texts]
