package ru.beryukhov.common

/**
 * Created by Andrey Beryukhov
 */
data class Post(val id: String, val userId: String, val message: String)

data class User(val id: String, val userName: String)

interface Common
interface Client
interface Backend

interface UserApi{
    //crud User
    suspend fun createUser(userName: String):Result<User>
    suspend fun getUsers():Result<List<User>>
    suspend fun updateUser(user: User):Result<User>
    suspend fun deleteUser(user: User): CompletableResult
}
interface PostApi{
    //crud Post
    suspend fun createPost(userId: String, message: String):Result<Post>
    suspend fun getPosts():Result<List<Post>>
    suspend fun updatePost(post: Post):Result<Post>
    suspend fun deletePost(post: Post): CompletableResult
}

interface CommonApi:UserApi, PostApi {

}

interface RepoApi : CommonApi {

}

interface DbApi : CommonApi {

}

interface NetworkApi : CommonApi {

}
