package dev.andrewohara.getit.dao

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonWriter
import com.squareup.moshi.Moshi
import dev.andrewohara.getit.*
import dev.zacsweers.moshix.reflect.MetadataKotlinJsonAdapterFactory
import org.http4k.connect.amazon.dynamodb.model.*
import org.http4k.format.*

val userIdAttr = Attribute.string().value(UserId).required("userId")
val listIdAttr = Attribute.uuid().value(ShoppingListId).required("listId")
val itemIdAttr = Attribute.uuid().value(ShoppingItemId).required("itemId")

private fun <T> AutoMappingConfiguration<T>.withCustomMappings() = apply {
    value(ShoppingListId)
    value(ShoppingListName)
    value(UserId)
    value(ShoppingItemId)
    value(ShoppingItemName)
}

object GetItMoshi : ConfigurableMoshi(
    Moshi.Builder()
        .add(MapAdapter)
        .add(ListAdapter)
        .also {
            it.asConfigurable()
                .withStandardMappings()
                .withCustomMappings()
        }.add(MetadataKotlinJsonAdapterFactory())
        .add(Unit::class.java, UnitAdapter)
)

private object UnitAdapter : JsonAdapter<Unit>() {
    override fun fromJson(reader: JsonReader) {
        reader.readJsonValue(); Unit
    }

    override fun toJson(writer: JsonWriter, value: Unit?) {
        value?.let { writer.beginObject().endObject() } ?: writer.nullValue()
    }
}