package dev.andrewohara.getit

import com.squareup.moshi.Moshi
import dev.zacsweers.moshix.reflect.MetadataKotlinJsonAdapterFactory
import org.http4k.format.ConfigurableMoshi
import org.http4k.format.ListAdapter
import org.http4k.format.MapAdapter
import org.http4k.format.asConfigurable
import org.http4k.format.value
import org.http4k.format.withStandardMappings

object GetItJson : ConfigurableMoshi(
    Moshi.Builder()
        .add(MapAdapter)
        .add(ListAdapter)
        .asConfigurable(MetadataKotlinJsonAdapterFactory())
        .withStandardMappings()
        .value(ShoppingListId)
        .value(ShoppingListName)
        .value(UserId)
        .value(ShoppingItemId)
        .value(ShoppingItemName)
        .done()
)
