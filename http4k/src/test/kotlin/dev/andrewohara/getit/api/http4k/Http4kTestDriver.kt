package dev.andrewohara.getit.api.http4k

import dev.andrewohara.getit.GetItService
import dev.andrewohara.getit.ShoppingItem
import dev.andrewohara.getit.ShoppingItemName
import dev.andrewohara.getit.ShoppingList
import dev.andrewohara.getit.ShoppingListName
import dev.andrewohara.getit.UserId
import dev.andrewohara.getit.dao.DynamoItemsDao.Companion.itemsDao
import dev.andrewohara.getit.dao.DynamoListsDao.Companion.listsDao
import dev.andrewohara.getit.http4k.toHttp4k
import org.http4k.connect.amazon.dynamodb.FakeDynamoDb
import org.http4k.connect.amazon.dynamodb.model.TableName
import org.http4k.core.HttpHandler
import org.http4k.core.Request

class Http4kTestDriver : HttpHandler {
    private val dynamoDb = FakeDynamoDb().client()

    val defaultUserId = UserId.of("123")

    val listsDao = dynamoDb.listsDao(TableName.of("lists"), create = true)
    val itemsDao = dynamoDb.itemsDao(TableName.of("items"), create = true)

    private val http = GetItService(listsDao, itemsDao).toHttp4k(null) { UserId.of(it) }
    override fun invoke(request: Request) = http(request)

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

fun Request.withUser(userId: UserId) = header("Authorization", "Bearer $userId")
