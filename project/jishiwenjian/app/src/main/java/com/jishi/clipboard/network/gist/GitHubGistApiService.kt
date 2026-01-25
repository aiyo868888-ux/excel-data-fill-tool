package com.jishi.clipboard.network.gist

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import timber.log.Timber

/**
 * GitHub Gist API 实现
 */
class GitHubGistApiService(
    private val authToken: String,
    private val httpClient: HttpClient
) : GistApiService {

    companion object {
        private const val BASE_URL = "https://api.github.com"
        private const val GISTS_ENDPOINT = "/gists"
    }

    override suspend fun createGist(request: GistRequest): GistResponse {
        Timber.d("创建 GitHub Gist")
        return httpClient.post("$BASE_URL$GISTS_ENDPOINT") {
            setGitHubAuthHeaders()
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }

    override suspend fun updateGist(id: String, request: GistRequest): GistResponse {
        Timber.d("更新 GitHub Gist: $id")
        return httpClient.patch("$BASE_URL$GISTS_ENDPOINT/$id") {
            setGitHubAuthHeaders()
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }

    override suspend fun getGist(id: String): GistResponse {
        Timber.d("获取 GitHub Gist: $id")
        return httpClient.get("$BASE_URL$GISTS_ENDPOINT/$id") {
            setGitHubAuthHeaders()
        }.body()
    }

    override suspend fun deleteGist(id: String): Boolean {
        Timber.d("删除 GitHub Gist: $id")
        try {
            val response: HttpResponse = httpClient.delete("$BASE_URL$GISTS_ENDPOINT/$id") {
                setGitHubAuthHeaders()
            }
            return response.status == HttpStatusCode.NoContent
        } catch (e: Exception) {
            Timber.e(e, "删除 Gist 失败")
            return false
        }
    }

    override suspend fun listUserGists(page: Int, perPage: Int): List<GistResponse> {
        Timber.d("列出用户 Gists: page=$page, perPage=$perPage")
        return httpClient.get("$BASE_URL$GISTS_ENDPOINT") {
            setGitHubAuthHeaders()
            parameter("page", page)
            parameter("per_page", perPage)
        }.body()
    }

    override suspend fun testConnection(): Boolean {
        return try {
            val response: HttpResponse = httpClient.get("$BASE_URL/user") {
                setGitHubAuthHeaders()
            }
            response.status == HttpStatusCode.OK
        } catch (e: Exception) {
            Timber.e(e, "GitHub 连接测试失败")
            false
        }
    }

    /**
     * 设置 GitHub API 认证头
     */
    private fun HttpRequestBuilder.setGitHubAuthHeaders() {
        headers {
            append("Authorization", "token $authToken")
            append("Accept", "application/vnd.github.v3+json")
            append("X-GitHub-Api-Version", "2022-11-28")
        }
    }
}
