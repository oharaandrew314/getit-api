package dev.andrewohara.getit.http4k

import dev.andrewohara.getit.GetItMoshi
import dev.andrewohara.getit.GetItService
import dev.andrewohara.getit.UserId
import dev.andrewohara.getit.api.Authorizer
import io.andrewohara.utils.http4k.logErrors
import io.andrewohara.utils.http4k.logSummary
import org.http4k.contract.contract
import org.http4k.contract.openapi.ApiInfo
import org.http4k.contract.openapi.v3.OpenApi3
import org.http4k.contract.openapi.v3.OpenApi3ApiRenderer
import org.http4k.contract.security.BearerAuthSecurity
import org.http4k.contract.ui.swaggerUi
import org.http4k.core.HttpHandler
import org.http4k.core.Method
import org.http4k.core.RequestContexts
import org.http4k.core.Uri
import org.http4k.core.then
import org.http4k.filter.AllowAll
import org.http4k.filter.AnyOf
import org.http4k.filter.CorsPolicy
import org.http4k.filter.OriginPolicy
import org.http4k.filter.ResponseFilters
import org.http4k.filter.ServerFilters
import org.http4k.lens.RequestContextKey
import org.http4k.routing.routes

fun GetItService.toHttp4k(corsOrigins: List<String>?, authorizer: Authorizer): HttpHandler {
    val corsPolicy = CorsPolicy(
        originPolicy = corsOrigins
            ?.let { OriginPolicy.AnyOf(it) }
            ?: OriginPolicy.AllowAll(),
        headers = listOf("Authorization"),
        methods = listOf(Method.GET, Method.POST, Method.PUT, Method.DELETE),
        credentials = true
    )

    val contexts = RequestContexts()
    val authLens = RequestContextKey.required<UserId>(contexts, "auth")
    val bearerSecurity = BearerAuthSecurity(authLens, authorizer::invoke)

    val apiV1 = contract {
        renderer = OpenApi3(
            apiInfo = ApiInfo("GetIt Api", "1"),
            json = GetItMoshi,
            apiRenderer = OpenApi3ApiRenderer(GetItMoshi)
        )
        descriptionPath = "/openapi"
        routes += toV1Routes(authLens)
        security = bearerSecurity
    }

    val ui = swaggerUi(
        descriptionRoute = Uri.of("/openapi"),
        displayOperationId = true,
        requestSnippetsEnabled = true
    )

    return ServerFilters.InitialiseRequestContext(contexts)
        .then(ServerFilters.Cors(corsPolicy))
        .then(ResponseFilters.logSummary())
        .then(ServerFilters.logErrors())
        .then(routes(apiV1, ui))
}
