package dev.andrewohara.getit.api

import io.ktor.http.auth.HttpAuthHeader
import io.ktor.server.auth.AuthenticationConfig
import io.ktor.server.auth.AuthenticationContext
import io.ktor.server.auth.AuthenticationFailedCause
import io.ktor.server.auth.AuthenticationProvider
import io.ktor.server.auth.Principal
import io.ktor.server.auth.UnauthorizedResponse
import io.ktor.server.auth.parseAuthorizationHeader
import io.ktor.server.response.respond

class BearerAuthenticationProvider(config: Config): AuthenticationProvider(config) {

    private val lookup = config.lookup

    override suspend fun onAuthenticate(context: AuthenticationContext) {
        val call = context.call
        val authHeader = call.request.parseAuthorizationHeader()
        val principal = (authHeader as? HttpAuthHeader.Single)
            ?.takeIf { it.authScheme.equals("Bearer", ignoreCase = true) }
            ?.blob
            ?.let(lookup)

        val cause = when {
            authHeader == null -> AuthenticationFailedCause.NoCredentials
            principal == null -> AuthenticationFailedCause.InvalidCredentials
            else -> null
        }

        if (cause != null) {
            context.challenge("BearerAuth", cause) { challenge, thisCall ->
                thisCall.respond(UnauthorizedResponse())
                challenge.complete()
            }
        }
        if (principal != null) {
            context.principal(principal)
        }
    }

    class Config(name: String?) : AuthenticationProvider.Config(name) {
        var lookup: (String) -> Principal? = { null }
    }
}

fun AuthenticationConfig.bearer(
    name: String? = null,
    configure: BearerAuthenticationProvider.Config.() -> Unit,
) {
    val provider = BearerAuthenticationProvider(BearerAuthenticationProvider.Config(name).apply(configure))
    register(provider)
}