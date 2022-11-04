package dev.andrewohara.getit.api

import dev.andrewohara.getit.api.security.Authorizer
import dev.andrewohara.getit.api.security.jwtRsaNimbus
import dev.andrewohara.getit.api.v1.createAuthorization
import dev.andrewohara.getit.api.v1.createRoutes
import dev.andrewohara.getit.api.v1.createService
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.install
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.resources.Resources

fun main(args: Array<String>) {
    val port = args.firstOrNull()?.toInt() ?: 8080
    embeddedServer(Netty, port = port) {
        install(ContentNegotiation) {
            json()
        }
        install(Resources)

        createAuthorization(
            Authorizer.jwtRsaNimbus(
                audience = environment.config.property("jwt_audience").getString()
            )
        )

        createRoutes(createService())
    }.start(wait = true)
}