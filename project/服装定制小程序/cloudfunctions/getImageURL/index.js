// 云函数：获取云存储图片临时链接
// 解决免费版云存储权限限制问题

const cloud = require('wx-server-sdk')
cloud.init({ env: cloud.DYNAMIC_CURRENT_ENV })

exports.main = async (event, context) => {
  const { fileIds } = event

  console.log('getImageURL 云函数被调用', event)

  if (!fileIds || fileIds.length === 0) {
    return {
      code: 400,
      error: 'fileIds 参数不能为空'
    }
  }

  try {
    const result = await cloud.getTempFileURL({
      fileList: fileIds.map(id => ({ fileID: id }))
    })

    console.log('获取临时链接成功:', result)

    return {
      code: 200,
      data: result.fileList
    }
  } catch (err) {
    console.error('获取临时链接失败', err)
    return {
      code: 500,
      error: err.message
    }
  }
}
