package ru.beryukhov.remote_domain.main

import android.app.Application
import android.util.Log
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import moxy.MvpPresenter
import moxy.presenterScope
import ru.beryukhov.client_lib.RemoteDomainClient
import ru.beryukhov.common.model.Entity
import ru.beryukhov.remote_domain.BuildConfig
import ru.beryukhov.remote_domain.SERVER_URL
import ru.beryukhov.remote_domain.SOCKET_URL

@ObsoleteCoroutinesApi
@ExperimentalCoroutinesApi
class MainPresenter(private val applicationContext: Application) : MvpPresenter<MainView>() {
    private lateinit var remoteDomainClient: RemoteDomainClient

    override fun onFirstViewAttach() {
        presenterScope.launch {
            // Coroutine that will be canceled when presenter is destroyed
            remoteDomainClient =
                RemoteDomainClient(applicationContext)
            remoteDomainClient.firstInit()
            remoteDomainClient.getEntitiesFlow().onEach(::updateEntityUI)
                .launchIn(CoroutineScope(Dispatchers.Default))

            remoteDomainClient.init(
                SERVER_URL,
                SOCKET_URL,
                BuildConfig.DEBUG
            )
        }
    }

    private suspend fun updateEntityUI(entities: List<Entity>){
        Log.d("MainActivity", "onEach: [$entities]")
        withContext(Dispatchers.Main) {
            viewState.updateEntityUI(entities)
        }
    }
}