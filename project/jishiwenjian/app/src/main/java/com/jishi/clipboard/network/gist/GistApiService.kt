package com.jishi.clipboard.network.gist

/**
 * 统一的 Gist API 接口
 */
interface GistApiService {
    /**
     * 创建新的 Gist
     */
    suspend fun createGist(request: GistRequest): GistResponse

    /**
     * 更新已存在的 Gist
     */
    suspend fun updateGist(id: String, request: GistRequest): GistResponse

    /**
     * 获取 Gist 详情
     */
    suspend fun getGist(id: String): GistResponse

    /**
     * 删除 Gist
     */
    suspend fun deleteGist(id: String): Boolean

    /**
     * 列出用户的所有 Gists
     */
    suspend fun listUserGists(page: Int = 1, perPage: Int = 30): List<GistResponse>

    /**
     * 测试连接是否有效
     */
    suspend fun testConnection(): Boolean
}
