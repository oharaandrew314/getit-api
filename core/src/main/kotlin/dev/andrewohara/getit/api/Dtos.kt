package dev.andrewohara.getit.api

import dev.andrewohara.getit.ShoppingItem
import dev.andrewohara.getit.ShoppingItemData
import dev.andrewohara.getit.ShoppingItemId
import dev.andrewohara.getit.ShoppingItemName
import dev.andrewohara.getit.ShoppingList
import dev.andrewohara.getit.ShoppingListData
import dev.andrewohara.getit.ShoppingListId
import dev.andrewohara.getit.ShoppingListName
import dev.andrewohara.getit.UserId
import kotlinx.serialization.Serializable

@Serializable
data class ShoppingListDtoV1(
    val userId: UserId,
    val listId: ShoppingListId,
    val name: ShoppingListName
)

@Serializable
data class ShoppingListDataDtoV1(
    val name: ShoppingListName
)

@Serializable
data class ShoppingItemDtoV1(
    val listId: ShoppingListId,
    val itemId: ShoppingItemId,
    val name: ShoppingItemName,
    val completed: Boolean
)

@Serializable
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

val sampleShoppingListDtoV1 = ShoppingListDtoV1(
    userId = UserId.of("user1"),
    listId = ShoppingListId.parse("b4485c15-bd74-46a1-a7bc-1cc27bf0ed58"),
    name = ShoppingListName.of("Groceries")
)

val sampleShoppingListDataDtoV1 = ShoppingListDataDtoV1(
    name = ShoppingListName.of("Birthday Party")
)

val sampleShoppingItemDataDtoV1 = ShoppingItemDataDtoV1(
    name = ShoppingItemName.of("chips"),
    completed = false
)

val sampleShoppingItemDtoV1 = ShoppingItemDtoV1(
    listId = sampleShoppingListDtoV1.listId,
    itemId = ShoppingItemId.parse("8b3fbc45-ffc6-4d63-9b97-b2bf049b8f32"),
    name = ShoppingItemName.of("iced tea"),
    completed = false
)
