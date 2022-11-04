package dev.andrewohara.getit

import dev.andrewohara.getit.api.v1.ShoppingItemIdSerializer
import dev.andrewohara.getit.api.v1.ShoppingItemNameSerializer
import dev.andrewohara.getit.api.v1.ShoppingListIdSerializer
import dev.andrewohara.getit.api.v1.ShoppingListNameSerializer
import dev.andrewohara.getit.api.v1.UserIdSerializer
import dev.forkhandles.values.NonEmptyStringValueFactory
import dev.forkhandles.values.StringValue
import dev.forkhandles.values.StringValueFactory
import dev.forkhandles.values.UUIDValue
import dev.forkhandles.values.UUIDValueFactory
import io.ktor.server.auth.Principal
import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable(with = UserIdSerializer::class)
class UserId(value: String) : StringValue(value), Principal {
    companion object : NonEmptyStringValueFactory<UserId>(::UserId)
}

@Serializable(with = ShoppingListIdSerializer::class)
class ShoppingListId(value: UUID) : UUIDValue(value) {
    companion object : UUIDValueFactory<ShoppingListId>(::ShoppingListId)
}

@Serializable(with = ShoppingItemIdSerializer::class)
class ShoppingItemId(value: UUID) : UUIDValue(value) {
    companion object : UUIDValueFactory<ShoppingItemId>(::ShoppingItemId)
}

@Serializable(with = ShoppingListNameSerializer::class)
class ShoppingListName(value: String) : StringValue(value) {
    companion object : NonEmptyStringValueFactory<ShoppingListName>(::ShoppingListName)
}

@Serializable(with = ShoppingItemNameSerializer::class)
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
