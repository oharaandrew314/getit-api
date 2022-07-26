package dev.andrewohara.getit.api

import dev.andrewohara.getit.GetItService
import dev.andrewohara.getit.UserId
import dev.andrewohara.getit.api.security.Authorizer
import dev.andrewohara.getit.api.v1.apiV1
import dev.andrewohara.getit.dao.*
import io.andrewohara.utils.http4k.ContractUi
import io.andrewohara.utils.http4k.connect.dynamodb.tableMapper
import io.andrewohara.utils.http4k.logErrors
import io.andrewohara.utils.http4k.logSummary
import org.http4k.cloudnative.env.Environment
import org.http4k.connect.amazon.dynamodb.DynamoDb
import org.http4k.contract.contract
import org.http4k.contract.openapi.ApiInfo
import org.http4k.contract.openapi.v3.OpenApi3
import org.http4k.contract.security.BearerAuthSecurity
import org.http4k.core.HttpHandler
import org.http4k.core.Method
import org.http4k.core.RequestContexts
import org.http4k.core.then
import org.http4k.filter.*
import org.http4k.lens.RequestContextKey

fun createCorsPolicy(env: Environment) = CorsPolicy(
    corsOrigins(env)
        ?.let { OriginPolicy.AnyOf(it) }
        ?: OriginPolicy.AllowAll()
    ,
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

    val apiV1 = ContractUi(
        pageTitle = "GetIt API",
        contract = contract {
            renderer = OpenApi3(
                ApiInfo(
                    title = "GetIt API",
                    version = "v1.0"
                )
            )
            descriptionPath =  "/openapi.json"
            routes += apiV1(authLens, service)
            security = bearerSecurity
        },
        descriptionPath =  "/openapi.json",
        displayOperationId = true,
    )

    return ServerFilters.InitialiseRequestContext(contexts)
        .then(ResponseFilters.logSummary())
        .then(ServerFilters.logErrors())
        .then(apiV1)
}