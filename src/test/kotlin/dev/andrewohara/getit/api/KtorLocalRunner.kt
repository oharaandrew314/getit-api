package dev.andrewohara.getit.api

import dev.andrewohara.getit.api.ktor.installGetIt
import dev.andrewohara.getit.createService
import dev.andrewohara.getit.itemsTableName
import dev.andrewohara.getit.listsTableName
import io.ktor.server.cio.CIO
import io.ktor.server.engine.embeddedServer
import org.http4k.cloudnative.env.Environment
import org.http4k.connect.amazon.CredentialsProvider
import org.http4k.connect.amazon.Profile
import org.http4k.connect.amazon.RegionProvider
import org.http4k.connect.amazon.dynamodb.DynamoDb
import org.http4k.connect.amazon.dynamodb.Http

fun main(args: Array<String>) {
    val port = args.firstOrNull()?.toInt() ?: 8080
    val env = Environment.ENV

    val dynamoDb = DynamoDb.Http(RegionProvider.Profile(env).orElseThrow(), CredentialsProvider.Profile(env))
    val service = createService(dynamoDb, listsTableName = listsTableName(env), itemsTableName = itemsTableName(env))

    embeddedServer(CIO, port = port) {
        installGetIt(service)
    }.start(wait = true)
}
