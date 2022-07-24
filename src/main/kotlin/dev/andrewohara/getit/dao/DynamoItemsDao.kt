package dev.andrewohara.getit.dao

import dev.andrewohara.getit.ShoppingItemId
import dev.andrewohara.getit.ShoppingItem
import dev.andrewohara.getit.ShoppingListId
import io.andrewohara.utils.http4k.connect.dynamodb.DynamoDbTableMapper

class DynamoItemsDao(private val table: DynamoDbTableMapper<ShoppingItem, ShoppingItemId, Unit>) {

    private val byList = table.index(itemsByList)

    operator fun plusAssign(item: ShoppingItem) = table.plusAssign(item)
    operator fun minusAssign(item: ShoppingItem) = table.minusAssign(item)
    operator fun get(listId: ShoppingListId) = byList.query(listId)
    operator fun get(itemId: ShoppingItemId) = table[itemId]
}