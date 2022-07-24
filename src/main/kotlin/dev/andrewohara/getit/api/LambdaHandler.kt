package dev.andrewohara.getit.api

import dev.andrewohara.getit.GetItService
import dev.andrewohara.getit.api.security.Authorizer
import dev.andrewohara.getit.api.security.google
import dev.andrewohara.getit.dao.*
import io.andrewohara.utils.http4k.connect.dynamodb.tableMapper
import org.http4k.cloudnative.env.Environment
import org.http4k.connect.amazon.dynamodb.DynamoDb
import org.http4k.connect.amazon.dynamodb.Http
import org.http4k.core.Method
import org.http4k.core.then
import org.http4k.filter.AnyOf
import org.http4k.filter.CorsPolicy
import org.http4k.filter.OriginPolicy
import org.http4k.filter.ServerFilters
import org.http4k.serverless.ApiGatewayV2LambdaFunction
import org.http4k.serverless.AppLoader

private val loader = AppLoader { sysEnv ->
    val env = Environment.from(sysEnv)
    val dynamo = DynamoDb.Http(env)

    val service = GetItService(
        lists = DynamoShoppingListDao(
            dynamo.tableMapper(
                TableName = listsTableName(env),
                hashKeyAttribute = listIdAttr,
                autoMarshalling = GetItMoshi
            )
        ),
        items = DynamoItemsDao(
            dynamo.tableMapper(
                TableName = itemsTableName(env),
                hashKeyAttribute = itemIdAttr,
                autoMarshalling = GetItMoshi
            )
        )
    )

    val corsPolicy = CorsPolicy(
        OriginPolicy.AnyOf(corsOrigins(env)),
        headers = listOf("Authorization"),
        methods = listOf(Method.GET, Method.POST, Method.PUT, Method.DELETE),
        credentials = true
    )

    ServerFilters.Cors(corsPolicy)
        .then(createApi(service, Authorizer.google()))

}

class LambdaHandler: ApiGatewayV2LambdaFunction(loader)