package ru.beryukhov.client_lib

import RN
import io.ktor.util.InternalAPI
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.flow.*
import ru.beryukhov.client_lib.db.DiffDao
import ru.beryukhov.client_lib.db.EntityDao
import ru.beryukhov.client_lib.http.ClientApi
import ru.beryukhov.client_lib.http.Credentials
import ru.beryukhov.client_lib.http.HttpClientRepositoryImpl
import ru.beryukhov.client_lib.push.push
import ru.beryukhov.common.model.Entity
import ru.beryukhov.common.model.Success
import ru.beryukhov.common.tree_diff.plus
import kotlin.jvm.Volatile

interface RemoteDomainClientApi {
    /**
     * Creates table in DB and removes old data if it was.
     */
    fun firstInit()

    /**
     * Opens WebSocket to receive data updates
     */
    fun init(serverUrl: String, socketUrl: String, logRequests: Boolean)

    /**
     * Returns stream of Entity changes
     */
    fun getEntityFlow(): Flow<Entity>

    fun getEntity(): Entity

    fun pushChanges(diff: Entity)

    fun getNewId(): String

}

expect class RemoteDomainClient : RemoteDomainClientApi

@InternalAPI
@ObsoleteCoroutinesApi
@ExperimentalCoroutinesApi
internal class RemoteDomainClientImpl(
    private val entityDao: EntityDao,
    private val diffDao: DiffDao,
    private val libState: LibState,
    private val reactiveNetwork: RN
) : RemoteDomainClientApi {

    @Volatile
    private var isInit = false
    @Volatile
    private var isTableInit = false

    private lateinit var clientApi: ClientApi<Entity>

    override fun firstInit() {
        if (!libState.getLibFirstInit()) {
            entityDao.createTable()
            diffDao.createTable()
            libState.saveLibFirstInit()
        }
        isTableInit = true
    }

    override fun init(serverUrl: String, socketUrl: String, logRequests: Boolean) {
        if (isInit) {
            return
        }
        val broadcastChannel = BroadcastChannel<Any>(Channel.CONFLATED)
        val httpClientRepository = HttpClientRepositoryImpl(serverUrl, logRequests)
        clientApi = httpClientRepository.clientApi

        GlobalScope.launch {
            broadcastChannel.consumeEach {
                log("RemoteDomainClientImpl", "event got")
                try {
                    val result = clientApi.get(Credentials(libState.getClientId(), libState.getEncodedPassword()!!))
                    if (result is Success) {
                        entityDao.update(result.value)
                    }
                } catch (e: Throwable) {
                    log("RemoteDomainClientImpl", "HTTP error: ${e.message}")
                }
            }
        }
        broadcastChannel.offer(Unit)
        reConnectWebSocket(socketUrl, broadcastChannel)
        reactiveNetwork.observeNetworkConnectivity().onEach {
            log("RemoteDomainClientImpl", "InternetConnectivity changed on $it")
            if (it.available) {
                tryToGetClientId()
                reConnectWebSocket(socketUrl, broadcastChannel)
                tryToSyncDiff()
            }
        }.launchIn(CoroutineScope(Dispatchers.Default))
        isInit = true
    }

    override fun getEntityFlow() =
        entityDao.getEntityFlow().combine(diffDao.getEntityFlow()) { entity, diff -> entity + diff }
            .onEach { log("RemoteDomainClientImpl", "combine: $it") }

    override fun getEntity() = entityDao.getEntity() + diffDao.getEntity()

    override fun pushChanges(diff: Entity) {
        GlobalScope.launch {
            try {
                clientApi.create(diff, Credentials(libState.getClientId(), libState.getEncodedPassword()!!))
            } catch (e: Throwable) {
                log("RemoteDomainClientImpl", "pushChanges HTTP error: ${e.message}")
                log("RemoteDomainClientImpl", "pushChanges diff: $diff")
                val update = getEntity() + diff
                log("RemoteDomainClientImpl", "pushChanges update: $update")
                diffDao.update(update)
            }
        }
    }

    override fun getNewId(): String {
        return libState.getClientId() + "_" + libState.getAndIncrementId()
    }

    private suspend fun tryToGetClientId() {
        if (libState.getClientId() == DEFAULT_CLIENT_ID) {
            try {
                //get from server
                val passwordEncoded = libState.generateAndSaveEncodedPassword()
                val result = clientApi.getClientId(passwordEncoded)
                if (result is Success) {
                    val clientId = result.value
                    //update in shared prefs
                    libState.setClientId(clientId)
                    //actualize diff
                    diffDao.updateJson(
                        replaceDefaultClientId(
                            DEFAULT_CLIENT_ID,
                            clientId,
                            diffDao.getEntityJson()
                        )
                    )
                }
            } catch (e: Throwable) {
                log("RemoteDomainClientImpl", "tryToGetClientId HTTP error: ${e.message}")
            }
        }
    }

    private fun reConnectWebSocket(socketUrl: String, broadcastChannel: BroadcastChannel<Any>) {
        push.startReceive(socketUrl = socketUrl) {
            //there is an ApiRequest in JSON for future optimizations with update method, etc.
            broadcastChannel.offer(Unit)
        }
    }

    private fun tryToSyncDiff() {
        GlobalScope.launch {
            try {
                clientApi.create(diffDao.getEntity(), Credentials(libState.getClientId(), libState.getEncodedPassword()!!))
                diffDao.update(Entity())
            } catch (e: Throwable) {
                log("RemoteDomainClientImpl", "tryToSyncDiff HTTP error: ${e.message}")
            }
        }
    }
}

fun replaceDefaultClientId(defaultClientId: String, clientId: String, diff: String): String {
    val regex = Regex("(?<=\")$defaultClientId(?=_\\d+\")")
    return diff.replace(regex, clientId)
}