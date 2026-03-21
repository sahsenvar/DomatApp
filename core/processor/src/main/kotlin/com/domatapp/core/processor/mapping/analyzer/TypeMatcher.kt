package com.domatapp.core.processor.mapping.analyzer

import com.domatapp.core.processor.mapping.model.FieldInfo
import com.domatapp.core.processor.mapping.model.MappingStrategy
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.google.devtools.ksp.symbol.Modifier

/**
 * Determines the appropriate mapping strategy for field transformations.
 */
class TypeMatcher(private val logger: KSPLogger) {

    // Custom converters registered via startKMapper { } (order = priority)
    private val customConverterRegistry = mutableMapOf<String, String>()

    /**
     * Sets custom converters from KMapper configuration.
     * Order is preserved: first converter has highest priority.
     */
    fun setCustomConverters(converters: List<String>) {
        customConverterRegistry.clear()

        converters.forEach { converterFqn ->
            logger.info("Custom converter registered: $converterFqn")
        }
    }

    /**
     * Registers a custom converter by extracting its source and target types.
     * Called by MappingProcessor after resolving converter class.
     */
    fun registerCustomConverter(
        converterFqn: String,
        sourceTypeFqn: String,
        targetTypeFqn: String
    ) {
        val key = "$sourceTypeFqn→$targetTypeFqn"

        // Check if there's a built-in converter for this type pair
        val builtInConverterFqn = getBuiltInConverterForKey(key)

        // Only register if not already present (first registered = highest priority)
        if (key !in customConverterRegistry) {
            customConverterRegistry[key] = converterFqn

            if (builtInConverterFqn != null) {
                val builtInName = builtInConverterFqn.substringAfterLast('.')
                val customName = converterFqn.substringAfterLast('.')
                logger.warn("✅ $customName registered ($key) → overrides built-in $builtInName")
            } else {
                val customName = converterFqn.substringAfterLast('.')
                logger.warn("✅ $customName registered ($key)")
            }
        } else {
            val existingName = customConverterRegistry[key]!!.substringAfterLast('.')
            val newName = converterFqn.substringAfterLast('.')
            logger.warn("⚠️ $newName ignored (already registered: $existingName has priority for $key)")
        }
    }

    /**
     * Gets the built-in converter FQN for a given type key, if exists.
     */
    private fun getBuiltInConverterForKey(key: String): String? {
        return when (key) {
            "kotlin.String→kotlin.Int" -> "com.domatapp.core.mapping.converter.builtin.StringToIntConverter"
            "kotlin.String→kotlin.Long" -> "com.domatapp.core.mapping.converter.builtin.StringToLongConverter"
            "kotlin.String→kotlin.Double" -> "com.domatapp.core.mapping.converter.builtin.StringToDoubleConverter"
            "kotlin.Int→kotlin.String" -> "com.domatapp.core.mapping.converter.builtin.IntToStringConverter"
            "kotlin.Long→kotlin.String" -> "com.domatapp.core.mapping.converter.builtin.LongToStringConverter"
            "kotlin.Double→kotlin.String" -> "com.domatapp.core.mapping.converter.builtin.DoubleToStringConverter"
            "kotlin.Int→kotlin.Long" -> "com.domatapp.core.mapping.converter.builtin.IntToLongConverter"
            "kotlin.String→kotlinx.datetime.Instant" -> "com.domatapp.core.mapping.converter.builtin.StringToInstantConverter"
            "kotlin.Long→kotlinx.datetime.Instant" -> "com.domatapp.core.mapping.converter.builtin.LongToInstantConverter"
            else -> null
        }
    }

