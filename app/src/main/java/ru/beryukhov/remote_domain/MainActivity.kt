package ru.beryukhov.remote_domain

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.features.ResponseException
import io.ktor.client.features.json.GsonSerializer
import io.ktor.client.features.json.JsonFeature
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.post
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.logging.HttpLoggingInterceptor
import okhttp3.logging.HttpLoggingInterceptor.Level.BODY
import okhttp3.logging.HttpLoggingInterceptor.Level.NONE
import ru.beryukhov.common.CompletableResult
import ru.beryukhov.common.Post
import ru.beryukhov.common.Result
import ru.beryukhov.common.User

class MainActivity : AppCompatActivity() {

    private val SERVER_URL = "http://10.0.2.2:8080"
    private val HEADER_CONTENT_TYPE = "Content-Type"
    private val HEADER_JSON = "application/json"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setupButton()
    }

    private fun setupButton() {
        val button: Button = findViewById(R.id.button)
        button.setOnClickListener {
            GlobalScope.launch {
                val interceptor = HttpLoggingInterceptor()
                interceptor.level = if (BuildConfig.DEBUG) BODY else NONE
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

                client.testPosts()

                client.testUsers()

                //todo other requests

                //check error
                client.makeRequest("get<Result.Success<List<User>>>(\"$SERVER_URL/error\")") {
                    get<Result.Success<List<User>>>("$SERVER_URL/error")
                }

                client.close()
            }
        }
    }

    private suspend fun HttpClient.testPosts() {
        makeRequest("get<Result.Success<List<Post>>(\"$SERVER_URL/post\")") {
            get<Result.Success<List<Post>>>("$SERVER_URL/post")
        }

        makeRequest("post<Result.Success<Post>>(\"$SERVER_URL/post\")") {
            post<Result.Success<Post>>("$SERVER_URL/post") {
                body = Post("0", "1", "New post")
                headers.append(HEADER_CONTENT_TYPE, HEADER_JSON)
            }
        }

        makeRequest("get<Result.Success<List<Post>>(\"$SERVER_URL/post\")") {
            get<Result.Success<List<Post>>>("$SERVER_URL/post")
        }

        makeCompletableRequest("delete(\"$SERVER_URL/post\")") {
            delete("$SERVER_URL/post") {
                body = Post("-1", "-1", "Test Post //Todo Remove")
                headers.append(HEADER_CONTENT_TYPE, HEADER_JSON)
            }
        }

        makeRequest("get<Result.Success<List<Post>>(\"$SERVER_URL/post\")") {
            get<Result.Success<List<Post>>>("$SERVER_URL/post")
        }
    }

    private suspend fun HttpClient.testUsers() {
        makeRequest("get<Result.Success<List<User>>(\"$SERVER_URL/user\")") {
            get<Result.Success<List<User>>>("$SERVER_URL/user")
        }

        makeRequest("post<Result.Success<User>>(\"$SERVER_URL/user\")") {
            post<Result.Success<User>>("$SERVER_URL/user") {
                body = User("0", "New user")
                headers.append(HEADER_CONTENT_TYPE, HEADER_JSON)
            }
        }

        makeRequest("get<Result.Success<List<User>>(\"$SERVER_URL/user\")") {
            get<Result.Success<List<User>>>("$SERVER_URL/user")
        }

        makeCompletableRequest("delete(\"$SERVER_URL/user\")") {
            delete("$SERVER_URL/user") {
                body = User("-1", "Test Testov //Todo Remove")
                headers.append(HEADER_CONTENT_TYPE, HEADER_JSON)
            }
        }

        makeRequest("get<Result.Success<List<User>>(\"$SERVER_URL/user\")") {
            get<Result.Success<List<User>>>("$SERVER_URL/user")
        }
    }

    private suspend inline fun <reified T> HttpClient.makeRequest(
        logMessage: String,
        block: HttpClient.() -> Result.Success<T>
    ) {
        log("\nSending request: $logMessage")
        try {

            val result = block.invoke(this)
            log(
                "Received result: ${result.value}"
            )
        } catch (e: ResponseException) {
            log(
                "Received error response: status = ${e.response.status}"
            )
        }
    }

    private suspend inline fun HttpClient.makeCompletableRequest(
        logMessage: String,
        block: HttpClient.() -> CompletableResult.Success
    ) {
        log("\nSending request: $logMessage")
        try {

            val result = block.invoke(this)
            log(
                "Received success result"
            )
        } catch (e: ResponseException) {
            log(
                "Received error response: status = ${e.response.status}"
            )
        }
    }

    private suspend fun log(s: String) {
        withContext(Dispatchers.Main) {
            val textView: TextView = findViewById(R.id.textView)
            textView.text = "${textView.text}\n$s"
        }
    }

}
