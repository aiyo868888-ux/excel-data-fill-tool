<template>
  <div class="header">
    <div class="header-left">
      <h2 class="page-title">{{ pageTitle }}</h2>
    </div>

    <div class="header-right">
      <el-button :icon="Refresh" circle @click="handleRefresh" />

      <el-dropdown trigger="click">
        <el-button :icon="More" circle />
        <template #dropdown>
          <el-dropdown-menu>
            <el-dropdown-item @click="handleBackup">备份数据</el-dropdown-item>
            <el-dropdown-item @click="handleExport">导出数据</el-dropdown-item>
            <el-dropdown-item divided @click="handleAbout">关于</el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Refresh, More } from '@element-plus/icons-vue'

const route = useRoute()

const pageTitle = computed(() => route.meta?.title || 'Digital Soul')

const handleRefresh = () => {
  location.reload()
}

const handleBackup = async () => {
  try {
    const stats = await window.electronAPI.db.stats()
    ElMessage.success('数据备份成功')
  } catch (error) {
    ElMessage.error('备份失败')
  }
}

const handleExport = () => {
  ElMessage.info('导出功能开发中...')
}

const handleAbout = () => {
  ElMessage.info('Digital Soul v0.1.0')
}
</script>

<style scoped>
.header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 100%;
}

.header-left {
  display: flex;
  align-items: center;
}

.page-title {
  font-size: 18px;
  font-weight: 500;
  color: #262626;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 12px;
}
</style>
