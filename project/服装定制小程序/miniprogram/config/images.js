/**
 * 图片CDN配置
 * 用于解决免费版云存储权限问题
 * 将图片托管到公共CDN，无需权限验证
 */

module.exports = {
  // CDN 基础路径
  cdnBaseURL: 'https://your-cdn-domain.com',

  // 图片路径映射表
  imagePaths: {
    // 轮播图
    'banners/ScreenShot_2026-01-12_102502_938.png': 'banners/banner1.png',
    'banners/ScreenShot_2026-01-12_102530_633.png': 'banners/banner2.png',
    'banners/ScreenShot_2026-01-12_102550_165.png': 'banners/banner3.png',

    // 分类图标
    'categories/2025春夏.png': 'categories/spring-2025.png',
    'categories/2025秋冬.png': 'categories/autumn-2025.png',
    'categories/翻领短袖.png': 'categories/polo-shirt.png',
    'categories/圆领T恤.png': 'categories/t-shirt.png',
    'categories/卫衣.png': 'categories/hoodie.png',
    'categories/棒球服.png': 'categories/baseball-jacket.png',
    'categories/马甲.png': 'categories/vest.png',
  },

  /**
   * 将云存储路径转换为CDN路径
   * @param {string} cloudPath - 云存储路径
   * @returns {string} CDN URL
   */
  toCDNURL(cloudPath) {
    if (!cloudPath || !cloudPath.startsWith('cloud://')) {
      return cloudPath // 不是云存储地址，直接返回
    }

    // 提取路径部分
    const pathParts = cloudPath.split('/')
    const fileName = pathParts[pathParts.length - 1]
    const folder = pathParts[pathParts.length - 2]
    const relativePath = `${folder}/${fileName}`

    // 查找映射
    const cdnPath = this.imagePaths[relativePath]
    if (cdnPath) {
      return `${this.cdnBaseURL}/${cdnPath}`
    }

    // 没有映射，返回原地址
    return cloudPath
  },

  /**
   * 批量转换
   * @param {Array<string>} cloudPaths - 云存储路径数组
   * @returns {Array<string>} CDN URL数组
   */
  batchToCDNURL(cloudPaths) {
    return cloudPaths.map(path => this.toCDNURL(path))
  }
}
