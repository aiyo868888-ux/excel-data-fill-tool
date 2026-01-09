# 欢快轻音乐 - Strudel 代码

## 音乐特征
- **风格**: 轻音乐 / New Age
- **情绪**: 欢快、明亮、轻松
- **速度**: 中等（BPM 100）
- **调性**: C 大调（明亮、温暖）
- **乐器**: 钟琴、钢片琴、轻柔的打击乐

## Strudel 代码

### 基础版本 - 简单旋律
```javascript
// 欢快的 C 大调旋律
stack(
  note("<c4 e4 g4 c5 g4 e4>")
    .sound("sine")
    .lpf(2000)
    .gain(0.6)
    .slow(2),
  sound("bd ~ ~ ~")
    .gain(0.3)
    .slow(2)
)
```

### 进阶版本 - 完整配器
```javascript
// 欢快轻音乐 - 完整版
stack(
  // 主旋律 - 明亮的钟琴音色
  note("<c4 e4 g4 c5 g4 e4 c4 g4>")
    .sound("sine")
    .lpf(3000)
    .gain(0.5)
    .slow(2),

  // 和声 - 柔和的铺垫
  note("<c3maj7 e3min7 f3maj7 c3maj7>")
    .sound("triangle")
    .lpf(1500)
    .gain(0.3)
    .slow(4),

  // 轻柔的节奏
  stack(
    sound("bd ~ ~ ~").gain(0.2),
    sound("~ ~ hh ~").gain(0.15)
  ).slow(2)
)
```

### 丰富版本 - 更有层次
```javascript
// 欢快轻音乐 - 丰富版
stack(
  // 主旋律 - 钢琴般的明亮音色
  note("<[c4 e4] [e4 g4] [g4 c5] [c5 g4] [g4 e4] [e4 c4] [c4 g4] [g4 c4]>")
    .sound("sine")
    .lpf(2500)
    .gain(0.5)
    .slow(2),

  // 对位旋律 - 装饰音
  note("g4~4")
    .sound("triangle")
    .lpf(2000)
    .gain(0.25)
    .slow(4),

  // 和弦铺垫
  note("<c3maj7 f3maj7 d3min7 g3>")
    .sound("sawtooth")
    .lpf(800)
    .gain(0.2)
    .slow(4),

  // 轻柔打击乐
  stack(
    sound("bd ~ ~ ~").gain(0.25).slow(2),
    sound("~ ~ hh ~").gain(0.2).slow(2),
    sound("~ cp ~ ~").gain(0.15).slow(4)
  ),

  // 高频装饰 - 铃声效果
  note("<c6 e6 g6 c6>")
    .sound("sine")
    .lpf(5000)
    .gain(0.15)
    .slow(8)
)
```

---

## 在 Claude Desktop 中使用

如果 MCP 配置成功，可以直接对话：

```
你：生成一首欢快的轻音乐
Claude：（调用 strudel MCP）
    正在生成...

    [播放音乐]
```

---

## 在线使用

访问：https://strudel.cc/

复制上面的代码粘贴到编辑器中，即可听到效果！

---

## 音乐说明

### 音符选择
- **C 大调**：c4, d4, e4, f4, g4, a4, b4, c5
- **和弦**：Cmaj7, Fmaj7, Dmin7, G7

### 结构
- **主旋律**：分解和弦上行下行
- **和声**：爵士和弦进行（ii-V-I）
- **节奏**：轻柔的底鼓和镲片
- **装饰**：高频铃声增加明亮感

### 情感表达
- ✅ 欢快：大调音阶、明亮的音色
- ✅ 轻松：慢速度、柔和的音量
- ✅ 温暖：中频突出、低频柔和

---

**创建时间**: 2025-01-06
**风格**: 轻音乐 / Ambient
**时长**: 循环播放
