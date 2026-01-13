// pages/category/category.js
const mockData = require('../../mock/data.js');
const CloudImageUtil = require('../../utils/cloud-image.js');

Page({
  data: {
    categoryId: '',
    categoryName: '',
    categories: [],
    products: [],
    selectedCategoryId: '',
    sortBy: 'default',
    loading: false
  },

  async onLoad(options) {
    console.log('分类页加载（本地模式）', options);

    const { id, name } = options;
    const selectedCategoryId = id || mockData.categories[0]._id;

    this.setData({
      categoryId: id || '',
      categoryName: name || '全部分类',
      selectedCategoryId,
      categories: mockData.categories
    });

    wx.showLoading({ title: '加载中...', mask: true });
    await this.loadProducts();
    wx.hideLoading();
  },

  onShow() {
    console.log('分类页 onShow');

    // 每次显示时检查是否有新的分类选择
    const app = getApp();
    if (app.globalData.selectedCategoryId) {
      const categoryId = app.globalData.selectedCategoryId;
      console.log('onShow: 检测到新的分类选择:', categoryId);

      // 查找分类信息
      const category = mockData.categories.find(c => c._id === categoryId);
      if (category) {
        console.log('找到分类:', category);

        // 更新选中的分类并加载商品
        this.setData({
          selectedCategoryId: categoryId,
          categoryName: category.name
        });

        this.loadProducts();

        // 清除 globalData
        app.globalData.selectedCategoryId = null;
      }
    }
  },

  /**
   * 加载商品列表（本地模式）
   */
  async loadProducts() {
    try {
      this.setData({ loading: true });

      const categoryId = this.data.selectedCategoryId;
      let products = mockData.products;

      // 筛选商品
      if (categoryId) {
        products = products.filter(p => p.categoryId === categoryId);
      }

      // 排序
      if (this.data.sortBy === 'price') {
        products.sort((a, b) => a.price - b.price);
      } else if (this.data.sortBy === 'sales') {
        products.sort((a, b) => b.sales - a.sales);
      }

      // 转换云存储图片为临时链接
      const productsWithImages = await CloudImageUtil.preloadImages(products);

      this.setData({ products: productsWithImages });
      console.log('商品加载成功:', productsWithImages.length);
    } catch (err) {
      console.error('商品加载失败', err);
      wx.showToast({ title: '商品加载失败', icon: 'none' });
    } finally {
      this.setData({ loading: false });
    }
  },

  /**
   * 分类切换
   */
  onCategoryChange(e) {
    const id = e.currentTarget.dataset.id;
    if (id === this.data.selectedCategoryId) return;

    const category = mockData.categories.find(c => c._id === id);
    this.setData({
      selectedCategoryId: id,
      categoryName: category ? category.name : '全部分类'
    });

    this.loadProducts();
  },

  /**
   * 排序切换
   */
  onSortChange(e) {
    const sort = e.currentTarget.dataset.sort;
    if (sort === this.data.sortBy) return;

    this.setData({ sortBy: sort });
    this.loadProducts();
  },

  /**
   * 点击商品
   */
  onProductTap(e) {
    console.log('=== 分类页 onProductTap 被调用 ===');
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
   * 搜索
   */
  onSearch() {
    wx.showToast({ title: '搜索功能开发中', icon: 'none' });
  },

  /**
   * 筛选
   */
  onFilter() {
    wx.showToast({ title: '筛选功能开发中', icon: 'none' });
  }
});
