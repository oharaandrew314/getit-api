package dev.andrewohara.getit.api.http4k

import dev.andrewohara.getit.ShoppingItemName
import dev.andrewohara.getit.ShoppingListName
import dev.andrewohara.getit.api.ShoppingItemDataDtoV1
import dev.andrewohara.getit.api.ShoppingListDataDtoV1
import dev.andrewohara.getit.api.toDtoV1
import dev.andrewohara.getit.http4k.itemArrayV1Lens
import dev.andrewohara.getit.http4k.itemDataV1Lens
import dev.andrewohara.getit.http4k.itemV1Lens
import dev.andrewohara.getit.http4k.listArrayV1Lens
import dev.andrewohara.getit.http4k.listDataV1Lens
import dev.andrewohara.getit.http4k.listV1Lens
import io.kotest.matchers.be
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.sequences.shouldBeEmpty
import io.kotest.matchers.sequences.shouldContainExactly
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import org.http4k.core.Method
import org.http4k.core.Request
import org.http4k.core.Status
import org.http4k.core.with
import org.http4k.kotest.shouldHaveBody
import org.http4k.kotest.shouldHaveStatus
import org.junit.jupiter.api.Test
import java.util.UUID

class Http4kApiV1Test {

    private val driver = Http4kTestDriver()

    @Test
    fun `get lists - unauthorized`() {
        Request(Method.GET, "/v1/lists")
            .let(driver)
            .shouldHaveStatus(Status.UNAUTHORIZED)
    }

    @Test
    fun `get lists`() {
        val list1 = driver.createList(driver.defaultUserId)
        val list2 = driver.createList(driver.defaultUserId)

        val response = Request(Method.GET, "/v1/lists")
            .withUser(driver.defaultUserId)
            .let(driver)

        response shouldHaveStatus Status.OK
        listArrayV1Lens(response).toList().shouldContainExactlyInAnyOrder(list1.toDtoV1(), list2.toDtoV1())
    }

    @Test
    fun `create list`() {
        val data = ShoppingListDataDtoV1(
            name = ShoppingListName.of("Stuff")
        )
        val response = Request(Method.POST, "/v1/lists")
            .withUser(driver.defaultUserId)
            .with(listDataV1Lens of data)
            .let(driver)

        response shouldHaveStatus Status.OK
        listV1Lens(response).should { list ->
            list.name shouldBe data.name
            driver.listsDao[driver.defaultUserId].map { it.toDtoV1() }.shouldContainExactly(list)
        }
    }

    @Test
    fun `delete list`() {
        val list = driver.createList()

        Request(Method.DELETE, "/v1/lists/${list.listId}")
            .withUser(list.userId)
            .let(driver)
            .shouldHaveStatus(Status.OK)

        driver.listsDao[driver.defaultUserId].shouldBeEmpty()
    }

    @Test
    fun `delete list - not found`() {
        Request(Method.DELETE, "/v1/lists/${UUID.randomUUID()}")
            .withUser(driver.defaultUserId)
            .let(driver)
            .shouldHaveStatus(Status.NOT_FOUND)
    }

    @Test
    fun `update list`() {
        val list = driver.createList()
        val data = ShoppingListDataDtoV1(
            name = ShoppingListName.of("Stuff")
        )

        val response = Request(Method.PUT, "/v1/lists/${list.listId}")
            .withUser(list.userId)
            .with(listDataV1Lens of data)
            .let(driver)

        response shouldHaveStatus Status.OK
        listV1Lens(response).should {
            it.listId shouldBe list.listId
            it.name shouldBe data.name
        }
        driver.listsDao[driver.defaultUserId].shouldContainExactly(list.copy(name = ShoppingListName.of("Stuff")))
    }

    @Test
    fun `add item to list`() {
        val list = driver.createList()
        val data = ShoppingItemDataDtoV1(
            name = ShoppingItemName.of("iced tea"),
            completed = false
        )

        val response = Request(Method.POST, "/v1/lists/${list.listId}/items")
            .withUser(list.userId)
            .with(itemDataV1Lens of data)
            .let(driver)

        response shouldHaveStatus Status.OK
        itemV1Lens(response).should { item ->
            item.name shouldBe data.name
            item.listId shouldBe list.listId
            driver.itemsDao[list.listId].map { it.toDtoV1() }.shouldContainExactly(item)
        }
    }

    @Test
    fun `get items`() {
        val list = driver.createList()
        val item1 = driver.createItem(list)
        val item2 = driver.createItem(list)

        val response = Request(Method.GET, "/v1/lists/${list.listId}/items")
            .withUser(list.userId)
            .let(driver)

        response shouldHaveStatus Status.OK
        itemArrayV1Lens(response).toList().shouldContainExactlyInAnyOrder(
            item1.toDtoV1(), item2.toDtoV1()
        )
    }

    @Test
    fun `update item`() {
        val list = driver.createList()
        val item1 = driver.createItem(list)
        val item2 = driver.createItem(list)

        val data = ShoppingItemDataDtoV1(name = ShoppingItemName.of("chips"), completed = false)

        val response = Request(Method.PUT, "/v1/lists/${list.listId}/items/${item2.itemId}")
            .withUser(list.userId)
            .with(itemDataV1Lens of data)
            .let(driver)

        response shouldHaveStatus Status.OK
        response.shouldHaveBody(itemV1Lens, be(item2.toDtoV1().copy(name = data.name)))
        driver.itemsDao[list.listId].toList().shouldContainExactlyInAnyOrder(
            item1, item2.copy(name = ShoppingItemName.of("chips"))
        )
    }

    @Test
    fun `delete item`() {
        val list = driver.createList()
        val item1 = driver.createItem(list)
        val item2 = driver.createItem(list)

        Request(Method.DELETE, "/v1/lists/${list.listId}/items/${item2.itemId}")
            .withUser(list.userId)
            .let(driver)
            .shouldHaveStatus(Status.OK)

        driver.itemsDao[list.listId].toList().shouldContainExactlyInAnyOrder(item1)
    }

    @Test
    fun `render openapi spec`() {
        Request(Method.GET, "/openapi")
            .let(driver)
            .shouldHaveStatus(Status.OK)
    }
}
