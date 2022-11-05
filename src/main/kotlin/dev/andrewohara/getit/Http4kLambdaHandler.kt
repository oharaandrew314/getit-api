package dev.andrewohara.getit

import dev.andrewohara.getit.api.Authorizer
import dev.andrewohara.getit.dao.DynamoItemsDao
import dev.andrewohara.getit.dao.DynamoShoppingListDao
import dev.andrewohara.getit.http4k.toHttp4k
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
    val authorizer = Authorizer.googleJwt(env)
    ServerFilters.Cors(corsPolicy)
        .then(service.toHttp4k(authorizer))
}

class Http4kLambdaHandler : ApiGatewayV2LambdaFunction(loader)
