package dev.andrewohara.getit

import dev.andrewohara.getit.api.Authorizer
import dev.andrewohara.getit.api.http4k.toHttp4k
import dev.andrewohara.getit.api.ktor.createAuthorization
import dev.andrewohara.getit.api.ktor.createRoutes
import dev.andrewohara.getit.dao.DynamoItemsDao
import dev.andrewohara.getit.dao.DynamoShoppingListDao
import io.ktor.client.HttpClient
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.install
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation as ClientContentNegotiation
import io.ktor.server.resources.Resources
import io.ktor.server.testing.testApplication
import org.http4k.connect.amazon.dynamodb.DynamoTable
import org.http4k.connect.amazon.dynamodb.FakeDynamoDb
import org.http4k.connect.amazon.dynamodb.model.TableName
import org.http4k.connect.storage.InMemory
import org.http4k.connect.storage.Storage
import org.http4k.core.HttpHandler
import org.http4k.core.Request

class TestDriver : HttpHandler {

    private val storage: Storage<DynamoTable> = Storage.InMemory()
    private val dynamoDb = FakeDynamoDb(storage).client()

    private val authorizer = Authorizer { UserId.of(it) }
    val defaultUserId = UserId.of("123")

    val listsDao = createListsMapper(dynamoDb, TableName.of("lists"),)
        .also { it.createTable() }
        .let { DynamoShoppingListDao(it) }

    val itemsDao = createItemsMapper(dynamoDb, TableName.of("items"))
        .also { it.createTable() }
        .let { DynamoItemsDao(it) }

    val service = GetItService(listsDao, itemsDao)

    private val api = service.toHttp4k(authorizer)
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

    operator fun invoke(testFn: suspend (HttpClient) -> Unit) = testApplication {
        application {
            install(Resources)
            install(ContentNegotiation) {
                json()
            }
            createAuthorization(authorizer)
            createRoutes(service)
        }

        val client = createClient {
            install(ClientContentNegotiation) {
                json()
            }
        }
        testFn(client)
    }
}

fun Request.withUser(userId: UserId) = header("Authorization", "Bearer $userId")
