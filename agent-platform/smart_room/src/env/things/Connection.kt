package things

import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import ru.gildor.coroutines.okhttp.await

class Connection {
    private val client = OkHttpClient()


    suspend fun getFromUrlAsync(url: String) : Response {
        return getFromUrl(url).await()
    }

    suspend fun postOnUrlAsync(url: String, params: String = "") : Response {
        return postOnUrl(url, params).await()
    }

    suspend fun putOnUrlAsync(url: String, params: String = "") : Response {
        return putOnUrl(url, params).await()
    }


    fun getFromUrl(url: String) : Call {
        return executeOnUrl("GET", url)
    }

    fun postOnUrl(url: String, params: String = "") : Call {
        return executeOnUrl("POST", url, params)
    }

    fun putOnUrl(url: String, params: String = "") : Call {
        return executeOnUrl("PUT", url, params)
    }


    private suspend fun executeOnUrlAsync(method: String, url: String, params: String = "") : Response {
        return executeOnUrl(method, url, params).await()
    }

    private fun executeOnUrl(method: String, url: String, params: String = "") : Call {
        return if (method == "GET") {
            client.newCall(
                Request.Builder()
                .url(url)
                .build()
            )
        } else {
            client.newCall(
                Request.Builder()
                .url(url)
                .addHeader("Content-Type", "application/json")
                .method(method, params.toRequestBody("application/json".toMediaType()))
                .build()
            )
        }


        /*val request = Request.Builder().url(url)

        if (method != "GET") {
            request.addHeader("Content-Type", "application/json")
            request.method(method, params.toRequestBody())
        }*/

        //return client.newCall(request.build())
    }
}