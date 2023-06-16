package com.crazy_coder.everfit.data.remote.api.middleware

import androidx.annotation.NonNull
import androidx.core.net.toUri
import com.crazy_coder.everfit.data.local.datastore.PreferenceDataStore
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import java.io.IOException
import javax.inject.Inject

@DelicateCoroutinesApi
class DefaultInterceptor @Inject constructor(
    private val preferenceDataStore: PreferenceDataStore
) : Interceptor {

    @Throws(IOException::class)
    override fun intercept(@NonNull chain: Interceptor.Chain): Response {
        val builder = initializeHeader(chain)
        val request = builder.build()
        return chain.proceed(request)
    }

    private fun initializeHeader(chain: Interceptor.Chain): Request.Builder {
        val originRequest = chain.request()

        val token = runBlocking { preferenceDataStore.token.first() }
        return originRequest.newBuilder()
            .header("Accept", "application/json")
            .addHeader("Cache-Control", "no-cache")
            .addHeader("Cache-Control", "no-store")
            .apply {
                changBaseUrl(originRequest)
                addTokenInApp(token)

            }
            .method(originRequest.method, originRequest.body)

    }

    private fun Request.Builder.addTokenInApp(token: String) {
        if (token.isNotEmpty()) {
            addHeader(KEY_TOKEN, TOKEN_TYPE + token)
        }
    }

    private fun Request.Builder.changBaseUrl(originRequest: Request) {
        val baseUrl = runBlocking { preferenceDataStore.baseUrl.first() }
        if (baseUrl.isNotEmpty()) {
            runCatching {
                val newUrl = originRequest.url.newBuilder()
                    .scheme(if (baseUrl.contains("https")) "https" else "http")
                    .host(baseUrl.toUri().host ?: return)
                    .build()
                url(newUrl)
            }
        }
    }

    companion object {
        private const val TOKEN_TYPE = "Bearer "
        private const val KEY_TOKEN = "Authorization"
    }
}
