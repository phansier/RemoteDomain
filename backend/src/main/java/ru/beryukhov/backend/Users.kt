package ru.beryukhov.backend

//import com.google.gson.GsonBuilder
import io.ktor.application.call
import io.ktor.locations.*
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.Route
import ru.beryukhov.common.model.User

/**
 * Created by Andrey Beryukhov
 */
@KtorExperimentalLocationsAPI
fun Route.users(backendRepository: BackendRepository) {
    /*val gson = GsonBuilder()
        .setPrettyPrinting()
        .create()*/

    post<Users> {
        val user = call.receive<User>()
        val result = backendRepository.createUser(userName = user.userName)
        val response = result.toResponse()
        call.respond(response.status, response.message)
    }

    get<Users> {
        val users = backendRepository.getUsers()
        val response = users.toResponse()
        call.respond(response.status, response.message)
    }

    put<Users> {
        TODO("not implemented")
    }

    delete<Users> {
        val user = call.receive<User>()
        val result = backendRepository.deleteUser(user)
        val response = result.toResponse()
        call.respond(response.status, response.message)
    }
}