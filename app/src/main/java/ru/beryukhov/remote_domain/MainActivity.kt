package ru.beryukhov.remote_domain

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
//import io.ktor.client.features.json.GsonSerializer
import io.ktor.client.features.json.JsonFeature
import io.ktor.client.features.json.serializer.KotlinxSerializer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.logging.HttpLoggingInterceptor
import okhttp3.logging.HttpLoggingInterceptor.Level.BODY
import okhttp3.logging.HttpLoggingInterceptor.Level.NONE
import ru.beryukhov.remote_domain.NetworkRepository.testError
import ru.beryukhov.remote_domain.NetworkRepository.testPosts
import ru.beryukhov.remote_domain.NetworkRepository.testUsers

class MainActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setupNetworkButton()
        setupDatabaseButton()
    }

    private fun setupNetworkButton() {
        val button: Button = findViewById(R.id.button)
        button.setOnClickListener {
            GlobalScope.launch {
                val interceptor = HttpLoggingInterceptor()
                interceptor.level = if (BuildConfig.DEBUG) BODY else NONE
                val client = HttpClient(OkHttp) {
                    install(JsonFeature) {
                        serializer = KotlinxSerializer()
                    }
                    engine {
                        addInterceptor(interceptor)
                    }

                }

                client.testPosts(::log)
                client.testUsers(::log)
                client.testError(::log)

                client.close()
            }
        }
    }

    private fun setupDatabaseButton() {
        testDb(this, ::log)
    }


    private suspend fun log(s: String) {
        withContext(Dispatchers.Main) {
            val textView: TextView = findViewById(R.id.textView)
            textView.text = "${textView.text}\n$s"
        }
    }

}
