package com.domatapp.core.resulting.error

/**
 * Domain errors specific to object mapping operations.
 */
sealed class MappingError(
    message: String? = null,
    cause: Throwable? = null
) : DomainError(message, cause) {

    /**
     * Required field is missing or null in the source object.
     *
     * @param fieldPath The path to the missing field (e.g., "AuthSession:userId")
     */
    data class RequiredFieldMissing(val fieldPath: String) : MappingError(
        "Required field missing: $fieldPath"
    )

    /**
     * Type conversion failed between source and target types.
     *
     * @param sourceType The source type name
     * @param targetType The target type name
     * @param cause The underlying exception
     */
    data class TypeConversion(
        val sourceType: String,
        val targetType: String,
        override val cause: Throwable?
    ) : MappingError(
        "Failed to convert $sourceType to $targetType",
        cause
    )

    /**
     * Collection is empty but target requires non-empty collection.
     *
     * @param fieldName The field name
     */
    data class EmptyCollection(val fieldName: String) : MappingError(
        "Collection cannot be empty: $fieldName"
    )

    /**
     * Circular dependency detected in nested object mapping.
     *
     * @param path The circular path (e.g., ["User", "Profile", "User"])
     */
    data class CircularDependency(val path: List<String>) : MappingError(
        "Circular dependency detected: ${path.joinToString(" → ")}"
    )
}
