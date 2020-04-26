package ru.beryukhov.client_lib.http

import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.features.json.GsonSerializer
import io.ktor.client.features.json.JsonFeature
import okhttp3.logging.HttpLoggingInterceptor

actual fun createHttpClient(logRequests: Boolean): HttpClient {
    val interceptor = HttpLoggingInterceptor().apply {
        level = if (logRequests)
            HttpLoggingInterceptor.Level.BODY
        else
            HttpLoggingInterceptor.Level.NONE
    }
    return HttpClient(OkHttp) {
        install(JsonFeature) {
            serializer = GsonSerializer {
                serializeNulls()
                disableHtmlEscaping()
            }
        }
        engine {
            addInterceptor(interceptor)
        }

    }
}