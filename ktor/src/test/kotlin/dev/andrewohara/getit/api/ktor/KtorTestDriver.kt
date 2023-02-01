package dev.andrewohara.getit.api.ktor

import dev.andrewohara.getit.GetItService
import dev.andrewohara.getit.ShoppingItem
import dev.andrewohara.getit.ShoppingItemName
import dev.andrewohara.getit.ShoppingList
import dev.andrewohara.getit.ShoppingListName
import dev.andrewohara.getit.UserId
import dev.andrewohara.getit.dao.DynamoItemsDao.Companion.itemsDao
import dev.andrewohara.getit.dao.DynamoListsDao.Companion.listsDao
import io.ktor.client.HttpClient
import io.ktor.serialization.jackson.jackson
import io.ktor.server.testing.testApplication
import org.http4k.connect.amazon.dynamodb.FakeDynamoDb
import org.http4k.connect.amazon.dynamodb.model.TableName
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation as ClientContentNegotiation

class KtorTestDriver {

    private val dynamoDb = FakeDynamoDb().client()

    val defaultUserId = UserId.of("123")

    val listsDao = dynamoDb.listsDao(TableName.of("lists"), create = true)
    val itemsDao = dynamoDb.itemsDao(TableName.of("items"), create = true)

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
                jackson()
            }
        }
        testFn(client)
    }
}
