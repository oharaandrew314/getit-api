package dev.andrewohara.getit.http4k

import dev.andrewohara.getit.ShoppingItem
import dev.andrewohara.getit.ShoppingItemData
import dev.andrewohara.getit.ShoppingItemName
import dev.andrewohara.getit.ShoppingList
import dev.andrewohara.getit.ShoppingListData
import dev.andrewohara.getit.ShoppingListName
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.util.UUID

class UuidSerializer : KSerializer<UUID> {
    override val descriptor = PrimitiveSerialDescriptor("UuidSerializer", PrimitiveKind.STRING)
    override fun deserialize(decoder: Decoder): UUID = UUID.fromString(decoder.decodeString())
    override fun serialize(encoder: Encoder, value: UUID) = encoder.encodeString(value.toString())
}

@Serializable
data class ShoppingListDtoV1(
    val userId: String,
    @Serializable(with = UuidSerializer::class)
    val listId: UUID,
    val name: String
)

@Serializable
data class ShoppingListDataDtoV1(
    val name: String
)

@Serializable
data class ShoppingItemDtoV1(
    @Serializable(with = UuidSerializer::class)
    val listId: UUID,
    @Serializable(with = UuidSerializer::class)
    val itemId: UUID,
    val name: String,
    val completed: Boolean
)

@Serializable
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
