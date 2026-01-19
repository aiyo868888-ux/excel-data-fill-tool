# 时间提取测试

## 测试案例："明天早上5点"

### 当前逻辑流程：

```
输入："明天早上5点"

extractAbsoluteTime():
1. calendar = Calendar.getInstance()  // 假设现在是 2026-01-18 20:00
2. 匹配时间：hour=5, minute=0
3. 检测到"明天"：calendar.add(DAY_OF_MONTH, 1)  // 变成 2026-01-19 20:00
4. 设置时间：calendar.set(HOUR_OF_DAY, 5)  // 变成 2026-01-19 05:00
5. 设置分钟：calendar.set(MINUTE, 0)
6. 结果：2026-01-19 05:00 ✅

ReminderConfirmDialog:
- timestamp = 2026-01-19 05:00
- selectedTimestamp = timestamp - 30分钟 = 2026-01-19 04:30 ✅
```

### 可能的问题：

1. **Calendar 对象复用问题**：如果在 `extractRelativeTime` 中先修改了 calendar，再在 `extractAbsoluteTime` 中使用，可能会有问题

2. **时间格式化问题**：`formatTimestamp` 可能显示错误

3. **30分钟减法问题**：可能减法操作有问题

## 实际测试：

需要查看 Logcat 输出：
- DateTimeExtractor 提取的时间戳
- ReminderConfirmDialog 接收的时间戳
- 最终显示的格式化时间
