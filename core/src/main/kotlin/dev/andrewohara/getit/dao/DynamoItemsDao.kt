package dev.andrewohara.getit.dao

import dev.andrewohara.getit.GetItMoshi
import dev.andrewohara.getit.ShoppingItem
import dev.andrewohara.getit.ShoppingItemId
import dev.andrewohara.getit.ShoppingItemName
import dev.andrewohara.getit.ShoppingListId
import org.http4k.connect.amazon.dynamodb.DynamoDb
import org.http4k.connect.amazon.dynamodb.mapper.DynamoDbTableMapper
import org.http4k.connect.amazon.dynamodb.mapper.minusAssign
import org.http4k.connect.amazon.dynamodb.mapper.plusAssign
import org.http4k.connect.amazon.dynamodb.mapper.tableMapper
import org.http4k.connect.amazon.dynamodb.model.Attribute
import org.http4k.connect.amazon.dynamodb.model.TableName
import java.util.UUID

class DynamoItemsDao private constructor(
    private val table: DynamoDbTableMapper<DynamoShoppingItem, UUID, UUID>
) {
    companion object {
        fun DynamoDb.itemsDao(tableName: TableName, create: Boolean = false) =
            tableMapper<DynamoShoppingItem, UUID, UUID>(
                tableName,
                hashKeyAttribute = Attribute.uuid().required("listId"),
                sortKeyAttribute = Attribute.uuid().required("itemId"),
                autoMarshalling = GetItMoshi
            )
                .also { if (create) it.createTable() }
                .let { DynamoItemsDao(it) }
    }

    private val byList = table.primaryIndex()

    operator fun plusAssign(item: ShoppingItem) = table.plusAssign(item.toDynamo())
    operator fun minusAssign(item: ShoppingItem) = table.minusAssign(item.toDynamo())
    operator fun get(listId: ShoppingListId) = byList.query(listId.value).map { it.toModel() }
    operator fun get(listId: ShoppingListId, itemId: ShoppingItemId) = table[listId.value, itemId.value]?.toModel()
}

private data class DynamoShoppingItem(
    val listId: UUID,
    val name: String,
    val itemId: UUID,
    val completed: Boolean
)

private fun DynamoShoppingItem.toModel() = ShoppingItem(
    listId = ShoppingListId.of(listId),
    itemId = ShoppingItemId.of(itemId),
    name = ShoppingItemName.of(name),
    completed = completed
)

private fun ShoppingItem.toDynamo() = DynamoShoppingItem(
    listId = listId.value,
    itemId = itemId.value,
    name = name.value,
    completed = completed
)
