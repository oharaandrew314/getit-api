package dev.andrewohara.getit

import com.squareup.moshi.Moshi
import org.http4k.format.ConfigurableMoshi
import org.http4k.format.EventAdapter
import org.http4k.format.ListAdapter
import org.http4k.format.MapAdapter
import org.http4k.format.ThrowableAdapter
import org.http4k.format.asConfigurable
import org.http4k.format.value
import org.http4k.format.withStandardMappings

object GetItMoshi : ConfigurableMoshi(
    Moshi.Builder()
        .addLast(EventAdapter)
        .addLast(ThrowableAdapter)
        .addLast(ListAdapter)
        .addLast(MapAdapter)
        .asConfigurable()
        .withStandardMappings()
        .value(UserId)
        .value(ShoppingListId)
        .value(ShoppingListName)
        .value(ShoppingItemId)
        .value(ShoppingItemName)
        .done()
)
