package dev.andrewohara.getit.http4k

import dev.andrewohara.getit.GetItService
import dev.andrewohara.getit.api.Authorizer
import dev.andrewohara.getit.api.jwt
import dev.andrewohara.getit.corsOrigins
import dev.andrewohara.getit.dao.DynamoItemsDao.Companion.itemsDao
import dev.andrewohara.getit.dao.DynamoListsDao.Companion.listsDao
import dev.andrewohara.getit.itemsTableName
import dev.andrewohara.getit.jwtAudience
import dev.andrewohara.getit.listsTableName
import org.http4k.cloudnative.env.Environment
import org.http4k.connect.amazon.CredentialsProvider
import org.http4k.connect.amazon.Profile
import org.http4k.connect.amazon.RegionProvider
import org.http4k.connect.amazon.dynamodb.DynamoDb
import org.http4k.connect.amazon.dynamodb.Http
import org.http4k.server.SunHttp
import org.http4k.server.asServer
import java.time.Clock

fun main(args: Array<String>) {
    val port = args.firstOrNull()?.toInt() ?: 8080
    val env = Environment.ENV
    val dynamoDb = DynamoDb.Http(RegionProvider.Profile(env).orElseThrow(), CredentialsProvider.Profile(env))

    GetItService(
        lists = dynamoDb.listsDao(listsTableName(env)),
        items = dynamoDb.itemsDao(itemsTableName(env))
    ).toHttp4k(
        corsOrigins = corsOrigins(env),
        authorizer = Authorizer.jwt(
            audience = listOf(jwtAudience(env)),
            clock = Clock.systemUTC()
        )
    )
        .asServer(SunHttp(port))
        .start()
        .also { println("Server running on http://localhost:$port") }
        .block()
}
