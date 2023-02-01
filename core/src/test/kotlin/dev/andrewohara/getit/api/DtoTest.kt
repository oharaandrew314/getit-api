package dev.andrewohara.getit.api

import dev.andrewohara.getit.GetItMoshi
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class DtoTest {

    private val marshaller = GetItMoshi

    @Test
    fun `serialize list`() {
        val json = marshaller.asFormatString(sampleShoppingListDtoV1)
        marshaller.asA<ShoppingListDtoV1>(json) shouldBe sampleShoppingListDtoV1
    }

    @Test
    fun `serialize item`() {
        val json = marshaller.asFormatString(sampleShoppingItemDtoV1)
        marshaller.asA<ShoppingItemDtoV1>(json) shouldBe sampleShoppingItemDtoV1
    }

    @Test
    fun `serialize list data`() {
        val json = marshaller.asFormatString(sampleShoppingListDataDtoV1)
        marshaller.asA<ShoppingListDataDtoV1>(json) shouldBe sampleShoppingListDataDtoV1
    }

    @Test
    fun `serialize item data`() {
        val json = marshaller.asFormatString(sampleShoppingItemDtoV1)
        marshaller.asA<ShoppingItemDtoV1>(json) shouldBe sampleShoppingItemDtoV1
    }
}
