package dev.andrewohara.getit.http4k

import dev.andrewohara.getit.GetItService
import dev.andrewohara.getit.ItemNotFound
import dev.andrewohara.getit.ListNotFound
import dev.andrewohara.getit.ShoppingError
import dev.andrewohara.getit.ShoppingItem
import dev.andrewohara.getit.ShoppingList
import dev.andrewohara.getit.Unauthorized
import dev.andrewohara.getit.UserId
import dev.andrewohara.getit.api.sampleShoppingItemDataDtoV1
import dev.andrewohara.getit.api.sampleShoppingItemDtoV1
import dev.andrewohara.getit.api.sampleShoppingListDataDtoV1
import dev.andrewohara.getit.api.sampleShoppingListDtoV1
import dev.andrewohara.getit.api.toDtoV1
import dev.andrewohara.getit.api.toModel
import dev.forkhandles.result4k.Result
import dev.forkhandles.result4k.map
import dev.forkhandles.result4k.recover
import org.http4k.contract.div
import org.http4k.contract.meta
import org.http4k.core.Method
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.core.with
import org.http4k.lens.RequestContextLens

private val getListsV1 = "/v1/lists" meta {
    operationId = "getListsV1"
    summary = "Get your Shopping Lists"
    returning(Status.OK, listArrayV1Lens to arrayOf(sampleShoppingListDtoV1))
} bindContract Method.GET

val createListV1 = "/v1/lists" meta {
    operationId = "createListV1"
    summary = "Create Shopping List"
    receiving(listDataV1Lens to sampleShoppingListDataDtoV1)
    returning(Status.OK, listV1Lens to sampleShoppingListDtoV1)
} bindContract Method.POST

val deleteListV1 = "/v1/lists" / listIdLens meta {
    operationId = "deleteListV1"
    summary = "Delete List"
    returning(
        Status.OK to "List deleted",
        Status.NOT_FOUND to "List not found"
    )
} bindContract Method.DELETE

val updateListV1 = "/v1/lists" / listIdLens meta {
    operationId = "updateListV1"
    summary = "Update List"
    receiving(listDataV1Lens to sampleShoppingListDataDtoV1)
    returning(
        Status.OK to "List deleted",
        Status.NOT_FOUND to "List not found"
    )
} bindContract Method.PUT

val addItemV1 = "/v1/lists" / listIdLens / "items" meta {
    operationId = "addItemV1"
    summary = "Add Item to List"
    receiving(itemDataV1Lens to sampleShoppingItemDataDtoV1)
    returning(Status.OK, itemV1Lens to sampleShoppingItemDtoV1)
    returning(Status.NOT_FOUND to "item not found")
} bindContract Method.POST

val getItemsV1 = "/v1/lists" / listIdLens / "items" meta {
    operationId = "getItemsV1"
    summary = "Get Items for List"
    returning(Status.OK, itemArrayV1Lens to arrayOf(sampleShoppingItemDtoV1))
    returning(Status.NOT_FOUND to "List not found")
} bindContract Method.GET

val updateItemV1 = "/v1/lists" / listIdLens / "items" / itemIdLens meta {
    operationId = "updateItemV1"
    summary = "Update Item"
    receiving(itemDataV1Lens to sampleShoppingItemDataDtoV1)
    returning(Status.OK, itemV1Lens to sampleShoppingItemDtoV1)
    returning(Status.NOT_FOUND to "item not found")
} bindContract Method.PUT

val deleteItemV1 = "/v1/lists" / listIdLens / "items" / itemIdLens meta {
    operationId = "deleteItemV1"
    summary = "Delete Item"
    returning(Status.OK to "Item deleted", Status.NOT_FOUND to "Item not found")
} bindContract Method.DELETE

fun GetItService.toV1Routes(auth: RequestContextLens<UserId>) = listOf(
    getListsV1 to { req: Request ->
        getLists(auth(req))
            .map { lists -> Response(Status.OK).with(listArrayV1Lens of lists.map { it.toDtoV1() }.toTypedArray()) }
            .orErrorResponse()
    },

    createListV1 to { req: Request ->
        createList(auth(req), listDataV1Lens(req).toModel())
            .toListResponse()
    },

    deleteListV1 to { listId ->
        { req ->
            deleteList(auth(req), listId)
                .toListResponse()
        }
    },

    updateListV1 to { listId ->
        { req ->
            updateList(auth(req), listId, listDataV1Lens(req).toModel())
                .toListResponse()
        }
    },

    addItemV1 to { listId, _ ->
        { req ->
            addItem(auth(req), listId, itemDataV1Lens(req).toModel())
                .toItemResponse()
        }
    },

    getItemsV1 to { listId, _ ->
        { req ->
            getItems(auth(req), listId)
                .map { items -> Response(Status.OK).with(itemArrayV1Lens of items.map { it.toDtoV1() }.toTypedArray()) }
                .orErrorResponse()
        }
    },

    updateItemV1 to { listId, _, itemId ->
        { req ->
            updateItem(auth(req), listId, itemId, itemDataV1Lens(req).toModel())
                .toItemResponse()
        }
    },

    deleteItemV1 to { listId, _, itemId ->
        { req ->
            deleteItem(auth(req), listId, itemId)
                .map { Response(Status.OK) }
                .recover { Response(Status.NOT_FOUND) }
        }
    }
)

private fun Result<ShoppingList, ShoppingError>.toListResponse() = this
    .map { list -> Response(Status.OK).with(listV1Lens of list.toDtoV1()) }
    .orErrorResponse()

private fun Result<ShoppingItem, ShoppingError>.toItemResponse() = this
    .map { item -> Response(Status.OK).with(itemV1Lens of item.toDtoV1()) }
    .orErrorResponse()

private fun Result<Response, ShoppingError>.orErrorResponse() = recover { error ->
    when (error) {
        is ListNotFound -> Response(Status.NOT_FOUND)
        is ItemNotFound -> Response(Status.NOT_FOUND)
        Unauthorized -> Response(Status.UNAUTHORIZED)
    }
}
