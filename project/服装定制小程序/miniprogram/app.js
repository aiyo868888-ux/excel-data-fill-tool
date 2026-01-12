// app.js
App({
  onLaunch() {
    console.log('小程序启动（本地模式）');

    // 本地模式 - 不使用云开发
    console.log('本地数据模式已启用');
  },

  globalData: {
    userInfo: null,
    version: '1.0.0-local',
    selectedProductId: null,      // 用于传递选中的商品ID
    selectedCategoryId: null,     // 用于传递选中的分类ID
    navigationTitle: '鼎盛服装定制供应链'  // 导航栏标题（可从后台配置）
  }
});
