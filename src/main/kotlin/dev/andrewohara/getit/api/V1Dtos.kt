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
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.util.UUID

object UserIdSerializer: KSerializer<UserId> {
    override val descriptor = PrimitiveSerialDescriptor("UserId", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder) = UserId(decoder.decodeString())
    override fun serialize(encoder: Encoder, value: UserId) = encoder.encodeString(value.value)
}

object ShoppingListIdSerializer: KSerializer<ShoppingListId> {
    override val descriptor = PrimitiveSerialDescriptor("ShoppingListId", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder) = ShoppingListId(UUID.fromString(decoder.decodeString()))
    override fun serialize(encoder: Encoder, value: ShoppingListId) = encoder.encodeString(value.value.toString())
}

object ShoppingListNameSerializer: KSerializer<ShoppingListName> {
    override val descriptor = PrimitiveSerialDescriptor("ShoppingListName", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder) = ShoppingListName(decoder.decodeString())
    override fun serialize(encoder: Encoder, value: ShoppingListName) = encoder.encodeString(value.value)
}

object ShoppingItemIdSerializer: KSerializer<ShoppingItemId> {
    override val descriptor = PrimitiveSerialDescriptor("ShoppingItemId", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder) = ShoppingItemId(UUID.fromString(decoder.decodeString()))
    override fun serialize(encoder: Encoder, value: ShoppingItemId) = encoder.encodeString(value.value.toString())
}

object ShoppingItemNameSerializer: KSerializer<ShoppingItemName> {
    override val descriptor = PrimitiveSerialDescriptor("ShoppingItemName", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder) = ShoppingItemName((decoder.decodeString()))
    override fun serialize(encoder: Encoder, value: ShoppingItemName) = encoder.encodeString(value.value)
}

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
