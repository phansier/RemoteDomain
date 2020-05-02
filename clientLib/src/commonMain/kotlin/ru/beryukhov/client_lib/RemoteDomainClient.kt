package ru.beryukhov.client_lib

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import ru.beryukhov.client_lib.db.Dao
import ru.beryukhov.client_lib.db.DaoStorageImpl
import ru.beryukhov.client_lib.db.DiffDao
import ru.beryukhov.client_lib.db.EntityDao
import ru.beryukhov.client_lib.http.ClientApi
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
    fun init(SERVER_URL: String, SOCKET_URL: String, logRequests: Boolean)

    /**
     * Returns stream of Entity changes
     */
    fun getEntityFlow(): Flow<Entity>

    fun pushChanges(diff: Entity)

}

expect class RemoteDomainClient : RemoteDomainClientApi

@ObsoleteCoroutinesApi
@ExperimentalCoroutinesApi
internal class RemoteDomainClientImpl(
    private val entityDao: EntityDao,
    private val diffDao: DiffDao,
    private val libState: LibState
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

    override fun init(SERVER_URL: String, SOCKET_URL: String, logRequests: Boolean) {
        if (isInit) {
            return
        }
        val broadcastChannel = BroadcastChannel<Any>(Channel.CONFLATED)
        val httpClientRepository = HttpClientRepositoryImpl(SERVER_URL, logRequests)
        clientApi = httpClientRepository.clientApi

        GlobalScope.launch {
            broadcastChannel.consumeEach {
                log("RemoteDomainClientImpl", "event got")
                try {
                    val result = clientApi.get("entity")
                    if (result is Success) {
                        entityDao.update(result.value)
                    }
                } catch (e: Throwable) {
                    log("RemoteDomainClientImpl", "HTTP error: ${e.message}")
                }
            }
        }
        broadcastChannel.offer(Unit)
        push.startReceive(socketUrl = SOCKET_URL) {
            //there is an ApiRequest in JSON for future optimizations with update method, etc.
            broadcastChannel.offer(Unit)
        }
        isInit = true
    }

    override fun getEntityFlow() =
        entityDao.getEntityFlow().combine(diffDao.getEntityFlow()) { entity, diff -> entity + diff }
            .onEach { log("RemoteDomainClientImpl", "combine: $it") }

    override fun pushChanges(diff: Entity) {
        GlobalScope.launch {
            try {
                clientApi.create(diff, "entity")
            } catch (e: Throwable) {
                log("RemoteDomainClientImpl", "pushChanges HTTP error: ${e.message}")
                log("RemoteDomainClientImpl", "pushChanges diff: $diff")
                val update = diffDao.getEntity() + diff
                log("RemoteDomainClientImpl", "pushChanges update: $update")
                diffDao.update(update)
            }
        }
    }
}

/*
Todo
Problem #1
Update after second message creates new record in database
 */