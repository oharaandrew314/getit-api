package dev.andrewohara.getit.api

import io.kotest.matchers.shouldBe
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Test

class DtoTest {

    @Test
    fun `serialize list`() {
        val json = Json.encodeToString(sampleShoppingListDtoV1)
        Json.decodeFromString<ShoppingListDtoV1>(json) shouldBe sampleShoppingListDtoV1
    }

    @Test
    fun `serialize item`() {
        val json = Json.encodeToString(sampleShoppingItemDtoV1)
        Json.decodeFromString<ShoppingItemDtoV1>(json) shouldBe sampleShoppingItemDtoV1
    }

    @Test
    fun `serialize list data`() {
        val json = Json.encodeToString(sampleShoppingListDataDtoV1)
        Json.decodeFromString<ShoppingListDataDtoV1>(json) shouldBe sampleShoppingListDataDtoV1
    }

    @Test
    fun `serialize item data`() {
        val json = Json.encodeToString(sampleShoppingItemDtoV1)
        Json.decodeFromString<ShoppingItemDtoV1>(json) shouldBe sampleShoppingItemDtoV1
    }
}
