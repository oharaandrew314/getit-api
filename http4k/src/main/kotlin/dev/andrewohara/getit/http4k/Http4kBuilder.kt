package dev.andrewohara.getit.http4k

import dev.andrewohara.getit.GetItService
import dev.andrewohara.getit.UserId
import dev.andrewohara.getit.api.Authorizer
import io.andrewohara.utils.http4k.logErrors
import io.andrewohara.utils.http4k.logSummary
import org.http4k.contract.contract
import org.http4k.contract.security.BearerAuthSecurity
import org.http4k.core.HttpHandler
import org.http4k.core.RequestContexts
import org.http4k.core.then
import org.http4k.filter.ResponseFilters
import org.http4k.filter.ServerFilters
import org.http4k.lens.RequestContextKey

fun GetItService.toHttp4k(authorizer: Authorizer): HttpHandler {
    val contexts = RequestContexts()
    val authLens = RequestContextKey.required<UserId>(contexts, "auth")
    val bearerSecurity = BearerAuthSecurity(authLens, authorizer::invoke)

    val apiV1 = contract {
        routes += toV1Routes(authLens)
        security = bearerSecurity
    }

    return ServerFilters.InitialiseRequestContext(contexts)
        .then(ResponseFilters.logSummary())
        .then(ServerFilters.logErrors())
        .then(apiV1)
}
