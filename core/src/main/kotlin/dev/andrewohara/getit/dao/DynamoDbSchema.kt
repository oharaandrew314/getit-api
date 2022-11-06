package dev.andrewohara.getit.dao

import dev.andrewohara.getit.ShoppingItemId
import dev.andrewohara.getit.ShoppingItemName
import dev.andrewohara.getit.ShoppingListId
import dev.andrewohara.getit.ShoppingListName
import dev.andrewohara.getit.UserId
import org.http4k.connect.amazon.dynamodb.model.Attribute
import org.http4k.connect.amazon.dynamodb.model.value
import org.http4k.format.ConfigurableKotlinxSerialization
import org.http4k.format.asConfigurable
import org.http4k.format.value
import org.http4k.format.withStandardMappings

val userIdAttr = Attribute.string().value(UserId).required("userId")
val listIdAttr = Attribute.uuid().value(ShoppingListId).required("listId")
val itemIdAttr = Attribute.uuid().value(ShoppingItemId).required("itemId")


internal object GetItDynamoDbJson : ConfigurableKotlinxSerialization({
    ignoreUnknownKeys = true
    asConfigurable()
        .withStandardMappings()
        .value(ShoppingListId)
        .value(ShoppingListName)
        .value(UserId)
        .value(ShoppingItemId)
        .value(ShoppingItemName)
        .done()
})