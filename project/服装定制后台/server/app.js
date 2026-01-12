// Express 服务器主文件
const express = require('express');
const cors = require('cors');
const path = require('path');
require('dotenv').config();

const cloudRoutes = require('./routes/cloud');
const productRoutes = require('./routes/products');
const categoryRoutes = require('./routes/categories');
const uploadRoutes = require('./routes/upload');

const app = express();
const PORT = process.env.PORT || 3000;

// 中间件
app.use(cors());
app.use(express.json());
app.use(express.urlencoded({ extended: true }));

// 静态文件服务
app.use(express.static(path.join(__dirname, '../admin/dist')));

// API 路由
app.use('/api/cloud', cloudRoutes);
app.use('/api/products', productRoutes);
app.use('/api/categories', categoryRoutes);
app.use('/api/upload', uploadRoutes);

// 健康检查
app.get('/api/health', (req, res) => {
  res.json({ status: 'ok', message: '后台服务运行正常' });
});

// 所有其他路由返回前端应用
app.get('*', (req, res) => {
  res.sendFile(path.join(__dirname, '../admin/dist/index.html'));
});

// 错误处理
app.use((err, req, res, next) => {
  console.error('服务器错误:', err);
  res.status(500).json({
    success: false,
    message: err.message || '服务器内部错误'
  });
});

app.listen(PORT, () => {
  console.log(`
╔══════════════════════════════════════════════════════════════╗
║                  后台管理系统启动成功                        ║
╠══════════════════════════════════════════════════════════════╣
║  本地地址: http://localhost:${PORT}                           ║
║  API地址:  http://localhost:${PORT}/api                       ║
╠══════════════════════════════════════════════════════════════╣
║  按 Ctrl+C 停止服务器                                         ║
╚══════════════════════════════════════════════════════════════╝
  `);
});
