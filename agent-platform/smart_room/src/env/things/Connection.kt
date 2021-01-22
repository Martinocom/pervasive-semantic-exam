package things

import okhttp3.*
import okhttp3.RequestBody.Companion.toRequestBody
import ru.gildor.coroutines.okhttp.await

class Connection {
    private val client = OkHttpClient()

    suspend fun getFromUrlAsync(url: String) : Response {
        return getFromUrl(url).await()
    }

    suspend fun postOnUrlAsync(url: String, params: String) : Response {
        return executeOnUrl(url, params).await()
    }

    fun getFromUrl(url: String) : Call {
        return executeOnUrl("GET", url)
    }

    fun postOnUrl(url: String, params: String = "") : Call {
        return executeOnUrl("POST", url, params)
    }


    private suspend fun executeOnUrlAsync(method: String, url: String, params: String = "") : Response {
        return executeOnUrl(method, url, params).await()
    }

    private fun executeOnUrl(method: String, url: String, params: String = "") : Call {
        val request = Request.Builder().url(url)

        if (method != "GET") {
            request.addHeader("Content-Type", "application/json")
            request.method(method, params.toRequestBody())
        }

        return client.newCall(request.build())
    }
}