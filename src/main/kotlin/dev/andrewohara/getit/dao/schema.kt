package dev.andrewohara.getit.dao

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.squareup.moshi.Moshi
import dev.andrewohara.getit.*
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
        .asConfigurable()
        .withStandardMappings()
        .withCustomMappings()
        .done()
)

object GetItOpenApiJackson : ConfigurableJackson(
    KotlinModule.Builder().build()
        .asConfigurable()
        .withStandardMappings()
        .withCustomMappings()
        .done()
        .deactivateDefaultTyping()
        .setSerializationInclusion(JsonInclude.Include.NON_NULL)
        .configure(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES, true)
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        .configure(DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES, false)
        .configure(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS, true)
        .configure(DeserializationFeature.USE_BIG_INTEGER_FOR_INTS, true)
)