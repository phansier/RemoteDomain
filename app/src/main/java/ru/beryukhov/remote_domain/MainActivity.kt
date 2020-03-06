package ru.beryukhov.remote_domain

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.consumeEach
import ru.beryukhov.common.model.Post
import ru.beryukhov.common.model.User
import ru.beryukhov.remote_domain.push.OkHttpPush

@ObsoleteCoroutinesApi
@ExperimentalCoroutinesApi
class MainActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setupNetworkButton()
        setupDatabaseButton()
        setupSocketButton()
    }

    private fun setupNetworkButton() {
        val button: Button = findViewById(R.id.button_http)
        button.setOnClickListener {
            testHttp(::log)
        }
    }

    private fun setupDatabaseButton() {
        val button: Button = findViewById(R.id.button_db)
        button.setOnClickListener {
            testDb(this, ::log)
        }
    }

    @SuppressLint("DefaultLocale")
    private fun setupSocketButton() {
        val broadcastChannel = BroadcastChannel<Any>(Channel.CONFLATED)

        val button: Button = findViewById(R.id.button_socket)
        val push = OkHttpPush()
        button.setOnClickListener {
            push.startReceive(socketUrl = SOCKET_URL, log = ::log) {
                //todo serialization to Json on Backend
                //todo change User and Post instanses by corresponding type flags
                if (it.toString().toLowerCase().contains("User")){
                    broadcastChannel.offer(User("", ""))
                }
                if (it.toString().toLowerCase().contains("post")){
                    broadcastChannel.offer(Post("", "", ""))
                }

            }
        }

        GlobalScope.launch {
            broadcastChannel.consumeEach {
                println("event got $it")
                when (it){
                    is User->{/*http<User>->db*/}
                    is Post->{/*http<Post>->db*/}
                }
            }
        }
    }


    private suspend fun log(s: String) {
        Log.i("MainActivity", s)
        /*withContext(Dispatchers.Main) {
            val textView: TextView = findViewById(R.id.textView)
            textView.text = "${textView.text}\n$s"
        }*/
    }

}
