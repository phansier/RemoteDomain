package ru.beryukhov.common

import kotlin.reflect.KClass

data class ApiRequest(val method: String, val entity: String) {
    constructor(method: ApiMethod, entity: KClass<out Any>) : this(
        method::class.simpleName ?: "",
        entity.simpleName ?: ""
    )

    val json: String get() = "{\"method\":\"${method}\",\"entity\":\"${entity}\"}"
}

interface ApiMethod
object Create : ApiMethod
object Read : ApiMethod
object Update : ApiMethod
object Delete : ApiMethod
