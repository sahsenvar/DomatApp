package com.domatapp.core.mapping.converter

import kotlin.reflect.KClass

/**
 * Platform-specific registry for MapTypeConverter instances.
 * Allows runtime lookup of converters by source and target types.
 */
expect object TypeConverterRegistry {
    /**
     * Registers a converter for the given source and target types.
     */
    fun <S : Any, T : Any> register(converter: MapTypeConverter<S, T>)

    /**
     * Retrieves a converter for the given source and target types.
     */
    fun <S : Any, T : Any> get(
        sourceType: KClass<S>,
        targetType: KClass<T>
    ): MapTypeConverter<S, T>?

    /**
     * Checks if a converter exists for the given source and target types.
     */
    fun <S : Any, T : Any> has(sourceType: KClass<S>, targetType: KClass<T>): Boolean
}
