package dev.andrewohara.getit.dao

import dev.andrewohara.getit.ShoppingList
import dev.andrewohara.getit.ShoppingListId
import dev.andrewohara.getit.UserId
import org.http4k.connect.amazon.dynamodb.DynamoDb
import org.http4k.connect.amazon.dynamodb.mapper.DynamoDbTableMapper
import org.http4k.connect.amazon.dynamodb.mapper.minusAssign
import org.http4k.connect.amazon.dynamodb.mapper.plusAssign
import org.http4k.connect.amazon.dynamodb.mapper.tableMapper
import org.http4k.connect.amazon.dynamodb.model.Attribute
import org.http4k.connect.amazon.dynamodb.model.TableName
import org.http4k.format.KotlinxSerialization
import java.util.UUID

class DynamoListsDao private constructor(
    private val table: DynamoDbTableMapper<ShoppingList, String, UUID>
) {
    companion object {
        fun DynamoDb.listsDao(tableName: TableName, create: Boolean = false) =
            tableMapper<ShoppingList, String, UUID>(
                tableName,
                hashKeyAttribute = Attribute.string().required("userId"),
                sortKeyAttribute = Attribute.uuid().required("listId"),
                autoMarshalling = KotlinxSerialization
            )
                .also { if (create) it.createTable() }
                .let { DynamoListsDao(it) }
    }

    private val byUser = table.primaryIndex()

    operator fun get(userId: UserId) = byUser.query(userId.value)
    operator fun get(userId: UserId, listId: ShoppingListId) = table[userId.value, listId.value]
    operator fun plusAssign(list: ShoppingList) = table.plusAssign(list)
    operator fun minusAssign(list: ShoppingList) = table.minusAssign(list)
}
