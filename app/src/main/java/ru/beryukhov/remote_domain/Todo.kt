package ru.beryukhov.remote_domain

/**
 * Created by Andrey Beryukhov
 */
data class Post(val id: String, val userId: String, val message: String)

data class User(val id: String, val userName: String)

interface Common
interface Client
interface Backend

interface CommonApi {
    //crud User
    suspend fun createUser(userName: String):Result<User>
    suspend fun getUsers():List<User>
    suspend fun updateUser(user: User):Result<User>
    suspend fun deleteUser(user: User):Result<Nothing>
    //crud Post
    suspend fun createPost(userId: String, message: String):Result<Post>
    suspend fun getPosts():List<Post>
    suspend fun updatePost(post: Post):Result<Post>
    suspend fun deletePost(post: Post):Result<Nothing>
}

interface RepoApi : CommonApi {

}

interface DbApi : CommonApi {

}

interface NetworkApi : CommonApi {

}
