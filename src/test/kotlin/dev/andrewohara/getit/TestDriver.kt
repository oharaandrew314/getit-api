package dev.andrewohara.getit

import dev.andrewohara.getit.api.createItemsMapper
import dev.andrewohara.getit.api.createListsMapper
import dev.andrewohara.getit.api.security.Authorizer
import dev.andrewohara.getit.api.toHttp4k
import dev.andrewohara.getit.api.v1.bearer
import dev.andrewohara.getit.api.v1.createAuthorization
import dev.andrewohara.getit.api.v1.createRoutes
import dev.andrewohara.getit.dao.DynamoItemsDao
import dev.andrewohara.getit.dao.DynamoShoppingListDao
import io.ktor.client.HttpClient
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.install
import io.ktor.server.auth.Authentication
import io.ktor.server.auth.basic
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
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
        install(ContentNegotiation) {
            json()
        }

        application {
            install(Resources)
            install(Authentication) {
                basic {
                }
            }
            // createAuthorization(authorizer)
            createRoutes(service)
        }

        val client = createClient {}
        testFn(client)
    }
}

fun Request.withUser(userId: UserId) = header("Authorization", "Bearer $userId")
