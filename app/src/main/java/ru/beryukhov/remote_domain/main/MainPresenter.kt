package ru.beryukhov.remote_domain.main

import android.app.Application
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import moxy.MvpPresenter
import moxy.presenterScope
import ru.beryukhov.client_lib.RemoteDomainClient
import ru.beryukhov.common.model.Entity
import ru.beryukhov.remote_domain.TheApplication

class MainPresenter(private val applicationContext: Application) : MvpPresenter<MainView>() {

    private val remoteDomainClient: RemoteDomainClient by lazy {
        (applicationContext as TheApplication).theInteractor.remoteDomainClient
    }


    override fun onFirstViewAttach() {
        // Coroutine that will be canceled when presenter is destroyed
        presenterScope.launch {
            remoteDomainClient.getEntityFlow()
                .onEach(::updateEntityUI)
                .launchIn(CoroutineScope(Dispatchers.Default))
        }
    }

    private suspend fun updateEntityUI(entity: Entity) {
        Log.d("MainPresenter", "onEach: [$entity]")
        withContext(Dispatchers.Main) {
            viewState.updateEntityUI(entity)
        }
    }
}