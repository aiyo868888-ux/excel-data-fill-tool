"""模糊时间解析器"""
from datetime import datetime, timedelta
from typing import Optional
import calendar

class FuzzyTimeParser:
    """解析模糊时间表达（月底、季度、周末等）"""

    def __init__(self, base_date: Optional[datetime] = None):
        self.base_date = base_date or datetime.now()

    def parse(self, text: str) -> Optional[datetime]:
        """
        解析模糊时间表达

        Args:
            text: 时间文本，如"月底"、"下季度初"

        Returns:
            datetime 对象，无法解析返回 None
        """
        text = text.strip()

        # 月底/月末/月终
        if any(keyword in text for keyword in ['月底', '月末', '月终']):
            return self._end_of_month(offset=self._get_offset(text))

        # 月初
        if '月初' in text:
            return self._start_of_month(offset=self._get_offset(text))

        # 季度末/季度底
        if any(keyword in text for keyword in ['季度末', '季度底']):
            return self._end_of_quarter(offset=self._get_offset(text))

        # 季度初
        if '季度初' in text:
            return self._start_of_quarter(offset=self._get_offset(text))

        # 年底/年末
        if any(keyword in text for keyword in ['年底', '年末']):
            return self._end_of_year(offset=self._get_offset(text))

        # 年初
        if '年初' in text:
            return self._start_of_year(offset=self._get_offset(text))

        # 周末
        if '周末' in text:
            return self._weekend(offset=self._get_offset(text))

        # 周初（周一）
        if '周初' in text or ('周一' in text and '本' in text):
            return self._start_of_week(offset=self._get_offset(text))

        # 具体星期几
        weekday_map = {'一': 0, '二': 1, '三': 2, '四': 3, '五': 4, '六': 5, '日': 6, '天': 6}
        for word, day in weekday_map.items():
            if f'周{word}' in text or f'星期{word}' in text:
                return self._specific_weekday(day, offset=self._get_offset(text))

        return None

    def _get_offset(self, text: str) -> int:
        """提取偏移量（上/下/本）"""
        if '上' in text:
            return -1
        elif '下' in text:
            return 1
        else:  # '本' 或无修饰词
            return 0

    def _end_of_month(self, offset: int = 0) -> datetime:
        """月底（当月最后一天 23:59:59）"""
        year = self.base_date.year
        month = self.base_date.month + offset

        # 处理月份溢出
        if month > 12:
            year += (month - 1) // 12
            month = ((month - 1) % 12) + 1
        elif month < 1:
            year += (month - 12) // 12
            month = 12 - (abs(month) % 12)

        last_day = calendar.monthrange(year, month)[1]
        return datetime(year, month, last_day, 23, 59, 59)

    def _start_of_month(self, offset: int = 0) -> datetime:
        """月初（当月第一天 00:00:00）"""
        year = self.base_date.year
        month = self.base_date.month + offset

        if month > 12:
            year += (month - 1) // 12
            month = ((month - 1) % 12) + 1
        elif month < 1:
            year += (month - 12) // 12
            month = 12 - (abs(month) % 12)

        return datetime(year, month, 1, 0, 0, 0)

    def _end_of_quarter(self, offset: int = 0) -> datetime:
        """季度末"""
        current_quarter = (self.base_date.month - 1) // 3 + 1
        target_quarter = current_quarter + offset

        if target_quarter > 4:
            year = self.base_date.year + (target_quarter - 1) // 4
            quarter = ((target_quarter - 1) % 4) + 1
        elif target_quarter < 1:
            year = self.base_date.year + (target_quarter - 4) // 4
            quarter = 4 - (abs(target_quarter) % 4)
        else:
            year = self.base_date.year
            quarter = target_quarter

        last_month = quarter * 3
        last_day = calendar.monthrange(year, last_month)[1]
        return datetime(year, last_month, last_day, 23, 59, 59)

    def _start_of_quarter(self, offset: int = 0) -> datetime:
        """季度初"""
        current_quarter = (self.base_date.month - 1) // 3 + 1
        target_quarter = current_quarter + offset

        if target_quarter > 4:
            year = self.base_date.year + (target_quarter - 1) // 4
            quarter = ((target_quarter - 1) % 4) + 1
        elif target_quarter < 1:
            year = self.base_date.year + (target_quarter - 4) // 4
            quarter = 4 - (abs(target_quarter) % 4)
        else:
            year = self.base_date.year
            quarter = target_quarter

        first_month = (quarter - 1) * 3 + 1
        return datetime(year, first_month, 1, 0, 0, 0)

    def _end_of_year(self, offset: int = 0) -> datetime:
        """年底"""
        year = self.base_date.year + offset
        return datetime(year, 12, 31, 23, 59, 59)

    def _start_of_year(self, offset: int = 0) -> datetime:
        """年初"""
        year = self.base_date.year + offset
        return datetime(year, 1, 1, 0, 0, 0)

    def _weekend(self, offset: int = 0) -> datetime:
        """周末（周六 00:00:00）"""
        target_date = self.base_date + timedelta(weeks=offset)
        # 找到下一个周六
        days_ahead = 5 - target_date.weekday()  # 5 = Saturday
        if days_ahead <= 0:  # 今天是周六或周日
            days_ahead += 7
        saturday = target_date + timedelta(days=days_ahead)
        return datetime(saturday.year, saturday.month, saturday.day, 0, 0, 0)

    def _start_of_week(self, offset: int = 0) -> datetime:
        """周初（周一 00:00:00）"""
        target_date = self.base_date + timedelta(weeks=offset)
        # 找到下一个周一
        days_ahead = 0 - target_date.weekday()  # 0 = Monday
        if days_ahead <= 0:
            days_ahead += 7
        monday = target_date + timedelta(days=days_ahead)
        return datetime(monday.year, monday.month, monday.day, 0, 0, 0)

    def _specific_weekday(self, weekday: int, offset: int = 0) -> datetime:
        """
        具体星期几
        weekday: 0=周一, 6=周日
        """
        target_date = self.base_date + timedelta(weeks=offset)
        days_ahead = weekday - target_date.weekday()
        if days_ahead <= 0:
            days_ahead += 7
        result = target_date + timedelta(days=days_ahead)
        return datetime(result.year, result.month, result.day, 0, 0, 0)
