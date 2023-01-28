package dev.andrewohara.getit.http4k

import dev.andrewohara.getit.ShoppingItemId
import dev.andrewohara.getit.ShoppingListId
import dev.andrewohara.getit.api.ShoppingItemDataDtoV1
import dev.andrewohara.getit.api.ShoppingItemDtoV1
import dev.andrewohara.getit.api.ShoppingListDataDtoV1
import dev.andrewohara.getit.api.ShoppingListDtoV1
import org.http4k.core.Body
import org.http4k.format.KotlinxSerialization.auto
import org.http4k.lens.Path
import org.http4k.lens.value

val itemIdLens = Path.value(ShoppingItemId).of("item_id")
val listIdLens = Path.value(ShoppingListId).of("list_id")

val itemArrayV1Lens = Body.auto<List<ShoppingItemDtoV1>>().toLens()
val listArrayV1Lens = Body.auto<List<ShoppingListDtoV1>>().toLens()
val itemV1Lens = Body.auto<ShoppingItemDtoV1>().toLens()
val listV1Lens = Body.auto<ShoppingListDtoV1>().toLens()
val listDataV1Lens = Body.auto<ShoppingListDataDtoV1>().toLens()
val itemDataV1Lens = Body.auto<ShoppingItemDataDtoV1>().toLens()
