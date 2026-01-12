// 云函数入口文件
const cloud = require('wx-server-sdk')
cloud.init({ env: cloud.DYNAMIC_CURRENT_ENV })
const db = cloud.database()

// 云函数入口函数
exports.main = async (event, context) => {
  const { wxContext } = cloud.getWXContext()
  const { page = 1, limit = 10 } = event

  console.log('getMyDesigns 云函数被调用', event)

  try {
    const result = await db.collection('designs')
      .where({ _openid: wxContext.OPENID })
      .orderBy('createTime', 'desc')
      .skip((page - 1) * limit)
      .limit(limit)
      .get()

    const countResult = await db.collection('designs')
      .where({ _openid: wxContext.OPENID })
      .count()

    return {
      code: 200,
      data: {
        list: result.data,
        total: countResult.total,
        page,
        limit
      }
    }
  } catch (err) {
    console.error('getMyDesigns 云函数执行失败', err)
    return {
      code: 500,
      error: err.message
    }
  }
}
