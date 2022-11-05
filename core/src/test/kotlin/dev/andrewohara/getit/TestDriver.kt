package dev.andrewohara.getit

import dev.andrewohara.getit.dao.DynamoItemsDao
import dev.andrewohara.getit.dao.DynamoShoppingListDao
import org.http4k.connect.amazon.dynamodb.FakeDynamoDb
import org.http4k.connect.amazon.dynamodb.model.TableName

class TestDriver {

    private val dynamoDb = FakeDynamoDb().client()

    val defaultUserId = UserId.of("123")

    val listsDao = createListsMapper(dynamoDb, TableName.of("lists"),)
        .also { it.createTable() }
        .let { DynamoShoppingListDao(it) }

    val itemsDao = createItemsMapper(dynamoDb, TableName.of("items"))
        .also { it.createTable() }
        .let { DynamoItemsDao(it) }

    val service = GetItService(listsDao, itemsDao)

    fun createList(
        userId: UserId = defaultUserId,
        name: String = "my list",
        vararg items: String
    ): ShoppingList {
        val list = ShoppingList(
            userId = userId,
            name = ShoppingListName.of(name)
        )
        listsDao += list

        for (itemName in items) {
            createItem(list, itemName)
        }

        return list
    }

    fun createItem(
        list: ShoppingList,
        name: String = "chips"
    ): ShoppingItem {
        val item = ShoppingItem(
            listId = list.listId,
            name = ShoppingItemName.of(name)
        )
        itemsDao += item
        return item
    }
}
