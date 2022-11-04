package dev.andrewohara.getit.api.v1.resources

import dev.andrewohara.getit.ShoppingItemId
import dev.andrewohara.getit.ShoppingListId
import io.ktor.resources.Resource
import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
@Resource("/v1/lists")
class ShoppingListsResource {
    @Serializable
    @Resource("{listId}")
    data class ListId(val lists: ShoppingListsResource = ShoppingListsResource(), val id: String) {
        fun toValue() = ShoppingListId(UUID.fromString(id))

        @Serializable
        @Resource("items")
        data class Items(val listId: ListId) {

            @Serializable
            @Resource("{itemId}")
            data class ItemId(val items: Items, val id: String) {
                fun toValue() = ShoppingItemId(UUID.fromString(id))
            }
        }
    }
}