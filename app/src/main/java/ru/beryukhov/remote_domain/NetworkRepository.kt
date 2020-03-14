package ru.beryukhov.remote_domain

import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.features.json.GsonSerializer
import io.ktor.client.features.json.JsonFeature
import io.ktor.client.request.get
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import okhttp3.logging.HttpLoggingInterceptor
import ru.beryukhov.common.model.Post
import ru.beryukhov.common.model.Result
import ru.beryukhov.common.model.User
import ru.beryukhov.remote_domain.NetworkRepository.testError
import ru.beryukhov.remote_domain.NetworkRepository.testPosts
import ru.beryukhov.remote_domain.NetworkRepository.testUsers
import ru.beryukhov.remote_domain.http.BaseHttpClient
import ru.beryukhov.remote_domain.http.ClientPostApi
import ru.beryukhov.remote_domain.http.ClientUserApi


private const val BASE_URL = "evening-forest-47787.herokuapp.com"
const val SOCKET_URL = "ws://$BASE_URL"
const val SERVER_URL = "http://$BASE_URL"

/**
 * Created by Andrey Beryukhov
 */
fun testHttp(log: suspend (String) -> Unit) {
    GlobalScope.launch {
        val interceptor = HttpLoggingInterceptor()
        interceptor.level =
            if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY else HttpLoggingInterceptor.Level.NONE
        val client = HttpClient(OkHttp) {
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

        client.testPosts(log)
        client.testUsers(log)
        client.testError(log)

        client.close()
    }
}


object NetworkRepository : BaseHttpClient() {

    suspend fun HttpClient.testPosts(log: suspend (String) -> Unit) {
        val clientPostApi =
            ClientPostApi(this, SERVER_URL, log)

        with(clientPostApi) {
            getPosts()
            createPost("1", "New post")
            getPosts()
            deletePost(Post("-1", "-1", "Test Post //Todo Remove"))
            getPosts()
        }
    }

    suspend fun HttpClient.testUsers(log: suspend (String) -> Unit) {
        val clientPostApi =
            ClientUserApi(this, SERVER_URL, log)

        with(clientPostApi) {
            getUsers()
            createUser("New user")
            getUsers()
            deleteUser(User("-1", "Test Testov //Todo Remove"))
            getUsers()
        }
    }

    suspend fun HttpClient.testError(log: suspend (String) -> Unit) {
        makeRequest("get<Result.Success<List<User>>>(\"$SERVER_URL/error\")", log) {
            get<Result.Success<List<User>>>("$SERVER_URL/error")
        }
    }

}

