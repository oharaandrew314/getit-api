package dev.andrewohara.getit.api

import dev.andrewohara.getit.GetItService
import dev.andrewohara.getit.UserId
import dev.andrewohara.getit.api.security.Authorizer
import dev.andrewohara.getit.api.v1.toV1Api
import dev.andrewohara.getit.dao.DynamoItemsDao
import dev.andrewohara.getit.dao.DynamoShoppingListDao
import dev.andrewohara.getit.dao.GetItMoshi
import dev.andrewohara.getit.dao.itemIdAttr
import dev.andrewohara.getit.dao.listIdAttr
import dev.andrewohara.getit.dao.userIdAttr
import io.andrewohara.utils.http4k.logErrors
import io.andrewohara.utils.http4k.logSummary
import org.http4k.cloudnative.env.Environment
import org.http4k.connect.amazon.dynamodb.DynamoDb
import org.http4k.connect.amazon.dynamodb.mapper.tableMapper
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

fun createCorsPolicy(env: Environment) = CorsPolicy(
    corsOrigins(env)
        ?.let { OriginPolicy.AnyOf(it) }
        ?: OriginPolicy.AllowAll(),
    headers = listOf("Authorization"),
    methods = listOf(Method.GET, Method.POST, Method.PUT, Method.DELETE),
    credentials = true
)

fun createService(dynamoDb: DynamoDb, env: Environment) = GetItService(
    lists = DynamoShoppingListDao(
        dynamoDb.tableMapper(
            TableName = listsTableName(env),
            hashKeyAttribute = userIdAttr,
            sortKeyAttribute = listIdAttr,
            autoMarshalling = GetItMoshi
        )
    ),
    items = DynamoItemsDao(
        dynamoDb.tableMapper(
            TableName = itemsTableName(env),
            hashKeyAttribute = listIdAttr,
            sortKeyAttribute = itemIdAttr,
            autoMarshalling = GetItMoshi
        )
    )
)

fun createApi(service: GetItService, authorizer: Authorizer): HttpHandler {
    val contexts = RequestContexts()
    val authLens = RequestContextKey.required<UserId>(contexts, "auth")
    val bearerSecurity = BearerAuthSecurity(authLens, authorizer::invoke)

    val apiV1 = contract {
        routes += service.toV1Api(authLens)
        security = bearerSecurity
    }

    return ServerFilters.InitialiseRequestContext(contexts)
        .then(ResponseFilters.logSummary())
        .then(ServerFilters.logErrors())
        .then(apiV1)
}
