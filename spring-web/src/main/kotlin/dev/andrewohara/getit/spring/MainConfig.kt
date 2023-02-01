package dev.andrewohara.getit.spring

import dev.andrewohara.getit.api.Authorizer
import dev.andrewohara.getit.api.GetVerifier
import dev.andrewohara.getit.api.googleJwkUri
import dev.andrewohara.getit.api.jwt
import dev.andrewohara.getit.api.rsaJwks
import dev.andrewohara.getit.dao.DynamoItemsDao.Companion.itemsDao
import dev.andrewohara.getit.dao.DynamoListsDao.Companion.listsDao
import dev.andrewohara.getit.itemsTableName
import dev.andrewohara.getit.listsTableName
import org.http4k.cloudnative.env.Environment
import org.http4k.connect.amazon.CredentialsProvider
import org.http4k.connect.amazon.Profile
import org.http4k.connect.amazon.RegionProvider
import org.http4k.connect.amazon.dynamodb.DynamoDb
import org.http4k.connect.amazon.dynamodb.Http
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import java.time.Clock

@Configuration
@Profile("main")
class MainConfig {
    private val env = Environment.ENV
    private val dynamoDb = DynamoDb.Http(RegionProvider.Profile(env).orElseThrow(), CredentialsProvider.Profile(env))

    @Bean fun listsDao() = dynamoDb.listsDao(listsTableName(env))
    @Bean fun itemsDao() = dynamoDb.itemsDao(itemsTableName(env))

    @Bean
    fun authorizer(): Authorizer = Authorizer.jwt(
        audience = System.getenv("jwt_audience"),
        getVerifier = GetVerifier.rsaJwks(googleJwkUri),
        clock = Clock.systemUTC()
    )
}
