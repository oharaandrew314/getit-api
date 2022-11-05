package dev.andrewohara.getit.api.http4k

import dev.andrewohara.getit.GetItService
import dev.andrewohara.getit.api.Authorizer
import dev.andrewohara.getit.corsOrigins
import dev.andrewohara.getit.createCorsPolicy
import dev.andrewohara.getit.createItemsMapper
import dev.andrewohara.getit.createListsMapper
import dev.andrewohara.getit.itemsTableName
import dev.andrewohara.getit.jwtAudience
import dev.andrewohara.getit.api.jwtRsaNimbus
import dev.andrewohara.getit.listsTableName
import dev.andrewohara.getit.dao.DynamoItemsDao
import dev.andrewohara.getit.dao.DynamoShoppingListDao
import org.http4k.cloudnative.env.Environment
import org.http4k.connect.amazon.dynamodb.DynamoDb
import org.http4k.connect.amazon.dynamodb.Http
import org.http4k.core.then
import org.http4k.filter.ServerFilters
import org.http4k.serverless.ApiGatewayV2LambdaFunction
import org.http4k.serverless.AppLoader

private val loader = AppLoader { sysEnv ->
    val env = Environment.from(sysEnv)
    val dynamo = DynamoDb.Http(env)
    val service = GetItService(
        lists = DynamoShoppingListDao(createListsMapper(dynamo, listsTableName(env))),
        items = DynamoItemsDao(createItemsMapper(dynamo, itemsTableName(env)))
    )
    val corsPolicy = createCorsPolicy(corsOrigins(env))
    val authorizer = Authorizer.jwtRsaNimbus(jwtAudience(env))

    ServerFilters.Cors(corsPolicy)
        .then(service.toHttp4k(authorizer))
}

class Http4kLambdaHandler : ApiGatewayV2LambdaFunction(loader)