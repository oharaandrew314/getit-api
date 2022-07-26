package dev.andrewohara.getit.api

import dev.andrewohara.getit.api.security.Authorizer
import dev.andrewohara.getit.fake
import org.http4k.connect.amazon.dynamodb.DynamoDb
import org.http4k.connect.amazon.dynamodb.Http
import org.http4k.core.*
import org.http4k.filter.*
import org.http4k.server.SunHttp
import org.http4k.server.asServer

fun main() {
    val dynamo = DynamoDb.Http()
    val service = createService(dynamo)
    val api = createApi(service, Authorizer.fake())

    val corsPolicy = CorsPolicy(
        OriginPolicy.AllowAll(),
        headers = listOf("Authorization"),
        methods = listOf(Method.GET, Method.POST, Method.PUT, Method.DELETE),
        credentials = true
    )

    ServerFilters.Cors(corsPolicy)
        .then(api)
        .asServer(SunHttp(8000))
        .start()
        .block()
}