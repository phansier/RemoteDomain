package ru.beryukhov.common

import ru.beryukhov.common.model.CompletableResult
import ru.beryukhov.common.model.Entity
import ru.beryukhov.common.model.Result
import kotlin.reflect.KClass

interface Common
interface Client
interface Backend



interface EntityApi {
    //crud Entity
    suspend fun create(entity: Entity): Result<Entity>

    suspend fun get(): Result<List<Entity>>
    suspend fun update(entity: Entity): Result<Entity>
    suspend fun delete(entity: Entity): CompletableResult
}

/*interface CommonApi : EntityApi

interface RepoApi : CommonApi

interface DbApi : CommonApi

interface NetworkApi : CommonApi*/

data class ApiRequest(val method: ApiMethod, val entity: KClass<out Any>) {
    val json: String get() = "{\"method\":\"${method::class.simpleName}\",\"entity\":\"${entity.simpleName}\"}"
}

interface ApiMethod
object Create : ApiMethod
object Read : ApiMethod
object Update : ApiMethod
object Delete : ApiMethod
