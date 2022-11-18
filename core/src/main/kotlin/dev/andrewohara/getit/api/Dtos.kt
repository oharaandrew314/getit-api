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
import dev.forkhandles.values.Value
import dev.forkhandles.values.ValueFactory
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.util.UUID

open class Values4KSerializer<T : Value<PRIM>, PRIM : Any>(private val factory: ValueFactory<T, PRIM>) : KSerializer<T> {
    override val descriptor = PrimitiveSerialDescriptor("Values4kSerializer", PrimitiveKind.STRING)
    override fun deserialize(decoder: Decoder): T = factory.parse(decoder.decodeString())
    override fun serialize(encoder: Encoder, value: T) = encoder.encodeString(factory.show(value))
}

object UserIdSerializer : Values4KSerializer<UserId, String>(UserId)
object ListIdSerializer : Values4KSerializer<ShoppingListId, UUID>(ShoppingListId)
object ItemIdSerializer : Values4KSerializer<ShoppingItemId, UUID>(ShoppingItemId)

@Serializable
data class ShoppingListDtoV1(
    @Serializable(with = UserIdSerializer::class)
    val userId: UserId,
    @Serializable(with = ListIdSerializer::class)
    val listId: ShoppingListId,
    val name: String
)

@Serializable
data class ShoppingListDataDtoV1(
    val name: String
)

@Serializable
data class ShoppingItemDtoV1(
    @Serializable(with = ListIdSerializer::class)
    val listId: ShoppingListId,
    @Serializable(with = ItemIdSerializer::class)
    val itemId: ShoppingItemId,
    val name: String,
    val completed: Boolean
)

@Serializable
data class ShoppingItemDataDtoV1(
    val name: String,
    val completed: Boolean
)

fun ShoppingItem.toDtoV1() = ShoppingItemDtoV1(
    listId = listId,
    itemId = itemId,
    name = name.value,
    completed = completed
)

fun ShoppingList.toDtoV1() = ShoppingListDtoV1(
    userId = userId,
    listId = listId,
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
    userId = UserId.of("user1"),
    listId = ShoppingListId.of(UUID.fromString("b4485c15-bd74-46a1-a7bc-1cc27bf0ed58")),
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
    itemId = ShoppingItemId.of(UUID.fromString("8b3fbc45-ffc6-4d63-9b97-b2bf049b8f32")),
    name = "iced tea",
    completed = false
)
