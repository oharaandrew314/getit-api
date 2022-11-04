package dev.andrewohara.getit.api.v1

import dev.andrewohara.getit.GetItService
import dev.andrewohara.getit.ItemNotFound
import dev.andrewohara.getit.ListNotFound
import dev.andrewohara.getit.ShoppingError
import dev.andrewohara.getit.ShoppingItem
import dev.andrewohara.getit.ShoppingList
import dev.andrewohara.getit.Unauthorized
import dev.andrewohara.getit.UserId
import dev.andrewohara.getit.api.createItemsMapper
import dev.andrewohara.getit.api.createListsMapper
import dev.andrewohara.getit.api.itemsTableName
import dev.andrewohara.getit.api.listsTableName
import dev.andrewohara.getit.api.security.Authorizer
import dev.andrewohara.getit.api.v1.resources.ShoppingListsResource
import dev.andrewohara.getit.dao.DynamoItemsDao
import dev.andrewohara.getit.dao.DynamoShoppingListDao
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
import io.ktor.server.application.install
import io.ktor.server.auth.Authentication
import io.ktor.server.auth.principal
import io.ktor.server.request.receive
import io.ktor.server.resources.get
import io.ktor.server.resources.post
import io.ktor.server.resources.delete
import io.ktor.server.resources.put
import io.ktor.server.response.respond
import io.ktor.server.response.respondText
import io.ktor.server.routing.routing
import io.ktor.util.pipeline.PipelineContext
import org.http4k.cloudnative.env.Environment
import org.http4k.connect.amazon.dynamodb.DynamoDb
import org.http4k.connect.amazon.dynamodb.Http
import org.http4k.connect.amazon.dynamodb.model.TableName

fun Application.createService(): GetItService {
    val env = Environment.from(environment.config.toMap().mapValues { it.toString() })
    val dynamoDb = DynamoDb.Http(env)
    return GetItService(
        lists = DynamoShoppingListDao(
            createListsMapper(
                dynamoDb,
                TableName.of(environment.config.property(listsTableName.toString()).getString())
            )
        ),
        items = DynamoItemsDao(
            createItemsMapper(
                dynamoDb,
                TableName.of(environment.config.property(itemsTableName.toString()).getString())
            )
        )
    )
}

fun Application.createRoutes(service: GetItService) = routing {
    // lists
    get<ShoppingListsResource> {
        authorize()
            .flatMap { service.getLists(it) }
            .map { it.map(ShoppingList::toDtoV1) }
            .respondTo(call)
    }
    post<ShoppingListsResource> {
        authorize()
            .flatMap { service.createList(it, call.receive()) }
            .map { it.toDtoV1() }.respondTo(call)
    }
    delete<ShoppingListsResource.ListId> { id ->
        authorize()
            .flatMap { service.deleteList(it, id.toValue()) }
            .map { it.toDtoV1() }
            .respondTo(call)
    }
    put<ShoppingListsResource.ListId> { id ->
        authorize()
            .flatMap { service.updateList(it, id.toValue(), call.receive()) }
            .map { it.toDtoV1() }
            .respondTo(call)
    }

    // items
    post<ShoppingListsResource.ListId.Items> { (listId) ->
        authorize()
            .flatMap { service.addItem(it, listId.toValue(), call.receive()) }
            .map { it.toDtoV1() }
            .respondTo(call)
    }
    get<ShoppingListsResource.ListId.Items> { (listId) ->
        authorize()
            .flatMap { service.getItems(it, listId.toValue()) }
            .map { it.map(ShoppingItem::toDtoV1) }
            .respondTo(call)
    }
    put<ShoppingListsResource.ListId.Items.ItemId> { itemId ->
        authorize()
            .flatMap { service.updateItem(it, itemId.items.listId.toValue(), itemId.toValue(), call.receive()) }
            .map { it.toDtoV1() }
            .respondTo(call)
    }
    delete<ShoppingListsResource.ListId.Items.ItemId> { itemId ->
        authorize()
            .flatMap { service.deleteItem(it, itemId.items.listId.toValue(), itemId.toValue()) }
            .map { it.toDtoV1() }
            .respondTo(call)
    }
}

fun Application.createAuthorization(authorizer: Authorizer) {
    install(Authentication) {
        bearer {
            lookup = { authorizer(it) }
        }
    }
}

private fun PipelineContext<Unit, ApplicationCall>.authorize() = call.principal<UserId>().asResultOr { Unauthorized }

private suspend fun Result<Any, ShoppingError>.respondTo(call: ApplicationCall) {
    this
        .peek { call.respond(it) }
        .peekFailure { when(it) {
            is ListNotFound -> call.respondText("list not found", status = HttpStatusCode.NotFound)
            is ItemNotFound -> call.respondText("item not found", status = HttpStatusCode.NotFound)
            Unauthorized -> call.respondText("Unauthorized", status = HttpStatusCode.Unauthorized)
        } }
}