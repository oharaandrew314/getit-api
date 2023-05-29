package dev.andrewohara.getit.api

import com.nimbusds.jose.JWSAlgorithm
import com.nimbusds.jose.jwk.source.JWKSource
import com.nimbusds.jose.jwk.source.RemoteJWKSet
import com.nimbusds.jose.proc.JWSVerificationKeySelector
import com.nimbusds.jose.proc.SecurityContext
import com.nimbusds.jwt.JWTClaimsSet
import com.nimbusds.jwt.SignedJWT
import com.nimbusds.jwt.proc.DefaultJWTClaimsVerifier
import com.nimbusds.jwt.proc.DefaultJWTProcessor
import dev.andrewohara.getit.UserId
import org.slf4j.LoggerFactory
import java.net.URL
import java.time.Clock
import java.util.Date

fun interface Authorizer {
    operator fun invoke(token: String): UserId?

    companion object
}

private class TestableClaimsVerifier(
    exactMatchClaims: JWTClaimsSet,
    requiredClaims: Set<String>,
    private val clock: Clock
) : DefaultJWTClaimsVerifier<SecurityContext>(exactMatchClaims, requiredClaims) {
    override fun currentTime(): Date = Date.from(clock.instant())
}

private val googleJwkUri = URL("https://www.googleapis.com/oauth2/v3/certs")
private const val googleIss = "accounts.google.com"

fun Authorizer.Companion.jwt(
    audience: List<String>,
    clock: Clock,
    issuer: String = googleIss,
    algorithm: JWSAlgorithm = JWSAlgorithm.RS256,
    jwkSource: JWKSource<SecurityContext> = RemoteJWKSet(googleJwkUri),
): Authorizer {
    val logger = LoggerFactory.getLogger("authorizer")

    val processor = DefaultJWTProcessor<SecurityContext>().apply {
        jwtClaimsSetVerifier = TestableClaimsVerifier(
            exactMatchClaims = JWTClaimsSet.Builder()
                .issuer(issuer)
                .audience(audience)
                .build(),
            emptySet(),
            clock = clock
        )
        jwsKeySelector = JWSVerificationKeySelector(algorithm, jwkSource)
    }

    return Authorizer { token ->
        kotlin
            .runCatching { SignedJWT.parse(token).let { processor.process(it, null) } }
            .onFailure { logger.debug("Failed to process JWT: $it") }
            .map { UserId.of(it.subject) }
            .getOrNull()
    }
}
