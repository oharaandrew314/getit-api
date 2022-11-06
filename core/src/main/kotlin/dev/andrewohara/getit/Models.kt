package dev.andrewohara.getit

import dev.forkhandles.values.NonEmptyStringValueFactory
import dev.forkhandles.values.StringValue
import dev.forkhandles.values.StringValueFactory
import dev.forkhandles.values.UUIDValue
import dev.forkhandles.values.UUIDValueFactory
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.util.UUID
import dev.forkhandles.values.Value
import dev.forkhandles.values.ValueFactory

open class Values4KSerializer<T: Value<PRIM>, PRIM: Any>(private val factory: ValueFactory<T, PRIM>) : KSerializer<T> {
    override val descriptor = PrimitiveSerialDescriptor("UuidSerializer", PrimitiveKind.STRING)
    override fun deserialize(decoder: Decoder): T = factory.parse(decoder.decodeString())
    override fun serialize(encoder: Encoder, value: T) = encoder.encodeString(factory.show(value))
}

object UserIdSerializer: Values4KSerializer<UserId, String>(UserId)
object ShoppingListIdSerializer: Values4KSerializer<ShoppingListId, UUID>(ShoppingListId)
object ShoppingItemIdSerializer: Values4KSerializer<ShoppingItemId, UUID>(ShoppingItemId)
object ShoppingListNameSerializer: Values4KSerializer<ShoppingListName, String>(ShoppingListName)
object ShoppingItemNameSerializer: Values4KSerializer<ShoppingItemName, String>(ShoppingItemName)

@Serializable(with = UserIdSerializer::class)
class UserId(value: String) : StringValue(value) {
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

@Serializable
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
