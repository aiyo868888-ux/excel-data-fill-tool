"""时间提取器 - 从文本中提取时间信息"""
from datetime import datetime, timedelta
from typing import Optional, Dict, Any
import re
from dateutil import parser as dateutil_parser

from ..config.patterns import TIME_PATTERNS
from ..utils.fuzzy_time import FuzzyTimeParser


class TimeExtractor:
    """时间提取器 - 支持绝对时间、相对时间、模糊时间"""

    def __init__(self, base_date: Optional[datetime] = None, timezone: str = 'Asia/Shanghai'):
        self.base_date = base_date or datetime.now()
        self.timezone = timezone
        self.fuzzy_parser = FuzzyTimeParser(self.base_date)

    def extract(self, text: str) -> Dict[str, Any]:
        """
        从文本中提取时间信息

        Args:
            text: 输入文本，如"明天下午3点开会"

        Returns:
            {
                "datetime": datetime对象,
                "type": "absolute" | "relative" | "fuzzy",
                "original_text": "明天下午3点",
                "confidence": 0.0-1.0
            }
        """
        result = {
            "datetime": None,
            "type": None,
            "original_text": "",
            "confidence": 0.0
        }

        # 1. 尝试解析模糊时间
        fuzzy_result = self._parse_fuzzy_time(text)
        if fuzzy_result:
            result.update(fuzzy_result)
            return result

        # 2. 尝试解析相对时间
        relative_result = self._parse_relative_time(text)
        if relative_result:
            result.update(relative_result)
            return result

        # 3. 尝试解析绝对时间
        absolute_result = self._parse_absolute_time(text)
        if absolute_result:
            result.update(absolute_result)
            return result

        return result

    def _parse_fuzzy_time(self, text: str) -> Optional[Dict[str, Any]]:
        """解析模糊时间"""
        for pattern in TIME_PATTERNS['fuzzy_time']:
            match = re.search(pattern, text)
            if match:
                dt = self.fuzzy_parser.parse(match.group(0))
                if dt:
                    return {
                        "datetime": dt,
                        "type": "fuzzy",
                        "original_text": match.group(0),
                        "confidence": 0.7
                    }
        return None

    def _parse_relative_time(self, text: str) -> Optional[Dict[str, Any]]:
        """解析相对时间"""
        # 今天
        if re.search(r'(今天|今日|今)', text):
            return {
                "datetime": self.base_date.replace(hour=9, minute=0, second=0, microsecond=0),
                "type": "relative",
                "original_text": "今天",
                "confidence": 0.95
            }

        # 明天
        if re.search(r'(明天|明日|明)(?![天日])', text):
            tomorrow = self.base_date + timedelta(days=1)
            return {
                "datetime": tomorrow.replace(hour=9, minute=0, second=0, microsecond=0),
                "type": "relative",
                "original_text": "明天",
                "confidence": 0.95
            }

        # 后天
        if re.search(r'(后天|大后天)', text):
            day_after = self.base_date + timedelta(days=2)
            return {
                "datetime": day_after.replace(hour=9, minute=0, second=0, microsecond=0),
                "type": "relative",
                "original_text": "后天",
                "confidence": 0.95
            }

        # N天后
        match = re.search(r'(\d+)(天|日)后', text)
        if match:
            days = int(match.group(1))
            future_date = self.base_date + timedelta(days=days)
            return {
                "datetime": future_date.replace(hour=9, minute=0, second=0, microsecond=0),
                "type": "relative",
                "original_text": match.group(0),
                "confidence": 0.9
            }

        # N周后
        match = re.search(r'(\d+)(周|星期)后', text)
        if match:
            weeks = int(match.group(1))
            future_date = self.base_date + timedelta(weeks=weeks)
            return {
                "datetime": future_date.replace(hour=9, minute=0, second=0, microsecond=0),
                "type": "relative",
                "original_text": match.group(0),
                "confidence": 0.9
            }

        # N个月后
        match = re.search(r'(\d+)个月后', text)
        if match:
            months = int(match.group(1))
            future_date = self._add_months(self.base_date, months)
            return {
                "datetime": future_date.replace(hour=9, minute=0, second=0, microsecond=0),
                "type": "relative",
                "original_text": match.group(0),
                "confidence": 0.9
            }

        return None

    def _parse_absolute_time(self, text: str) -> Optional[Dict[str, Any]]:
        """解析绝对时间"""
        # 提取所有可能的时间模式
        time_matches = []

        # 1. 查找日期
        date_str = None
        for pattern in TIME_PATTERNS['absolute_date']:
            match = re.search(pattern, text)
            if match:
                date_str = match.group(0)
                break

        # 2. 查找时间点
        time_str = None
        for pattern in TIME_PATTERNS['time_point']:
            match = re.search(pattern, text)
            if match:
                time_str = match.group(0)
                break

        # 3. 组合日期和时间
        if date_str or time_str:
            combined = f"{date_str or ''} {time_str or ''}".strip()

            try:
                # 使用 dateutil 解析
                dt = dateutil_parser.parse(combined, fuzzy=True, default=self.base_date)

                return {
                    "datetime": dt,
                    "type": "absolute",
                    "original_text": combined,
                    "confidence": 0.95
                }
            except Exception as e:
                pass

        # 4. 尝试直接解析整个文本
        try:
            dt = dateutil_parser.parse(text, fuzzy=True, default=self.base_date)
            return {
                "datetime": dt,
                "type": "absolute",
                "original_text": text,
                "confidence": 0.8
            }
        except:
            pass

        return None

    @staticmethod
    def _add_months(date: datetime, months: int) -> datetime:
        """给日期增加月份"""
        year = date.year + (date.month + months - 1) // 12
        month = ((date.month + months - 1) % 12) + 1
        day = min(date.day, [31, 29 if year % 4 == 0 else 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31][month - 1])
        return date.replace(year=year, month=month, day=day)
