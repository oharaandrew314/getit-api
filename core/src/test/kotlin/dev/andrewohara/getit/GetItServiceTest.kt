package dev.andrewohara.getit

import dev.mrbergin.kotest.result4k.shouldBeFailure
import dev.mrbergin.kotest.result4k.shouldBeSuccess
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import java.util.UUID

class GetItServiceTest {

    private val driver = TestDriver()
    private val testObj = driver.service

    @Test
    fun `create list`() {
        testObj.createList(
            userId = driver.defaultUserId,
            data = ShoppingListData(ShoppingListName.of("Food"))
        ).shouldBeSuccess { list ->
            list.name shouldBe ShoppingListName.of("Food")

            driver.listsDao[driver.defaultUserId].toList().shouldContainExactly(list)
        }
    }

    @Test
    fun `get lists for user`() {
        val list1 = driver.createList(driver.defaultUserId)
        val list2 = driver.createList(driver.defaultUserId)
        driver.createList(UserId.of("otherUser"))

        testObj.getLists(driver.defaultUserId) shouldBeSuccess {
            it.shouldContainExactlyInAnyOrder(list1, list2)
        }
    }

    @Test
    fun `delete list`() {
        val list1 = driver.createList(driver.defaultUserId)
        val list2 = driver.createList(driver.defaultUserId)

        testObj.deleteList(driver.defaultUserId, list1.listId) shouldBeSuccess list1

        driver.listsDao[driver.defaultUserId].toList().shouldContainExactly(list2)
    }

    @Test
    fun `delete list as wrong user`() {
        val list = driver.createList(driver.defaultUserId)
        val otherUser = UserId.of("otherUser")

        testObj.deleteList(otherUser, list.listId) shouldBeFailure ListNotFound(otherUser, list.listId)
    }

    @Test
    fun `delete missing list`() {
        val listId = ShoppingListId.of(UUID.randomUUID())
        testObj.deleteList(driver.defaultUserId, listId) shouldBeFailure ListNotFound(driver.defaultUserId, listId)
    }

    @Test
    fun `add item to missing list`() {
        val listId = ShoppingListId.of(UUID.randomUUID())
        val data = ShoppingItemData(
            name = ShoppingItemName.of("chips"),
            completed = false
        )
        testObj.addItem(driver.defaultUserId, listId, data) shouldBeFailure ListNotFound(driver.defaultUserId, listId)
    }

    @Test
    fun `add item to list as wrong user`() {
        val list = driver.createList(driver.defaultUserId)
        val otherUser = UserId.of("otherUser")
        val data = ShoppingItemData(
            name = ShoppingItemName.of("chips"),
            completed = false
        )

        testObj.addItem(otherUser, list.listId, data) shouldBeFailure ListNotFound(otherUser, list.listId)
    }

    @Test
    fun `add item to list`() {
        val list = driver.createList(driver.defaultUserId)
        val item1 = driver.createItem(list, "chips")
        val data = ShoppingItemData(
            name = ShoppingItemName.of("iced tea"),
            completed = false
        )

        testObj.addItem(driver.defaultUserId, list.listId, data) shouldBeSuccess { item2 ->
            item2.name shouldBe data.name
            driver.itemsDao[list.listId].toList().shouldContainExactlyInAnyOrder(item1, item2)
        }
    }

    @Test
    fun `get items for missing list`() {
        val listId = ShoppingListId.of(UUID.randomUUID())
        testObj.getItems(driver.defaultUserId, listId) shouldBeFailure ListNotFound(driver.defaultUserId, listId)
    }

    @Test
    fun `get items for list as wrong user`() {
        val list = driver.createList(driver.defaultUserId)
        val otherUser = UserId.of("otherUser")

        testObj.getItems(otherUser, list.listId) shouldBeFailure ListNotFound(otherUser, list.listId)
    }

