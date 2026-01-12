// 云函数入口文件
const cloud = require('wx-server-sdk')
cloud.init({ env: cloud.DYNAMIC_CURRENT_ENV })
const db = cloud.database()

// 云函数入口函数
exports.main = async (event, context) => {
  const { wxContext } = cloud.getWXContext()
  const { designId, productId, productName, canvas, thumbnail } = event

  console.log('saveDesign 云函数被调用', event)

  try {
    const designData = {
      _openid: wxContext.OPENID, // 自动获取用户openid
      productId,
      productName,
      canvas,
      thumbnail,
      updateTime: new Date()
    }

    let result

    if (designId) {
      // 更新设计
      result = await db.collection('designs').doc(designId).update({
        data: designData
      })
      console.log('设计更新成功', result)
    } else {
      // 新建设计
      designData.createTime = new Date()
      result = await db.collection('designs').add({
        data: designData
      })
      console.log('设计创建成功', result)
    }

    return {
      code: 200,
      data: {
        designId: result._id || designId
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
