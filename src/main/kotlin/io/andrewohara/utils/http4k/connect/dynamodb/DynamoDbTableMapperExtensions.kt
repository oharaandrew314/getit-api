package io.andrewohara.utils.http4k.connect.dynamodb

fun <Document: Any, HashKey: Any, SortKey: Any> DynamoDbTableMapper<Document, HashKey, SortKey>.update(
    hashKey: HashKey,
    sortKey: SortKey? = null,
    updateFn: (Document) -> Document
): Document? {
    val original = get(hashKey, sortKey) ?: return null
    val updated = updateFn(original)
    plusAssign(updated)
    return updated
}