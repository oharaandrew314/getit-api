package dev.andrewohara.getit.api

import dev.andrewohara.getit.corsOrigins
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
import org.http4k.server.SunHttp
import org.http4k.server.asServer

fun main(args: Array<String>) {
    val port = args.firstOrNull()?.toInt() ?: 8080
    val env = Environment.ENV
    val dynamoDb = DynamoDb.Http(env, credentialsProvider = CredentialsProvider.Profile(env))

    createService(
        dynamoDb,
        listsTableName = listsTableName(env),
        itemsTableName = itemsTableName(env)
    ).toHttp4k(
        corsOrigins = corsOrigins(env),
        authorizer = Authorizer.googleJwt(env)
    )
        .asServer(SunHttp(port))
        .start()
        .block()
}
