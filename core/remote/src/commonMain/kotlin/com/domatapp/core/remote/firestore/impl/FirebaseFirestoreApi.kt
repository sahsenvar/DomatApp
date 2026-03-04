package com.domatapp.core.remote.firestore.impl

import com.domatapp.core.remote.firestore.api.FirestoreApi
import com.domatapp.core.remote.firestore.model.Direction
import com.domatapp.core.remote.firestore.model.OrderByClause
import com.domatapp.core.remote.firestore.model.WhereClause
import com.domatapp.core.remote.firestore.model.WhereOperator
import com.domatapp.core.remote.mapper.toRemoteError
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.firestore.Query
import dev.gitlive.firebase.firestore.firestore
import dev.gitlive.firebase.firestore.where
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.serializer
import org.koin.core.annotation.Single
import kotlin.reflect.KClass

@Single
class FirebaseFirestoreApi : FirestoreApi {

    private val firestore by lazy { Firebase.firestore }

    @OptIn(InternalSerializationApi::class)
    override suspend fun <T : Any> getDocument(
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
    override suspend fun <T : Any> queryCollection(
        collection: String,
        filters: List<WhereClause>,
        orderBy: List<OrderByClause>,
        limit: Int?,
        responseType: KClass<T>
    ): List<T> = try {
        val query = buildQuery(collection, filters, orderBy, limit)
        val snapshot = query.get()
        snapshot.documents.map { it.data(responseType.serializer()) }
    } catch (e: Exception) {
        throw e.toRemoteError()
    }

    override suspend fun addDocument(
        collection: String,
        data: Any
    ): String = try {
        val docRef = firestore.collection(collection).add(data)
        docRef.id
    } catch (e: Exception) {
        throw e.toRemoteError()
    }

    override suspend fun setDocument(
        collection: String,
        documentId: String,
        data: Any,
        merge: Boolean
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

    override suspend fun updateDocument(
        collection: String,
        documentId: String,
        fields: Map<String, Any?>
    ) = try {
        val docRef = firestore.collection(collection).document(documentId)
        docRef.update(fields)
    } catch (e: Exception) {
        throw e.toRemoteError()
    }

    override suspend fun deleteDocument(
        collection: String,
        documentId: String
    ) = try {
        firestore.collection(collection).document(documentId).delete()
    } catch (e: Exception) {
        throw e.toRemoteError()
    }

    @OptIn(InternalSerializationApi::class)
    override fun <T : Any> observeDocument(
        collection: String,
        documentId: String,
        responseType: KClass<T>
    ): Flow<T> {
        return firestore.collection(collection).document(documentId).snapshots
            .map { snapshot -> snapshot.data(responseType.serializer()) }
    }

    @OptIn(InternalSerializationApi::class)
    override fun <T : Any> observeCollection(
        collection: String,
        filters: List<WhereClause>,
        orderBy: List<OrderByClause>,
        limit: Int?,
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
                    @Suppress("UNCHECKED_CAST")
                    WhereOperator.ARRAY_CONTAINS_ANY -> clause.field containsAny (value as List<Any>)
                    @Suppress("UNCHECKED_CAST")
                    WhereOperator.IN -> clause.field inArray (value as List<Any>)
                    @Suppress("UNCHECKED_CAST")
                    WhereOperator.NOT_IN -> clause.field notInArray (value as List<Any>)
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
