<template>
  <el-container class="app-layout">
    <el-aside width="240px" class="sidebar">
      <Sidebar />
    </el-aside>

    <el-container>
      <el-header height="60px" class="header">
        <Header />
      </el-header>

      <el-main class="main-content">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup lang="ts">
import Sidebar from './Sidebar.vue'
import Header from './Header.vue'
</script>

<style scoped>
.app-layout {
  height: 100vh;
  width: 100vw;
}

.sidebar {
  background: #001529;
  color: #fff;
  overflow: hidden;
}

.header {
  background: #fff;
  border-bottom: 1px solid #e8e8e8;
  padding: 0 24px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.main-content {
  background: #f0f2f5;
  padding: 24px;
  overflow-y: auto;
}
</style>
