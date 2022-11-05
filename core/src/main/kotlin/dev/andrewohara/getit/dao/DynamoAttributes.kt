package dev.andrewohara.getit.dao

import dev.andrewohara.getit.ShoppingItemId
import dev.andrewohara.getit.ShoppingListId
import dev.andrewohara.getit.UserId
import org.http4k.connect.amazon.dynamodb.model.Attribute
import org.http4k.connect.amazon.dynamodb.model.value

val userIdAttr = Attribute.string().value(UserId).required("userId")
val listIdAttr = Attribute.uuid().value(ShoppingListId).required("listId")
val itemIdAttr = Attribute.uuid().value(ShoppingItemId).required("itemId")
