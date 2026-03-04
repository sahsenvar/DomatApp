package com.domatapp.core.remote.firestore.api

import com.domatapp.core.remote.firestore.model.OrderByClause
import com.domatapp.core.remote.firestore.model.WhereClause
import kotlinx.coroutines.flow.Flow
import kotlin.reflect.KClass

/**
 * Firestore API abstraction for document CRUD and realtime observation.
 * Implementation: FirebaseFirestoreApi
 */
interface FirestoreApi {

    suspend fun <T : Any> getDocument(
        collection: String,
        documentId: String,
        responseType: KClass<T>
    ): T

    suspend fun <T : Any> queryCollection(
        collection: String,
        filters: List<WhereClause> = emptyList(),
        orderBy: List<OrderByClause> = emptyList(),
        limit: Int? = null,
        responseType: KClass<T>
    ): List<T>

    suspend fun addDocument(
        collection: String,
        data: Any
    ): String

    suspend fun setDocument(
        collection: String,
        documentId: String,
        data: Any,
        merge: Boolean = false
    )

    suspend fun updateDocument(
        collection: String,
        documentId: String,
        fields: Map<String, Any?>
    )

    suspend fun deleteDocument(
        collection: String,
        documentId: String
    )

    fun <T : Any> observeDocument(
        collection: String,
        documentId: String,
        responseType: KClass<T>
    ): Flow<T>

    fun <T : Any> observeCollection(
        collection: String,
        filters: List<WhereClause> = emptyList(),
        orderBy: List<OrderByClause> = emptyList(),
        limit: Int? = null,
        responseType: KClass<T>
    ): Flow<List<T>>
}
