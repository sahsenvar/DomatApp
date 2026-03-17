package com.domatapp.core.remote.firestore

import com.domatapp.core.remote.firestore.model.Direction
import com.domatapp.core.remote.firestore.model.OrderByClause
import com.domatapp.core.remote.firestore.model.WhereClause
import com.domatapp.core.remote.firestore.model.WhereOperator
import com.domatapp.core.remote.mapper.toRemoteError
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.firestore.Query
import dev.gitlive.firebase.firestore.firestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.serializer
import org.koin.core.annotation.Single
import kotlin.reflect.KClass

@Single
class FirebaseFirestoreClient {

    private val firestore by lazy { Firebase.firestore }

    @OptIn(InternalSerializationApi::class)
    suspend fun <T : Any> getDocument(
        collection: String,
        documentId: String,
        responseType: KClass<T>
    ): T = try {
        val snapshot = firestore.collection(collection).document(documentId).get()
        snapshot.data(responseType.serializer())
    } catch (e: Exception) {
        throw e.toRemoteError()
    }

    @OptIn(InternalSerializationApi::class)
    suspend fun <T : Any> queryCollection(
        collection: String,
        filters: List<WhereClause> = emptyList(),
        orderBy: List<OrderByClause> = emptyList(),
        limit: Int? = null,
        responseType: KClass<T>
    ): List<T> = try {
        val query = buildQuery(collection, filters, orderBy, limit)
        val snapshot = query.get()
        snapshot.documents.map { it.data(responseType.serializer()) }
    } catch (e: Exception) {
        throw e.toRemoteError()
    }

    suspend fun addDocument(
        collection: String,
        data: Any
    ): String = try {
        val docRef = firestore.collection(collection).add(data)
        docRef.id
    } catch (e: Exception) {
        throw e.toRemoteError()
    }

    suspend fun setDocument(
        collection: String,
        documentId: String,
        data: Any,
        merge: Boolean = false
    ) = try {
        val docRef = firestore.collection(collection).document(documentId)
        if (merge) {
            docRef.set(data, merge = true)
        } else {
            docRef.set(data)
        }
    } catch (e: Exception) {
        throw e.toRemoteError()
    }

    suspend fun updateDocument(
        collection: String,
        documentId: String,
        fields: Map<String, Any?>
    ) = try {
        val docRef = firestore.collection(collection).document(documentId)
        docRef.update(fields)
    } catch (e: Exception) {
        throw e.toRemoteError()
    }

    suspend fun deleteDocument(
        collection: String,
        documentId: String
    ) = try {
        firestore.collection(collection).document(documentId).delete()
    } catch (e: Exception) {
        throw e.toRemoteError()
    }

    @OptIn(InternalSerializationApi::class)
    fun <T : Any> observeDocument(
        collection: String,
        documentId: String,
        responseType: KClass<T>
    ): Flow<T> {
        return firestore.collection(collection).document(documentId).snapshots
            .map { snapshot -> snapshot.data(responseType.serializer()) }
    }

    @OptIn(InternalSerializationApi::class)
    fun <T : Any> observeCollection(
        collection: String,
        filters: List<WhereClause> = emptyList(),
        orderBy: List<OrderByClause> = emptyList(),
        limit: Int? = null,
        responseType: KClass<T>
    ): Flow<List<T>> {
        val query = buildQuery(collection, filters, orderBy, limit)
        return query.snapshots.map { snapshot ->
            snapshot.documents.map { it.data(responseType.serializer()) }
        }
    }

    private fun buildQuery(
        collection: String,
        filters: List<WhereClause>,
        orderBy: List<OrderByClause>,
        limit: Int?
    ): Query {
        var query: Query = firestore.collection(collection)

        filters.forEach { clause ->
            val value = clause.value
            query = query.where {
                when (clause.operator) {
                    WhereOperator.EQUAL_TO -> clause.field equalTo value
                    WhereOperator.NOT_EQUAL_TO -> clause.field notEqualTo value
                    WhereOperator.LESS_THAN -> clause.field lessThan value!!
                    WhereOperator.LESS_THAN_OR_EQUAL_TO -> clause.field lessThanOrEqualTo value!!
                    WhereOperator.GREATER_THAN -> clause.field greaterThan value!!
                    WhereOperator.GREATER_THAN_OR_EQUAL_TO -> clause.field greaterThanOrEqualTo value!!
                    WhereOperator.ARRAY_CONTAINS -> clause.field contains value!!
                    WhereOperator.ARRAY_CONTAINS_ANY -> {
                        val listValue = value as? List<*>
                            ?: throw IllegalArgumentException("ARRAY_CONTAINS_ANY value must be a List")
                        clause.field containsAny listValue.filterNotNull()
                    }

                    WhereOperator.IN -> {
                        val listValue = value as? List<*>
                            ?: throw IllegalArgumentException("IN value must be a List")
                        clause.field inArray listValue.filterNotNull()
                    }

                    WhereOperator.NOT_IN -> {
                        val listValue = value as? List<*>
                            ?: throw IllegalArgumentException("NOT_IN value must be a List")
                        clause.field notInArray listValue.filterNotNull()
                    }
                }
            }
        }

        orderBy.forEach { order ->
            query = when (order.direction) {
                Direction.ASCENDING -> query.orderBy(order.field)
                Direction.DESCENDING -> query.orderBy(order.field, dev.gitlive.firebase.firestore.Direction.DESCENDING)
            }
        }

        if (limit != null) {
            query = query.limit(limit)
        }

        return query
    }
}
