package dev.andrewohara.getit

import dev.andrewohara.getit.api.Authorizer
import dev.andrewohara.getit.dao.DynamoItemsDao.Companion.itemsDao
import dev.andrewohara.getit.dao.DynamoListsDao.Companion.listsDao
import dev.andrewohara.getit.http4k.toHttp4k
import org.http4k.cloudnative.env.Environment
import org.http4k.connect.amazon.dynamodb.DynamoDb
import org.http4k.connect.amazon.dynamodb.Http
import org.http4k.serverless.ApiGatewayV2FnLoader
import org.http4k.serverless.ApiGatewayV2LambdaFunction
import org.http4k.serverless.AppLoader
import org.http4k.serverless.AwsLambdaRuntime
import org.http4k.serverless.asServer

private val loader = AppLoader { sysEnv ->
    val env = Environment.from(sysEnv)
    val dynamo = DynamoDb.Http(env)

    GetItService(
        lists = dynamo.listsDao(listsTableName(env)),
        items = dynamo.itemsDao(itemsTableName(env))
    ).toHttp4k(
        authorizer = Authorizer.googleJwt(env),
        corsOrigins = corsOrigins(env)
    )
}

class Http4kLambdaHandler : ApiGatewayV2LambdaFunction(loader)

fun main() {
    ApiGatewayV2FnLoader(loader).asServer(AwsLambdaRuntime()).start()
}
