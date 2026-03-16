package com.domatapp.core.processor.config

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider
import com.google.devtools.ksp.symbol.ClassKind
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.Modifier
import com.google.devtools.ksp.validate
import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.ksp.toClassName
import com.squareup.kotlinpoet.ksp.toTypeName

/**
 * KSP Processor that generates implementations for @ConfigDataSource interfaces.
 *
 * Analyzes which backend types (DataStore, RemoteConfig) each DataSource uses
 * and injects only the required dependencies.
 *
 * - LocalConfig annotations (@SaveLocalConfig, @RetrieveLocalConfig, @ObserveLocalConfig, @ClearLocalConfig, @ClearAllLocalConfig) → inject @Named DataStore<Preferences>
 * - RemoteConfig annotations (@RetrieveRemoteConfig, @ObserveRemoteConfig) → inject FirebaseRemoteConfig
 */
class ConfigDataSourceProcessor(
    private val codeGenerator: CodeGenerator,
    private val logger: KSPLogger
) : SymbolProcessor {

    private val localConfigAnnotations = setOf(
        "SaveLocalConfig", "RetrieveLocalConfig", "ObserveLocalConfig",
        "ClearLocalConfig", "ClearAllLocalConfig"
    )
    private val remoteConfigAnnotations = setOf(
        "RetrieveRemoteConfig", "ObserveRemoteConfig"
    )

    override fun process(resolver: Resolver): List<KSAnnotated> {
        val configDataSourceAnnotation = "com.domatapp.core.config.annotations.ConfigDataSource"

        val symbols = resolver.getSymbolsWithAnnotation(configDataSourceAnnotation)
        val validSymbols = symbols.filter { it is KSClassDeclaration && it.validate() }

        validSymbols.filterIsInstance<KSClassDeclaration>().forEach { classDeclaration ->
            if (classDeclaration.classKind != ClassKind.INTERFACE) {
                logger.error(
                    "@ConfigDataSource can only be applied to interfaces",
                    classDeclaration
                )
                return@forEach
            }

            generateImplementation(classDeclaration)
        }

        return emptyList()
    }

    // ─── Backend Dependency Analysis ─────────────────────────────────────────────

    private data class BackendDependencies(
        val needsDataStore: Boolean,
        val storeName: String,
        val needsRemoteConfig: Boolean
    )

    private fun analyzeBackendDependencies(interfaceDeclaration: KSClassDeclaration): BackendDependencies {
        var needsDataStore = false
        var needsRemoteConfig = false

        interfaceDeclaration.getAllFunctions()
            .filter { it.parentDeclaration == interfaceDeclaration }
            .forEach { function ->
                function.annotations.forEach { annotation ->
                    val name = annotation.shortName.asString()
                    when {
                        name in localConfigAnnotations -> needsDataStore = true
                        name in remoteConfigAnnotations -> needsRemoteConfig = true
                    }
                }
            }

        val storeName = if (needsDataStore) {
            val annotation = interfaceDeclaration.annotations.first {
                it.shortName.asString() == "ConfigDataSource"
            }
            val name = annotation.arguments
                .firstOrNull { it.name?.asString() == "name" }?.value as? String
                ?: annotation.arguments.firstOrNull()?.value as? String
                ?: ""
            if (name.isEmpty()) {
                throw IllegalArgumentException(
                    "@ConfigDataSource must have a 'name' parameter when DataStore annotations are used on ${interfaceDeclaration.simpleName.asString()}"
                )
            }
            name
        } else ""

        return BackendDependencies(needsDataStore, storeName, needsRemoteConfig)
    }

    // ─── Implementation Generation ───────────────────────────────────────────────

    private val dataStoreClass = ClassName("androidx.datastore.core", "DataStore")
    private val preferencesClass = ClassName("androidx.datastore.preferences.core", "Preferences")
    private val namedAnnotation = ClassName("org.koin.core.annotation", "Named")
    private val singleAnnotation = ClassName("org.koin.core.annotation", "Single")
    private val remoteConfigClass =
        ClassName("dev.gitlive.firebase.remoteconfig", "FirebaseRemoteConfig")

    private fun generateImplementation(interfaceDeclaration: KSClassDeclaration) {
        val packageName = interfaceDeclaration.packageName.asString()
        val interfaceName = interfaceDeclaration.simpleName.asString()
        val implClassName = "${interfaceName}Impl"

        logger.info("Generating $implClassName for $interfaceName")

        val deps = analyzeBackendDependencies(interfaceDeclaration)

        // Build constructor with only needed dependencies
        val constructorBuilder = FunSpec.constructorBuilder()
        val properties = mutableListOf<PropertySpec>()

        if (deps.needsDataStore) {
            val dataStoreType = dataStoreClass.parameterizedBy(preferencesClass)
            constructorBuilder.addParameter(
                ParameterSpec.builder("dataStore", dataStoreType)
                    .addAnnotation(
                        AnnotationSpec.builder(namedAnnotation)
                            .addMember("%S", deps.storeName)
                            .build()
                    )
                    .build()
            )
            properties.add(
                PropertySpec.builder("dataStore", dataStoreType)
                    .initializer("dataStore")
                    .addModifiers(KModifier.PRIVATE)
                    .build()
            )
        }

        if (deps.needsRemoteConfig) {
            constructorBuilder.addParameter("remoteConfig", remoteConfigClass)
            properties.add(
                PropertySpec.builder("remoteConfig", remoteConfigClass)
                    .initializer("remoteConfig")
                    .addModifiers(KModifier.PRIVATE)
                    .build()
            )
        }

        val typeSpec = TypeSpec.classBuilder(implClassName)
            .addAnnotation(singleAnnotation)
            .addSuperinterface(interfaceDeclaration.toClassName())
            .primaryConstructor(constructorBuilder.build())
            .addProperties(properties)
            .apply {
                interfaceDeclaration.getAllFunctions()
                    .filter { it.parentDeclaration == interfaceDeclaration }
                    .forEach { function ->
                        addFunction(generateFunctionImplementation(function))
                    }
            }
            .build()

        // Create file and write
        val file = codeGenerator.createNewFile(
            dependencies = Dependencies(aggregating = false, interfaceDeclaration.containingFile!!),
            packageName = packageName,
            fileName = implClassName
        )

        file.bufferedWriter().use { writer ->
            writer.write("package $packageName\n\n")

            val fileSpecBuilder = FileSpec.builder("", implClassName)

            if (deps.needsDataStore) {
                fileSpecBuilder.addImport("androidx.datastore.preferences.core", "edit")
                fileSpecBuilder.addImport(
                    "androidx.datastore.preferences.core",
                    "stringPreferencesKey", "intPreferencesKey", "booleanPreferencesKey",
                    "longPreferencesKey", "floatPreferencesKey", "doublePreferencesKey"
                )
                fileSpecBuilder.addImport("kotlinx.coroutines.flow", "map")
                fileSpecBuilder.addImport("kotlinx.coroutines.flow", "first")
            }

            if (deps.needsRemoteConfig) {
                fileSpecBuilder.addImport("kotlinx.coroutines.flow", "flow")
                fileSpecBuilder.addImport("kotlinx.coroutines.flow", "distinctUntilChanged")
                fileSpecBuilder.addImport("kotlinx.coroutines", "delay")
            }

            val fileSpec = fileSpecBuilder.addType(typeSpec).build()

            val content = StringBuilder()
            fileSpec.writeTo(content)

            var contentStr = content.toString().trim()
            contentStr = contentStr.replace("`data`", "data")
            contentStr = contentStr.replace("`annotation`", "annotation")

            writer.write(contentStr)
        }
    }

    // ─── Function Implementation Generation ──────────────────────────────────────

    private fun isFlowReturnType(function: KSFunctionDeclaration): Boolean {
        val returnType = function.returnType?.resolve() ?: return false
        return returnType.declaration.simpleName.asString() == "Flow"
    }

    private fun generateFunctionImplementation(function: KSFunctionDeclaration): FunSpec {
        val functionName = function.simpleName.asString()

        val annotation = function.annotations.firstOrNull { ann ->
            val name = ann.shortName.asString()
            name in localConfigAnnotations || name in remoteConfigAnnotations
        } ?: throw IllegalArgumentException(
            "Function $functionName must have a ConfigDataSource annotation (SaveLocalConfig, RetrieveLocalConfig, ObserveLocalConfig, ClearLocalConfig, ClearAllLocalConfig, RetrieveRemoteConfig, ObserveRemoteConfig)"
        )

        val annotationName = annotation.shortName.asString()

        // Compile-time validation: Retrieve vs Observe return type checks
        when (annotationName) {
            "RetrieveLocalConfig" -> {
                if (isFlowReturnType(function)) {
                    logger.error(
                        "@RetrieveLocalConfig must not return Flow. Use @ObserveLocalConfig for Flow-based observation.",
                        function
                    )
                }
            }

            "ObserveLocalConfig" -> {
                if (!isFlowReturnType(function)) {
                    logger.error(
                        "@ObserveLocalConfig must return Flow<T>. Use @RetrieveLocalConfig for single-value retrieval.",
                        function
                    )
                }
            }

            "RetrieveRemoteConfig" -> {
                if (isFlowReturnType(function)) {
                    logger.error(
                        "@RetrieveRemoteConfig must not return Flow. Use @ObserveRemoteConfig for Flow-based observation.",
                        function
                    )
                }
            }

            "ObserveRemoteConfig" -> {
                if (!isFlowReturnType(function)) {
                    logger.error(
                        "@ObserveRemoteConfig must return Flow<T>. Use @RetrieveRemoteConfig for single-value retrieval.",
                        function
                    )
                }
            }
        }

        return FunSpec.builder(functionName)
            .addModifiers(KModifier.OVERRIDE)
            .apply {
                // Add parameters
                function.parameters.forEach { param ->
                    addParameter(param.name!!.asString(), param.type.toTypeName())
                }

                // Add suspend if function is suspend
                if (function.modifiers.contains(Modifier.SUSPEND)) {
                    addModifiers(KModifier.SUSPEND)
                }

                // Add return type
                val returnType = function.returnType
                if (returnType != null) {
                    val resolved = returnType.resolve()
                    if (resolved.declaration.simpleName.asString() != "Unit") {
                        returns(returnType.toTypeName())
                    }
                }

                // Generate body
                when (annotationName) {
                    "SaveLocalConfig" -> addCode(generateSaveBody(annotation, function))
                    "RetrieveLocalConfig" -> addCode(generateRetrieveBody(annotation, function))
                    "ObserveLocalConfig" -> addCode(generateObserveBody(annotation, function))
                    "ClearLocalConfig" -> addCode(generateClearBody(annotation))
                    "ClearAllLocalConfig" -> addCode(generateClearAllBody())
                    "RetrieveRemoteConfig" -> addCode(
                        generateRetrieveRemoteConfigBody(
                            annotation,
                            function
                        )
                    )

                    "ObserveRemoteConfig" -> addCode(
                        generateObserveRemoteConfigBody(
                            annotation,
                            function
                        )
                    )
                }
            }
            .build()
    }

    // ─── DataStore Method Body Generators ────────────────────────────────────────

    private fun extractKey(annotation: KSAnnotation): String {
        val namedKey =
            annotation.arguments.firstOrNull { it.name?.asString() == "key" }?.value as? String
        if (namedKey != null) return namedKey
        val positionalKey = annotation.arguments.firstOrNull()?.value as? String
        return positionalKey
            ?: throw IllegalArgumentException("Annotation must have a 'key' parameter")
    }

    private fun preferencesKeyFn(typeName: String): String = when (typeName) {
        "String" -> "stringPreferencesKey"
        "Int" -> "intPreferencesKey"
        "Boolean" -> "booleanPreferencesKey"
        "Long" -> "longPreferencesKey"
        "Float" -> "floatPreferencesKey"
        "Double" -> "doublePreferencesKey"
        else -> "stringPreferencesKey"
    }

    private fun generateSaveBody(
        annotation: KSAnnotation,
        function: KSFunctionDeclaration
    ): CodeBlock {
        val key = extractKey(annotation)

        if (function.parameters.isEmpty()) {
            throw IllegalArgumentException("@SaveLocalConfig requires at least one parameter to save.")
        }

        val param = function.parameters.first()
        val paramName = param.name!!.asString()
        val paramType = param.type.resolve().declaration.simpleName.asString()
        val keyFn = preferencesKeyFn(paramType)

        return CodeBlock.builder()
            .add("dataStore.edit { prefs ->\n")
            .indent()
            .addStatement("prefs[%L(%S)] = %L", keyFn, key, paramName)
            .unindent()
            .add("}\n")
            .build()
    }

    /**
     * @RetrieveLocalConfig → suspend fun returning single value via first()
     */
    private fun generateRetrieveBody(
        annotation: KSAnnotation,
        function: KSFunctionDeclaration
    ): CodeBlock {
        val key = extractKey(annotation)

        val returnType = function.returnType?.resolve()
        // Return type is the direct type (e.g. String?), not Flow
        val typeName = returnType?.declaration?.simpleName?.asString() ?: "String"
        val keyFn = preferencesKeyFn(typeName)

        return CodeBlock.builder()
            .addStatement(
                "return dataStore.data.map { prefs -> prefs[%L(%S)] }.first()",
                keyFn,
                key
            )
            .build()
    }

    /**
     * @ObserveLocalConfig → fun returning Flow<T>
     */
    private fun generateObserveBody(
        annotation: KSAnnotation,
        function: KSFunctionDeclaration
    ): CodeBlock {
        val key = extractKey(annotation)

        val returnType = function.returnType?.resolve()
        val flowTypeArg = returnType?.arguments?.firstOrNull()?.type?.resolve()
        val innerTypeName = flowTypeArg?.declaration?.simpleName?.asString() ?: "String"
        val keyFn = preferencesKeyFn(innerTypeName)

        return CodeBlock.builder()
            .addStatement("return dataStore.data.map { prefs -> prefs[%L(%S)] }", keyFn, key)
            .build()
    }

    private fun generateClearBody(annotation: KSAnnotation): CodeBlock {
        val key = extractKey(annotation)

        return CodeBlock.builder()
            .add("dataStore.edit { prefs ->\n")
            .indent()
            .addStatement("prefs.remove(stringPreferencesKey(%S))", key)
            .addStatement("prefs.remove(intPreferencesKey(%S))", key)
            .addStatement("prefs.remove(booleanPreferencesKey(%S))", key)
            .addStatement("prefs.remove(longPreferencesKey(%S))", key)
            .addStatement("prefs.remove(floatPreferencesKey(%S))", key)
            .addStatement("prefs.remove(doublePreferencesKey(%S))", key)
            .unindent()
            .add("}\n")
            .build()
    }

    private fun generateClearAllBody(): CodeBlock {
        return CodeBlock.builder()
            .addStatement("dataStore.edit { prefs -> prefs.clear() }")
            .build()
    }

    // ─── RemoteConfig Method Body Generators ─────────────────────────────────────

    private fun remoteConfigValueAccessor(typeName: String): String = when (typeName) {
        "Boolean" -> "asBoolean()"
        "String" -> "asString()"
        "Long" -> "asLong()"
        "Double" -> "asDouble()"
        else -> "asString()"
    }

    /**
     * @RetrieveRemoteConfig → suspend fun with automatic fetchAndActivate()
     */
    private fun generateRetrieveRemoteConfigBody(
        annotation: KSAnnotation,
        function: KSFunctionDeclaration
    ): CodeBlock {
        val key = annotation.arguments.firstOrNull()?.value as? String
            ?: throw IllegalArgumentException("@RetrieveRemoteConfig must have a 'key' parameter")

        val returnType = function.returnType?.resolve()
        val returnTypeName = returnType?.declaration?.simpleName?.asString() ?: "String"
        val accessor = remoteConfigValueAccessor(returnTypeName)

        return CodeBlock.builder()
            .addStatement("remoteConfig.fetchAndActivate()")
            .addStatement("return remoteConfig.getValue(%S).%L", key, accessor)
            .build()
    }

    /**
     * @ObserveRemoteConfig → fun returning Flow<T> with polling and auto fetchAndActivate()
     */
    private fun generateObserveRemoteConfigBody(
        annotation: KSAnnotation,
        function: KSFunctionDeclaration
    ): CodeBlock {
        val key = annotation.arguments.firstOrNull()?.value as? String
            ?: throw IllegalArgumentException("@ObserveRemoteConfig must have a 'key' parameter")

        val returnType = function.returnType?.resolve()
        val flowType = returnType?.arguments?.firstOrNull()?.type?.resolve()
        val flowTypeName = flowType?.declaration?.simpleName?.asString() ?: "String"
        val accessor = remoteConfigValueAccessor(flowTypeName)

        return CodeBlock.builder()
            .add("return flow {\n")
            .indent()
            .add("while (true) {\n")
            .indent()
            .addStatement("remoteConfig.fetchAndActivate()")
            .addStatement("emit(remoteConfig.getValue(%S).%L)", key, accessor)
            .addStatement("delay(%L)", REMOTE_CONFIG_POLL_INTERVAL)
            .unindent()
            .add("}\n")
            .unindent()
            .add("}.distinctUntilChanged()\n")
            .build()
    }

    companion object {
        private const val REMOTE_CONFIG_POLL_INTERVAL = 300_000L // 5 minutes
    }
}

class ConfigDataSourceProcessorProvider : SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
        return ConfigDataSourceProcessor(
            codeGenerator = environment.codeGenerator,
            logger = environment.logger
        )
    }
}
