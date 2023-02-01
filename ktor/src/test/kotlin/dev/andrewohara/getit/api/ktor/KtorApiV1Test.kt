package dev.andrewohara.getit.api.ktor

import dev.andrewohara.getit.ShoppingItemName
import dev.andrewohara.getit.ShoppingListName
import dev.andrewohara.getit.UserId
import dev.andrewohara.getit.api.ShoppingItemDataDtoV1
import dev.andrewohara.getit.api.ShoppingItemDtoV1
import dev.andrewohara.getit.api.ShoppingListDataDtoV1
import dev.andrewohara.getit.api.ShoppingListDtoV1
import dev.andrewohara.getit.api.toDtoV1
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.sequences.shouldBeEmpty
import io.kotest.matchers.sequences.shouldContainExactly
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import io.ktor.client.call.body
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpMessageBuilder
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import org.junit.jupiter.api.Test
import java.util.UUID

class KtorApiV1Test {

    private val driver = KtorTestDriver()

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
            withUser(list1.userId)
        }

        response.status shouldBe HttpStatusCode.OK
        response.body<List<ShoppingListDtoV1>>().shouldContainExactlyInAnyOrder(list1.toDtoV1(), list2.toDtoV1())
    }

    @Test
    fun `create list`() = driver { client ->
        val data = ShoppingListDataDtoV1(
            name = "stuff"
        )

        val response = client.post("/v1/lists") {
            withUser(driver.defaultUserId)
            withJson(data)
        }

        response.status shouldBe HttpStatusCode.OK
        response.body<ShoppingListDtoV1>() should { list ->
            list.name shouldBe data.name
            driver.listsDao[driver.defaultUserId].map { it.toDtoV1() }.shouldContainExactly(list)
        }
    }

    @Test
    fun `delete list`() = driver { client ->
        val list = driver.createList()

        client.delete("/v1/lists/${list.listId}") {
            withUser(list.userId)
        }.status shouldBe HttpStatusCode.OK

        driver.listsDao[driver.defaultUserId].shouldBeEmpty()
    }

    @Test
    fun `delete list - not found`() = driver { client ->
        client.delete("/v1/lists/${UUID.randomUUID()}") {
            withUser(driver.defaultUserId)
        }.status shouldBe HttpStatusCode.NotFound
    }

    @Test
    fun `update list`() = driver { client ->
        val list = driver.createList()
        val data = ShoppingListDataDtoV1(
            name = "Stuff"
        )

        val response = client.put("/v1/lists/${list.listId}") {
            withUser(list.userId)
            withJson(data)
        }

        response.status shouldBe HttpStatusCode.OK
        response.body<ShoppingListDtoV1>() should { updated ->
            updated.listId shouldBe list.listId.toString()
            updated.name shouldBe data.name
        }

        driver.listsDao[driver.defaultUserId].shouldContainExactly(list.copy(name = ShoppingListName.of("Stuff")))
    }

    @Test
    fun `add item to list`() = driver { client ->
        val list = driver.createList()
        val data = ShoppingItemDataDtoV1(
            name = "iced tea",
            completed = false
        )

        val response = client.post("/v1/lists/${list.listId}/items") {
            withUser(list.userId)
            withJson(data)
        }

        response.status shouldBe HttpStatusCode.OK
        response.body<ShoppingItemDtoV1>() should { item ->
            item.name shouldBe data.name
            item.listId shouldBe list.listId.toString()
            driver.itemsDao[list.listId].map { it.toDtoV1() }.shouldContainExactly(item)
        }
    }

    @Test
    fun `get items`() = driver { client ->
        val list = driver.createList()
        val item1 = driver.createItem(list)
        val item2 = driver.createItem(list)

        val response = client.get("/v1/lists/${list.listId}/items") {
            withUser(list.userId)
        }

        response.status shouldBe HttpStatusCode.OK
        response.body<List<ShoppingItemDtoV1>>().shouldContainExactlyInAnyOrder(
            item1.toDtoV1(), item2.toDtoV1()
        )
    }

    @Test
    fun `update item`() = driver { client ->
        val list = driver.createList()
        val item1 = driver.createItem(list)
        val item2 = driver.createItem(list)

        val data = ShoppingItemDataDtoV1(name = "chips", completed = false)

        val response = client.put("v1/lists/${list.listId}/items/${item2.itemId}") {
            withUser(list.userId)
            withJson(data)
        }

        response.status shouldBe HttpStatusCode.OK
        response.body<ShoppingItemDtoV1>() shouldBe item2.toDtoV1().copy(name = data.name)

        driver.itemsDao[list.listId].toList().shouldContainExactlyInAnyOrder(
            item1, item2.copy(name = ShoppingItemName.of("chips"))
        )
    }

    @Test
    fun `delete item`() = driver { client ->
        val list = driver.createList()
        val item1 = driver.createItem(list)
        val item2 = driver.createItem(list)

        client.delete("/v1/lists/${list.listId}/items/${item2.itemId}") {
            withUser(list.userId)
        }.status shouldBe HttpStatusCode.OK

        driver.itemsDao[list.listId].toList().shouldContainExactlyInAnyOrder(item1)
    }
}

private fun HttpRequestBuilder.withJson(body: Any) {
    contentType(ContentType.Application.Json)
    setBody(body)
}

private fun HttpMessageBuilder.withUser(userId: UserId) {
    header("Authorization", "Bearer $userId")
}
