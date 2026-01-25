<template>
  <div class="test-view">
    <h1>Digital Soul 测试页面</h1>
    <el-card>
      <template #header>
        <span>基础功能测试</span>
      </template>
      <el-space direction="vertical" :size="20" style="width: 100%">
        <el-alert type="success" :closable="false">
          ✅ Vue 3 运行正常
        </el-alert>

        <el-alert type="success" :closable="false">
          ✅ Element Plus 加载成功
        </el-alert>

        <el-alert type="success" :closable="false">
          ✅ 路由工作正常
        </el-alert>

        <el-divider />

        <el-button type="primary" @click="testClick">
          点击测试 ({{ count }})
        </el-button>

        <el-input v-model="testInput" placeholder="输入测试..." />

        <el-descriptions title="当前状态" :column="1" border>
          <el-descriptions-item label="页面">
            Test.vue
          </el-descriptions-item>
          <el-descriptions-item label="Vue 版本">
            3.4.0
          </el-descriptions-item>
          <el-descriptions-item label="Element Plus">
            已加载
          </el-descriptions-item>
          <el-descriptions-item label="点击次数">
            {{ count }}
          </el-descriptions-item>
          <el-descriptions-item label="输入内容">
            {{ testInput || '(空)' }}
          </el-descriptions-item>
        </el-descriptions>
      </el-space>
    </el-card>

    <el-card style="margin-top: 20px">
      <template #header>
        <span>导航测试</span>
      </template>
      <el-space>
        <el-button @click="$router.push('/home')">对话</el-button>
        <el-button @click="$router.push('/soul')">分身画像</el-button>
        <el-button @click="$router.push('/decision')">决策模拟</el-button>
        <el-button @click="$router.push('/tools')">工具箱</el-button>
        <el-button @click="$router.push('/analytics')">数据分析</el-button>
        <el-button @click="$router.push('/settings')">设置</el-button>
      </el-space>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { ElMessage } from 'element-plus'

const count = ref(0)
const testInput = ref('')

const testClick = () => {
  count.value++
  ElMessage.success(`点击了 ${count.value} 次！`)
}
</script>

<style scoped>
.test-view {
  padding: 24px;
  max-width: 1200px;
  margin: 0 auto;
}

h1 {
  margin-bottom: 24px;
  color: #303133;
}
</style>
