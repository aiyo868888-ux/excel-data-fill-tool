// 云函数入口文件
const cloud = require('wx-server-sdk')
cloud.init({ env: cloud.DYNAMIC_CURRENT_ENV })
const db = cloud.database()

// 云函数入口函数
exports.main = async (event, context) => {
  const { wxContext } = cloud.getWXContext()
  const { designId, productId, productName, productImage, elements } = event

  console.log('saveDesign 云函数被调用', event)

  try {
    const designData = {
      _openid: wxContext.OPENID, // 自动获取用户openid
      productId,
      productName,
      productImage,
      elements, // 设计元素数组（位置、大小、旋转等）
      updateTime: new Date().toISOString()
    }

    let result

    if (designId) {
      // 更新设计（使用 designId 字段查询）
      const { data: existing } = await db.collection('designs')
        .where({ designId })
        .get()

      if (existing.length > 0) {
        result = await db.collection('designs').doc(existing[0]._id).update({
          data: designData
        })
        console.log('设计更新成功', result)
      } else {
        // 不存在则创建
        designData.designId = designId
        designData.createTime = designData.updateTime
        result = await db.collection('designs').add({
          data: designData
        })
        console.log('设计创建成功', result)
      }
    } else {
      // 新建设计
      const newDesignId = `design_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`
      designData.designId = newDesignId
      designData.createTime = designData.updateTime
      result = await db.collection('designs').add({
        data: designData
      })
      console.log('设计创建成功', result)
    }

    return {
      code: 200,
      data: {
        designId: designId || designData.designId
      }
    }
  } catch (err) {
    console.error('saveDesign 云函数执行失败', err)
    return {
      code: 500,
      error: err.message
    }
  }
}
