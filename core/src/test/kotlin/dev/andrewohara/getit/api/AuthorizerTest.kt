package dev.andrewohara.getit.api

import com.nimbusds.jose.crypto.MACVerifier
import dev.andrewohara.getit.UserId
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset

class AuthorizerTest {

    private val authorizer = Authorizer.jwt(
        audience = "getit-test",
        issuer = listOf("jwtTest"),
        clock = Clock.fixed(Instant.parse("2022-11-05T12:00:00Z"), ZoneOffset.UTC),
        getVerifier = { MACVerifier("qwertyuiopasdfghjklzxcvbnm123456") }
    )

    @Test
    fun `verify malformed jwt`() {
        authorizer("token") shouldBe null
    }

    @Test
    fun `verify jwt`() {
        val jwt = javaClass.classLoader.getResourceAsStream("valid.jwt")!!.reader().readText()

        authorizer(jwt) shouldBe UserId.of("user123")
    }

    @Test
    fun `verify expired`() {
        val jwt = javaClass.classLoader.getResourceAsStream("expired.jwt")!!.reader().readText()

        authorizer(jwt) shouldBe null
    }

    @Test
    fun `verify invalid issuer`() {
        val jwt = javaClass.classLoader.getResourceAsStream("invalidIss.jwt")!!.reader().readText()

        authorizer(jwt) shouldBe null
    }

    @Test
    fun `verify invalid audience`() {
        val jwt = javaClass.classLoader.getResourceAsStream("invalidAud.jwt")!!.reader().readText()

        authorizer(jwt) shouldBe null
    }
}
