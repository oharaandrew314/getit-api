package dev.andrewohara.getit.api

import dev.andrewohara.getit.TestDriver
import dev.andrewohara.getit.api.v1.ShoppingListDtoV1
import dev.andrewohara.getit.api.v1.toDtoV1
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldBe
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.http.HttpStatusCode
import org.junit.jupiter.api.Test

class KtorApiV1Test {

    private val driver = TestDriver()

    @Test
    fun `get lists - unauthorized`() = driver { client ->
        val response = client.get("/v1/lists")
        response.status shouldBe HttpStatusCode.Unauthorized
    }

    @Test
    fun `get lists`() = driver { client ->
        val list1 = driver.createList(driver.defaultUserId)
        val list2 = driver.createList(driver.defaultUserId)

        val response = client.get("/v1/lists") {
            header("Authorization", "Bearer ${driver.defaultUserId}")
        }

        response.status shouldBe HttpStatusCode.OK
        response.body<List<ShoppingListDtoV1>>().shouldContainExactlyInAnyOrder(list1.toDtoV1(), list2.toDtoV1())
    }
}