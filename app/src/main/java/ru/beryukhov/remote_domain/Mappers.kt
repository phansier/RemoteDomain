package ru.beryukhov.remote_domain

private typealias BackUser = ru.beryukhov.common.model.User
typealias DbUser = ru.beryukhov.remote_domain.User
private typealias BackPost = ru.beryukhov.common.model.Post


fun User.map() = BackUser(id, user_name)
fun BackUser.map() = User.Impl(id, userName)