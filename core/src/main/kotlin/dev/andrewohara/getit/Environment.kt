package dev.andrewohara.getit

import dev.andrewohara.getit.api.Authorizer
import dev.andrewohara.getit.api.GetVerifier
import dev.andrewohara.getit.api.googleJwkUri
import dev.andrewohara.getit.api.jwt
import dev.andrewohara.getit.api.rsaJwks
import org.http4k.cloudnative.env.Environment
import org.http4k.cloudnative.env.EnvironmentKey
import org.http4k.connect.amazon.dynamodb.model.TableName
import org.http4k.lens.nonEmptyString
import org.http4k.lens.string
import org.http4k.lens.value
import java.time.Clock

val corsOrigins = EnvironmentKey.nonEmptyString()
    .map { it.split(",") }
    .optional("cors_origins")

val listsTableName = EnvironmentKey.value(TableName).required("lists_table_name")
val itemsTableName = EnvironmentKey.value(TableName).required("items_table_name")
val jwtAudience = EnvironmentKey.string().required("jwt_audience")

fun Authorizer.Companion.googleJwt(env: Environment) = Authorizer.jwt(
    audience = jwtAudience(env),
    getVerifier = GetVerifier.rsaJwks(googleJwkUri),
    clock = Clock.systemUTC()
)
