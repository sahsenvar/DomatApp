package com.domatapp.core.remote.firestore.model

data class WhereClause(
    val field: String,
    val operator: WhereOperator,
    val value: Any?
)

enum class WhereOperator {
    EQUAL_TO, NOT_EQUAL_TO,
    LESS_THAN, LESS_THAN_OR_EQUAL_TO,
    GREATER_THAN, GREATER_THAN_OR_EQUAL_TO,
    ARRAY_CONTAINS, ARRAY_CONTAINS_ANY,
    IN, NOT_IN
}

data class OrderByClause(
    val field: String,
    val direction: Direction = Direction.ASCENDING
)

enum class Direction { ASCENDING, DESCENDING }
