package dev.andrewohara.getit

import dev.andrewohara.getit.api.Authorizer
import dev.andrewohara.getit.api.jwt
import dev.andrewohara.getit.dao.DynamoItemsDao.Companion.itemsDao
import dev.andrewohara.getit.dao.DynamoListsDao.Companion.listsDao
import dev.andrewohara.getit.http4k.toHttp4k
import org.http4k.client.Java8HttpClient
import org.http4k.cloudnative.env.Environment
import org.http4k.connect.amazon.dynamodb.DynamoDb
import org.http4k.connect.amazon.dynamodb.Http
import org.http4k.serverless.ApiGatewayV2FnLoader
import org.http4k.serverless.ApiGatewayV2LambdaFunction
import org.http4k.serverless.AppLoader
import org.http4k.serverless.AwsLambdaRuntime
import org.http4k.serverless.asServer
import java.time.Clock
import java.time.Duration
import java.time.Instant

private val loader = AppLoader { sysEnv ->
    val env = Environment.from(sysEnv)

    val connectStart = Instant.now()
    val dynamo = DynamoDb.Http(env, http = Java8HttpClient())
    println("http4k-connect: complete in ${Duration.between(connectStart, Instant.now()).toMillis()} ms")

    val mapperStart = Instant.now()
    val lists = dynamo.listsDao(listsTableName(env))
    val items = dynamo.itemsDao(itemsTableName(env))
    println("table mapper: complete in ${Duration.between(mapperStart, Instant.now()).toMillis()} ms")

    val authorizerStart = Instant.now()
    val authorizer = Authorizer.jwt(
        audience = listOf(jwtAudience(env)),
        clock = Clock.systemUTC()
    )
    println("authorizer: complete in ${Duration.between(authorizerStart, Instant.now()).toMillis()} ms")

    val contractStart = Instant.now()
    GetItService(lists, items).toHttp4k(
        authorizer = authorizer,
        corsOrigins = corsOrigins(env)
    ).also { println("http4k-contract: complete in ${Duration.between(contractStart, Instant.now()).toMillis()} ms") }
}

class Http4kLambdaHandler : ApiGatewayV2LambdaFunction(loader)

fun main() {
    val httpStart = Instant.now()
    val http = Java8HttpClient()
    println("JavaHttpClient: complete in ${Duration.between(httpStart, Instant.now()).toMillis()} ms")

    val runtimeStart = Instant.now()
    val runtime = AwsLambdaRuntime(http = http)
    println("AwsLambdaRuntime: complete in ${Duration.between(runtimeStart, Instant.now()).toMillis()} ms")

    val loaderStart = Instant.now()
    val loader = ApiGatewayV2FnLoader(loader)
    println("ApiGatewayV2FnLoader: complete in ${Duration.between(loaderStart, Instant.now()).toMillis()} ms")

    val serverStart = Instant.now()
    loader.asServer(runtime).start()
    println("server start: complete in ${Duration.between(serverStart, Instant.now()).toMillis()} ms")
}
