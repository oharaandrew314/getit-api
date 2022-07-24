package dev.andrewohara.getit

import dev.andrewohara.getit.dao.DynamoItemsDao
import dev.andrewohara.getit.dao.DynamoShoppingListDao
import dev.forkhandles.result4k.*

class GetItService(private val lists: DynamoShoppingListDao, private val items: DynamoItemsDao) {

    // lists

    fun createList(userId: UserId, data: ShoppingListData): Result<ShoppingList, ShoppingError> {
        val list = ShoppingList(userId)(data)
        lists += list
        return Success(list)
    }

    fun getLists(userId: UserId): Result<List<ShoppingList>, ShoppingError> {
        val lists = lists[userId].toList()
        return Success(lists)
    }

    fun deleteList(userId: UserId, listId: ShoppingListId): Result<ShoppingList, ShoppingError> {
        return lists[userId, listId]
            .asResultOr { ListNotFound(userId, listId) }
            .peek { lists -= it }
    }

    fun updateList(userId: UserId, listId: ShoppingListId, data: ShoppingListData): Result<ShoppingList, ShoppingError> {
        return lists[userId, listId]
            .asResultOr { ListNotFound(userId, listId) }
            .map { list -> list(data) }
            .peek { list -> lists += list }
    }

    // items

    fun getItems(userId: UserId, listId: ShoppingListId): Result<List<ShoppingItem>, ShoppingError> {
        return lists[userId, listId]
            .asResultOr { ListNotFound(userId, listId) }
            .map { items[listId].toList() }
    }

    fun addItem(userId: UserId, listId: ShoppingListId, data: ShoppingItemData): Result<ShoppingItem, ShoppingError> {
        return lists[userId, listId]
            .asResultOr { ListNotFound(userId, listId) }
            .map { ShoppingItem(listId)(data) }
            .peek { item -> items += item }
    }

    fun completeItem(itemId: ShoppingItemId): Result<ShoppingItem, ShoppingError>  {
        return items[itemId]
            .asResultOr { ItemNotFound(itemId) }
            .map { item ->
                val updated = item.copy(completed = true)
                items += updated
                updated
            }
    }

    fun deleteItem(itemId: ShoppingItemId): Result<ShoppingItem, ShoppingError> {
        return items[itemId]
            .asResultOr { ItemNotFound(itemId) }
            .peek { items -= it }
    }
}