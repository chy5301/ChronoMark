package io.github.chy5301.chronomark.data.network

import kotlinx.coroutines.test.runTest
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.Response
import okhttp3.ResponseBody
import okio.Buffer
import okio.BufferedSource
import okio.ForwardingSource
import okio.buffer
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * ReleaseApiService 测试：用拦截器伪造服务端响应，验证所有返回路径都会关闭响应体，
 * 否则 OkHttp 连接无法回收（日志中出现 "A connection was leaked"）
 */
class ReleaseApiServiceTest {

    private class TrackingBody(private val content: String) : ResponseBody() {
        var closed = false
        private val trackedSource: BufferedSource by lazy {
            object : ForwardingSource(Buffer().writeUtf8(content)) {
                override fun close() {
                    closed = true
                    super.close()
                }
            }.buffer()
        }

        override fun contentType() = "application/json".toMediaType()
        override fun contentLength() = -1L
        override fun source() = trackedSource
    }

    private fun serviceRespondingWith(code: Int, body: TrackingBody): ReleaseApiService {
        val client = OkHttpClient.Builder()
            .addInterceptor(Interceptor { chain ->
                Response.Builder()
                    .request(chain.request())
                    .protocol(Protocol.HTTP_1_1)
                    .code(code)
                    .message("status $code")
                    .body(body)
                    .build()
            })
            .build()
        return ReleaseApiService(client)
    }

    @Test
    fun `fetchFromGitHub - error status - closes response body`() = runTest {
        val body = TrackingBody("""{"message":"API rate limit exceeded"}""")

        val result = serviceRespondingWith(403, body).fetchFromGitHub()

        assertTrue(result.isFailure)
        assertTrue("response body must be closed", body.closed)
    }

    @Test
    fun `fetchFromGitee - error status - closes response body`() = runTest {
        val body = TrackingBody("""{"message":"Not Found"}""")

        val result = serviceRespondingWith(404, body).fetchFromGitee()

        assertTrue(result.isFailure)
        assertTrue("response body must be closed", body.closed)
    }

    @Test
    fun `fetchFromGitHub - malformed json - closes response body`() = runTest {
        val body = TrackingBody("not json")

        val result = serviceRespondingWith(200, body).fetchFromGitHub()

        assertTrue(result.isFailure)
        assertTrue("response body must be closed", body.closed)
    }
}
