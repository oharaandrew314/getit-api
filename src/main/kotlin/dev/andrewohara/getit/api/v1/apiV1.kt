package dev.andrewohara.getit.api.v1

import dev.andrewohara.getit.*
import dev.forkhandles.result4k.Result
import dev.forkhandles.result4k.map
import dev.forkhandles.result4k.recover
import org.http4k.contract.ContractRoute
import org.http4k.contract.div
import org.http4k.contract.meta
import org.http4k.core.*
import org.http4k.lens.RequestContextLens

fun apiV1(auth: RequestContextLens<UserId>, service: GetItService): List<ContractRoute> {

    val getListsV1: ContractRoute = "/v1/lists" meta {
        operationId = "getListsV1"
        summary = "Get your Shopping Lists"
        returning(Status.OK, listArrayV1Lens to arrayOf(sampleShoppingListDtoV1))
    } bindContract Method.GET to { req: Request ->
        service.getLists(auth(req))
            .map { lists -> Response(Status.OK).with(listArrayV1Lens of lists.map { it.toDtoV1() }.toTypedArray()) }
            .orErrorResponse()
    }

    val createListV1: ContractRoute = "/v1/lists" meta {
        operationId = "createListV1"
        summary = "Create Shopping List"
        receiving(listDataV1Lens to sampleShoppingListDataDtoV1)
        returning(Status.OK, listV1Lens to sampleShoppingListDtoV1)
    } bindContract Method.POST to { req: Request ->
        service.createList(auth(req), listDataV1Lens(req).toModel())
            .toListResponse()
    }

    val deleteListV1: ContractRoute = "/v1/lists" / listIdLens meta {
        operationId = "deleteListV1"
        summary = "Delete List"
        returning(
            Status.OK to "List deleted",
            Status.NOT_FOUND to "List not found"
        )
    } bindContract Method.DELETE to { listId ->
        { req ->
            service.deleteList(auth(req), listId)
                .toListResponse()
        }
    }

    val updateListV1: ContractRoute = "/v1/lists" / listIdLens meta {
        operationId = "updateListV1"
        summary = "Update List"
        receiving(listDataV1Lens to sampleShoppingListDataDtoV1)
        returning(
            Status.OK to "List deleted",
            Status.NOT_FOUND to "List not found"
        )
    } bindContract Method.PUT to { listId ->
        { req ->
            service.updateList(auth(req), listId, listDataV1Lens(req).toModel())
                .toListResponse()
        }
    }

    val addItemV1: ContractRoute = "/v1/lists" / listIdLens / "items" meta {
        operationId = "addItemV1"
        summary = "Add Item to List"
        receiving(itemDataV1Lens to sampleShoppingItemDataDtoV1)
        returning(Status.OK, itemV1Lens to sampleShoppingItemDtoV1)
        returning(Status.NOT_FOUND to "item not found")
    } bindContract Method.POST to { listId, _ ->
        { req ->
            service.addItem(auth(req), listId, itemDataV1Lens(req).toModel())
                .toItemResponse()
        }
    }

    val getItemsV1: ContractRoute = "/v1/lists" / listIdLens / "items" meta {
        operationId = "getItemsV1"
        summary = "Get Items for List"
        returning(Status.OK, itemArrayV1Lens to arrayOf(sampleShoppingItemDtoV1))
        returning(Status.NOT_FOUND to "List not found")
    } bindContract Method.GET to { listId, _ ->
        { req ->
            service.getItems(auth(req), listId)
                .map { items -> Response(Status.OK).with(itemArrayV1Lens of items.map { it.toDtoV1() }.toTypedArray()) }
                .orErrorResponse()
        }
    }

    val updateItemV1: ContractRoute = "/v1/lists" / listIdLens / "items" / itemIdLens meta {
        operationId = "updateItemV1"
        summary = "Update Item"
        receiving(itemDataV1Lens to sampleShoppingItemDataDtoV1)
        returning(Status.OK, itemV1Lens to sampleShoppingItemDtoV1)
        returning(Status.NOT_FOUND to "item not found")
    } bindContract Method.PUT to { listId, _, itemId ->
        { req ->
            service.updateItem(auth(req), listId, itemId, itemDataV1Lens(req).toModel())
                .toItemResponse()
        }
    }

    val deleteItemV1: ContractRoute = "/v1/lists" / listIdLens / "items" / itemIdLens meta {
        operationId = "deleteItemV1"
        summary = "Delete Item"
        returning(Status.OK to "Item deleted", Status.NOT_FOUND to "Item not found")
    } bindContract Method.DELETE to { listId, _, itemId ->
        { req ->
            service.deleteItem(auth(req), listId, itemId)
                .map { Response(Status.OK) }
                .recover { Response(Status.NOT_FOUND) }
        }
    }

    return listOf(
        getListsV1, createListV1, deleteListV1, updateListV1,
        addItemV1, getItemsV1, updateItemV1, deleteItemV1
    )
}

private fun Result<ShoppingList, ShoppingError>.toListResponse() = this
    .map { list -> Response(Status.OK).with(listV1Lens of list.toDtoV1()) }
    .orErrorResponse()

private fun Result<ShoppingItem, ShoppingError>.toItemResponse() = this
    .map { item -> Response(Status.OK).with(itemV1Lens of item.toDtoV1()) }
    .orErrorResponse()

private fun Result<Response, ShoppingError>.orErrorResponse() = recover { error ->
    when(error) {
        is ListNotFound -> Response(Status.NOT_FOUND)
        is ItemNotFound -> Response(Status.NOT_FOUND)
    }
}