package dev.andrewohara.getit.api

import dev.andrewohara.getit.GetItService
import dev.andrewohara.getit.UserId
import dev.andrewohara.getit.api.security.Authorizer
import dev.andrewohara.getit.api.v1.apiV1
import io.andrewohara.utils.http4k.ContractUi
import io.andrewohara.utils.http4k.logErrors
import io.andrewohara.utils.http4k.logSummary
import org.http4k.contract.contract
import org.http4k.contract.openapi.ApiInfo
import org.http4k.contract.openapi.ApiRenderer
import org.http4k.contract.openapi.v3.AutoJsonToJsonSchema
import org.http4k.contract.openapi.v3.OpenApi3
import org.http4k.contract.security.BearerAuthSecurity
import org.http4k.core.HttpHandler
import org.http4k.core.RequestContexts
import org.http4k.core.then
import org.http4k.filter.ResponseFilters
import org.http4k.filter.ServerFilters
import org.http4k.format.Gson
import org.http4k.lens.RequestContextKey

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
                ),
                json = Gson,
                apiRenderer = ApiRenderer.Auto(Gson, AutoJsonToJsonSchema(Gson))
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