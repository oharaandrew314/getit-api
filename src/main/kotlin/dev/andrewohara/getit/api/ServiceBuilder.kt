package dev.andrewohara.getit.api

import dev.andrewohara.getit.GetItService
import dev.andrewohara.getit.ShoppingItem
import dev.andrewohara.getit.ShoppingItemId
import dev.andrewohara.getit.ShoppingList
import dev.andrewohara.getit.ShoppingListId
import dev.andrewohara.getit.UserId
import dev.andrewohara.getit.api.security.Authorizer
import dev.andrewohara.getit.api.v1.toV1Routes
import dev.andrewohara.getit.dao.GetItMoshi
import dev.andrewohara.getit.dao.itemIdAttr
import dev.andrewohara.getit.dao.listIdAttr
import dev.andrewohara.getit.dao.userIdAttr
import io.andrewohara.utils.http4k.logErrors
import io.andrewohara.utils.http4k.logSummary
import org.http4k.connect.amazon.dynamodb.DynamoDb
import org.http4k.connect.amazon.dynamodb.mapper.tableMapper
import org.http4k.connect.amazon.dynamodb.model.TableName
import org.http4k.contract.contract
import org.http4k.contract.security.BearerAuthSecurity
import org.http4k.core.HttpHandler
import org.http4k.core.Method
import org.http4k.core.RequestContexts
import org.http4k.core.then
import org.http4k.filter.AllowAll
import org.http4k.filter.AnyOf
import org.http4k.filter.CorsPolicy
import org.http4k.filter.OriginPolicy
import org.http4k.filter.ResponseFilters
import org.http4k.filter.ServerFilters
import org.http4k.lens.RequestContextKey

fun createCorsPolicy(corsOrigins: List<String>?) = CorsPolicy(
    originPolicy = corsOrigins
        ?.let { OriginPolicy.AnyOf(it) }
        ?: OriginPolicy.AllowAll(),
    headers = listOf("Authorization"),
    methods = listOf(Method.GET, Method.POST, Method.PUT, Method.DELETE),
    credentials = true
)

fun createListsMapper(
    dynamoDb: DynamoDb,
    tableName: TableName
) = dynamoDb.tableMapper<ShoppingList, UserId, ShoppingListId>(
        TableName = tableName,
        hashKeyAttribute = userIdAttr,
        sortKeyAttribute = listIdAttr,
        autoMarshalling = GetItMoshi
    )

fun createItemsMapper(
    dynamoDb: DynamoDb,
    tableName: TableName
) = dynamoDb.tableMapper<ShoppingItem, ShoppingListId, ShoppingItemId>(
    TableName = tableName,
    hashKeyAttribute = listIdAttr,
    sortKeyAttribute = itemIdAttr,
    autoMarshalling = GetItMoshi
)

fun GetItService.toHttp4k(authorizer: Authorizer): HttpHandler {
    val contexts = RequestContexts()
    val authLens = RequestContextKey.required<UserId>(contexts, "auth")
    val bearerSecurity = BearerAuthSecurity(authLens, authorizer::invoke)

    val apiV1 = contract {
        routes += toV1Routes(authLens)
        security = bearerSecurity
    }

    return ServerFilters.InitialiseRequestContext(contexts)
        .then(ResponseFilters.logSummary())
        .then(ServerFilters.logErrors())
        .then(apiV1)
}

