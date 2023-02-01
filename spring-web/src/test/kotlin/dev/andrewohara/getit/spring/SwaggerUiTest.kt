package dev.andrewohara.getit.spring

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.boot.test.web.client.exchange
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.RequestEntity
import org.springframework.test.context.ActiveProfiles
import java.net.URI

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class SwaggerUiTest(@Autowired val restTemplate: TestRestTemplate) {
    @Test
    fun `get swagger ui`() {
        val request = RequestEntity<Unit>(
            HttpMethod.GET,
            URI.create("/swagger-ui/index.html")
        )

        val response = restTemplate.exchange<String>(request)
        response.statusCode shouldBe HttpStatus.OK
    }
}
