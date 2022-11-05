package dev.andrewohara.getit.api.ktor

import dev.andrewohara.getit.GetItService
import dev.andrewohara.getit.UserId
import dev.andrewohara.getit.api.Authorizer
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.auth.Authentication
import io.ktor.server.auth.Principal
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.resources.Resources

data class GetItPrincipal(val userId: UserId) : Principal

private fun Application.createAuthorization(authorizer: Authorizer) {
    install(Authentication) {
        bearer("Bearer") {
            lookup = { token ->
                authorizer(token)?.let { GetItPrincipal(it) }
            }
        }
    }
}

fun Application.installGetIt(service: GetItService) {
    install(Resources)
    install(ContentNegotiation) {
        json()
    }
    createAuthorization { UserId.of(it) }
    createRoutes(service)
}
