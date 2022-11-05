package dev.andrewohara.getit.http4k

import dev.andrewohara.getit.GetItJson.auto
import dev.andrewohara.getit.ShoppingItemId
import dev.andrewohara.getit.ShoppingListId
import org.http4k.core.Body
import org.http4k.lens.Path
import org.http4k.lens.value
import java.util.UUID

val itemIdLens = Path.value(ShoppingItemId).of("item_id")
val listIdLens = Path.value(ShoppingListId).of("list_id")

val itemArrayV1Lens = Body.auto<Array<ShoppingItemDtoV1>>().toLens()
val listArrayV1Lens = Body.auto<Array<ShoppingListDtoV1>>().toLens()
val itemV1Lens = Body.auto<ShoppingItemDtoV1>().toLens()
val listV1Lens = Body.auto<ShoppingListDtoV1>().toLens()
val listDataV1Lens = Body.auto<ShoppingListDataDtoV1>().toLens()
val itemDataV1Lens = Body.auto<ShoppingItemDataDtoV1>().toLens()

val sampleShoppingListDtoV1 = ShoppingListDtoV1(
    userId = "user1",
    listId = UUID.fromString("b4485c15-bd74-46a1-a7bc-1cc27bf0ed58"),
    name = "Groceries"
)

val sampleShoppingListDataDtoV1 = ShoppingListDataDtoV1(
    name = "Birthday Party"
)

val sampleShoppingItemDataDtoV1 = ShoppingItemDataDtoV1(
    name = "chips",
    completed = false
)

val sampleShoppingItemDtoV1 = ShoppingItemDtoV1(
    listId = sampleShoppingListDtoV1.listId,
    itemId = UUID.fromString("8b3fbc45-ffc6-4d63-9b97-b2bf049b8f32"),
    name = "iced tea",
    completed = false
)
