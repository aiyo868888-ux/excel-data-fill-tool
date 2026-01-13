// pages/profile/profile.js
const cloudService = require('../../utils/cloud.js');

Page({
  data: {
    userInfo: {},
    guideEnabled: false
  },

  onLoad() {
    this.getUserInfo();
  },

  onShow() {
    // 每次显示页面时刷新用户信息
    this.getUserInfo();
  },

  /**
   * 获取用户信息
   */
  async getUserInfo() {
    try {
      // 获取微信用户信息
      const res = await wx.getUserProfile({
        desc: '用于完善用户资料'
      });

      this.setData({
        userInfo: res.userInfo
      });
    } catch (err) {
      console.log('获取用户信息失败', err);
      // 使用默认信息
      this.setData({
        userInfo: {
          nickName: '微信用户',
          avatarUrl: ''
        }
      });
    }
  },

  /**
   * 更换头像
   */
  onChangeAvatar() {
    wx.chooseImage({
      count: 1,
      sizeType: ['compressed'],
      sourceType: ['album', 'camera'],
      success: async (res) => {
        const filePath = res.tempFilePaths[0];
        cloudService.showLoading('上传中...');

        try {
          // 1. 验证文件大小（限制2MB）
          const fileInfo = await wx.getFileInfo({
            filePath
          });

          if (fileInfo.size > 2 * 1024 * 1024) {
            cloudService.hideLoading();
            wx.showToast({
              title: '图片不能超过2MB',
              icon: 'none'
            });
            return;
          }

          // 2. 生成安全的云存储路径
          // 使用用户ID（openid）作为路径前缀，避免冲突
          const userInfo = wx.getStorageSync('userInfo') || {};
          const userId = userInfo.openid || 'anonymous';
          const timestamp = Date.now();
          const randomStr = Math.random().toString(36).substring(2, 11); // 使用 substring 替代 substr
          const cloudPath = `avatars/${userId}_${timestamp}_${randomStr}.png`;

          // 3. 上传到云存储
          const fileID = await cloudService.uploadFile(cloudPath, filePath);

          // 4. 更新用户信息
          this.setData({
            'userInfo.avatarUrl': fileID
          });

          // 5. 保存到本地存储
          wx.setStorageSync('userInfo', {
            ...this.data.userInfo,
            avatarUrl: fileID
          });

          cloudService.showSuccess('更新成功');
        } catch (err) {
          console.error('上传失败', err);
          cloudService.showError('上传失败');
        } finally {
          cloudService.hideLoading();
        }
      }
    });
  },

  /**
   * 我的设计
   */
  onMyDesigns() {
    wx.navigateTo({
      url: '/pages/my-designs/my-designs'
    });
  },

  /**
   * 个人资料
   */
  onProfile() {
    wx.showToast({ title: '个人资料功能开发中', icon: 'none' });
  },

  /**
   * 原创精品
   */
  onOriginal() {
    wx.showToast({ title: '原创精品功能开发中', icon: 'none' });
  },

  /**
   * 我的收藏
   */
  onFavorite() {
    wx.showToast({ title: '收藏功能开发中', icon: 'none' });
  },

  /**
   * 使用手册
   */
  onHelp() {
    wx.showToast({ title: '使用手册功能开发中', icon: 'none' });
  },

  /**
   * 向导设置切换
   */
  onGuideChange(e) {
    this.setData({
      guideEnabled: e.detail.value
    });
    wx.setStorageSync('guideEnabled', e.detail.value);
  }
});
