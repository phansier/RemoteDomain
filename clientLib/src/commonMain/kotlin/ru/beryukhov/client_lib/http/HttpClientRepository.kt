package ru.beryukhov.client_lib.http

import ru.beryukhov.common.model.Entity

interface HttpClientRepository{
    val clientApi: ClientApi<Entity>
}