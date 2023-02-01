package dev.andrewohara.getit.dao

import dev.andrewohara.getit.GetItMoshi
import dev.andrewohara.getit.ShoppingList
import dev.andrewohara.getit.ShoppingListId
import dev.andrewohara.getit.ShoppingListName
import dev.andrewohara.getit.UserId
import org.http4k.connect.amazon.dynamodb.DynamoDb
import org.http4k.connect.amazon.dynamodb.mapper.DynamoDbTableMapper
import org.http4k.connect.amazon.dynamodb.mapper.minusAssign
import org.http4k.connect.amazon.dynamodb.mapper.plusAssign
import org.http4k.connect.amazon.dynamodb.mapper.tableMapper
import org.http4k.connect.amazon.dynamodb.model.Attribute
import org.http4k.connect.amazon.dynamodb.model.TableName
import java.util.UUID

class DynamoListsDao private constructor(
    private val table: DynamoDbTableMapper<DynamoShoppingList, String, UUID>
) {
    companion object {
        fun DynamoDb.listsDao(tableName: TableName, create: Boolean = false) =
            tableMapper<DynamoShoppingList, String, UUID>(
                tableName,
                hashKeyAttribute = Attribute.string().required("userId"),
                sortKeyAttribute = Attribute.uuid().required("listId"),
                autoMarshalling = GetItMoshi
            )
                .also { if (create) it.createTable() }
                .let { DynamoListsDao(it) }
    }

    private val byUser = table.primaryIndex()

    operator fun get(userId: UserId) = byUser.query(userId.value).map { it.toModel() }
    operator fun get(userId: UserId, listId: ShoppingListId) = table[userId.value, listId.value]?.toModel()
    operator fun plusAssign(list: ShoppingList) = table.plusAssign(list.toDynamo())
    operator fun minusAssign(list: ShoppingList) = table.minusAssign(list.toDynamo())
}

private data class DynamoShoppingList(
    val userId: String,
    val name: String,
    val listId: String
)

private fun DynamoShoppingList.toModel() = ShoppingList(
    userId = UserId.of(userId),
    listId = ShoppingListId.parse(listId),
    name = ShoppingListName.of(name)
)

private fun ShoppingList.toDynamo() = DynamoShoppingList(
    userId = userId.value,
    listId = listId.value.toString(),
    name = name.value
)
