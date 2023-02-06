package dev.andrewohara.getit

import dev.andrewohara.getit.api.Values4KStringSerializer
import dev.forkhandles.values.NonEmptyStringValueFactory
import dev.forkhandles.values.StringValue
import dev.forkhandles.values.StringValueFactory
import dev.forkhandles.values.UUIDValue
import dev.forkhandles.values.UUIDValueFactory
import dev.forkhandles.values.random
import kotlinx.serialization.Serializable
import java.util.UUID

object UserIdSerializer : Values4KStringSerializer<UserId, String>(UserId)
object ShoppingListIdSerializer : Values4KStringSerializer<ShoppingListId, UUID>(ShoppingListId)
object ShoppingItemIdSerializer : Values4KStringSerializer<ShoppingItemId, UUID>(ShoppingItemId)
object ShoppingListNameSerializer : Values4KStringSerializer<ShoppingListName, String>(ShoppingListName)
object ShoppingItemNameSerializer : Values4KStringSerializer<ShoppingItemName, String>(ShoppingItemName)

@Serializable(with = UserIdSerializer::class)
class UserId private constructor(value: String) : StringValue(value) {
    companion object : NonEmptyStringValueFactory<UserId>(::UserId)
}

@Serializable(with = ShoppingListIdSerializer::class)
class ShoppingListId private constructor(value: UUID) : UUIDValue(value) {
    companion object : UUIDValueFactory<ShoppingListId>(::ShoppingListId)
}

@Serializable(with = ShoppingItemIdSerializer::class)
class ShoppingItemId private constructor(value: UUID) : UUIDValue(value) {
    companion object : UUIDValueFactory<ShoppingItemId>(::ShoppingItemId)
}

@Serializable(with = ShoppingListNameSerializer::class)
class ShoppingListName private constructor(value: String) : StringValue(value) {
    companion object : NonEmptyStringValueFactory<ShoppingListName>(::ShoppingListName)
}

@Serializable(with = ShoppingItemNameSerializer::class)
class ShoppingItemName private constructor(value: String) : StringValue(value) {
    companion object : StringValueFactory<ShoppingItemName>(::ShoppingItemName)
}

@Serializable
data class ShoppingList(
    val userId: UserId,
    val name: ShoppingListName = ShoppingListName.of("Shopping List"),
    val listId: ShoppingListId = ShoppingListId.random()
)

operator fun ShoppingList.invoke(data: ShoppingListData) = copy(
    name = data.name
)

data class ShoppingListData(
    val name: ShoppingListName
)

@Serializable
data class ShoppingItem(
    val listId: ShoppingListId,
    val name: ShoppingItemName = ShoppingItemName.of(""),
    val itemId: ShoppingItemId = ShoppingItemId.random(),
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
