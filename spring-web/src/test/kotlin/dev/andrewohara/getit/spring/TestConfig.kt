package dev.andrewohara.getit.spring

import dev.andrewohara.getit.UserId
import dev.andrewohara.getit.api.Authorizer
import dev.andrewohara.getit.createItemsMapper
import dev.andrewohara.getit.createListsMapper
import dev.andrewohara.getit.dao.DynamoItemsDao
import dev.andrewohara.getit.dao.DynamoShoppingListDao
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
    fun authorizer2() = Authorizer { UserId.of(it) }

    @Bean
    @Primary
    fun listsDao2() = createListsMapper(dynamoDb, TableName.of("lists"))
        .also { it.createTable() }
        .let { DynamoShoppingListDao(it) }

    @Bean
    @Primary
    fun itemsDao2() = createItemsMapper(dynamoDb, TableName.of("items"))
        .also { it.createTable() }
        .let { DynamoItemsDao(it) }
}
