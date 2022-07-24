package dev.andrewohara.getit.api.v1

import dev.andrewohara.getit.*

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
    val name: ShoppingItemName
)

data class ShoppingItemDataDtoV1(
    val name: ShoppingItemName
)

fun ShoppingItem.toDtoV1() = ShoppingItemDtoV1(
    listId = listId,
    itemId = itemId,
    name = name
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
    name = name
)