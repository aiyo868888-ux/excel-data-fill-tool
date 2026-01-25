/**
 * 路由配置
 */

import { createRouter, createWebHashHistory, RouteRecordRaw } from 'vue-router'

const routes: RouteRecordRaw[] = [
  {
    path: '/',
    redirect: '/home'
  },
  {
    path: '/home',
    name: 'Home',
    component: () => import('@/views/Home.vue'),
    meta: { title: '对话' }
  },
  {
    path: '/soul',
    name: 'Soul',
    component: () => import('@/views/SoulView.vue'),
    meta: { title: '分身画像' }
  },
  {
    path: '/decision',
    name: 'Decision',
    component: () => import('@/views/DecisionView.vue'),
    meta: { title: '决策模拟' }
  },
  {
    path: '/tools',
    name: 'Tools',
    component: () => import('@/views/ToolsView.vue'),
    meta: { title: '工具箱' }
  },
  {
    path: '/analytics',
    name: 'Analytics',
    component: () => import('@/views/AnalyticsView.vue'),
    meta: { title: '数据分析' }
  },
  {
    path: '/settings',
    name: 'Settings',
    component: () => import('@/views/SettingsView.vue'),
    meta: { title: '设置' }
  }
]

const router = createRouter({
  history: createWebHashHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  // 设置页面标题
  if (to.meta?.title) {
    document.title = `${to.meta.title} - Digital Soul`
  }
  next()
})

export default router
