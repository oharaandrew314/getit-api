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
    println("AppLoader: start")
    val env = Environment.from(sysEnv)

    println("http4k-connect: start")
    val dynamo = DynamoDb.Http(env)
    val lists = dynamo.listsDao(listsTableName(env))
    val items = dynamo.itemsDao(itemsTableName(env))
    println("http4k-connect: complete")

    println("authorizer: start")
    val authorizer = Authorizer.googleJwt(env)
    println("authorizer: complete")

    println("http4k-contract: start")
    GetItService(lists, items).toHttp4k(
        authorizer = authorizer,
        corsOrigins = corsOrigins(env)
    ).also { println("http4k-contract: complete") }
        .also { println("AppLoader: complete") }
}

class Http4kLambdaHandler : ApiGatewayV2LambdaFunction(loader)

fun main() {
    println("AwsLambdaRuntime: start")
    val server = ApiGatewayV2FnLoader(loader).asServer(AwsLambdaRuntime())
    println("AwsLambdaRuntime: complete")
    server.start()
}
