package ru.beryukhov.remote_domain.http

import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.features.json.GsonSerializer
import io.ktor.client.features.json.JsonFeature
import okhttp3.logging.HttpLoggingInterceptor
import ru.beryukhov.remote_domain.BuildConfig
import ru.beryukhov.remote_domain.SERVER_URL

class HttpClientRepositoryImpl(log: suspend (String) -> Unit) :
    HttpClientRepository {
    private val httpClient: HttpClient by lazy {
        val interceptor = HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG)
                HttpLoggingInterceptor.Level.BODY
            else
                HttpLoggingInterceptor.Level.NONE
        }
        HttpClient(OkHttp) {
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

    override val clientUserApi: ClientUserApi by lazy {
        ClientUserApi(
            httpClient,
            SERVER_URL,
            log
        )
    }

    override val clientPostApi: ClientPostApi by lazy {
        ClientPostApi(
            httpClient,
            SERVER_URL,
            log
        )
    }
}