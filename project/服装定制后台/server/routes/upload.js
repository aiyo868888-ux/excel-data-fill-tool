// 文件上传路由
const express = require('express');
const router = express.Router();
const multer = require('multer');
const { cloud } = require('../config/cloud');

// 配置 multer
const storage = multer.memoryStorage();
const upload = multer({ storage });

// 上传图片到云存储
router.post('/', upload.single('file'), async (req, res) => {
  try {
    if (!req.file) {
      return res.status(400).json({
        success: false,
        message: '请选择要上传的文件'
      });
    }

    // 生成文件名
    const fileName = `${Date.now()}-${req.file.originalname}`;
    const cloudPath = `products/${fileName}`;

    // 上传到云存储
    const result = await cloud.uploadFile({
      cloudPath,
      fileContent: req.file.buffer
    });

    // 获取文件访问URL
    const fileList = await cloud.getTempFileURL({
      fileList: [result.fileID]
    });

    res.json({
      success: true,
      data: {
        fileID: result.fileID,
        url: fileList.fileList[0].tempFileURL,
        cloudPath
      }
    });
  } catch (error) {
    console.error('文件上传失败:', error);
    res.status(500).json({
      success: false,
      message: error.message
    });
  }
});

module.exports = router;
