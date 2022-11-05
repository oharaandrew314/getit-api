package dev.andrewohara.getit.api

import com.nimbusds.jose.crypto.RSASSAVerifier
import com.nimbusds.jose.jwk.JWKSet
import com.nimbusds.jwt.SignedJWT
import dev.andrewohara.getit.UserId
import org.slf4j.LoggerFactory
import java.net.URL

fun interface Authorizer {
    operator fun invoke(token: String): UserId?

    companion object
}

private val googleJwkUri = URL("https://www.googleapis.com/oauth2/v3/certs")
private val googleIss = listOf("https://accounts.google.com", "accounts.google.com")

fun Authorizer.Companion.jwtRsaNimbus(
    audience: String,
    jwkUri: URL = googleJwkUri,
    issuer: List<String> = googleIss
): Authorizer {
    val publicKeys = JWKSet.load(jwkUri)
    val log = LoggerFactory.getLogger("root")

    return Authorizer { token ->
        val jwt = SignedJWT.parse(token)
        val claims = jwt.jwtClaimsSet

        if (claims.issuer !in issuer) {
            log.info("JWT failed issuer verification: ${claims.issuer}")
            return@Authorizer null
        }
        if (audience !in claims.audience) {
            log.info("JWT failed audience verification: ${claims.audience}")
            return@Authorizer null
        }

        val key = publicKeys.getKeyByKeyId(jwt.header.keyID).toRSAKey()
        val verifier = RSASSAVerifier(key)

        kotlin
            .runCatching { jwt.verify(verifier) }
            .onFailure { log.error("Error verifying JWT", it) }
            .map { UserId.of(jwt.jwtClaimsSet.subject) }
            .getOrNull()
    }
}
