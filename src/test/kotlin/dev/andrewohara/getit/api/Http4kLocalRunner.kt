package dev.andrewohara.getit.api

import dev.andrewohara.getit.GetItService
import dev.andrewohara.getit.api.security.Authorizer
import dev.andrewohara.getit.api.security.jwtRsaNimbus
import dev.andrewohara.getit.api.v1.createAuthorization
import dev.andrewohara.getit.api.v1.createRoutes
import dev.andrewohara.getit.api.v1.createService
import dev.andrewohara.getit.dao.DynamoItemsDao
import dev.andrewohara.getit.dao.DynamoShoppingListDao
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.install
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.resources.Resources
import org.http4k.cloudnative.env.Environment
import org.http4k.connect.amazon.dynamodb.DynamoDb
import org.http4k.connect.amazon.dynamodb.Http
import org.http4k.core.then
import org.http4k.filter.ServerFilters
import org.http4k.server.SunHttp
import org.http4k.server.asServer

fun main2() {
    val env = Environment.ENV
    val dynamo = DynamoDb.Http(env)
    val service = GetItService(
        lists = DynamoShoppingListDao(createListsMapper(dynamo, env)),
        items = DynamoItemsDao(createItemsMapper(dynamo, env))
    )
    val authorizer = Authorizer.jwtRsaNimbus(jwtAudience(env))
    val api = createApi(service, authorizer)
    val corsPolicy = createCorsPolicy(env)

    ServerFilters.Cors(corsPolicy)
        .then(api)
        .asServer(SunHttp(8080))
        .start()
        .block()
}

fun main(args: Array<String>) {
    val port = args.firstOrNull()?.toInt() ?: 8080
    embeddedServer(Netty, port = port) {
        install(ContentNegotiation) {
            json()
        }
        install(Resources)
        createAuthorization()
        createRoutes(createService())
    }.start(wait = true)
}