    fun determineMappingStrategy(
        sourceField: FieldInfo,
        targetField: FieldInfo,
        isReverse: Boolean = false
    ): MappingStrategy {
        // 1. Check @UseConverter
        if (sourceField.useConverter != null) {
            return MappingStrategy.Convert(sourceField.useConverter)
        }

        // 2. Check same type
        if (isSameType(sourceField.type, targetField.type)) {
            return MappingStrategy.Direct
        }

        // 3. Check collection types
        if (isCollectionType(sourceField.type) && isCollectionType(targetField.type)) {
            val sourceElementType = extractCollectionElementType(sourceField.type)
            val targetElementType = extractCollectionElementType(targetField.type)

            if (sourceElementType != null && targetElementType != null) {
                val elementStrategy = if (isSameType(sourceElementType, targetElementType)) {
                    MappingStrategy.Direct
                } else if (isDataClass(sourceElementType) && isDataClass(targetElementType)) {
                    val mapperName = "to${targetElementType.declaration.simpleName.asString()}"
                    MappingStrategy.Nested(mapperName)
                } else {
                    MappingStrategy.Direct
                }
                return MappingStrategy.Collection(elementStrategy)
            }
        }

        // 4. Check nested object mapping
        if (isDataClass(sourceField.type) && isDataClass(targetField.type)) {
            val mapperName = "to${targetField.type.declaration.simpleName.asString()}"
            return MappingStrategy.Nested(mapperName)
        }

        // 5. Check converters (custom first, then built-in)
        // For reverse mapping, swap source and target to find the correct converter
        val converterFqn = if (isReverse) {
            findConverter(targetField.type, sourceField.type)
        } else {
            findConverter(sourceField.type, targetField.type)
        }

        if (converterFqn != null) {
            return MappingStrategy.Convert(converterFqn)
        }

        logger.warn("No mapping strategy found for ${sourceField.name}: ${sourceField.type} → ${targetField.type}")
        return MappingStrategy.Direct
    }

    /**
     * Finds a converter for the given source → target type pair.
     *
     * Priority:
     * 1. Custom converters (from startKMapper) - first registered has highest priority
     * 2. Built-in converters
     */
    private fun findConverter(source: KSType, target: KSType): String? {
        val sourceFqn = source.declaration.qualifiedName?.asString()
        val targetFqn = target.declaration.qualifiedName?.asString()
        val key = "$sourceFqn→$targetFqn"

        // 1. Check custom converters (highest priority)
        customConverterRegistry[key]?.let { return it }

        // 2. Check built-in converters (fallback)
        return findBuiltInConverter(source, target)
    }

    private fun isSameType(source: KSType, target: KSType): Boolean {
        return source.declaration.qualifiedName?.asString() ==
                target.declaration.qualifiedName?.asString()
    }

    fun isCollectionType(type: KSType): Boolean {
        val fqn = type.declaration.qualifiedName?.asString() ?: return false
        return fqn.startsWith("kotlin.collections.List") ||
                fqn.startsWith("kotlin.collections.Set") ||
                fqn.startsWith("kotlinx.collections.immutable")
    }

    fun extractCollectionElementType(type: KSType): KSType? {
        return type.arguments.firstOrNull()?.type?.resolve()
    }

    private fun isDataClass(type: KSType): Boolean {
        val decl = type.declaration as? KSClassDeclaration ?: return false
        return decl.modifiers.contains(Modifier.DATA)
    }

    private fun findBuiltInConverter(source: KSType, target: KSType): String? {
        val sourceFqn = source.declaration.qualifiedName?.asString()
        val targetFqn = target.declaration.qualifiedName?.asString()

        return when ("$sourceFqn→$targetFqn") {
            // String conversions
            "kotlin.String→kotlin.Int" -> "com.domatapp.core.mapping.converter.builtin.StringToIntConverter"
            "kotlin.String→kotlin.Long" -> "com.domatapp.core.mapping.converter.builtin.StringToLongConverter"
            "kotlin.String→kotlin.Double" -> "com.domatapp.core.mapping.converter.builtin.StringToDoubleConverter"

            // Number to String conversions
            "kotlin.Int→kotlin.String" -> "com.domatapp.core.mapping.converter.builtin.IntToStringConverter"
            "kotlin.Long→kotlin.String" -> "com.domatapp.core.mapping.converter.builtin.LongToStringConverter"
            "kotlin.Double→kotlin.String" -> "com.domatapp.core.mapping.converter.builtin.DoubleToStringConverter"

            // Number conversions
            "kotlin.Int→kotlin.Long" -> "com.domatapp.core.mapping.converter.builtin.IntToLongConverter"

            // Date conversions
            "kotlin.String→kotlinx.datetime.Instant" -> "com.domatapp.core.mapping.converter.builtin.StringToInstantConverter"
            "kotlin.Long→kotlinx.datetime.Instant" -> "com.domatapp.core.mapping.converter.builtin.LongToInstantConverter"

            else -> null
        }
    }
}
