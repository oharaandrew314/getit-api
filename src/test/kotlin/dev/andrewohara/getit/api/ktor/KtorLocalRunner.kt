package dev.andrewohara.getit.api.ktor

import dev.andrewohara.getit.api.Authorizer
import dev.andrewohara.getit.api.jwtRsaNimbus
import dev.andrewohara.getit.createService
import dev.andrewohara.getit.jwtAudience
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.install
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.resources.Resources
import org.http4k.cloudnative.env.Environment
import org.http4k.connect.amazon.CredentialsProvider
import org.http4k.connect.amazon.Profile
import org.http4k.connect.amazon.dynamodb.DynamoDb
import org.http4k.connect.amazon.dynamodb.Http

fun main(args: Array<String>) {
    val port = args.firstOrNull()?.toInt() ?: 8080
    val env = Environment.ENV
    val dynamoDb = DynamoDb.Http(env, credentialsProvider = CredentialsProvider.Profile(env))
    val service = createService(env, dynamoDb = dynamoDb)

    embeddedServer(Netty, port = port) {
        install(ContentNegotiation) {
            json()
        }
        install(Resources)

        createAuthorization(
            Authorizer.jwtRsaNimbus(audience = jwtAudience(env))
        )

        createRoutes(service)
    }.start(wait = true)
}