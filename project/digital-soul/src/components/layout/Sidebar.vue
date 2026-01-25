<template>
  <div class="sidebar">
    <div class="logo">
      <el-icon class="logo-icon"><User /></el-icon>
      <span class="logo-text">Digital Soul</span>
    </div>

    <el-menu
      :default-active="activeMenu"
      class="sidebar-menu"
      background-color="#001529"
      text-color="rgba(255, 255, 255, 0.65)"
      active-text-color="#fff"
      router
    >
      <el-menu-item index="/home">
        <el-icon><ChatDotRound /></el-icon>
        <span>对话</span>
      </el-menu-item>

      <el-menu-item index="/soul">
        <el-icon><Avatar /></el-icon>
        <span>分身画像</span>
      </el-menu-item>

      <el-menu-item index="/decision">
        <el-icon><Briefcase /></el-icon>
        <span>决策模拟</span>
      </el-menu-item>

      <el-menu-item index="/tools">
        <el-icon><Tools /></el-icon>
        <span>工具箱</span>
      </el-menu-item>

      <el-menu-item index="/analytics">
        <el-icon><DataAnalysis /></el-icon>
        <span>数据分析</span>
      </el-menu-item>

      <el-menu-item index="/settings">
        <el-icon><Setting /></el-icon>
        <span>设置</span>
      </el-menu-item>
    </el-menu>

    <div class="sidebar-footer">
      <div class="user-info">
        <el-avatar size="small" icon="User" />
        <span class="username">用户</span>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import {
  User,
  ChatDotRound,
  Avatar,
  Briefcase,
  Tools,
  DataAnalysis,
  Setting
} from '@element-plus/icons-vue'

const route = useRoute()

const activeMenu = computed(() => route.path)
</script>

<style scoped>
.sidebar {
  height: 100%;
  display: flex;
  flex-direction: column;
}

.logo {
  height: 64px;
  display: flex;
  align-items: center;
  padding: 0 24px;
  background: rgba(255, 255, 255, 0.05);
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.logo-icon {
  font-size: 24px;
  color: #1890ff;
  margin-right: 12px;
}

.logo-text {
  font-size: 18px;
  font-weight: 600;
  color: #fff;
}

.sidebar-menu {
  flex: 1;
  border: none;
  overflow-y: auto;
}

.sidebar-footer {
  height: 64px;
  padding: 0 24px;
  display: flex;
  align-items: center;
  border-top: 1px solid rgba(255, 255, 255, 0.1);
}

.user-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.username {
  color: rgba(255, 255, 255, 0.85);
  font-size: 14px;
}
</style>
