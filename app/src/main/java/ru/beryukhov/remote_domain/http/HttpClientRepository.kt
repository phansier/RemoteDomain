package ru.beryukhov.remote_domain.http

interface HttpClientRepository{
    val clientUserApi: ClientUserApi
    val clientPostApi: ClientPostApi
}