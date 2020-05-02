package ru.beryukhov.remote_domain

import RN
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

@ExperimentalCoroutinesApi
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        RN(applicationContext).observeNetworkConnectivity().onEach {
            Log.i("MainActivity", "InternetConnectivity changed on $it")
        }.launchIn(CoroutineScope(Dispatchers.Default))
    }

}