    @Test
    fun `get items for list`() {
        val list = driver.createList(driver.defaultUserId)
        val item1 = driver.createItem(list, "chips")
        val item2 = driver.createItem(list, "iced tea")

        testObj.getItems(list.userId, list.listId) shouldBeSuccess {
            it.shouldContainExactlyInAnyOrder(item1, item2)
        }
    }

    @Test
    fun `update missing item`() {
        val list = driver.createList()
        val itemId = ShoppingItemId.of(UUID.randomUUID())
        testObj.updateItem(list.userId, list.listId, itemId, itemData) shouldBeFailure
            ItemNotFound(list.listId, itemId)
    }

    @Test
    fun `update item for wrong list`() {
        val list = driver.createList()
        val item = driver.createItem(list)

        val otherList = driver.createList()

        testObj.updateItem(list.userId, otherList.listId, item.itemId, itemData) shouldBeFailure
            ItemNotFound(otherList.listId, item.itemId)
    }

    @Test
    fun `update item for wrong user`() {
        val list = driver.createList()
        val item = driver.createItem(list)

        testObj.updateItem(otherUserId, list.listId, item.itemId, itemData) shouldBeFailure
            ListNotFound(otherUserId, list.listId)
    }

    @Test
    fun `update item`() {
        val list = driver.createList(driver.defaultUserId)
        val item1 = driver.createItem(list, "chips")
        val item2 = driver.createItem(list, "iced tea")

        val data = ShoppingItemData(
            name = ShoppingItemName.of("Jarritos"),
            completed = true
        )

        testObj.updateItem(list.userId, list.listId, item2.itemId, data) shouldBeSuccess { updated ->
            updated shouldBe item2.copy(
                name = ShoppingItemName.of("Jarritos"),
                completed = true
            )
            driver.itemsDao[list.listId].toList().shouldContainExactlyInAnyOrder(item1, updated)
        }
    }

    @Test
    fun `delete missing item`() {
        val list = driver.createList()
        val itemId = ShoppingItemId.of(UUID.randomUUID())
        testObj.deleteItem(list.userId, list.listId, itemId) shouldBeFailure ItemNotFound(list.listId, itemId)
    }

    @Test
    fun `delete item for wrong user`() {
        val list = driver.createList()
        val item = driver.createItem(list)
        testObj.deleteItem(otherUserId, list.listId, item.itemId) shouldBeFailure ListNotFound(otherUserId, list.listId)
    }

    @Test
    fun `delete item`() {
        val list = driver.createList(driver.defaultUserId)
        val item1 = driver.createItem(list, "chips")
        val item2 = driver.createItem(list, "iced tea")

        testObj.deleteItem(list.userId, list.listId, item2.itemId) shouldBeSuccess item2
        driver.itemsDao[list.listId].toList().shouldContainExactly(item1)
    }

    @Test
    fun `update missing list`() {
        val listId = ShoppingListId.of(UUID.randomUUID())
        val data = ShoppingListData(name = ShoppingListName.of("Cool Stuff"))

        testObj.updateList(driver.defaultUserId, listId, data) shouldBeFailure
            ListNotFound(driver.defaultUserId, listId)
    }

    @Test
    fun `update list as wrong user`() {
        val list = driver.createList(driver.defaultUserId)
        val otherUser = UserId.of("otherUser")
        val data = ShoppingListData(name = ShoppingListName.of("Cool Stuff"))

        testObj.updateList(otherUser, list.listId, data) shouldBeFailure
            ListNotFound(otherUser, list.listId)
    }

    @Test
    fun `update shopping list`() {
        val list = driver.createList(driver.defaultUserId)
        val data = ShoppingListData(name = ShoppingListName.of("Cool Stuff"))

        val expected = list.copy(name = data.name)
        testObj.updateList(list.userId, list.listId, data) shouldBeSuccess expected
        driver.listsDao[list.userId, list.listId] shouldBe expected
    }
}
