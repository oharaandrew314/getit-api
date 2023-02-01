package dev.andrewohara.getit.spring

import dev.andrewohara.getit.UserId
import dev.andrewohara.getit.api.Authorizer
import dev.andrewohara.getit.dao.DynamoItemsDao.Companion.itemsDao
import dev.andrewohara.getit.dao.DynamoListsDao.Companion.listsDao
import org.http4k.connect.amazon.dynamodb.FakeDynamoDb
import org.http4k.connect.amazon.dynamodb.model.TableName
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.context.annotation.Profile

@Configuration
@Profile("test")
class TestConfig {

    private val dynamoDb = FakeDynamoDb().client()

    @Bean
    @Primary
    fun faeAuthorizer() = Authorizer { UserId.of(it) }

    @Bean
    @Primary
    fun fakeListsDao() = dynamoDb.listsDao(TableName.of("lists"), create = true)

    @Bean
    @Primary
    fun fakeItemsDao() = dynamoDb.itemsDao(TableName.of("items"), create = true)
}
