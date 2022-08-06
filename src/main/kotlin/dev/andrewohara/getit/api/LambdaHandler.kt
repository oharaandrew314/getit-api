package dev.andrewohara.getit.api

import dev.andrewohara.getit.api.security.Authorizer
import dev.andrewohara.getit.api.security.jwtRsaNimbus
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
    val service = createService(dynamo, env)
    val corsPolicy = createCorsPolicy(env)
    val authorizer = Authorizer.jwtRsaNimbus(jwtAudience(env))

    ServerFilters.Cors(corsPolicy)
        .then(createApi(service, authorizer))

}

class LambdaHandler: ApiGatewayV2LambdaFunction(loader)