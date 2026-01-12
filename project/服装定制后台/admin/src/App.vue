<template>
  <el-container class="app-container">
    <!-- 侧边栏 -->
    <el-aside width="200px" class="sidebar">
      <div class="logo">
        <h2>服装定制</h2>
        <p>后台管理系统</p>
      </div>
      <el-menu
        :default-active="currentRoute"
        router
        background-color="#304156"
        text-color="#bfcbd9"
        active-text-color="#409EFF"
      >
        <el-menu-item index="/dashboard">
          <el-icon><DataBoard /></el-icon>
          <span>数据概览</span>
        </el-menu-item>
        <el-menu-item index="/products">
          <el-icon><Goods /></el-icon>
          <span>商品管理</span>
        </el-menu-item>
        <el-menu-item index="/categories">
          <el-icon><Menu /></el-icon>
          <span>分类管理</span>
        </el-menu-item>
      </el-menu>
    </el-aside>

    <!-- 主内容区 -->
    <el-container>
      <el-header>
        <div class="header-content">
          <h3>{{ pageTitle }}</h3>
          <el-button type="primary" @click="checkCloudStatus">检查云开发连接</el-button>
        </div>
      </el-header>
      <el-main>
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { computed, onMounted } from 'vue';
import { useRoute } from 'vue-router';
import axios from 'axios';

const route = useRoute();
const currentRoute = computed(() => route.path);

const pageTitle = computed(() => {
  const titles = {
    '/dashboard': '数据概览',
    '/products': '商品管理',
    '/categories': '分类管理'
  };
  return titles[route.path] || '后台管理系统';
});

const checkCloudStatus = async () => {
  try {
    const res = await axios.get('/api/cloud/stats');
    if (res.data.success) {
      let message = '云开发连接成功！\n\n';
      res.data.data.forEach(item => {
        message += `${item.name}: ${item.count} 条\n`;
      });
      ElMessageBox.alert(message, '连接状态', { type: 'success' });
    }
  } catch (error) {
    ElMessageBox.alert('云开发连接失败：' + error.message, '错误', { type: 'error' });
  }
};

onMounted(() => {
  console.log('后台管理系统启动');
});
</script>

<style scoped>
.app-container {
  height: 100vh;
}

.sidebar {
  background-color: #304156;
  color: #fff;
}

.logo {
  padding: 20px;
  text-align: center;
  border-bottom: 1px solid #434a50;
}

.logo h2 {
  margin: 0;
  font-size: 20px;
  color: #fff;
}

.logo p {
  margin: 5px 0 0;
  font-size: 12px;
  color: #bfcbd9;
}

.el-header {
  background-color: #fff;
  border-bottom: 1px solid #e6e6e6;
  display: flex;
  align-items: center;
  padding: 0 20px;
}

.header-content {
  width: 100%;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-content h3 {
  margin: 0;
}

.el-main {
  background-color: #f0f2f5;
  padding: 20px;
}

.el-menu {
  border-right: none;
}
</style>
