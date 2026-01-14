/**
 * 简化版图片加载器
 * 直接使用云函数获取临时链接
 */
const CLOUD_FUNCTION_NAME = 'getImageURL';

/**
 * 加载单个对象的单个图片字段
 * @param {Object} item - 包含图片字段的对象
 * @param {string} imageField - 图片字段名
 * @returns {Promise<Object>} 替换图片后的对象
 */
async function loadItemImage(item, imageField = 'image') {
  if (!item || !item[imageField]) {
    return item;
  }

  const imageUrl = item[imageField];

  // 如果是数组，跳过（由专门的方法处理）
  if (Array.isArray(imageUrl)) {
    return item;
  }

  if (!imageUrl.startsWith('cloud://')) {
    return item; // 不是云存储图片，直接返回
  }

  try {
    const result = await wx.cloud.callFunction({
      name: CLOUD_FUNCTION_NAME,
      data: { fileIds: [imageUrl] }
    });

    if (result.result && result.result.code === 200 && result.result.data[0]) {
      const tempURL = result.result.data[0].tempFileURL;
      if (tempURL) {
        return { ...item, [imageField]: tempURL };
      }
    }
  } catch (err) {
    console.warn(`[SimpleImageLoader] 图片加载失败:`, err);
  }

  return item; // 失败时返回原对象
}

/**
 * 批量加载对象的图片（支持数组）
 * @param {Array|Object} items - 对象数组或单个对象
 * @param {string} imageField - 图片字段名
 * @returns {Promise<Array|Object>} 替换图片后的对象
 */
async function loadImages(items, imageField = 'image') {
  if (!items) return items;

  // 处理数组
  if (Array.isArray(items)) {
    const promises = items.map(item => loadProductImages(item));
    return await Promise.all(promises);
  }

  // 处理单个对象（商品对象）
  return await loadProductImages(items);
}

/**
 * 加载商品详情（包括 images 数组）
 * @param {Object} product - 商品对象
 * @returns {Promise<Object>} 替换图片后的商品
 */
async function loadProductImages(product) {
  if (!product) return product;

  let loadedProduct = { ...product };

  // 加载 images 数组（商品主图）
  if (loadedProduct.images && Array.isArray(loadedProduct.images)) {
    try {
      const cloudImages = loadedProduct.images.filter(img => typeof img === 'string' && img.startsWith('cloud://'));

      if (cloudImages.length > 0) {
        const result = await wx.cloud.callFunction({
          name: CLOUD_FUNCTION_NAME,
          data: { fileIds: cloudImages }
        });

        if (result.result && result.result.code === 200 && result.result.data) {
          const tempURLs = result.result.data.map(f => f.tempFileURL || cloudImages[0]);

          loadedProduct = {
            ...loadedProduct,
            images: loadedProduct.images.map(img => {
              if (typeof img === 'string' && img.startsWith('cloud://')) {
                const index = cloudImages.indexOf(img);
                return tempURLs[index] || img;
              }
              return img;
            })
          };
        }
      }
    } catch (err) {
      console.warn('[SimpleImageLoader] 商品图片数组加载失败:', err);
    }
  }

  // 加载 detailImages 数组（商品详情图）
  if (loadedProduct.detailImages && Array.isArray(loadedProduct.detailImages)) {
    try {
      const cloudImages = loadedProduct.detailImages.filter(img => typeof img === 'string' && img.startsWith('cloud://'));

      if (cloudImages.length > 0) {
        const result = await wx.cloud.callFunction({
          name: CLOUD_FUNCTION_NAME,
          data: { fileIds: cloudImages }
        });

        if (result.result && result.result.code === 200 && result.result.data) {
          const tempURLs = result.result.data.map(f => f.tempFileURL || cloudImages[0]);

          loadedProduct = {
            ...loadedProduct,
            detailImages: loadedProduct.detailImages.map(img => {
              if (typeof img === 'string' && img.startsWith('cloud://')) {
                const index = cloudImages.indexOf(img);
                return tempURLs[index] || img;
              }
              return img;
            })
          };
        }
      }
    } catch (err) {
      console.warn('[SimpleImageLoader] 详情图数组加载失败:', err);
    }
  }

  return loadedProduct;
}

module.exports = {
  loadImages,
  loadProductImages
};
