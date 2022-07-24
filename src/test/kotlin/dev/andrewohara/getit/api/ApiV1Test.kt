package dev.andrewohara.getit.api

import dev.andrewohara.getit.*
import dev.andrewohara.getit.api.v1.*
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.sequences.shouldBeEmpty
import io.kotest.matchers.sequences.shouldContainExactly
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import org.http4k.core.Method
import org.http4k.core.Request
import org.http4k.core.Status
import org.http4k.core.with
import org.http4k.kotest.shouldHaveStatus
import org.junit.jupiter.api.Test

class ApiV1Test {

    private val driver = TestDriver()

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
            it.listId  shouldBe list.listId
            it.name shouldBe data.name
        }
        driver.listsDao[driver.defaultUserId].shouldContainExactly(list.copy(name = data.name))
    }

    @Test
    fun `add item to list`() {
        val list = driver.createList()
        val data = ShoppingItemDataDtoV1(
            name = ShoppingItemName.of("iced tea")
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
}