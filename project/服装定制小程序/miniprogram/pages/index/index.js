// pages/index/index.js
const mockData = require('../../mock/data.js');
const store = require('../../store/index.js');
const CloudImageUtil = require('../../utils/cloud-image.js');

Page({
  data: {
    banners: [],
    categories: [],
    hotProducts: [],
    newProducts: [],
    loading: false
  },

  async onLoad() {
    console.log('首页加载（本地模式）');
    await this.loadData();
  },

  /**
   * 加载页面数据（本地模式）
   */
  async loadData() {
    try {
      console.log('[index] 开始加载数据');
      wx.showLoading({ title: '加载中...', mask: true });

      // 所有商品
      const allProducts = mockData.products || [];

      if (allProducts.length === 0) {
        console.warn('[index] 商品数据为空');
      }

      // 热门商品（销量前4）
      const hotProducts = [...allProducts]
        .sort((a, b) => b.sales - a.sales)
        .slice(0, 4);

      // 新品（取后4个）
      const newProducts = allProducts.slice(-4);

      // 转换云存储图片为临时链接（失败时使用原数据）
      const banners = await CloudImageUtil.preloadImages(mockData.banners || []);
      const categories = await CloudImageUtil.preloadImages(
        (mockData.categories || []).filter(c => !c.parentId)
      );
      const hotProductsWithImages = await CloudImageUtil.preloadImages(hotProducts);
      const newProductsWithImages = await CloudImageUtil.preloadImages(newProducts);

      this.setData({
        banners,
        categories,
        hotProducts: hotProductsWithImages,
        newProducts: newProductsWithImages,
        loading: false
      });

      console.log('[index] 数据加载成功');
    } catch (err) {
      console.error('[index] 数据加载失败', err);
      wx.hideLoading();

      // 降级：直接使用原始数据（不转换图片）
      const allProducts = mockData.products || [];
      this.setData({
        banners: mockData.banners || [],
        categories: (mockData.categories || []).filter(c => !c.parentId),
        hotProducts: allProducts.slice(0, 4),
        newProducts: allProducts.slice(-4),
        loading: false
      });

      wx.showToast({ title: '图片加载异常', icon: 'none' });
    }
  },

  /**
   * 点击搜索框
   */
  onSearch() {
    wx.showToast({ title: '搜索功能开发中', icon: 'none' });
  },

  /**
   * 点击分类
   */
  onCategoryTap(e) {
    const { id, name } = e.currentTarget.dataset;
    console.log('点击分类:', id, name);

    // 保存选中的分类ID到 Store
    store.selectCategory(id);
    console.log('已保存分类ID到 Store:', store.selectedCategoryId);

    // 跳转到分类页（使用 switchTab 因为分类页在 tabBar 中）
    wx.switchTab({
      url: '/pages/category/category',
      success: () => {
        console.log('跳转到分类页成功');
      },
      fail: (err) => {
        console.error('跳转失败:', err);
        wx.showToast({ title: '跳转失败', icon: 'none' });
      }
    });
  },

  /**
   * 点击商品
   */
  onProductTap(e) {
    console.log('=== onProductTap 被调用 ===');
    console.log('事件对象:', e);
    console.log('dataset:', e.currentTarget.dataset);

    const id = e.currentTarget.dataset.id;
    console.log('点击商品 ID:', id);

    if (!id) {
      console.error('商品 ID 为空！');
      wx.showToast({ title: '商品ID错误', icon: 'none' });
      return;
    }

    // 保存选中的商品ID到 Store
    store.selectProduct(id);
    console.log('已保存到 Store:', store.selectedProductId);

    // 跳转到商品详情页（使用 navigateTo 因为详情页在分包中）
    wx.navigateTo({
      url: `/packageA/pages/design/design?productId=${id}`,
      success: () => {
        console.log('跳转到商品详情页成功');
      },
      fail: (err) => {
        console.error('跳转失败:', err);
        wx.showToast({ title: '跳转失败', icon: 'none' });
      }
    });
  },

  /**
   * 查看更多商品
   */
  onMoreProducts() {
    wx.switchTab({
      url: '/pages/category/category'
    });
  }
});
