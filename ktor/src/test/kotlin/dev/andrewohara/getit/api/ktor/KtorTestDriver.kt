package dev.andrewohara.getit.api.ktor

import dev.andrewohara.getit.GetItService
import dev.andrewohara.getit.ShoppingItem
import dev.andrewohara.getit.ShoppingItemName
import dev.andrewohara.getit.ShoppingList
import dev.andrewohara.getit.ShoppingListName
import dev.andrewohara.getit.UserId
import dev.andrewohara.getit.createItemsMapper
import dev.andrewohara.getit.createListsMapper
import dev.andrewohara.getit.dao.DynamoItemsDao
import dev.andrewohara.getit.dao.DynamoShoppingListDao
import io.ktor.client.HttpClient
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.testing.testApplication
import org.http4k.connect.amazon.dynamodb.FakeDynamoDb
import org.http4k.connect.amazon.dynamodb.model.TableName
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation as ClientContentNegotiation

class KtorTestDriver {

    private val dynamoDb = FakeDynamoDb().client()

    val defaultUserId = UserId.of("123")

    val listsDao = createListsMapper(dynamoDb, TableName.of("lists"),)
        .also { it.createTable() }
        .let { DynamoShoppingListDao(it) }

    val itemsDao = createItemsMapper(dynamoDb, TableName.of("items"))
        .also { it.createTable() }
        .let { DynamoItemsDao(it) }

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

    operator fun invoke(testFn: suspend (HttpClient) -> Unit) = testApplication {
        application {
            installGetIt(GetItService(listsDao, itemsDao))
        }

        val client = createClient {
            install(ClientContentNegotiation) {
                json()
            }
        }
        testFn(client)
    }
}
