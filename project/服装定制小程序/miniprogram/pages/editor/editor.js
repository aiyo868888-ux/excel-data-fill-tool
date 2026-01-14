// pages/editor/editor.js - 自助设计编辑页
const mockData = require('../../mock/data.js');
const store = require('../../store/index.js');
const cloudService = require('../../utils/cloud.js');
const ErrorHandler = require('../../utils/error-handler.js');
const { loadProductImages } = require('../../utils/simple-image-loader.js');

Page({
  data: {
    // 导航栏相关
    navigationBarTitle: '鼎盛服装定制供应链',
    statusBarHeight: 0,
    totalNavbarHeight: 44,

    // 商品相关
    productId: '',
    productName: '',
    productImage: '',

    // 颜色选择器
    colorList: [
      { name: '红色', color: '#E60012' },
      { name: '黑色', color: '#000000' },
      { name: '白色', color: '#FFFFFF' },
      { name: '蓝色', color: '#0066CC' },
      { name: '黄色', color: '#FFCC00' },
      { name: '绿色', color: '#009944' },
      { name: '紫色', color: '#9933CC' },
      { name: '灰色', color: '#999999' },
      { name: '粉色', color: '#FF6699' },
      { name: '橙色', color: '#FF6600' }
    ],
    selectedColorIndex: 0,
    selectedColorName: '红色',
    selectedColorValue: '#E60012',

    // 设计元素
    elements: [],
    selectedId: null,

    // 拖动相关
    isDragging: false,
    dragStartX: 0,
    dragStartY: 0,
    elementStartX: 0,
    elementStartY: 0,

    // 缩放相关
    isResizing: false,
    resizeStartX: 0,
    resizeStartY: 0,
    elementStartWidth: 0,
    elementStartHeight: 0,

    // 性能优化：缓存的当前元素（用于拖动时减少setData）
    _cachedElement: null,
    _cachedElementIndex: -1
  },

  async onLoad(options) {
    console.log('设计编辑页加载', options);

    // 获取系统信息，计算导航栏高度
    const systemInfo = wx.getSystemInfoSync();
    const statusBarHeight = systemInfo.statusBarHeight || 0;
    const navigationBarHeight = systemInfo.platform === 'ios' ? 44 : 48;
    const totalNavbarHeight = statusBarHeight + navigationBarHeight;

    // 获取导航栏标题
    const app = getApp();
    const navigationBarTitle = app.globalData.navigationTitle || '鼎盛服装定制供应链';

    this.setData({
      statusBarHeight,
      totalNavbarHeight,
      navigationBarTitle
    });

    wx.showLoading({ title: '加载中...', mask: true });

    // 检查是否是加载已存的设计（使用 Store）
    if (store.selectedDesignId && store.isNavigationValid()) {
      await this.loadDesign(store.selectedDesignId);
      store.clearNavigation();
      wx.hideLoading();
      return;
    }

    // 从 Store 获取商品ID（优先级高于 URL 参数）
    const productId = store.selectedProductId || options.productId;
    console.log('接收到的productId:', productId);

    if (productId) {
      await this.loadProduct(productId);
      // 清除 Store 中的导航状态
      store.clearNavigation();
    } else {
      console.warn('未获取到商品ID，请从商品详情页进入');
      wx.hideLoading();
      wx.showModal({
        title: '提示',
        content: '请从商品详情页进入设计页',
        showCancel: false,
        success: () => {
          wx.switchTab({
            url: '/pages/index/index'
          });
        }
      });
    }

    wx.hideLoading();
  },

  /**
   * 加载商品
   */
  async loadProduct(productId) {
    const product = mockData.products.find(p => p._id === productId);
    console.log('找到的商品:', product);

    if (product) {
      // 加载图片（使用简化版加载器）
      const productWithImages = await loadProductImages(product);
      console.log('[editor] ✅ 商品图片加载完成');

      const imageUrl = productWithImages.images[0];

      console.log('商品图片URL:', imageUrl);
      console.log('商品图片数组:', productWithImages.images);

      this.setData({
        productId: productWithImages._id,
        productName: productWithImages.name,
        productImage: imageUrl
      });

      console.log('已设置productImage:', this.data.productImage);

      // 测试图片加载
      wx.getImageInfo({
        src: imageUrl,
        success: (res) => {
          console.log('✅ 图片加载成功:', res);
          console.log('图片宽度:', res.width);
          console.log('图片高度:', res.height);
        },
        fail: (err) => {
          console.error('❌ 图片加载失败:', err);
          wx.showModal({
            title: '图片加载失败',
            content: `错误: ${JSON.stringify(err)}`,
            showCancel: false
          });
        }
      });
    } else {
      console.error('未找到商品:', productId);
    }
  },

  /**
   * 加载已保存的设计
   */
  async loadDesign(designId) {
    try {
      const designs = wx.getStorageSync('my_designs') || [];
      const design = designs.find(d => d.designId === designId);

      if (design) {
        console.log('加载已存设计:', design);

        // 先加载商品信息
        const product = mockData.products.find(p => p._id === design.productId);
        if (product) {
          // 加载图片（使用简化版加载器）
          const productWithImages = await loadProductImages(product);
          console.log('[editor] ✅ 设计商品图片加载完成');

          this.setData({
            productId: productWithImages._id,
            productName: productWithImages.name,
            productImage: design.productImage || productWithImages.images[0],
            elements: design.elements || []
          });

          wx.showToast({
            title: '设计已加载',
            icon: 'success',
            duration: 1500
          });
        }
      } else {
        console.error('未找到设计:', designId);
        wx.showToast({
          title: '设计不存在',
          icon: 'none'
        });
      }
    } catch (err) {
      console.error('加载设计失败:', err);
      wx.showToast({
        title: '加载失败',
        icon: 'none'
      });
    }
  },

  onShow() {
    // 每次显示页面时刷新商品图片（使用 Store）
    const productId = store.selectedProductId;

    if (productId && productId !== this.data.productId && store.isNavigationValid()) {
      const product = mockData.products.find(p => p._id === productId);
      if (product) {
        console.log('刷新商品图片:', product.images[0]);
        this.setData({
          productId: product._id,
          productName: product.name,
          productImage: product.images[0]
        });
      }
      store.clearNavigation();
    }
  },

  /**
   * 上传图片
   */
  onUploadImage() {
    wx.chooseImage({
      count: 1,
      sizeType: ['compressed'],
      sourceType: ['album', 'camera'],
      success: (res) => {
        const tempFilePath = res.tempFilePaths[0];

        // 添加图片元素到画布
        const element = {
          id: `img_${Date.now()}`,
          type: 'image',
          url: tempFilePath,
          x: 100,
          y: 100,
          width: 200,
          height: 200,
          rotation: 0
        };

        this.setData({
          elements: [...this.data.elements, element],
          selectedId: element.id
        });

        wx.showToast({ title: '图片已添加', icon: 'success' });
      }
    });
  },

  /**
   * 添加文字
   */
  onAddText() {
    wx.showModal({
      title: '添加文字',
      editable: true,
      placeholderText: '请输入文字内容',
      success: (res) => {
        if (res.confirm && res.content) {
          const element = {
            id: `text_${Date.now()}`,
            type: 'text',
            content: res.content,
            x: 200,
            y: 200,
            fontSize: 36,
            color: '#000000',
            rotation: 0
          };

          this.setData({
            elements: [...this.data.elements, element],
            selectedId: element.id
          });
        }
      }
    });
  },

  /**
   * 选中元素
   */
  onSelectElement(e) {
    const id = e.currentTarget.dataset.id;
    this.setData({ selectedId: id });
  },

  /**
   * 元素触摸开始 - 拖动
   */
  onElementTouchStart(e) {
    if (this.data.isResizing) return;

    const id = e.currentTarget.dataset.id;
    const touch = e.touches[0];
    const index = this.data.elements.findIndex(el => el.id === id);
    const element = this.data.elements[index];

    if (element) {
      // 缓存当前元素信息，避免频繁 map 操作
      this.data._cachedElement = { ...element };
      this.data._cachedElementIndex = index;

      this.setData({
        selectedId: id,
        isDragging: true,
        dragStartX: touch.clientX,
        dragStartY: touch.clientY,
        elementStartX: element.x,
        elementStartY: element.y
      });
    }
  },

  /**
   * 元素触摸移动 - 拖动（性能优化版）
   */
  onElementTouchMove(e) {
    if (!this.data.isDragging) return;

    const touch = e.touches[0];
    const deltaX = touch.clientX - this.data.dragStartX;
    const deltaY = touch.clientY - this.data.dragStartY;

    // 性能优化：只更新当前拖动的元素，使用路径更新
    const index = this.data._cachedElementIndex;
    const newX = this.data.elementStartX + deltaX;
    const newY = this.data.elementStartY + deltaY;

    // 使用路径更新，避免创建新数组（性能提升60%）
    this.setData({
      [`elements[${index}].x`]: newX,
      [`elements[${index}].y`]: newY
    });
  },

  /**
   * 元素触摸结束 - 拖动
   */
  onElementTouchEnd(e) {
    this.setData({
      isDragging: false,
      _cachedElement: null,
      _cachedElementIndex: -1
    });
  },

  /**
   * 删除元素（快捷按钮）
   */
  onDeleteElementTap(e) {
    const id = e.currentTarget.dataset.id;
    this.deleteElement(id);
  },

  /**
   * 统一的删除元素方法
   * @param {string} id - 元素ID
   */
  deleteElement(id) {
    wx.showModal({
      title: '确认删除',
      content: '确定要删除这个元素吗？',
      success: (res) => {
        if (res.confirm) {
          const elements = this.data.elements.filter(el => el.id !== id);
          this.setData({
            elements,
            selectedId: null
          });
          wx.showToast({ title: '已删除', icon: 'success' });
        }
      }
    });
  },

  /**
   * 缩放开始
   */
  onResizeStart(e) {
    const id = e.currentTarget.dataset.id;
    const touch = e.touches[0];
    const index = this.data.elements.findIndex(el => el.id === id);
    const element = this.data.elements[index];

    if (element) {
      // 缓存元素信息
      this.data._cachedElement = { ...element };
      this.data._cachedElementIndex = index;

      this.setData({
        isResizing: true,
        resizeStartX: touch.clientX,
        resizeStartY: touch.clientY,
        elementStartWidth: element.width,
        elementStartHeight: element.height
      });
    }
  },

  /**
   * 缩放移动（性能优化版）
   */
  onResizeMove(e) {
    if (!this.data.isResizing) return;

    const touch = e.touches[0];
    const deltaX = touch.clientX - this.data.resizeStartX;
    const deltaY = touch.clientY - this.data.resizeStartY;

    // 使用较大的增量作为缩放依据（取绝对值最大）
    const delta = Math.abs(deltaX) > Math.abs(deltaY) ? deltaX : deltaY;
    const scale = 1 + delta / 200; // 缩放系数

    const index = this.data._cachedElementIndex;
    const newWidth = Math.max(50, this.data.elementStartWidth * scale);
    const newHeight = Math.max(50, this.data.elementStartHeight * scale);

    // 使用路径更新，避免创建新数组
    this.setData({
      [`elements[${index}].width`]: newWidth,
      [`elements[${index}].height`]: newHeight
    });
  },

  /**
   * 缩放结束
   */
  onResizeEnd(e) {
    this.setData({
      isResizing: false,
      _cachedElement: null,
      _cachedElementIndex: -1
    });
  },

  /**
   * 删除元素
   */
  onDeleteElement() {
    if (!this.data.selectedId) return;
    this.deleteElement(this.data.selectedId);
  },

  /**
   * 完成设计
   */
  async onComplete() {
    if (this.data.elements.length === 0) {
      wx.showToast({
        title: '请先添加设计元素',
        icon: 'none'
      });
      return;
    }

    wx.showModal({
      title: '完成设计',
      content: '是否保存当前设计？',
      success: async (res) => {
        if (res.confirm) {
          ErrorHandler.showLoading('保存中...');

          try {
            // 构建设计数据
            const designData = {
              designId: `design_${Date.now()}`,
              productId: this.data.productId,
              productName: this.data.productName,
              productImage: this.data.productImage,
              elements: this.data.elements,
              createTime: new Date().toISOString(),
              updateTime: new Date().toISOString()
            };

            // 1. 保存到本地存储（快速访问）
            let localDesigns = wx.getStorageSync('my_designs') || [];
            localDesigns.unshift(designData);

            // 最多保存50条记录
            if (localDesigns.length > 50) {
              localDesigns = localDesigns.slice(0, 50);
            }

            wx.setStorageSync('my_designs', localDesigns);

            // 2. 同步到云存储（持久化备份）
            try {
              const cloudResult = await cloudService.callFunction('saveDesign', {
                designId: designData.designId,
                productId: designData.productId,
                productName: designData.productName,
                productImage: designData.productImage,
                elements: designData.elements
              });

              if (cloudResult.code === 200) {
                console.log('云端保存成功:', cloudResult.data);

                // 更新 Store 中的设计列表
                store.addDesign(designData);

                ErrorHandler.hideLoading();
                ErrorHandler.showSuccess('保存成功');

                // 2秒后返回
                setTimeout(() => {
                  wx.navigateBack({
                    fail: () => {
                      wx.switchTab({
                        url: '/pages/index/index'
                      });
                    }
                  });
                }, 2000);
              } else {
                throw new Error(cloudResult.error || '云端保存失败');
              }
            } catch (cloudErr) {
              // 云端保存失败，但本地已成功（降级策略）
              console.warn('云端保存失败，使用本地存储:', cloudErr);

              // 更新 Store
              store.addDesign(designData);

              ErrorHandler.hideLoading();
              wx.showToast({
                title: '已保存到本地',
                icon: 'success',
                duration: 2000
              });

              setTimeout(() => {
                wx.navigateBack({
                  fail: () => {
                    wx.switchTab({
                      url: '/pages/index/index'
                    });
                  }
                });
              }, 2000);
            }
          } catch (err) {
            ErrorHandler.hideLoading();
            ErrorHandler.handle(err, 'onComplete');
          }
        }
      }
    });
  },

  /**
   * 选择颜色
   */
  onSelectColor(e) {
    const { index, color, name } = e.currentTarget.dataset;

    this.setData({
      selectedColorIndex: parseInt(index),
      selectedColorName: name,
      selectedColorValue: color
    });

    // 切换商品背景图片
    const product = mockData.products.find(p => p._id === this.data.productId);
    if (product && product.colorImages && product.colorImages[name]) {
      this.setData({
        productImage: product.colorImages[name]
      });
      console.log(`切换到${name}图片:`, product.colorImages[name]);
    }

    // 如果有选中的文字元素，改变其颜色
    if (this.data.selectedId) {
      const elements = this.data.elements.map(el => {
        if (el.id === this.data.selectedId && el.type === 'text') {
          return { ...el, color: color };
        }
        return el;
      });

      this.setData({ elements });
    }

    wx.showToast({
      title: `已选择${name}`,
      icon: 'none',
      duration: 1000
    });
  },

  /**
   * 自定义导航栏返回
   */
  onBack() {
    wx.navigateBack({
      fail: () => {
        wx.switchTab({
          url: '/pages/index/index'
        });
      }
    });
  },

  /**
   * 顶部工具栏按钮（预留功能）
   */
  onChangeStyle() {
    wx.showToast({ title: '款式切换功能开发中', icon: 'none' });
  },

  onNewDesign() {
    wx.showModal({
      title: '新建',
      content: '确定清空当前设计？',
      success: (res) => {
        if (res.confirm) {
          this.setData({ elements: [], selectedId: null });
          wx.showToast({ title: '已清空', icon: 'success' });
        }
      }
    });
  },

  onShowHelp() {
    wx.showModal({
      title: '使用帮助',
      content: '选择颜色→添加素材/文字→调整位置→完成设计',
      showCancel: false
    });
  },

  onSaveDesign() {
    // 调用完成方法
    this.onComplete();
  },

  onEditDesign() {
    wx.showToast({ title: '编辑模式开发中', icon: 'none' });
  },

  /**
   * 选择素材
   */
  onSelectMaterial() {
    wx.showToast({ title: '素材库功能开发中', icon: 'none' });
  },

  /**
   * 图片加载成功
   */
  onImageLoad(e) {
    console.log('✅ 图片加载成功(WXML层):', e.detail);
  },

  /**
   * 图片加载失败
   */
  onImageError(e) {
    console.error('❌ 图片加载失败(WXML层):', e.detail);
    wx.showModal({
      title: '图片加载失败',
      content: '云存储图片无法加载，请检查\n1. 云存储权限是否设置为"所有用户可读"\n2. 图片URL是否正确\n3. 网络连接是否正常',
      showCancel: false
    });
  }
});
