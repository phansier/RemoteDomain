package ru.beryukhov.remote_domain

typealias BackUser = ru.beryukhov.common.model.User
typealias BackPost = ru.beryukhov.common.model.Post


fun User.map() = BackUser(id, user_name)
fun BackUser.map() = User.Impl(id, userName)