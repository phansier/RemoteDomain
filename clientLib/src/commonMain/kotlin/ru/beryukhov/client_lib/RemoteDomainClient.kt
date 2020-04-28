package ru.beryukhov.client_lib

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import ru.beryukhov.client_lib.db.Dao
import ru.beryukhov.client_lib.db.DaoStorageImpl
import ru.beryukhov.client_lib.db.EntityDao
import ru.beryukhov.client_lib.http.ClientApi
import ru.beryukhov.client_lib.http.HttpClientRepositoryImpl
import ru.beryukhov.client_lib.push.push
import ru.beryukhov.common.model.Entity
import ru.beryukhov.common.model.Success
import ru.beryukhov.common.tree_diff.DiffImpl

interface RemoteDomainClientApi {
    /**
     * Creates table in DB and removes old data if it was.
     * Todo check table exists by Framework
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
internal class RemoteDomainClientImpl(entityDao: EntityDao) : RemoteDomainClientApi {

    private val dbRepo: DaoStorageImpl by lazy {
        DaoStorageImpl().apply {
            addDao(
                Entity::class,
                entityDao
            )
        }
    }
    private val entityDao: Dao<Entity> by lazy { dbRepo.getDao(Entity::class) as Dao<Entity> }

    private lateinit var clientApi: ClientApi<Entity>

    override fun firstInit() {
        entityDao.createTable()
    }

    override fun init(SERVER_URL: String, SOCKET_URL: String, logRequests: Boolean) {
        //todo check double init
        val broadcastChannel = BroadcastChannel<Any>(Channel.CONFLATED)
        val httpClientRepository = HttpClientRepositoryImpl(SERVER_URL, logRequests)
        clientApi = httpClientRepository.clientApi
        val entityDao = dbRepo.getDao(Entity::class) as Dao<Entity>

        GlobalScope.launch {
            broadcastChannel.consumeEach {
                log("RemoteDomainClientImpl", "event got")
                val result = clientApi.get("entity")
                if (result is Success) {
                    entityDao.update(result.value.last())
                }
            }
        }
        broadcastChannel.offer(Unit)
        push.startReceive(socketUrl = SOCKET_URL) {
            //there is an ApiRequest in JSON for future optimizations with update method, etc.
            broadcastChannel.offer(Unit)
        }
    }

    override fun getEntityFlow() = entityDao.getEntityFlow()

    override fun pushChanges(diff: Entity) {
        GlobalScope.launch {
            clientApi.create(DiffImpl.apply(entityDao.getEntity(), diff), "entity")
        }
    }
}