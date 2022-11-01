package dev.andrewohara.getit

import dev.andrewohara.getit.api.createApi
import dev.andrewohara.getit.api.createItemsMapper
import dev.andrewohara.getit.api.createListsMapper
import dev.andrewohara.getit.api.itemsTableName
import dev.andrewohara.getit.api.listsTableName
import dev.andrewohara.getit.api.security.Authorizer
import dev.andrewohara.getit.dao.DynamoItemsDao
import dev.andrewohara.getit.dao.DynamoShoppingListDao
import org.http4k.cloudnative.env.Environment
import org.http4k.connect.amazon.dynamodb.DynamoTable
import org.http4k.connect.amazon.dynamodb.FakeDynamoDb
import org.http4k.connect.amazon.dynamodb.model.TableName
import org.http4k.connect.storage.InMemory
import org.http4k.connect.storage.Storage
import org.http4k.core.HttpHandler
import org.http4k.core.Request
import org.http4k.core.with

private val testEnv get() = Environment.ENV.with(
    listsTableName of TableName.of("lists"),
    itemsTableName of TableName.of("items")
)

class TestDriver : HttpHandler {

    private val storage: Storage<DynamoTable> = Storage.InMemory()
    private val dynamoDb = FakeDynamoDb(storage).client()
    val defaultUserId = UserId.of("123")

    val listsDao = createListsMapper(dynamoDb, testEnv)
        .also { it.createTable() }
        .let { DynamoShoppingListDao(it) }

    val itemsDao = createItemsMapper(dynamoDb, testEnv)
        .also { it.createTable() }
        .let { DynamoItemsDao(it) }

    val service = GetItService(listsDao, itemsDao)

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
