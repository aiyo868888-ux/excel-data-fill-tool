// 分类管理路由
const express = require('express');
const router = express.Router();
const { db } = require('../config/cloud');

// 获取分类列表
router.get('/', async (req, res) => {
  try {
    const result = await db.collection('categories')
      .orderBy('sortOrder', 'asc')
      .get();

    res.json({
      success: true,
      data: result.data
    });
  } catch (error) {
    res.status(500).json({
      success: false,
      message: error.message
    });
  }
});

// 新增分类
router.post('/', async (req, res) => {
  try {
    const data = {
      ...req.body,
      createTime: new Date()
    };

    const result = await db.collection('categories').add({ data });

    res.json({
      success: true,
      data: { _id: result._id }
    });
  } catch (error) {
    res.status(500).json({
      success: false,
      message: error.message
    });
  }
});

// 更新分类
router.put('/:id', async (req, res) => {
  try {
    await db.collection('categories').doc(req.params.id).update({ data: req.body });

    res.json({ success: true });
  } catch (error) {
    res.status(500).json({ success: false, message: error.message });
  }
});

// 删除分类
router.delete('/:id', async (req, res) => {
  try {
    await db.collection('categories').doc(req.params.id).remove();
    res.json({ success: true });
  } catch (error) {
    res.status(500).json({ success: false, message: error.message });
  }
});

module.exports = router;
