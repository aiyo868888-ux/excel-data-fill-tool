<template>
  <div class="dashboard">
    <el-row :gutter="20">
      <el-col :span="6" v-for="stat in stats" :key="stat.name">
        <el-card class="stat-card">
          <div class="stat-content">
            <el-icon class="stat-icon" :color="stat.color">
              <component :is="stat.icon" />
            </el-icon>
            <div class="stat-info">
              <p class="stat-label">{{ stat.label }}</p>
              <p class="stat-value">{{ stat.value }}</p>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-card class="welcome-card" style="margin-top: 20px;">
      <h3>欢迎使用服装定制小程序后台管理系统</h3>
      <p>本系统用于管理小程序的商品、分类、设计模板等数据。</p>
      <el-steps :active="1" style="margin-top: 20px;">
        <el-step title="配置环境ID" description="在 .env 文件中配置云开发环境ID"></el-step>
        <el-step title="启动服务" description="运行 npm install 和 npm start"></el-step>
        <el-step title="管理数据" description="添加商品、分类等数据"></el-step>
      </el-steps>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import axios from 'axios';

const stats = ref([
  { name: 'products', label: '商品总数', value: 0, icon: 'Goods', color: '#409EFF' },
  { name: 'categories', label: '分类总数', value: 0, icon: 'Menu', color: '#67C23A' },
  { name: 'designs', label: '设计总数', value: 0, icon: 'Edit', color: '#E6A23C' },
  { name: 'templates', label: '模板总数', value: 0, icon: 'Document', color: '#F56C6C' }
]);

onMounted(async () => {
  try {
    const res = await axios.get('/api/cloud/stats');
    if (res.data.success) {
      res.data.data.forEach(item => {
        const stat = stats.value.find(s => s.name === item.name);
        if (stat) {
          stat.value = item.count;
        }
      });
    }
  } catch (error) {
    console.error('获取统计数据失败:', error);
  }
});
</script>

<style scoped>
.stat-card {
  margin-bottom: 20px;
}

.stat-content {
  display: flex;
  align-items: center;
}

.stat-icon {
  font-size: 48px;
  margin-right: 20px;
}

.stat-info {
  flex: 1;
}

.stat-label {
  margin: 0;
  font-size: 14px;
  color: #909399;
}

.stat-value {
  margin: 10px 0 0;
  font-size: 28px;
  font-weight: bold;
  color: #303133;
}

.welcome-card h3 {
  margin-top: 0;
}
</style>
