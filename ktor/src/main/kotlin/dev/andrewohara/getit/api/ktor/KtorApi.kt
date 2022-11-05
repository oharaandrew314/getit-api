package dev.andrewohara.getit.api.ktor

import dev.andrewohara.getit.GetItService
import dev.andrewohara.getit.ItemNotFound
import dev.andrewohara.getit.ListNotFound
import dev.andrewohara.getit.ShoppingError
import dev.andrewohara.getit.ShoppingItem
import dev.andrewohara.getit.ShoppingList
import dev.andrewohara.getit.Unauthorized
import dev.forkhandles.result4k.Result
import dev.forkhandles.result4k.asResultOr
import dev.forkhandles.result4k.flatMap
import dev.forkhandles.result4k.map
import dev.forkhandles.result4k.peek
import dev.forkhandles.result4k.peekFailure
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.call
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.principal
import io.ktor.server.request.receive
import io.ktor.server.resources.delete
import io.ktor.server.resources.get
import io.ktor.server.resources.post
import io.ktor.server.resources.put
import io.ktor.server.response.respond
import io.ktor.server.response.respondText
import io.ktor.server.routing.routing
import io.ktor.util.pipeline.PipelineContext

fun Application.createRoutes(service: GetItService) = routing {
    authenticate("Bearer") {
        // lists
        get<ShoppingListsResource> {
            authorize()
                .flatMap { service.getLists(it.userId) }
                .map { it.map(ShoppingList::toDtoV1) }
                .respondTo(call)
        }
        post<ShoppingListsResource> {
            authorize()
                .flatMap { service.createList(it.userId, call.receive<ShoppingListDataDtoV1>().toModel()) }
                .map { it.toDtoV1() }.respondTo(call)
        }
        delete<ShoppingListsResource.ListId> { id ->
            authorize()
                .flatMap { service.deleteList(it.userId, id.toValue()) }
                .map { it.toDtoV1() }
                .respondTo(call)
        }
        put<ShoppingListsResource.ListId> { id ->
            authorize()
                .flatMap { service.updateList(it.userId, id.toValue(), call.receive<ShoppingListDataDtoV1>().toModel()) }
                .map { it.toDtoV1() }
                .respondTo(call)
        }

        // items
        post<ShoppingListsResource.ListId.Items> { (listId) ->
            authorize()
                .flatMap { service.addItem(it.userId, listId.toValue(), call.receive<ShoppingItemDataDtoV1>().toModel()) }
                .map { it.toDtoV1() }
                .respondTo(call)
        }
        get<ShoppingListsResource.ListId.Items> { (listId) ->
            authorize()
                .flatMap { service.getItems(it.userId, listId.toValue()) }
                .map { it.map(ShoppingItem::toDtoV1) }
                .respondTo(call)
        }
        put<ShoppingListsResource.ListId.Items.ItemId> { itemId ->
            authorize()
                .flatMap { service.updateItem(it.userId, itemId.items.listId.toValue(), itemId.toValue(), call.receive<ShoppingItemDataDtoV1>().toModel()) }
                .map { it.toDtoV1() }
                .respondTo(call)
        }
        delete<ShoppingListsResource.ListId.Items.ItemId> { itemId ->
            authorize()
                .flatMap { service.deleteItem(it.userId, itemId.items.listId.toValue(), itemId.toValue()) }
                .map { it.toDtoV1() }
                .respondTo(call)
        }
    }
}

private fun PipelineContext<Unit, ApplicationCall>.authorize() = call.principal<GetItPrincipal>().asResultOr { Unauthorized }

private suspend fun Result<Any, ShoppingError>.respondTo(call: ApplicationCall) {
    this
        .peek { call.respond(it) }
        .peekFailure { error ->
            when (error) {
                is ListNotFound -> call.respondText("list not found", status = HttpStatusCode.NotFound)
                is ItemNotFound -> call.respondText("item not found", status = HttpStatusCode.NotFound)
                Unauthorized -> call.respondText("Unauthorized", status = HttpStatusCode.Unauthorized)
            }
        }
}
