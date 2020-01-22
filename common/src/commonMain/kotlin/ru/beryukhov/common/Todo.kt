package ru.beryukhov.common

import ru.beryukhov.common.model.CompletableResult
import ru.beryukhov.common.model.Post
import ru.beryukhov.common.model.Result
import ru.beryukhov.common.model.User

interface Common
interface Client
interface Backend

interface UserApi {
    //crud User
    suspend fun createUser(userName: String): Result<User>

    suspend fun getUsers(): Result<List<User>>
    suspend fun updateUser(user: User): Result<User>
    suspend fun deleteUser(user: User): CompletableResult
}
interface PostApi {
    //crud Post
    suspend fun createPost(userId: String, message: String): Result<Post>

    suspend fun getPosts(): Result<List<Post>>
    suspend fun updatePost(post: Post): Result<Post>
    suspend fun deletePost(post: Post): CompletableResult

    suspend fun getPostsDiff(from: Long, to: Long): Result<List<Post>>
}

interface CommonApi : UserApi, PostApi

interface RepoApi : CommonApi

interface DbApi : CommonApi

interface NetworkApi : CommonApi
