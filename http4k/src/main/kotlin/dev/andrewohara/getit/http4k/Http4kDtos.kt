package dev.andrewohara.getit.http4k

import dev.andrewohara.getit.ShoppingItem
import dev.andrewohara.getit.ShoppingItemData
import dev.andrewohara.getit.ShoppingItemName
import dev.andrewohara.getit.ShoppingList
import dev.andrewohara.getit.ShoppingListData
import dev.andrewohara.getit.ShoppingListName
import java.util.UUID

data class ShoppingListDtoV1(
    val userId: String,
    val listId: UUID,
    val name: String
)

data class ShoppingListDataDtoV1(
    val name: String
)

data class ShoppingItemDtoV1(
    val listId: UUID,
    val itemId: UUID,
    val name: String,
    val completed: Boolean
)

data class ShoppingItemDataDtoV1(
    val name: String,
    val completed: Boolean
)

fun ShoppingItem.toDtoV1() = ShoppingItemDtoV1(
    listId = listId.value,
    itemId = itemId.value,
    name = name.value,
    completed = completed
)

fun ShoppingList.toDtoV1() = ShoppingListDtoV1(
    userId = userId.value,
    listId = listId.value,
    name = name.value
)

fun ShoppingListDataDtoV1.toModel() = ShoppingListData(
    name = ShoppingListName.of(name)
)

fun ShoppingItemDataDtoV1.toModel() = ShoppingItemData(
    name = ShoppingItemName.of(name),
    completed = completed
)
