package ru.beryukhov.backend

import ru.beryukhov.common.*
import ru.beryukhov.common.Post

/**
 * Created by Andrey Beryukhov
 */

class BackendRepository(
    private val postRepository: PostRepository,
    private val userRepository: UserRepository
) : RepoApi, Backend,
    PostApi by postRepository,
    UserApi by userRepository {

}

class PostRepository : PostApi {
    @Volatile
    private var nextId: Int = 0

    private val posts = mutableListOf<Post>(Post("-1","-1","Test Post //Todo Remove"))

    override suspend fun createPost(userId: String, message: String): Result<Post> {
        return Result.Success(Post(id = "${++nextId}", userId = userId, message = message))
    }

    override suspend fun getPosts(): Result<List<Post>> {
        return Result.Success(posts.toList())
    }

    override suspend fun updatePost(post: Post): Result<Post> {
        TODO("not implemented")
    }

    override suspend fun deletePost(post: Post): CompletableResult {
        return if (posts.remove(post)) CompletableResult.Success else CompletableResult.Failure(
            NoSuchElementException()
        )
    }

}

class UserRepository : UserApi {
    @Volatile
    private var nextId: Int = 0

    private val users = mutableListOf<User>(User("-1","Test Testov //Todo Remove"))

    override suspend fun createUser(userName: String): Result<User> {
        return Result.Success(User(id = "${++nextId}", userName = userName))
    }

    override suspend fun getUsers(): Result<List<User>> {
        //return Result.Success(users.toList())
        return Result.Failure(UnknownError())
    }

    override suspend fun updateUser(user: User): Result<User> {
        TODO("not implemented")
    }

    override suspend fun deleteUser(user: User): CompletableResult {
        return if (users.remove(user)) CompletableResult.Success else CompletableResult.Failure(
            NoSuchElementException()
        )
    }

}