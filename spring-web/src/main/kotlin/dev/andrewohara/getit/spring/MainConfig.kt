package dev.andrewohara.getit.spring

import dev.andrewohara.getit.api.Authorizer
import dev.andrewohara.getit.api.GetVerifier
import dev.andrewohara.getit.api.googleJwkUri
import dev.andrewohara.getit.api.jwt
import dev.andrewohara.getit.api.rsaJwks
import dev.andrewohara.getit.createItemsMapper
import dev.andrewohara.getit.createListsMapper
import dev.andrewohara.getit.dao.DynamoItemsDao
import dev.andrewohara.getit.dao.DynamoShoppingListDao
import org.http4k.cloudnative.env.Environment
import org.http4k.connect.amazon.CredentialsProvider
import org.http4k.connect.amazon.Profile
import org.http4k.connect.amazon.RegionProvider
import org.http4k.connect.amazon.dynamodb.DynamoDb
import org.http4k.connect.amazon.dynamodb.Http
import org.http4k.connect.amazon.dynamodb.model.TableName
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import java.time.Clock

@Configuration
@Profile("main")
class MainConfig {
    private val env = Environment.ENV
    private val dynamoDb = DynamoDb.Http(RegionProvider.Profile(env).orElseThrow(), CredentialsProvider.Profile(env))

    @Bean
    fun listsDao() = DynamoShoppingListDao(createListsMapper(dynamoDb, TableName.of(env["lists_table_name"]!!)))

    @Bean
    fun itemsDao() = DynamoItemsDao(createItemsMapper(dynamoDb, TableName.of(env["items_table_name"]!!)))

    @Bean
    fun authorizer(): Authorizer = Authorizer.jwt(
        audience = System.getenv("jwt_audience"),
        getVerifier = GetVerifier.rsaJwks(googleJwkUri),
        clock = Clock.systemUTC()
    )
}
