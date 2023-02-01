package dev.andrewohara.getit.spring

import dev.andrewohara.getit.GetItService
import dev.andrewohara.getit.ItemNotFound
import dev.andrewohara.getit.ListNotFound
import dev.andrewohara.getit.ShoppingError
import dev.andrewohara.getit.ShoppingItem
import dev.andrewohara.getit.ShoppingItemId
import dev.andrewohara.getit.ShoppingList
import dev.andrewohara.getit.ShoppingListId
import dev.andrewohara.getit.Unauthorized
import dev.andrewohara.getit.UserId
import dev.andrewohara.getit.api.ShoppingItemDataDtoV1
import dev.andrewohara.getit.api.ShoppingItemDtoV1
import dev.andrewohara.getit.api.ShoppingListDataDtoV1
import dev.andrewohara.getit.api.ShoppingListDtoV1
import dev.andrewohara.getit.api.toDtoV1
import dev.andrewohara.getit.api.toModel
import dev.forkhandles.result4k.get
import dev.forkhandles.result4k.map
import dev.forkhandles.result4k.mapFailure
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/v1")
class GetItController(@Autowired private val service: GetItService) {

    @GetMapping("/lists")
    fun getLists(auth: Authentication): ResponseEntity<List<ShoppingListDtoV1>> = service
        .getLists(auth.userId)
        .map { ResponseEntity.ok(it.map(ShoppingList::toDtoV1)) }
        .mapFailure { it.toEntity<List<ShoppingListDtoV1>>() }
        .get()

    @PostMapping("/lists")
    fun createList(auth: Authentication, @RequestBody data: ShoppingListDataDtoV1): ResponseEntity<ShoppingListDtoV1> {
        return service
            .createList(auth.userId, data.toModel())
            .map { ResponseEntity.ok(it.toDtoV1()) }
            .mapFailure { it.toEntity<ShoppingListDtoV1>() }
            .get()
    }

    @DeleteMapping("/lists/{id}")
    fun deleteList(auth: Authentication, @PathVariable id: UUID): ResponseEntity<Unit> = service
        .deleteList(auth.userId, id.listId())
        .map { ResponseEntity.ok().build<Unit>() }
        .mapFailure { it.toEntity<Unit>() }
        .get()

    @PutMapping("/lists/{id}")
    fun updateList(auth: Authentication, @PathVariable id: UUID, @RequestBody data: ShoppingListDataDtoV1): ResponseEntity<ShoppingListDtoV1> = service
        .updateList(auth.userId, id.listId(), data.toModel())
        .map { ResponseEntity.ok(it.toDtoV1()) }
        .mapFailure { it.toEntity<ShoppingListDtoV1>() }
        .get()

    @PostMapping("/lists/{id}/items")
    fun addItem(
        auth: Authentication,
        @PathVariable id: UUID,
        @RequestBody data: ShoppingItemDataDtoV1
    ): ResponseEntity<ShoppingItemDtoV1> = service
        .addItem(auth.userId, id.listId(), data.toModel())
        .map { ResponseEntity.ok(it.toDtoV1()) }
        .mapFailure { it.toEntity<ShoppingItemDtoV1>() }
        .get()

    @GetMapping("/lists/{id}/items")
    fun getItems(auth: Authentication, @PathVariable id: UUID): ResponseEntity<List<ShoppingItemDtoV1>> = service
        .getItems(auth.userId, id.listId())
        .map { ResponseEntity.ok(it.map(ShoppingItem::toDtoV1)) }
        .mapFailure { it.toEntity<List<ShoppingItemDtoV1>>() }
        .get()

    @PutMapping("/lists/{listId}/items/{itemId}")
    fun updateItem(
        auth: Authentication,
        @PathVariable listId: UUID,
        @PathVariable itemId: UUID,
        @RequestBody data: ShoppingItemDataDtoV1
    ): ResponseEntity<ShoppingItemDtoV1> = service
        .updateItem(auth.userId, listId.listId(), itemId.itemId(), data.toModel())
        .map { ResponseEntity.ok(it.toDtoV1()) }
        .mapFailure { it.toEntity<ShoppingItemDtoV1>() }
        .get()

    @DeleteMapping("/lists/{listId}/items/{itemId}")
    @ResponseStatus(HttpStatus.OK, reason = "item deleted")
//    @ResponseStatus(HttpStatus.NOT_FOUND, reason = "item not found")
    fun deleteItem(
        auth: Authentication,
        @PathVariable listId: UUID,
        @PathVariable itemId: UUID,
    ): ResponseEntity<Unit> = service
        .deleteItem(auth.userId, listId.listId(), itemId.itemId())
        .map { ResponseEntity.ok().build<Unit>() }
        .mapFailure { it.toEntity<Unit>() }
        .get()

    private fun <T> ShoppingError.toEntity(): ResponseEntity<T> = when (this) {
        is ListNotFound -> ResponseEntity.notFound().build()
        is ItemNotFound -> ResponseEntity.notFound().build()
        Unauthorized -> ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()
    }

    private val Authentication.userId get() = principal as UserId
    private fun UUID.listId() = ShoppingListId.of(this)
    private fun UUID.itemId() = ShoppingItemId.of(this)
}
