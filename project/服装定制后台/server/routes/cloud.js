// 云开发状态路由
const express = require('express');
const router = express.Router();
const { db } = require('../config/cloud');

// 获取数据库统计信息
router.get('/stats', async (req, res) => {
  try {
    const collections = ['products', 'categories', 'designs', 'templates', 'users'];

    const stats = await Promise.all(
      collections.map(async (name) => {
        try {
          const result = await db.collection(name).count();
          return { name, count: result.total };
        } catch (error) {
          return { name, count: 0, error: error.message };
        }
      })
    );

    res.json({
      success: true,
      data: stats
    });
  } catch (error) {
    res.status(500).json({
      success: false,
      message: error.message
    });
  }
});

module.exports = router;
