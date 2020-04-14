package ru.beryukhov.client_lib.http

import ru.beryukhov.client_lib.db.Dao
import ru.beryukhov.common.model.Entity
import kotlin.reflect.KClass

interface HttpClientRepository{
    val clientApi: ClientApi<Entity>
}