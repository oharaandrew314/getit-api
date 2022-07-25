package dev.andrewohara.getit.api.v1

import dev.andrewohara.getit.*
import java.util.*

val sampleShoppingListDtoV1 = ShoppingListDtoV1(
    userId = UserId.of("user1"),
    listId = ShoppingListId.of(UUID.fromString("b4485c15-bd74-46a1-a7bc-1cc27bf0ed58")),
    name = ShoppingListName.of("Groceries")
)

val sampleShoppingListDataDtoV1 = ShoppingListDataDtoV1(
    name = ShoppingListName.of("Birthday Party")
)

val sampleShoppingItemDataDtoV1 = ShoppingItemDataDtoV1(
    name = ShoppingItemName.of("chips"),
    completed = false
)

val sampleShoppingItemDtoV1 = ShoppingItemDtoV1(
    listId = sampleShoppingListDtoV1.listId,
    itemId = ShoppingItemId.of(UUID.fromString("8b3fbc45-ffc6-4d63-9b97-b2bf049b8f32")),
    name = ShoppingItemName.of("iced tea")
)