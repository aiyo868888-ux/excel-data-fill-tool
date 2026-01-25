# 使用示例

## 1. 快速开始

```python
from smart_todo_parser import TodoParser
from datetime import datetime

# 创建解析器
parser = TodoParser(language='zh-CN')

# 解析简单任务
result = parser.parse("明天下午3点开会")
print(result)
# {
#     'task': '开会',
#     'datetime': '2025-01-21 15:00:00',
#     'priority': 'normal',
#     'tags': [],
#     'type': 'relative',
#     'raw_text': '明天下午3点开会',
#     'success': True
# }
```

## 2. 基础时间表达

### 绝对时间

```python
parser.parse("2025年3月15日下午3点开会")
# datetime: 2025-03-15 15:00:00

parser.parse("1月25号交报告")
# datetime: 2025-01-25 00:00:00

parser.parse("下午3点开会")
# datetime: 今天 15:00:00
```

### 相对时间

```python
parser.parse("今天提交报告")
# datetime: 今天 09:00:00

parser.parse("明天开会")
# datetime: 明天 09:00:00

parser.parse("后天面试")
# datetime: 后天 09:00:00

parser.parse("3天后截止")
# datetime: 3天后 09:00:00

parser.parse("2周后复查")
# datetime: 2周后 09:00:00

parser.parse("3个月后付款")
# datetime: 3个月后 09:00:00
```

## 3. 模糊时间

```python
parser.parse("月底前完成")
# datetime: 当月最后一天 23:59:59

parser.parse("月初提交计划")
# datetime: 当月第一天 00:00:00

parser.parse("下月底总结")
# datetime: 下个月最后一天 23:59:59

parser.parse("季度末汇报")
# datetime: 本季度最后一天 23:59:59

parser.parse("周末聚会")
# datetime: 本周六 00:00:00

parser.parse("下周三开会")
# datetime: 下周三 00:00:00
```

## 4. 优先级识别

```python
# 高优先级（紧急关键词或24小时内）
parser.parse("明天紧急处理bug")
# priority: high

parser.parse("今天提交代码")
# priority: high

# 中等优先级
parser.parse("下周重要会议")
# priority: medium

# 低优先级
parser.parse("有空时整理文档")
# priority: low

# 普通优先级
parser.parse("下周三开会")
# priority: normal
```

## 5. 标签提取

```python
# 单个标签
parser.parse("明天开会 #工作")
# tags: ['工作']
# task: '开会'

# 多个标签
parser.parse("明天开会 #工作 #重要")
# tags: ['工作', '重要']

# 标签和任务混合
parser.parse("明天下午3点开会 #工作 准备PPT")
# tags: ['工作']
# task: '开会，准备PPT'
```

## 6. 复杂场景

```python
# 时间 + 标签 + 优先级
result = parser.parse("明天下午3点紧急开会 #工作 准备PPT")
# {
#     'task': '开会，准备PPT',
#     'datetime': '2025-01-21 15:00:00',
#     'priority': 'high',
#     'tags': ['工作']
# }
```

## 7. 批量解析

```python
todos = [
    "明天开会",
    "下周三提交报告 #工作",
    "月底前完成项目",
    "后天面试 #求职"
]

results = parser.parse_batch(todos)
for result in results:
    print(f"任务：{result['task']}")
    print(f"时间：{result['datetime']}")
    print(f"优先级：{result['priority']}")
    print("---")
```

## 8. 无时间任务

```python
result = parser.parse("整理文档")
# {
#     'task': '整理文档',
#     'datetime': None,
#     'priority': 'normal',
#     'tags': []
# }
```

## 9. 自定义基准日期（测试用）

```python
from datetime import datetime

# 设置基准日期为 2025年1月20日
base_date = datetime(2025, 1, 20, 10, 0, 0)
parser = TodoParser(base_date=base_date)

result = parser.parse("明天开会")
# datetime: 2025-01-21 09:00:00（基于2025-01-20计算）
```

## 10. 完整工作流示例

```python
from smart_todo_parser import TodoParser

# 初始化
parser = TodoParser(language='zh-CN')

# 用户输入
user_inputs = [
    "明天下午3点开会 #工作",
    "月底前提交报告 #重要",
    "后天早上9点面试 #求职",
    "有空时整理文档 #个人"
]

# 解析
results = parser.parse_batch(user_inputs)

# 处理结果
for result in results:
    if result['success']:
        print(f"✓ 任务：{result['task']}")
        if result['datetime']:
            print(f"  时间：{result['datetime']}")
        print(f"  优先级：{result['priority']}")
        if result['tags']:
            print(f"  标签：{', '.join(result['tags'])}")
        print()
    else:
        print(f"✗ 无法解析：{result['raw_text']}")
```

## 11. 集成到应用

### Flask API 示例

```python
from flask import Flask, request, jsonify
from smart_todo_parser import TodoParser

app = Flask(__name__)
parser = TodoParser()

@app.route('/parse', methods=['POST'])
def parse_todo():
    data = request.json
    text = data.get('text', '')

    result = parser.parse(text)

    return jsonify(result)

if __name__ == '__main__':
    app.run(debug=True)
```

### 命令行工具

```python
#!/usr/bin/env python3
import sys
from smart_todo_parser import TodoParser

def main():
    parser = TodoParser()

    if len(sys.argv) > 1:
        text = ' '.join(sys.argv[1:])
    else:
        text = input("请输入待办事项：")

    result = parser.parse(text)

    if result['success']:
        print(f"\n✓ 任务：{result['task']}")
        if result['datetime']:
            print(f"  时间：{result['datetime']}")
        print(f"  优先级：{result['priority']}")
        if result['tags']:
            print(f"  标签：{', '.join(result['tags'])}")
    else:
        print("✗ 解析失败")

if __name__ == '__main__':
    main()
```

## 12. 错误处理

```python
parser = TodoParser()

# 空文本
result = parser.parse("")
# success: False

# 无法识别的时间
result = parser.parse("某天某时开会")
# success: True (无时间也算成功)
# datetime: None
# task: '某天某时开会'
```

## 13. 性能优化建议

```python
# 对于大量文本，使用批量解析
texts = [f"第{i}个任务" for i in range(1000)]
results = parser.parse_batch(texts)  # 批量处理更快

# 缓存常用结果
from functools import lru_cache

@lru_cache(maxsize=1000)
def cached_parse(text):
    return parser.parse(text)
```

## 14. 测试用例

```python
import pytest
from smart_todo_parser import TodoParser
from datetime import datetime

def test_tomorrow():
    base_date = datetime(2025, 1, 20, 10, 0, 0)
    parser = TodoParser(base_date=base_date)

    result = parser.parse("明天开会")
    assert result['datetime'] == '2025-01-21 09:00:00'
    assert result['task'] == '开会'

if __name__ == '__main__':
    pytest.main()
```

## 15. 扩展：添加自定义时间模式

```python
from smart_todo_parser.config.patterns import TIME_PATTERNS

# 添加自定义模式
TIME_PATTERNS['custom'] = [
    r'大后天',  # 3天后
    r'下下个星期',  # 2周后
]

parser = TodoParser()
result = parser.parse("大后天开会")
```
