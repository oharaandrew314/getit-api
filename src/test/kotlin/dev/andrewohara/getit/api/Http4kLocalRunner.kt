package dev.andrewohara.getit.api

import dev.andrewohara.getit.GetItService
import dev.andrewohara.getit.api.security.Authorizer
import dev.andrewohara.getit.api.security.jwtRsaNimbus
import dev.andrewohara.getit.dao.DynamoItemsDao
import dev.andrewohara.getit.dao.DynamoShoppingListDao
import org.http4k.cloudnative.env.Environment
import org.http4k.connect.amazon.dynamodb.DynamoDb
import org.http4k.connect.amazon.dynamodb.Http
import org.http4k.core.then
import org.http4k.filter.ServerFilters
import org.http4k.server.SunHttp
import org.http4k.server.asServer

fun main() {
    val env = Environment.ENV
    val dynamo = DynamoDb.Http(env)
    val service = GetItService(
        lists = DynamoShoppingListDao(createListsMapper(dynamo, listsTableName(env))),
        items = DynamoItemsDao(createItemsMapper(dynamo, itemsTableName(env)))
    )
    val authorizer = Authorizer.jwtRsaNimbus(jwtAudience(env))
    val corsPolicy = createCorsPolicy(corsOrigins(env))

    ServerFilters.Cors(corsPolicy)
        .then(service.toHttp4k(authorizer))
        .asServer(SunHttp(8080))
        .start()
        .block()
}

