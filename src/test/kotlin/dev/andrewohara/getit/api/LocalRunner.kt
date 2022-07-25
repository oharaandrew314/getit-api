package dev.andrewohara.getit.api

import dev.andrewohara.getit.GetItService
import dev.andrewohara.getit.api.security.Authorizer
import dev.andrewohara.getit.dao.*
import dev.andrewohara.getit.fake
import io.andrewohara.utils.http4k.connect.dynamodb.tableMapper
import org.http4k.cloudnative.env.Environment
import org.http4k.connect.amazon.dynamodb.DynamoDb
import org.http4k.connect.amazon.dynamodb.Http
import org.http4k.core.Method
import org.http4k.core.then
import org.http4k.filter.*
import org.http4k.server.SunHttp
import org.http4k.server.asServer

fun main() {
    val env = Environment.ENV

    val dynamo = DynamoDb.Http(env)

    val service = GetItService(
        lists = DynamoShoppingListDao(
            dynamo.tableMapper(
                TableName = listsTableName(env),
                hashKeyAttribute = userIdAttr,
                sortKeyAttribute = listIdAttr,
                autoMarshalling = GetItMoshi
            )
        ),
        items = DynamoItemsDao(
            dynamo.tableMapper(
                TableName = itemsTableName(env),
                hashKeyAttribute = listIdAttr,
                sortKeyAttribute = itemIdAttr,
                autoMarshalling = GetItMoshi
            )
        )
    )

    val corsPolicy = CorsPolicy(
        OriginPolicy.AllowAll(),
        headers = listOf("Authorization"),
        methods = listOf(Method.GET, Method.POST, Method.PUT, Method.DELETE),
        credentials = true
    )

    ServerFilters.Cors(corsPolicy)
        .then(createApi(service, Authorizer.fake()))
        .asServer(SunHttp(8000))
        .block()
}