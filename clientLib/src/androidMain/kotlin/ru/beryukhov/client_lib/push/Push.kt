package ru.beryukhov.client_lib.push

actual val push: Push by lazy { OkHttpPush() }