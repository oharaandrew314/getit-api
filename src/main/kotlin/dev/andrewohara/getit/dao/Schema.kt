package dev.andrewohara.getit.dao

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonWriter
import com.squareup.moshi.Moshi
import dev.andrewohara.getit.ShoppingItemId
import dev.andrewohara.getit.ShoppingItemName
import dev.andrewohara.getit.ShoppingListId
import dev.andrewohara.getit.ShoppingListName
import dev.andrewohara.getit.UserId
import dev.zacsweers.moshix.reflect.MetadataKotlinJsonAdapterFactory
import org.http4k.connect.amazon.dynamodb.model.Attribute
import org.http4k.connect.amazon.dynamodb.model.value
import org.http4k.format.AutoMappingConfiguration
import org.http4k.format.ConfigurableMoshi
import org.http4k.format.ListAdapter
import org.http4k.format.MapAdapter
import org.http4k.format.asConfigurable
import org.http4k.format.value
import org.http4k.format.withStandardMappings

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
