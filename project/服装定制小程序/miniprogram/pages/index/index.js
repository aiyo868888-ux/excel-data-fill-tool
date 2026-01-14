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
    console.log('[index] ========== loadData 开始 ==========');
    wx.showLoading({ title: '加载中...', mask: true });

    try {
      console.log('[index] 开始加载数据');

      // 所有商品
      const allProducts = mockData.products || [];
      console.log('[index] 商品总数:', allProducts.length);

      if (allProducts.length === 0) {
        console.warn('[index] 商品数据为空');
      }

      // 热门商品（销量前4）
      const hotProducts = [...allProducts]
        .sort((a, b) => b.sales - a.sales)
        .slice(0, 4);
      console.log('[index] 热门商品数量:', hotProducts.length);

      // 新品（取后4个）
      const newProducts = allProducts.slice(-4);
      console.log('[index] 新品数量:', newProducts.length);

      console.log('[index] 原始 banners 数量:', (mockData.banners || []).length);
      console.log('[index] 第一个 banner image:', (mockData.banners || [])[0]?.image);

      // 转换云存储图片为临时链接（设置3秒超时）
      console.log('[index] 开始转换 banners 图片...');
      const banners = await this.withTimeout(
        CloudImageUtil.preloadImages(mockData.banners || []),
        3000
      ).catch((err) => {
        console.warn('[index] banners图片转换失败，使用原始数据:', err);
        return mockData.banners || [];
      });

      console.log('[index] banners 转换完成，第一个 banner:', banners[0]);

      console.log('[index] 开始转换 categories 图片...');
      const categories = await this.withTimeout(
        CloudImageUtil.preloadImages(
          (mockData.categories || []).filter(c => !c.parentId)
        ),
        3000
      ).catch((err) => {
        console.warn('[index] categories图片转换失败，使用原始数据:', err);
        return (mockData.categories || []).filter(c => !c.parentId);
      });

      console.log('[index] 开始转换 hotProducts 图片...');
      const hotProductsWithImages = await this.withTimeout(
        CloudImageUtil.preloadImages(hotProducts),
        3000
      ).catch((err) => {
        console.warn('[index] hotProducts图片转换失败，使用原始数据:', err);
        return hotProducts;
      });

      console.log('[index] hotProducts 转换完成，第一个商品:', hotProductsWithImages[0]);

      console.log('[index] 开始转换 newProducts 图片...');
      const newProductsWithImages = await this.withTimeout(
        CloudImageUtil.preloadImages(newProducts),
        3000
      ).catch((err) => {
        console.warn('[index] newProducts图片转换失败，使用原始数据:', err);
        return newProducts;
      });

      console.log('[index] 所有图片转换完成，准备 setData');

      this.setData({
        banners,
        categories,
        hotProducts: hotProductsWithImages,
        newProducts: newProductsWithImages,
        loading: false
      });

      console.log('[index] ✅ setData 完成');
      console.log('[index] banners[0].image 类型:', typeof this.data.banners[0]?.image);
      console.log('[index] banners[0].image 值:', this.data.banners[0]?.image);
      console.log('[index] ========== 数据加载完成 ==========');
    } catch (err) {
      console.error('[index] ❌ 数据加载失败', err);

      // 降级：直接使用原始数据（不转换图片）
      const allProducts = mockData.products || [];
      this.setData({
        banners: mockData.banners || [],
        categories: (mockData.categories || []).filter(c => !c.parentId),
        hotProducts: allProducts.slice(0, 4),
        newProducts: allProducts.slice(-4),
        loading: false
      });

      wx.showToast({ title: '部分图片加载失败', icon: 'none' });
    } finally {
      wx.hideLoading();
    }
  },

  /**
   * 超时包装器 - 防止图片转换卡住
   * @param {Promise} promise - 要执行的Promise
   * @param {number} timeout - 超时时间（毫秒）
   * @returns {Promise} 带超时的Promise
   */
  withTimeout(promise, timeout) {
    return Promise.race([
      promise,
      new Promise((_, reject) =>
        setTimeout(() => reject(new Error(`操作超时 (${timeout}ms)`)), timeout)
      )
    ]);
  },

  /**
   * 点击搜索框
   */
  onSearch() {
    wx.showToast({ title: '搜索功能开发中', icon: 'none' });
  },

  /**
   * 跳转到测试页面
   */
  onGoTest() {
    wx.navigateTo({
      url: '/pages/test/test'
    });
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
