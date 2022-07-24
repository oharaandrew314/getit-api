package dev.andrewohara.getit

import dev.forkhandles.values.*
import java.util.*

class UserId private constructor(value: String): StringValue(value) {
    companion object : NonEmptyStringValueFactory<UserId>(::UserId)
}

class ShoppingListId private constructor(value: UUID): UUIDValue(value) {
    companion object: UUIDValueFactory<ShoppingListId>(::ShoppingListId)
}

class ShoppingItemId private constructor(value: UUID): UUIDValue(value) {
    companion object: UUIDValueFactory<ShoppingItemId>(::ShoppingItemId)
}

class ShoppingListName private constructor(value: String): StringValue(value) {
    companion object: NonEmptyStringValueFactory<ShoppingListName>(::ShoppingListName)
}

class ShoppingItemName private constructor(value: String): StringValue(value) {
    companion object: StringValueFactory<ShoppingItemName>(::ShoppingItemName)
}

data class ShoppingList(
    val userId: UserId,
    val name: ShoppingListName = ShoppingListName.of("Shopping List"),
    val listId: ShoppingListId = ShoppingListId.of(UUID.randomUUID())
)

operator fun ShoppingList.invoke(data: ShoppingListData) = copy(
    name = data.name
)

data class ShoppingListData(
    val name: ShoppingListName
)

data class ShoppingItem(
    val listId: ShoppingListId,
    val name: ShoppingItemName = ShoppingItemName.of(""),
    val itemId: ShoppingItemId = ShoppingItemId.of(UUID.randomUUID()),
    val completed: Boolean = false
)

data class ShoppingItemData(
    val name: ShoppingItemName
)

operator fun ShoppingItem.invoke(data: ShoppingItemData) = copy(
    name = data.name
)

