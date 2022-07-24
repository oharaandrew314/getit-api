package dev.andrewohara.getit.dao

import dev.andrewohara.getit.ShoppingList
import dev.andrewohara.getit.ShoppingListId
import dev.andrewohara.getit.UserId
import io.andrewohara.utils.http4k.connect.dynamodb.DynamoDbTableMapper

class DynamoShoppingListDao(private val table: DynamoDbTableMapper<ShoppingList, UserId, ShoppingListId>) {

    private val byUser = table.primaryIndex()

    operator fun get(userId: UserId) = byUser.query(userId)
    operator fun get(userId: UserId, listId: ShoppingListId) = table[userId, listId]
    operator fun plusAssign(list: ShoppingList) = table.plusAssign(list)
    operator fun minusAssign(list: ShoppingList) = table.minusAssign(list)
}