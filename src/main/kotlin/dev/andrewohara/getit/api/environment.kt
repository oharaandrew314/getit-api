package dev.andrewohara.getit.api

import org.http4k.cloudnative.env.EnvironmentKey
import org.http4k.connect.amazon.dynamodb.model.TableName
import org.http4k.lens.nonEmptyString
import org.http4k.lens.value

val corsOrigins = EnvironmentKey.nonEmptyString()
    .map { it.split(",") }
    .optional("cors_origins")

val listsTableName = EnvironmentKey.value(TableName).required("lists_table_name")
val itemsTableName = EnvironmentKey.value(TableName).required("items_table_name")