package dev.andrewohara.getit.dao

import dev.andrewohara.getit.ShoppingItem
import dev.andrewohara.getit.ShoppingItemId
import dev.andrewohara.getit.ShoppingListId
import org.http4k.connect.amazon.dynamodb.DynamoDb
import org.http4k.connect.amazon.dynamodb.mapper.DynamoDbTableMapper
import org.http4k.connect.amazon.dynamodb.mapper.minusAssign
import org.http4k.connect.amazon.dynamodb.mapper.plusAssign
import org.http4k.connect.amazon.dynamodb.mapper.tableMapper
import org.http4k.connect.amazon.dynamodb.model.Attribute
import org.http4k.connect.amazon.dynamodb.model.TableName
import org.http4k.format.KotlinxSerialization
import java.util.UUID

class DynamoItemsDao private constructor(
    private val table: DynamoDbTableMapper<ShoppingItem, UUID, UUID>
) {
    companion object {
        fun DynamoDb.itemsDao(tableName: TableName, create: Boolean = false) =
            tableMapper<ShoppingItem, UUID, UUID>(
                tableName,
                hashKeyAttribute = Attribute.uuid().required("listId"),
                sortKeyAttribute = Attribute.uuid().required("itemId"),
                autoMarshalling = KotlinxSerialization
            )
                .also { if (create) it.createTable() }
                .let { DynamoItemsDao(it) }
    }

    private val byList = table.primaryIndex()

    operator fun plusAssign(item: ShoppingItem) = table.plusAssign(item)
    operator fun minusAssign(item: ShoppingItem) = table.minusAssign(item)
    operator fun get(listId: ShoppingListId) = byList.query(listId.value)
    operator fun get(listId: ShoppingListId, itemId: ShoppingItemId) = table[listId.value, itemId.value]
}
