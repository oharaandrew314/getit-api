package dev.andrewohara.getit

import dev.forkhandles.values.NonEmptyStringValueFactory
import dev.forkhandles.values.StringValue
import dev.forkhandles.values.StringValueFactory
import dev.forkhandles.values.UUIDValue
import dev.forkhandles.values.UUIDValueFactory
import java.util.UUID

class UserId(value: String) : StringValue(value) {
    companion object : NonEmptyStringValueFactory<UserId>(::UserId)
}

class ShoppingListId(value: UUID) : UUIDValue(value) {
    companion object : UUIDValueFactory<ShoppingListId>(::ShoppingListId)
}

class ShoppingItemId(value: UUID) : UUIDValue(value) {
    companion object : UUIDValueFactory<ShoppingItemId>(::ShoppingItemId)
}

class ShoppingListName(value: String) : StringValue(value) {
    companion object : NonEmptyStringValueFactory<ShoppingListName>(::ShoppingListName)
}

class ShoppingItemName(value: String) : StringValue(value) {
    companion object : StringValueFactory<ShoppingItemName>(::ShoppingItemName)
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
    val name: ShoppingItemName,
    val completed: Boolean
)

operator fun ShoppingItem.invoke(data: ShoppingItemData) = copy(
    name = data.name,
    completed = data.completed
)
