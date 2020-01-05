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
import io.ktor.client.request.get
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.logging.HttpLoggingInterceptor
import okhttp3.logging.HttpLoggingInterceptor.Level.BODY
import okhttp3.logging.HttpLoggingInterceptor.Level.NONE
import ru.beryukhov.common.Post
import ru.beryukhov.common.Result
import ru.beryukhov.common.User

class MainActivity : AppCompatActivity() {

    private val SERVER_URL = "http://10.0.2.2:8080"

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

                client.makeRequest("get<Result.Success<List<Post>>(\"$SERVER_URL/post\")") {
                    get<Result.Success<List<Post>>>("$SERVER_URL/post")
                }

                //todo other requests

                //check error
                client.makeRequest("get<Result.Success<List<User>>>(\"$SERVER_URL/error\")") {
                    get<Result.Success<List<User>>>("$SERVER_URL/error")
                }

                client.close()
            }
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

    private suspend fun log(s: String) {
        withContext(Dispatchers.Main) {
            val textView: TextView = findViewById(R.id.textView)
            textView.text = "${textView.text}\n$s"
        }
    }

}
