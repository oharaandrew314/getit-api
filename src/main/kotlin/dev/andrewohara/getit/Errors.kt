package dev.andrewohara.getit

sealed interface ShoppingError
data class ListNotFound(val userId: UserId, val listId: ShoppingListId) : ShoppingError
data class ItemNotFound(val listId: ShoppingListId, val itemId: ShoppingItemId) : ShoppingError
object Unauthorized: ShoppingError
