package dev.andrewohara.getit

import dev.andrewohara.getit.api.createApi
import dev.andrewohara.getit.api.security.Authorizer
import dev.andrewohara.getit.dao.*
import io.andrewohara.utils.http4k.connect.dynamodb.tableMapper
import org.http4k.connect.amazon.dynamodb.DynamoTable
import org.http4k.connect.amazon.dynamodb.FakeDynamoDb
import org.http4k.connect.amazon.dynamodb.model.TableName
import org.http4k.connect.storage.InMemory
import org.http4k.connect.storage.Storage
import org.http4k.core.HttpHandler
import org.http4k.core.Request

class TestDriver: HttpHandler {

    private val storage: Storage<DynamoTable> = Storage.InMemory()
    private val dynamoDb = FakeDynamoDb(storage).client()
    val defaultUserId = UserId.of("123")

    val listsDao = dynamoDb
        .tableMapper<ShoppingList, UserId, ShoppingListId>(TableName.of("lists"), userIdAttr, listIdAttr, autoMarshalling = GetItMoshi)
        .also { it.createTable() }
        .let { DynamoShoppingListDao(it) }

    val itemsDao = dynamoDb
        .tableMapper<ShoppingItem, ShoppingListId, ShoppingItemId>(TableName.of("items"), listIdAttr, itemIdAttr, autoMarshalling = GetItMoshi)
        .also { it.createTable() }
        .let { DynamoItemsDao(it) }

    val service = GetItService(
        lists = listsDao,
        items = itemsDao
    )

    private val api = createApi(service, Authorizer.fake())
    override fun invoke(request: Request) = api(request)

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
fun Authorizer.Companion.fake() = Authorizer { UserId.of(it) }