// 商品管理路由
const express = require('express');
const router = express.Router();
const { db } = require('../config/cloud');

// 获取商品列表
router.get('/', async (req, res) => {
  try {
    const { page = 1, limit = 10, categoryId, keyword, status } = req.query;

    const collection = db.collection('products');
    let query = {};

    // 构建查询条件
    if (categoryId) {
      query.categoryId = categoryId;
    }
    if (status) {
      query.status = parseInt(status);
    }
    if (keyword) {
      query.name = db.RegExp({
        regexp: keyword,
        options: 'i'
      });
    }

    // 查询总数
    const countResult = await collection.where(query).count();
    const total = countResult.total;

    // 分页查询
    const skip = (page - 1) * limit;
    const result = await collection
      .where(query)
      .orderBy('createTime', 'desc')
      .skip(skip)
      .limit(parseInt(limit))
      .get();

    res.json({
      success: true,
      data: {
        list: result.data,
        total,
        page: parseInt(page),
        limit: parseInt(limit)
      }
    });
  } catch (error) {
    console.error('获取商品列表失败:', error);
    res.status(500).json({
      success: false,
      message: error.message
    });
  }
});

// 获取单个商品
router.get('/:id', async (req, res) => {
  try {
    const result = await db.collection('products').doc(req.params.id).get();

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

// 新增商品
router.post('/', async (req, res) => {
  try {
    const data = {
      ...req.body,
      createTime: new Date(),
      updateTime: new Date()
    };

    const result = await db.collection('products').add({ data });

    res.json({
      success: true,
      message: '商品添加成功',
      data: { _id: result._id }
    });
  } catch (error) {
    res.status(500).json({
      success: false,
      message: error.message
    });
  }
});

// 更新商品
router.put('/:id', async (req, res) => {
  try {
    const data = {
      ...req.body,
      updateTime: new Date()
    };

    await db.collection('products').doc(req.params.id).update({ data });

    res.json({
      success: true,
      message: '商品更新成功'
    });
  } catch (error) {
    res.status(500).json({
      success: false,
      message: error.message
    });
  }
});

// 删除商品
router.delete('/:id', async (req, res) => {
  try {
    await db.collection('products').doc(req.params.id).remove();

    res.json({
      success: true,
      message: '商品删除成功'
    });
  } catch (error) {
    res.status(500).json({
      success: false,
      message: error.message
    });
  }
});

// 批量删除商品
router.delete('/batch/:ids', async (req, res) => {
  try {
    const ids = req.params.ids.split(',');
    const deletePromises = ids.map(id =>
      db.collection('products').doc(id).remove()
    );

    await Promise.all(deletePromises);

    res.json({
      success: true,
      message: `成功删除 ${ids.length} 个商品`
    });
  } catch (error) {
    res.status(500).json({
      success: false,
      message: error.message
    });
  }
});

module.exports = router;
