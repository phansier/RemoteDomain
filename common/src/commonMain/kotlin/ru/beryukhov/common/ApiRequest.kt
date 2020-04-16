package ru.beryukhov.common

import kotlin.reflect.KClass

data class ApiRequest(val method: ApiMethod, val entity: KClass<out Any>) {
    val json: String get() = "{\"method\":\"${method::class.simpleName}\",\"entity\":\"${entity.simpleName}\"}"
}

interface ApiMethod
object Create : ApiMethod
object Read : ApiMethod
object Update : ApiMethod
object Delete : ApiMethod
