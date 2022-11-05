package dev.andrewohara.getit.dao

import dev.andrewohara.getit.ShoppingItem
import dev.andrewohara.getit.ShoppingItemId
import dev.andrewohara.getit.ShoppingListId
import org.http4k.connect.amazon.dynamodb.mapper.DynamoDbTableMapper

class DynamoItemsDao(private val table: DynamoDbTableMapper<ShoppingItem, ShoppingListId, ShoppingItemId>) {

    private val byList = table.primaryIndex()

    operator fun plusAssign(item: ShoppingItem) = table.plusAssign(item)
    operator fun minusAssign(item: ShoppingItem) = table.minusAssign(item)
    operator fun get(listId: ShoppingListId) = byList.query(listId)
    operator fun get(listId: ShoppingListId, itemId: ShoppingItemId) = table[listId, itemId]
}
