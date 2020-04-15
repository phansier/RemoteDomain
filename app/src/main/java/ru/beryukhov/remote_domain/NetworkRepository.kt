package ru.beryukhov.remote_domain

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import ru.beryukhov.client_lib.http.ClientApi
import ru.beryukhov.client_lib.http.HttpClientRepositoryImpl
import ru.beryukhov.common.model.Entity
import ru.beryukhov.common.model.Post
import ru.beryukhov.common.model.User

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
        val httpClientRepository = HttpClientRepositoryImpl(SERVER_URL, BuildConfig.DEBUG, log)
        httpClientRepository.addClientApi(
            User::class,
            ClientUserApi(httpClientRepository.httpClient, SERVER_URL, log) as ClientApi<in Entity>
        )
        httpClientRepository.addClientApi(
            Post::class,
            ClientPostApi(httpClientRepository.httpClient, SERVER_URL, log) as ClientApi<in Entity>
        )

        val clientUserApi = httpClientRepository.getClientApi(User::class)!!
        val clientPostApi = httpClientRepository.getClientApi(Post::class)!!
        /*clientPostApi.testPosts(log)
        clientUserApi.testUsers(log)*/
        with(clientPostApi){
            get("post")
            create(Post("0", "1", "New post"), "post")
            get("post")
            delete(Post("-1", "-1", "Test Post //Todo Remove"), "post")
            get("post")
        }
        with(clientUserApi){
            get("user")
            create(User("0", "New user"),"user")
            get("user")
            delete(User("-1", "Test Testov //Todo Remove"),"user")
            get("user")
        }
    }
}


/*object NetworkRepository {

    suspend fun ClientApi<in Entity>.testPosts(log: suspend (String) -> Unit) {
        get("post")
        create(Post("0", "1", "New post"), "post")
        get("post")
        delete(Post("-1", "-1", "Test Post //Todo Remove"), "post")
        get("post")
    }

    suspend fun ClientApi<in Entity>.testUsers(log: suspend (String) -> Unit) {

        get("user")
        create(User("0","New user"),"user")
        get("user")
        delete(User("-1", "Test Testov //Todo Remove"),"user")
        get("user")
    }

}*/

