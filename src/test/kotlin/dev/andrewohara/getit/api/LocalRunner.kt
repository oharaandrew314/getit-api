package dev.andrewohara.getit.api

import dev.andrewohara.getit.api.security.Authorizer
import dev.andrewohara.getit.fake
import org.http4k.cloudnative.env.Environment
import org.http4k.connect.amazon.dynamodb.DynamoDb
import org.http4k.connect.amazon.dynamodb.Http
import org.http4k.core.*
import org.http4k.filter.*
import org.http4k.server.SunHttp
import org.http4k.server.asServer

fun main() {
    val env = Environment.ENV
    val dynamo = DynamoDb.Http(env)
    val service = createService(dynamo, env)
    val api = createApi(service, Authorizer.fake())
    val corsPolicy = createCorsPolicy(env)

    ServerFilters.Cors(corsPolicy)
        .then(api)
        .asServer(SunHttp(8080))
        .start()
        .block()
}