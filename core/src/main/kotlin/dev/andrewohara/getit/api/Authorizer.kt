package dev.andrewohara.getit.api

import com.nimbusds.jose.JWSVerifier
import com.nimbusds.jose.crypto.RSASSAVerifier
import com.nimbusds.jose.jwk.JWKSet
import com.nimbusds.jwt.SignedJWT
import dev.andrewohara.getit.UserId
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.net.URL
import java.text.ParseException
import java.time.Clock

fun interface Authorizer {
    operator fun invoke(token: String): UserId?

    companion object
}

fun interface GetVerifier {
    operator fun invoke(keyId: String?): JWSVerifier?

    companion object
}

fun GetVerifier.Companion.rsaJwks(url: URL): GetVerifier {
    val set = JWKSet.load(url)
    return GetVerifier { keyId ->
        keyId
            ?.let { set.getKeyByKeyId(keyId) }
            ?.let { RSASSAVerifier(it.toRSAKey()) }
    }
}

val googleJwkUri = URL("https://www.googleapis.com/oauth2/v3/certs")
private val googleIss = listOf("https://accounts.google.com", "accounts.google.com")

fun Authorizer.Companion.jwt(
    audience: String,
    getVerifier: GetVerifier,
    clock: Clock,
    issuer: List<String> = googleIss,
    logger: Logger = LoggerFactory.getLogger(Authorizer::class.java)
): Authorizer {
    return Authorizer { token ->
        val jwt = try {
            SignedJWT.parse(token)
        } catch (e: ParseException) {
            logger.debug("Error parsing JWT: ${e.message}")
            return@Authorizer null
        }

        val claims = jwt.jwtClaimsSet

        if (claims.issuer !in issuer) {
            logger.info("JWT failed issuer verification: ${claims.issuer}")
            return@Authorizer null
        }
        if (audience !in claims.audience) {
            logger.info("JWT failed audience verification: ${claims.audience}")
            return@Authorizer null
        }

        if (claims.expirationTime.toInstant() < clock.instant()) {
            logger.info("JWT failed expiration check: ${claims.expirationTime}")
            return@Authorizer null
        }

        val verifier = getVerifier(jwt.header.keyID)
        if (verifier == null) {
            logger.error("Could not find public key: ${jwt.header.keyID}")
            return@Authorizer null
        }

        kotlin
            .runCatching { jwt.verify(verifier) }
            .onFailure { logger.error("Error verifying JWT", it) }
            .map { UserId.of(jwt.jwtClaimsSet.subject) }
            .getOrNull()
    }
}
