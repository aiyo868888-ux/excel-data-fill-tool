/**
 * Store - 简易状态管理系统
 * 单例模式，用于管理全局状态和数据持久化
 */

class Store {
  constructor() {
    // 初始化状态
    this.state = {
      // 导航状态（带版本控制，防止过期数据）
      navigation: {
        selectedProductId: null,
        selectedDesignId: null,
        selectedCategoryId: null,
        timestamp: null,
        version: 0
      },
      // 设计列表缓存
      designs: [],
      // 用户信息
      userInfo: null
    };

    // 从本地存储恢复状态
    this._hydrate();
  }

  /**
   * 从本地存储恢复状态
   * @private
   */
  _hydrate() {
    try {
      const saved = wx.getStorageSync('store_state');
      if (saved && saved.navigation) {
        // 验证数据是否过期（5分钟有效期）
        const now = Date.now();
        const isExpired = saved.navigation.timestamp &&
          (now - saved.navigation.timestamp > 5 * 60 * 1000);

        if (!isExpired) {
          this.state = { ...this.state, ...saved };
          console.log('Store 状态已从本地恢复');
        } else {
          console.log('Store 状态已过期，使用默认值');
          // 清除过期的导航状态
          saved.navigation = {
            selectedProductId: null,
            selectedDesignId: null,
            selectedCategoryId: null,
            timestamp: null,
            version: 0
          };
          this.state = { ...this.state, ...saved };
        }
      }
    } catch (err) {
      console.error('Store 状态恢复失败:', err);
    }
  }

  /**
   * 持久化状态到本地存储
   * @private
   */
  _persist() {
    try {
      wx.setStorageSync('store_state', this.state);
    } catch (err) {
      console.error('Store 状态持久化失败:', err);
    }
  }

  /**
   * Getters - 获取状态
   */
  get selectedProductId() {
    return this.state.navigation.selectedProductId;
  }

  get selectedDesignId() {
    return this.state.navigation.selectedDesignId;
  }

  get selectedCategoryId() {
    return this.state.navigation.selectedCategoryId;
  }

  get designs() {
    return this.state.designs;
  }

  /**
   * Actions - 修改状态
   */

  /**
   * 选择商品
   * @param {string} productId - 商品ID
   */
  selectProduct(productId) {
    this.state.navigation.selectedProductId = productId;
    this.state.navigation.timestamp = Date.now();
    this.state.navigation.version++;
    this._persist();
    console.log('Store: 已选择商品', productId);
  }

  /**
   * 选择设计
   * @param {string} designId - 设计ID
   * @param {string} productId - 关联的商品ID
   */
  selectDesign(designId, productId = null) {
    this.state.navigation.selectedDesignId = designId;
    if (productId) {
      this.state.navigation.selectedProductId = productId;
    }
    this.state.navigation.timestamp = Date.now();
    this.state.navigation.version++;
    this._persist();
    console.log('Store: 已选择设计', designId);
  }

  /**
   * 选择分类
   * @param {string} categoryId - 分类ID
   */
  selectCategory(categoryId) {
    this.state.navigation.selectedCategoryId = categoryId;
    this.state.navigation.timestamp = Date.now();
    this._persist();
    console.log('Store: 已选择分类', categoryId);
  }

  /**
   * 清除导航状态
   */
  clearNavigation() {
    this.state.navigation = {
      selectedProductId: null,
      selectedDesignId: null,
      selectedCategoryId: null,
      timestamp: null,
      version: this.state.navigation.version + 1
    };
    this._persist();
    console.log('Store: 已清除导航状态');
  }

  /**
   * 更新设计列表
   * @param {Array} designs - 设计列表
   */
  setDesigns(designs) {
    this.state.designs = designs;
    this._persist();
    console.log('Store: 已更新设计列表', designs.length, '条');
  }

  /**
   * 添加设计到列表
   * @param {Object} design - 设计对象
   */
  addDesign(design) {
    // 添加到列表开头
    this.state.designs.unshift(design);

    // 限制最多保存50条
    if (this.state.designs.length > 50) {
      this.state.designs = this.state.designs.slice(0, 50);
    }

    this._persist();
    console.log('Store: 已添加设计', design.designId);
  }

  /**
   * 删除设计
   * @param {string} designId - 设计ID
   */
  removeDesign(designId) {
    this.state.designs = this.state.designs.filter(d => d.designId !== designId);
    this._persist();
    console.log('Store: 已删除设计', designId);
  }

  /**
   * 验证导航状态是否有效（5分钟内）
   * @returns {boolean}
   */
  isNavigationValid() {
    if (!this.state.navigation.timestamp) return false;
    const now = Date.now();
    return (now - this.state.navigation.timestamp) < 5 * 60 * 1000;
  }
}

// 创建单例
const store = new Store();

module.exports = store;
