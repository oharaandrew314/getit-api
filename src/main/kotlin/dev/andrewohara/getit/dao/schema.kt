package dev.andrewohara.getit.dao

import com.squareup.moshi.Moshi
import dev.andrewohara.getit.*
import io.andrewohara.utils.http4k.connect.dynamodb.DynamoDbTableMapperSchema
import org.http4k.connect.amazon.dynamodb.model.*
import org.http4k.format.*

val userIdAttr = Attribute.string().value(UserId).required("userId")
val listIdAttr = Attribute.uuid().value(ShoppingListId).required("listId")
val itemIdAttr = Attribute.uuid().value(ShoppingItemId).required("itemId")

val itemsByList = DynamoDbTableMapperSchema.GlobalSecondary(
    indexName = IndexName.of("lists"),
    hashKeyAttribute = listIdAttr,
    sortKeyAttribute = itemIdAttr
)

object GetItMoshi : ConfigurableMoshi(
    Moshi.Builder()
        .add(MapAdapter)
        .add(ListAdapter)
        .asConfigurable()
        .withStandardMappings()
        .value(ShoppingListId)
        .value(ShoppingListName)
        .value(UserId)
        .value(ShoppingItemId)
        .value(ShoppingItemName)
        .done()
)