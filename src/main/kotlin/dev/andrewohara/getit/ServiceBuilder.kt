package dev.andrewohara.getit

import dev.andrewohara.getit.dao.DynamoItemsDao
import dev.andrewohara.getit.dao.DynamoShoppingListDao
import dev.andrewohara.getit.dao.itemIdAttr
import dev.andrewohara.getit.dao.listIdAttr
import dev.andrewohara.getit.dao.userIdAttr
import org.http4k.cloudnative.env.Environment
import org.http4k.connect.amazon.dynamodb.DynamoDb
import org.http4k.connect.amazon.dynamodb.mapper.tableMapper
import org.http4k.connect.amazon.dynamodb.model.TableName
import org.http4k.core.Method
import org.http4k.filter.AllowAll
import org.http4k.filter.AnyOf
import org.http4k.filter.CorsPolicy
import org.http4k.filter.OriginPolicy

fun createService(env: Environment, dynamoDb: DynamoDb) = GetItService(
    lists = DynamoShoppingListDao(createListsMapper(dynamoDb, listsTableName(env))),
    items = DynamoItemsDao(createItemsMapper(dynamoDb, itemsTableName(env)))
)

fun createCorsPolicy(corsOrigins: List<String>?) = CorsPolicy(
    originPolicy = corsOrigins
        ?.let { OriginPolicy.AnyOf(it) }
        ?: OriginPolicy.AllowAll(),
    headers = listOf("Authorization"),
    methods = listOf(Method.GET, Method.POST, Method.PUT, Method.DELETE),
    credentials = true
)

fun createListsMapper(
    dynamoDb: DynamoDb,
    tableName: TableName
) = dynamoDb.tableMapper<ShoppingList, UserId, ShoppingListId>(
        TableName = tableName,
        hashKeyAttribute = userIdAttr,
        sortKeyAttribute = listIdAttr,
        autoMarshalling = GetItJson
    )

fun createItemsMapper(
    dynamoDb: DynamoDb,
    tableName: TableName
) = dynamoDb.tableMapper<ShoppingItem, ShoppingListId, ShoppingItemId>(
    TableName = tableName,
    hashKeyAttribute = listIdAttr,
    sortKeyAttribute = itemIdAttr,
    autoMarshalling = GetItJson
)
