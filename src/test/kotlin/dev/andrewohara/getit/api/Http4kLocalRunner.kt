package dev.andrewohara.getit.api

import dev.andrewohara.getit.corsOrigins
import dev.andrewohara.getit.createCorsPolicy
import dev.andrewohara.getit.createService
import dev.andrewohara.getit.googleJwt
import dev.andrewohara.getit.http4k.toHttp4k
import dev.andrewohara.getit.itemsTableName
import dev.andrewohara.getit.listsTableName
import org.http4k.cloudnative.env.Environment
import org.http4k.connect.amazon.CredentialsProvider
import org.http4k.connect.amazon.Profile
import org.http4k.connect.amazon.dynamodb.DynamoDb
import org.http4k.connect.amazon.dynamodb.Http
import org.http4k.core.then
import org.http4k.filter.ServerFilters
import org.http4k.server.SunHttp
import org.http4k.server.asServer

fun main(args: Array<String>) {
    val port = args.firstOrNull()?.toInt() ?: 8080
    val env = Environment.ENV
    val dynamoDb = DynamoDb.Http(env, credentialsProvider = CredentialsProvider.Profile(env))

    val service = createService(dynamoDb, listsTableName = listsTableName(env), itemsTableName = itemsTableName(env))
    val authorizer = Authorizer.googleJwt(env)
    val corsPolicy = createCorsPolicy(corsOrigins(env))

    ServerFilters.Cors(corsPolicy)
        .then(service.toHttp4k(authorizer))
        .asServer(SunHttp(port))
        .start()
        .block()
}
