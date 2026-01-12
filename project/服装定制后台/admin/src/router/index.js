import { createRouter, createWebHistory } from 'vue-router';
import Dashboard from '../views/Dashboard.vue';
import Products from '../views/Products.vue';
import Categories from '../views/Categories.vue';

const routes = [
  {
    path: '/',
    redirect: '/dashboard'
  },
  {
    path: '/dashboard',
    component: Dashboard
  },
  {
    path: '/products',
    component: Products
  },
  {
    path: '/categories',
    component: Categories
  }
];

const router = createRouter({
  history: createWebHistory(),
  routes
});

export default router;
