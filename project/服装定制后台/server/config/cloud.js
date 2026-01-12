// 云开发配置
const cloud = require('wx-server-sdk');

// 初始化云开发
cloud.init({
  env: process.env.CLOUD_ENV_ID || 'cloud1-xxx'
});

const db = cloud.database();

module.exports = {
  cloud,
  db
};
