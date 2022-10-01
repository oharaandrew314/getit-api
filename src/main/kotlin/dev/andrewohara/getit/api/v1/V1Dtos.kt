package dev.andrewohara.getit.api.v1

import dev.andrewohara.getit.ShoppingItem
import dev.andrewohara.getit.ShoppingItemData
import dev.andrewohara.getit.ShoppingItemId
import dev.andrewohara.getit.ShoppingItemName
import dev.andrewohara.getit.ShoppingList
import dev.andrewohara.getit.ShoppingListData
import dev.andrewohara.getit.ShoppingListId
import dev.andrewohara.getit.ShoppingListName
import dev.andrewohara.getit.UserId

data class ShoppingListDtoV1(
    val userId: UserId,
    val listId: ShoppingListId,
    val name: ShoppingListName
)

data class ShoppingListDataDtoV1(
    val name: ShoppingListName
)

data class ShoppingItemDtoV1(
    val listId: ShoppingListId,
    val itemId: ShoppingItemId,
    val name: ShoppingItemName,
    val completed: Boolean
)

data class ShoppingItemDataDtoV1(
    val name: ShoppingItemName,
    val completed: Boolean
)

fun ShoppingItem.toDtoV1() = ShoppingItemDtoV1(
    listId = listId,
    itemId = itemId,
    name = name,
    completed = completed
)

fun ShoppingList.toDtoV1() = ShoppingListDtoV1(
    userId = userId,
    listId = listId,
    name = name
)

fun ShoppingListDataDtoV1.toModel() = ShoppingListData(
    name = name
)

fun ShoppingItemDataDtoV1.toModel() = ShoppingItemData(
    name = name,
    completed = completed
)
