package dev.andrewohara.getit.api

import dev.andrewohara.getit.ShoppingItem
import dev.andrewohara.getit.ShoppingItemData
import dev.andrewohara.getit.ShoppingItemName
import dev.andrewohara.getit.ShoppingList
import dev.andrewohara.getit.ShoppingListData
import dev.andrewohara.getit.ShoppingListName
import kotlinx.serialization.Serializable

@Serializable
data class ShoppingListDtoV1(
    val userId: String,
    val listId: String,
    val name: String
)

@Serializable
data class ShoppingListDataDtoV1(
    val name: String
)

@Serializable
data class ShoppingItemDtoV1(
    val listId: String,
    val itemId: String,
    val name: String,
    val completed: Boolean
)

@Serializable
data class ShoppingItemDataDtoV1(
    val name: String,
    val completed: Boolean
)

fun ShoppingItem.toDtoV1() = ShoppingItemDtoV1(
    listId = listId.value.toString(),
    itemId = itemId.value.toString(),
    name = name.value,
    completed = completed
)

fun ShoppingList.toDtoV1() = ShoppingListDtoV1(
    userId = userId.value,
    listId = listId.value.toString(),
    name = name.value
)

fun ShoppingListDataDtoV1.toModel() = ShoppingListData(
    name = ShoppingListName.of(name)
)

fun ShoppingItemDataDtoV1.toModel() = ShoppingItemData(
    name = ShoppingItemName.of(name),
    completed = completed
)

val sampleShoppingListDtoV1 = ShoppingListDtoV1(
    userId = "user1",
    listId = "b4485c15-bd74-46a1-a7bc-1cc27bf0ed58",
    name = "Groceries"
)

val sampleShoppingListDataDtoV1 = ShoppingListDataDtoV1(
    name = "Birthday Party"
)

val sampleShoppingItemDataDtoV1 = ShoppingItemDataDtoV1(
    name = "chips",
    completed = false
)

val sampleShoppingItemDtoV1 = ShoppingItemDtoV1(
    listId = sampleShoppingListDtoV1.listId,
    itemId = "8b3fbc45-ffc6-4d63-9b97-b2bf049b8f32",
    name = "iced tea",
    completed = false
)
