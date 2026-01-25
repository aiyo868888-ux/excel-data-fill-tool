# OCR库选择与配置

## OCR引擎对比

### PaddleOCR (推荐)

**优点:**
- 中文识别准确率高
- 支持表格结构识别
- 无需额外安装软件
- 支持倾斜校正

**缺点:**
- 安装包较大（~200MB）
- 首次运行下载模型较慢

**安装:**
```bash
pip install paddleocr paddlepaddle
```

**使用场景:**
- 中文表格识别
- 复杂表格结构
- 需要高准确率

### Tesseract

**优点:**
- 轻量级
- 社区活跃
- 支持多语言

**缺点:**
- 需要单独安装软件
- 中文识别准确率较低
- 表格结构识别能力弱

**安装:**
```bash
pip install pytesseract pillow
# Windows: 下载安装 Tesseract-OCR
# 添加到系统 PATH
```

**使用场景:**
- 纯英文表格
- 简单表格结构
- 资源受限环境

## PaddleOCR高级配置

### 自定义模型

```python
from paddleocr import PaddleOCR

# 使用不同模型
ocr = PaddleOCR(
    use_angle_cls=True,      # 启用方向分类器
    lang='ch',               # 语言: ch(中文), en(英文), fr(法文)
    use_gpu=False,           # 是否使用GPU
    show_log=False,          # 显示日志
    det_model_dir=None,      # 自定义检测模型
    rec_model_dir=None,      # 自定义识别模型
    cls_model_dir=None,      # 自定义方向分类器模型
    det_limit_side_len=960,  # 检测长边限制
    det_db_thresh=0.3,       # 检测阈值
    det_db_box_thresh=0.6,   # 框选阈值
)
```

### 表格识别参数

```python
# 针对表格优化
ocr = PaddleOCR(
    det_db_thresh=0.2,       # 降低阈值以识别表格线
    det_db_box_thresh=0.5,
    det_limit_side_len=1920, # 提高分辨率
)
```

## Tesseract配置

### Page Segmentation Modes (psm)

```bash
--psm 3   # 默认，自动分页
--psm 4   # 假设单列文本
--psm 6   # 假设单行文本
--psm 11  # 稀疏文本
```

### OCR Engine Modes (oem)

```bash
--oem 1   # LSTM神经网络
--oem 3   # 默认，自动选择
```

### 示例

```bash
tesseract input.png output -l chi_sim+eng --psm 6
```

## 图片预处理

### 提高识别准确率

```python
from PIL import Image, ImageEnhance, ImageFilter

def preprocess_image(image_path):
    img = Image.open(image_path)

    # 转为灰度
    img = img.convert('L')

    # 增强对比度
    enhancer = ImageEnhance.Contrast(img)
    img = enhancer.enhance(2.0)

    # 去噪
    img = img.filter(ImageFilter.MedianFilter())

    # 二值化
    threshold = 127
    img = img.point(lambda x: 255 if x > threshold else 0, '1')

    return img
```

## 常见问题

### PaddleOCR首次运行慢

首次运行会下载模型文件，耐心等待。

### Tesseract找不到

确保Tesseract-OCR在PATH中，或指定路径：

```python
pytesseract.pytesseract.tesseract_cmd = r'C:\Program Files\Tesseract-OCR\tesseract.exe'
```

### 识别结果为空

检查：
1. 图片路径是否正确
2. 图片格式是否支持（PNG, JPG, JPEG）
3. 图片质量是否足够
4. OCR引擎是否安装正确
