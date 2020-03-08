package ru.beryukhov.backend

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.BroadcastChannel
import ru.beryukhov.common.*
import ru.beryukhov.common.model.*
import ru.beryukhov.common.model.Error
import ru.beryukhov.common.model.Post

/**
 * Created by Andrey Beryukhov
 */

@ExperimentalCoroutinesApi
class BackendRepository(
    private val postRepository: PostRepository,
    private val userRepository: UserRepository
) : RepoApi, Backend,
    PostApi by postRepository,
    UserApi by userRepository

@ExperimentalCoroutinesApi
class PostRepository(private val broadcastChannel: BroadcastChannel<ApiRequest>) : PostApi {
    @Volatile
    private var nextId: Int = 0

    private val posts = mutableListOf<Post>(
        Post("-1", "-1", "Test Post //Todo Remove")
    )

    override suspend fun createPost(userId: String, message: String): Result<Post> {
        val post = Post(
            id = "${++nextId}",
            userId = userId,
            message = message
        )
        posts.add(post)
        broadcastChannel.offer(ApiRequest(method = Create, entity = Post::class))
        return Result.Success(post)
    }

    override suspend fun getPosts(): Result<List<Post>> {
        return Result.Success(posts.toList())
    }

    /*override suspend fun getPostsDiff(from: Long, to: Long): Result<Diff<List<Post>>> {

    }*/

    override suspend fun updatePost(post: Post): Result<Post> {
        broadcastChannel.offer(ApiRequest(method = Update, entity = Post::class))
        TODO("not implemented")
    }

    override suspend fun deletePost(post: Post): CompletableResult {
        return if (posts.remove(post)) {
            broadcastChannel.offer(ApiRequest(method = Delete, entity = Post::class))
            CompletableResult.Success
        } else CompletableResult.Failure(
            Error.NoSuchElementError("todo")
        )
    }

}

@ExperimentalCoroutinesApi
class UserRepository(private val broadcastChannel: BroadcastChannel<ApiRequest>) : UserApi {
    @Volatile
    private var nextId: Int = 0

    private val users = mutableListOf<User>(
        User("-1", "Test Testov //Todo Remove")
    )

    override suspend fun createUser(userName: String): Result<User> {
        val user = User(
            id = "${++nextId}",
            userName = userName
        )
        users.add(user)
        broadcastChannel.offer(ApiRequest(method = Create, entity = User::class))
        return Result.Success(user)
    }

    override suspend fun getUsers(): Result<List<User>> {
        return Result.Success(users.toList())
    }

    override suspend fun updateUser(user: User): Result<User> {
        broadcastChannel.offer(ApiRequest(method = Update, entity = User::class))
        TODO("not implemented")
    }

    override suspend fun deleteUser(user: User): CompletableResult {
        return if (users.remove(user)) {
            broadcastChannel.offer(ApiRequest(method = Delete, entity = User::class))
            CompletableResult.Success
        } else CompletableResult.Failure(
            Error.NoSuchElementError("todo")
        )
    }

}