package dev.andrewohara.getit.api.security

import com.auth0.jwk.UrlJwkProvider
import com.auth0.jwt.JWT
import com.auth0.jwt.JWTVerifier
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.interfaces.RSAKeyProvider
import dev.andrewohara.getit.UserId
import java.net.URL
import java.security.PublicKey
import java.security.interfaces.RSAPublicKey

fun interface Authorizer {
    operator fun invoke(token: String): UserId?

    companion object
}

private val googleJwkUri = URL("https://www.googleapis.com/oauth2/v3/certs")
private val googleIss = listOf("https://accounts.google.com", "accounts.google.com")

fun Authorizer.Companion.googleJwt(
    audience: String,
    jwkUri: URL = googleJwkUri,
    issuer: List<String> = googleIss,
): Authorizer {
    val jwkProvider = UrlJwkProvider(jwkUri)
    val keyProvider: RSAKeyProvider = object : RSAKeyProvider {
        override fun getPublicKeyById(kid: String?): RSAPublicKey {
            val publicKey: PublicKey = jwkProvider.get(kid).publicKey
            return publicKey as RSAPublicKey
        }

        override fun getPrivateKey() = null
        override fun getPrivateKeyId() = null
    }

    val algorithm = Algorithm.RSA256(keyProvider)
    val verifier = JWT.require(algorithm)
        .withIssuer(*issuer.toTypedArray())
        .withAudience(audience)
        .build()

    return jwtRsa(verifier)
}

fun Authorizer.Companion.jwtRsa(verifier: JWTVerifier) = Authorizer { token ->
    val jwt = JWT.decode(token)

    kotlin
        .runCatching { verifier.verify(jwt) }
        .map { UserId.of(jwt.subject) }
        .getOrNull()
}