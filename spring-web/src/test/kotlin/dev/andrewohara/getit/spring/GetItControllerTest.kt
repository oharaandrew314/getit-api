package dev.andrewohara.getit.spring

import dev.andrewohara.getit.ShoppingItem
import dev.andrewohara.getit.ShoppingItemName
import dev.andrewohara.getit.ShoppingList
import dev.andrewohara.getit.ShoppingListName
import dev.andrewohara.getit.UserId
import dev.andrewohara.getit.api.ShoppingItemDataDtoV1
import dev.andrewohara.getit.api.ShoppingItemDtoV1
import dev.andrewohara.getit.api.ShoppingListDataDtoV1
import dev.andrewohara.getit.api.ShoppingListDtoV1
import dev.andrewohara.getit.api.toDtoV1
import dev.andrewohara.getit.dao.DynamoItemsDao
import dev.andrewohara.getit.dao.DynamoListsDao
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.boot.test.web.client.exchange
import org.springframework.boot.test.web.client.getForEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.RequestEntity
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.annotation.DirtiesContext.ClassMode
import org.springframework.test.context.ActiveProfiles
import java.net.URI

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
class GetItControllerTest(
    @Autowired val restTemplate: TestRestTemplate,
    @Autowired val items: DynamoItemsDao,
    @Autowired val lists: DynamoListsDao
) {

    private val user1 = UserId.of("user1")
    private val user2 = UserId.of("user2")

    @Test
    fun `get lists - unauthorized`() {
        val response = restTemplate.getForEntity<List<ShoppingListDtoV1>>("/v1/lists")
        response.statusCode shouldBe HttpStatus.UNAUTHORIZED
    }

    @Test
    fun `get lists - authorized`() {
        val list1 = user1.withList("List 1")
        val list2 = user1.withList("List 2")

        val request = RequestEntity<Unit>(
            user1.toAuthHeaders(),
            HttpMethod.GET,
            URI.create("/v1/lists")
        )

        val response = restTemplate.exchange<List<ShoppingListDtoV1>>(request)
        response.statusCode shouldBe HttpStatus.OK
        response.body.shouldContainExactlyInAnyOrder(list1.toDtoV1(), list2.toDtoV1())
    }

    @Test
    fun `create list`() {
        val request = RequestEntity(
            ShoppingListDataDtoV1("new list"),
            user1.toAuthHeaders(),
            HttpMethod.POST,
            URI.create("/v1/lists")
        )

        val response = restTemplate.exchange<ShoppingListDtoV1>(request)
        response.statusCode shouldBe HttpStatus.OK

        val created = response.body.shouldNotBeNull()
        created.name shouldBe "new list"
        created.userId shouldBe user1.toString()

        lists[user1].map { it.toDtoV1() }.toList().shouldContainExactly(created)
    }

    @Test
    fun `delete list`() {
        val list1 = user1.withList("List 1")
        val list2 = user1.withList("List 2")

        val request = RequestEntity<Unit>(
            user1.toAuthHeaders(),
            HttpMethod.DELETE,
            URI.create("/v1/lists/${list1.listId}")
        )

        val response = restTemplate.exchange<Unit>(request)
        response.statusCode shouldBe HttpStatus.OK

        lists[user1].toList().shouldContainExactly(list2)
    }

    @Test
    fun `delete list - other user`() {
        val list1 = user1.withList("List 1")
        val list2 = user1.withList("List 2")

        val request = RequestEntity<Unit>(
            user2.toAuthHeaders(),
            HttpMethod.DELETE,
            URI.create("/v1/lists/${list1.listId}")
        )

        val response = restTemplate.exchange<Unit>(request)
        response.statusCode shouldBe HttpStatus.NOT_FOUND

        lists[user1].toList().shouldContainExactlyInAnyOrder(list1, list2)
    }

    @Test
    fun `update list`() {
        val list1 = user1.withList("List 1")
        val list2 = user1.withList("List 2")

        val request = RequestEntity(
            ShoppingListDataDtoV1("updated list"),
            user1.toAuthHeaders(),
            HttpMethod.PUT,
            URI.create("/v1/lists/${list1.listId}")
        )

        val response = restTemplate.exchange<ShoppingListDtoV1>(request)
        response.statusCode shouldBe HttpStatus.OK
        response.body shouldBe list1.toDtoV1().copy(name = "updated list")

        lists[user1].toList().shouldContainExactlyInAnyOrder(
            list1.copy(name = ShoppingListName.of("updated list")),
            list2
        )
    }

    @Test
    fun `add item to list`() {
        val list1 = user1.withList("List 1")
        val item1 = list1.withItem("bacon")

        val request = RequestEntity(
            ShoppingItemDataDtoV1("eggs", false),
            user1.toAuthHeaders(),
            HttpMethod.POST,
            URI.create("/v1/lists/${list1.listId}/items")
        )

        val response = restTemplate.exchange<ShoppingItemDtoV1>(request)
        response.statusCode shouldBe HttpStatus.OK

        val created = response.body.shouldNotBeNull()
        created.listId shouldBe list1.listId.toString()
        created.name shouldBe "eggs"
        created.completed shouldBe false

        items[list1.listId]
            .map { it.toDtoV1() }
            .toList()
            .shouldContainExactlyInAnyOrder(item1.toDtoV1(), created)
    }

    @Test
    fun `get items`() {
        val list1 = user1.withList("List 1")
        val item1 = list1.withItem("bacon")
        val item2 = list1.withItem("eggs")

        val request = RequestEntity<Unit>(
            user1.toAuthHeaders(),
            HttpMethod.GET,
            URI.create("/v1/lists/${list1.listId}/items")
        )

        val response = restTemplate.exchange<List<ShoppingItemDtoV1>>(request)
        response.statusCode shouldBe HttpStatus.OK
        response.body.shouldNotBeNull().shouldContainExactlyInAnyOrder(
            item1.toDtoV1(),
            item2.toDtoV1()
        )
    }

    @Test
    fun `update item`() {
        val list1 = user1.withList("List 1")
        val item1 = list1.withItem("bacon")
        val item2 = list1.withItem("eggs")

        val request = RequestEntity(
            ShoppingItemDataDtoV1(name = "bacon", completed = true),
            user1.toAuthHeaders(),
            HttpMethod.PUT,
            URI.create("/v1/lists/${list1.listId}/items/${item1.itemId}")
        )

        val response = restTemplate.exchange<ShoppingItemDtoV1>(request)
        response.statusCode shouldBe HttpStatus.OK
        val updated = response.body.shouldNotBeNull()
        updated shouldBe item1.toDtoV1().copy(completed = true)

        items[list1.listId].toList().shouldContainExactlyInAnyOrder(
            item1.copy(completed = true),
            item2
        )
    }

    @Test
    fun `delete item`() {
        val list1 = user1.withList("List 1")
        val item1 = list1.withItem("bacon")
        val item2 = list1.withItem("eggs")

        val request = RequestEntity<Unit>(
            user1.toAuthHeaders(),
            HttpMethod.DELETE,
            URI.create("/v1/lists/${list1.listId}/items/${item1.itemId}")
        )

        val response = restTemplate.exchange<Unit>(request)
        response.statusCode shouldBe HttpStatus.OK

        items[list1.listId].toList().shouldContainExactlyInAnyOrder(item2)
    }

    private fun UserId.withList(name: String) =
        ShoppingList(this, ShoppingListName.of(name))
            .also(lists::plusAssign)

    private fun ShoppingList.withItem(name: String, completed: Boolean = false) =
        ShoppingItem(listId, ShoppingItemName.of(name), completed = completed)
            .also(items::plusAssign)
}

private fun UserId.toAuthHeaders() = HttpHeaders().apply {
    set("Authorization", "Bearer $value")
}
