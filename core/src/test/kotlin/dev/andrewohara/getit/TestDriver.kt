package dev.andrewohara.getit

import dev.andrewohara.getit.dao.DynamoItemsDao.Companion.itemsDao
import dev.andrewohara.getit.dao.DynamoListsDao.Companion.listsDao
import org.http4k.connect.amazon.dynamodb.FakeDynamoDb
import org.http4k.connect.amazon.dynamodb.model.TableName

class TestDriver {
    val defaultUserId = UserId.of("123")

    private val dynamoDb = FakeDynamoDb().client()
    val listsDao = dynamoDb.listsDao(TableName.of("lists"), create = true)
    val itemsDao = dynamoDb.itemsDao(TableName.of("items"), create = true)

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
