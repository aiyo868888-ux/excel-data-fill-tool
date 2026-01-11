// pages/index/index.js
const mockData = require('../../mock/data.js');

Page({
  data: {
    banners: [],
    categories: [],
    hotProducts: [],
    newProducts: [],
    loading: false
  },

  onLoad() {
    console.log('首页加载（本地模式）');
    this.loadData();
  },

  /**
   * 加载页面数据（本地模式）
   */
  loadData() {
    try {
      // 所有商品
      const allProducts = mockData.products;

      // 热门商品（销量前4）
      const hotProducts = [...allProducts]
        .sort((a, b) => b.sales - a.sales)
        .slice(0, 4);

      // 新品（取后4个）
      const newProducts = allProducts.slice(-4);

      this.setData({
        loading: true,
        banners: mockData.banners,
        categories: mockData.categories.filter(c => !c.parentId), // 只显示一级分类
        hotProducts,
        newProducts
      });
      console.log('本地数据加载成功');
    } catch (err) {
      console.error('数据加载失败', err);
      wx.showToast({ title: '数据加载失败', icon: 'none' });
    } finally {
      this.setData({ loading: false });
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

    // 跳转到分类页
    wx.navigateTo({
      url: `/pages/category/category?id=${id}&name=${name}`
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

    // 保存选中的商品ID到全局数据
    const app = getApp();
    app.globalData.selectedProductId = id;
    console.log('已保存到 globalData:', app.globalData.selectedProductId);

    // 跳转到设计页（使用 switchTab 因为设计页在 tabBar 中）
    wx.switchTab({
      url: '/pages/design/design',
      success: () => {
        console.log('跳转到设计页成功');
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
