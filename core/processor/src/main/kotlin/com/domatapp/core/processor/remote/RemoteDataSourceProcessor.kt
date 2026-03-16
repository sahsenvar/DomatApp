package com.domatapp.core.processor.remote

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
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.ksp.toClassName
import com.squareup.kotlinpoet.ksp.toTypeName

/**
 * KSP Processor that generates concrete client-based implementations for @RemoteDataSource interfaces.
 *
 * Analyzes which backend types (REST, WebSocket, Firestore, RemoteConfig) each DataSource uses
 * and injects only the required concrete clients (HttpClient, Json, etc.).
 */
class RemoteDataSourceProcessor(
    private val codeGenerator: CodeGenerator,
    private val logger: KSPLogger
) : SymbolProcessor {

    // Annotation groupings per backend type
    private val restAnnotations = setOf("GET", "POST", "PUT", "PATCH", "DELETE")
    private val socketAnnotations = setOf("Subscribe", "Send")
    private val firestoreAnnotations = setOf(
        "GetDocument", "AddDocument", "SetDocument", "UpdateDocument",
        "DeleteDocument", "QueryCollection", "ObserveDocument", "ObserveCollection"
    )
    // Concrete client class names
    private val httpClientClass = ClassName("io.ktor.client", "HttpClient")
    private val jsonClass = ClassName("kotlinx.serialization.json", "Json")
    private val firestoreClientClass = ClassName("com.domatapp.core.remote.firestore", "FirebaseFirestoreClient")
    private val webSocketSessionClass =
        ClassName("io.ktor.client.plugins.websocket", "DefaultClientWebSocketSession")

    override fun process(resolver: Resolver): List<KSAnnotated> {
        val remoteDataSourceAnnotation = "com.domatapp.core.remote.annotations.RemoteDataSource"

        val symbols = resolver.getSymbolsWithAnnotation(remoteDataSourceAnnotation)
        val validSymbols = symbols.filter { it is KSClassDeclaration && it.validate() }

        validSymbols.filterIsInstance<KSClassDeclaration>().forEach { classDeclaration ->
            if (classDeclaration.classKind != ClassKind.INTERFACE) {
                logger.error("@RemoteDataSource can only be applied to interfaces", classDeclaration)
                return@forEach
            }

            generateImplementation(classDeclaration)
        }

        return emptyList()
    }

    /**
     * Determines which backend types are used by scanning all method annotations.
     */
    private data class BackendDependencies(
        val needsRest: Boolean,
        val needsSocket: Boolean,
        val needsFirestore: Boolean
    )

    private fun analyzeBackendDependencies(interfaceDeclaration: KSClassDeclaration): BackendDependencies {
        var needsRest = false
        var needsSocket = false
        var needsFirestore = false

        interfaceDeclaration.getAllFunctions()
            .filter { it.parentDeclaration == interfaceDeclaration }
            .forEach { function ->
                function.annotations.forEach { annotation ->
                    val name = annotation.shortName.asString()
                    when {
                        name in restAnnotations -> needsRest = true
                        name in socketAnnotations -> needsSocket = true
                        name in firestoreAnnotations -> needsFirestore = true
                    }
                }
            }

        return BackendDependencies(needsRest, needsSocket, needsFirestore)
    }

    private fun generateImplementation(interfaceDeclaration: KSClassDeclaration) {
        val packageName = interfaceDeclaration.packageName.asString()
        val interfaceName = interfaceDeclaration.simpleName.asString()
        val implClassName = "${interfaceName}Impl"

        logger.info("Generating $implClassName for $interfaceName")

        val deps = analyzeBackendDependencies(interfaceDeclaration)

        // Build constructor with only needed dependencies
        val constructorBuilder = FunSpec.constructorBuilder()
        val properties = mutableListOf<PropertySpec>()

        if (deps.needsRest || deps.needsSocket) {
            constructorBuilder.addParameter("httpClient", httpClientClass)
            properties.add(
                PropertySpec.builder("httpClient", httpClientClass)
                    .initializer("httpClient")
                    .addModifiers(KModifier.PRIVATE)
                    .build()
            )
        }
        if (deps.needsSocket) {
            constructorBuilder.addParameter("json", jsonClass)
            properties.add(
                PropertySpec.builder("json", jsonClass)
                    .initializer("json")
                    .addModifiers(KModifier.PRIVATE)
                    .build()
            )

            // Add connections map for WebSocket sessions
            val mapType = ClassName("kotlin.collections", "MutableMap")
                .parameterizedBy(ClassName("kotlin", "String"), webSocketSessionClass)

            properties.add(
                PropertySpec.builder("connections", mapType)
                    .initializer("mutableMapOf()")
                    .addModifiers(KModifier.PRIVATE)
                    .build()
            )
        }
        if (deps.needsFirestore) {
            constructorBuilder.addParameter("firestoreClient", firestoreClientClass)
            properties.add(
                PropertySpec.builder("firestoreClient", firestoreClientClass)
                    .initializer("firestoreClient")
                    .addModifiers(KModifier.PRIVATE)
                    .build()
            )
        }
        val typeSpec = TypeSpec.classBuilder(implClassName)
            .addSuperinterface(interfaceDeclaration.toClassName())
            .primaryConstructor(constructorBuilder.build())
            .addProperties(properties)
            .apply {
                // Generate method implementations
                interfaceDeclaration.getAllFunctions()
                    .filter { it.parentDeclaration == interfaceDeclaration }
                    .forEach { function ->
                        addFunction(generateFunctionImplementation(function))
                    }
            }
            .build()

        // Create FileSpec and write manually to avoid keyword escaping
        val file = codeGenerator.createNewFile(
            dependencies = Dependencies(aggregating = false, interfaceDeclaration.containingFile!!),
            packageName = packageName,
            fileName = implClassName
        )

        file.bufferedWriter().use { writer ->
            // Write package declaration without escaping
            writer.write("package $packageName\n\n")

            // Write imports and type using KotlinPoet
            val fileSpecBuilder = FileSpec.builder("", implClassName)

            if (deps.needsRest) {
                fileSpecBuilder.addImport(
                    "io.ktor.client.request",
                    "get",
                    "post",
                    "put",
                    "patch",
                    "delete",
                    "parameter",
                    "header",
                    "setBody"
                )
                fileSpecBuilder.addImport("io.ktor.client.call", "body")
                fileSpecBuilder.addImport("io.ktor.http", "contentType", "ContentType")
            }

            if (deps.needsSocket) {
                fileSpecBuilder.addImport("io.ktor.client.plugins.websocket", "webSocket")
                fileSpecBuilder.addImport("io.ktor.websocket", "Frame", "readBytes", "readText")
                fileSpecBuilder.addImport(
                    "kotlinx.coroutines.flow",
                    "flow",
                    "catch",
                    "onCompletion"
                )
                fileSpecBuilder.addImport(
                    "kotlinx.serialization",
                    "builtins.serializer",
                    "json.Json"
                )
                fileSpecBuilder.addImport("com.domatapp.core.remote.mapper", "toRemoteError")
            }

            val fileSpec = fileSpecBuilder.addType(typeSpec).build()

            // Write the content (without package declaration since we wrote it manually)
            val content = StringBuilder()
            fileSpec.writeTo(content)

            // Remove the empty package declaration and backticks from keywords
            var contentStr = content.toString().trim()

            // Remove backticks around 'data' and 'annotation' keywords
            contentStr = contentStr.replace("`data`", "data")
            contentStr = contentStr.replace("`annotation`", "annotation")

            writer.write(contentStr)
        }
    }

    private fun generateFunctionImplementation(function: KSFunctionDeclaration): FunSpec {
        val functionName = function.simpleName.asString()

        // Find HTTP method annotation
        val httpAnnotation = function.annotations.firstOrNull { annotation ->
            val annotationName = annotation.shortName.asString()
            annotationName in restAnnotations || annotationName in socketAnnotations ||
                    annotationName in firestoreAnnotations
        }

        if (httpAnnotation == null) {
            throw IllegalArgumentException("Function $functionName must have an HTTP method annotation")
        }

        val httpMethod = httpAnnotation.shortName.asString()
        // For Firestore annotations the parameter is named "collection", for others it's "path"
        val firstArg = httpAnnotation.arguments.firstOrNull()?.value as? String
        val path = firstArg
            ?: throw IllegalArgumentException("Annotation $httpMethod must have a path/collection parameter")

        return FunSpec.builder(functionName)
            .addModifiers(KModifier.OVERRIDE)
            .apply {
                // Add suspend if function is suspend
                if (function.modifiers.contains(Modifier.SUSPEND)) {
                    addModifiers(KModifier.SUSPEND)
                }

                // Add parameters (toTypeName handles generic types like Map<String, String>)
                function.parameters.forEach { param ->
                    addParameter(
                        param.name!!.asString(),
                        param.type.toTypeName()
                    )
                }

                // Add return type (toTypeName handles generic types like Flow<String>)
                val resolvedReturnType = function.returnType
                if (resolvedReturnType != null) {
                    returns(resolvedReturnType.toTypeName())
                }

                // Generate method body
                addCode(generateMethodBody(httpMethod, path, function, httpAnnotation))
            }
            .build()
    }

    /**
     * Resolves the client variable name based on annotation type.
     */
    private fun clientNameFor(httpMethod: String): String = when {
        httpMethod in restAnnotations -> "httpClient"
        httpMethod in socketAnnotations -> "httpClient"
        httpMethod in firestoreAnnotations -> "firestoreClient"
        else -> throw IllegalArgumentException("Unknown annotation: $httpMethod")
    }

    private fun generateMethodBody(
        httpMethod: String,
        path: String,
        function: KSFunctionDeclaration,
        annotation: KSAnnotation
    ): CodeBlock {
        val codeBuilder = CodeBlock.builder()
        val client = clientNameFor(httpMethod)

        // Parse parameters
        val bodyParam = function.parameters.find { param ->
            param.annotations.any { it.shortName.asString() == "Body" }
        }

        val queryParams = function.parameters.filter { param ->
            param.annotations.any { it.shortName.asString() == "Query" }
        }

        val headerParams = function.parameters.filter { param ->
            param.annotations.any { it.shortName.asString() == "Header" }
        }

        val headerMapParam = function.parameters.find { param ->
            param.annotations.any { it.shortName.asString() == "HeaderMap" }
        }

        val pathParams = function.parameters.filter { param ->
            param.annotations.any { it.shortName.asString() == "Path" }
        }

        // Build path with replacements
        var finalPath = path
        pathParams.forEach { param ->
            val paramName = param.name!!.asString()
            val pathAnnotation = param.annotations.first { it.shortName.asString() == "Path" }
            val pathName = (pathAnnotation.arguments.firstOrNull()?.value as? String)?.takeIf { it.isNotEmpty() } ?: paramName
            finalPath = finalPath.replace("{$pathName}", "\${$paramName}")
        }

        when (httpMethod) {
            "GET", "DELETE" -> {
                val methodName = httpMethod.lowercase()
                codeBuilder.add("return $client.$methodName(%S) {\n", finalPath)
                codeBuilder.indent()

                // Query params
                queryParams.forEach { param ->
                    val paramName = param.name!!.asString()
                    val queryAnnotation =
                        param.annotations.first { it.shortName.asString() == "Query" }
                    val queryName =
                        (queryAnnotation.arguments.firstOrNull()?.value as? String)?.takeIf { it.isNotEmpty() }
                            ?: paramName
                    codeBuilder.addStatement("parameter(%S, %L)", queryName, paramName)
                }

                // Headers
                headerParams.forEach { param ->
                    val paramName = param.name!!.asString()
                    val headerAnnotation =
                        param.annotations.first { it.shortName.asString() == "Header" }
                    val headerName = headerAnnotation.arguments.first().value as String
                    codeBuilder.addStatement("header(%S, %L)", headerName, paramName)
                }
                if (headerMapParam != null) {
                    codeBuilder.addStatement(
                        "%L.forEach { (key, value) -> header(key, value) }",
                        headerMapParam.name!!.asString()
                    )
                }

                codeBuilder.unindent()
                codeBuilder.add("}.body()\n")
            }

            "POST", "PUT", "PATCH" -> {
                val methodName = httpMethod.lowercase()
                codeBuilder.add("return $client.$methodName(%S) {\n", finalPath)
                codeBuilder.indent()

                // Query params
                queryParams.forEach { param ->
                    val paramName = param.name!!.asString()
                    val queryAnnotation =
                        param.annotations.first { it.shortName.asString() == "Query" }
                    val queryName =
                        (queryAnnotation.arguments.firstOrNull()?.value as? String)?.takeIf { it.isNotEmpty() }
                            ?: paramName
                    codeBuilder.addStatement("parameter(%S, %L)", queryName, paramName)
                }

                // Headers
                headerParams.forEach { param ->
                    val paramName = param.name!!.asString()
                    val headerAnnotation =
                        param.annotations.first { it.shortName.asString() == "Header" }
                    val headerName = headerAnnotation.arguments.first().value as String
                    codeBuilder.addStatement("header(%S, %L)", headerName, paramName)
                }
                if (headerMapParam != null) {
                    codeBuilder.addStatement(
                        "%L.forEach { (key, value) -> header(key, value) }",
                        headerMapParam.name!!.asString()
                    )
                }

                // Body
                if (bodyParam != null) {
                    codeBuilder.addStatement("contentType(ContentType.Application.Json)")
                    codeBuilder.addStatement("setBody(%L)", bodyParam.name!!.asString())
                }

                codeBuilder.unindent()
                codeBuilder.add("}.body()\n")
            }

            "Subscribe" -> {
                val returnType = function.returnType?.resolve()
                val messageType = returnType?.arguments?.firstOrNull()?.type?.resolve()
                if (messageType != null) {
                    codeBuilder.add("return flow {\n")
                    codeBuilder.indent()
                    codeBuilder.add("$client.webSocket(%S) {\n", finalPath)
                    codeBuilder.indent()
                    codeBuilder.addStatement("connections[%S] = this", finalPath)
                    codeBuilder.add("for (frame in incoming) {\n")
                    codeBuilder.indent()
                    codeBuilder.add("when (frame) {\n")
                    codeBuilder.indent()
                    codeBuilder.add("is Frame.Text -> {\n")
                    codeBuilder.indent()
                    codeBuilder.addStatement("val text = frame.readText()")
                    codeBuilder.addStatement(
                        "val message = json.decodeFromString(%T.serializer(), text)",
                        messageType.toClassName()
                    )
                    codeBuilder.addStatement("emit(message)")
                    codeBuilder.unindent()
                    codeBuilder.add("}\n")
                    codeBuilder.add("is Frame.Binary -> {\n")
                    codeBuilder.indent()
                    codeBuilder.addStatement("val bytes = frame.readBytes()")
                    codeBuilder.addStatement(
                        "val message = json.decodeFromString(%T.serializer(), bytes.decodeToString())",
                        messageType.toClassName()
                    )
                    codeBuilder.addStatement("emit(message)")
                    codeBuilder.unindent()
                    codeBuilder.add("}\n")
                    codeBuilder.add("else -> {}\n")
                    codeBuilder.unindent()
                    codeBuilder.add("}\n")
                    codeBuilder.unindent()
                    codeBuilder.add("}\n")
                    codeBuilder.unindent()
                    codeBuilder.add("}\n")
                    codeBuilder.unindent()
                    codeBuilder.add("}.catch { e -> throw e.toRemoteError() }\n")
                    codeBuilder.add(".onCompletion { connections.remove(%S) }\n", finalPath)
                }
            }

            "Send" -> {
                codeBuilder.add("return try {\n")
                codeBuilder.indent()
                codeBuilder.add(
                    "val connection = connections[%S] ?: throw IllegalStateException(%S)\n",
                    finalPath,
                    "No active WebSocket connection for path: $finalPath. Call subscribe() first."
                )

                if (bodyParam != null) {
                    codeBuilder.add("@Suppress(%S)\n", "UNCHECKED_CAST")
                    codeBuilder.addStatement(
                        "val serializer = %L::class.serializer() as kotlinx.serialization.KSerializer<Any>",
                        bodyParam.name!!.asString()
                    )
                    codeBuilder.addStatement(
                        "val serialized = json.encodeToString(serializer, %L)",
                        bodyParam.name!!.asString()
                    )
                    codeBuilder.addStatement("connection.send(Frame.Text(serialized))")
                }

                val returnType = function.returnType?.resolve()
                if (returnType != null) {
                    codeBuilder.add("when (val response = connection.incoming.receive()) {\n")
                    codeBuilder.indent()
                    codeBuilder.add("is Frame.Text -> {\n")
                    codeBuilder.indent()
                    codeBuilder.addStatement(
                        "json.decodeFromString(%T.serializer(), response.readText())",
                        returnType.toClassName()
                    )
                    codeBuilder.unindent()
                    codeBuilder.add("}\n")
                    codeBuilder.add("is Frame.Binary -> {\n")
                    codeBuilder.indent()
                    codeBuilder.addStatement(
                        "json.decodeFromString(%T.serializer(), response.readBytes().decodeToString())",
                        returnType.toClassName()
                    )
                    codeBuilder.unindent()
                    codeBuilder.add("}\n")
                    codeBuilder.add(
                        "else -> throw IllegalStateException(%S)\n",
                        "Unexpected frame type"
                    )
                    codeBuilder.unindent()
                    codeBuilder.add("}\n")
                }

                codeBuilder.unindent()
                codeBuilder.add("} catch (e: Exception) {\n")
                codeBuilder.indent()
                codeBuilder.addStatement("throw e.toRemoteError()")
                codeBuilder.unindent()
                codeBuilder.add("}\n")
            }

            "GetDocument" -> {
                val documentIdParam = findDocumentIdParam(function)
                val returnType = function.returnType?.resolve()
                codeBuilder.add("return $client.getDocument(\n")
                codeBuilder.indent()
                codeBuilder.addStatement("collection = %S,", finalPath)
                codeBuilder.addStatement("documentId = %L,", documentIdParam)
                codeBuilder.add("responseType = %T::class\n", returnType?.toClassName())
                codeBuilder.unindent()
                codeBuilder.add(")\n")
            }

            "AddDocument" -> {
                val bodyParamName = findBodyParam(function)
                codeBuilder.add("return $client.addDocument(\n")
                codeBuilder.indent()
                codeBuilder.addStatement("collection = %S,", finalPath)
                codeBuilder.add("data = %L\n", bodyParamName)
                codeBuilder.unindent()
                codeBuilder.add(")\n")
            }

            "SetDocument" -> {
                val documentIdParam = findDocumentIdParam(function)
                val bodyParamName = findBodyParam(function)
                val mergeArg = annotation.arguments
                    .firstOrNull { it.name?.asString() == "merge" }
                    ?.value as? Boolean ?: false
                codeBuilder.add("return $client.setDocument(\n")
                codeBuilder.indent()
                codeBuilder.addStatement("collection = %S,", finalPath)
                codeBuilder.addStatement("documentId = %L,", documentIdParam)
                codeBuilder.addStatement("data = %L,", bodyParamName)
                codeBuilder.add("merge = %L\n", mergeArg)
                codeBuilder.unindent()
                codeBuilder.add(")\n")
            }

            "UpdateDocument" -> {
                val documentIdParam = findDocumentIdParam(function)
                val fieldParams = findFieldParams(function)
                codeBuilder.add("return $client.updateDocument(\n")
                codeBuilder.indent()
                codeBuilder.addStatement("collection = %S,", finalPath)
                codeBuilder.addStatement("documentId = %L,", documentIdParam)
                codeBuilder.add("fields = mapOf(")
                fieldParams.forEachIndexed { index, (fieldName, paramName) ->
                    if (index > 0) codeBuilder.add(", ")
                    codeBuilder.add("%S to %L", fieldName, paramName)
                }
                codeBuilder.add(")\n")
                codeBuilder.unindent()
                codeBuilder.add(")\n")
            }

            "DeleteDocument" -> {
                val documentIdParam = findDocumentIdParam(function)
                codeBuilder.add("return $client.deleteDocument(\n")
                codeBuilder.indent()
                codeBuilder.addStatement("collection = %S,", finalPath)
                codeBuilder.add("documentId = %L\n", documentIdParam)
                codeBuilder.unindent()
                codeBuilder.add(")\n")
            }

            "QueryCollection" -> {
                val returnType = function.returnType?.resolve()
                val listElementType = returnType?.arguments?.firstOrNull()?.type?.resolve()
                val whereParams = findWhereParams(function)
                val orderByClauses = findOrderByClauses(function)
                val limitValue = findLimitValue(function)

                codeBuilder.add("return $client.queryCollection(\n")
                codeBuilder.indent()
                codeBuilder.addStatement("collection = %S,", finalPath)

                // Filters
                generateWhereFilters(codeBuilder, whereParams)

                // OrderBy
                generateOrderByClauses(codeBuilder, orderByClauses)

                // Limit
                codeBuilder.addStatement("limit = %L,", limitValue)

                codeBuilder.add("responseType = %T::class\n", listElementType?.toClassName())
                codeBuilder.unindent()
                codeBuilder.add(")\n")
            }

            "ObserveDocument" -> {
                val documentIdParam = findDocumentIdParam(function)
                val returnType = function.returnType?.resolve()
                val flowType = returnType?.arguments?.firstOrNull()?.type?.resolve()
                codeBuilder.add("return $client.observeDocument(\n")
                codeBuilder.indent()
                codeBuilder.addStatement("collection = %S,", finalPath)
                codeBuilder.addStatement("documentId = %L,", documentIdParam)
                codeBuilder.add("responseType = %T::class\n", flowType?.toClassName())
                codeBuilder.unindent()
                codeBuilder.add(")\n")
            }

            "ObserveCollection" -> {
                val returnType = function.returnType?.resolve()
                val listType = returnType?.arguments?.firstOrNull()?.type?.resolve()
                val elementType = listType?.arguments?.firstOrNull()?.type?.resolve()
                val whereParams = findWhereParams(function)
                val orderByClauses = findOrderByClauses(function)
                val limitValue = findLimitValue(function)

                codeBuilder.add("return $client.observeCollection(\n")
                codeBuilder.indent()
                codeBuilder.addStatement("collection = %S,", finalPath)

                // Filters
                generateWhereFilters(codeBuilder, whereParams)

                // OrderBy
                generateOrderByClauses(codeBuilder, orderByClauses)

                // Limit
                codeBuilder.addStatement("limit = %L,", limitValue)

                codeBuilder.add("responseType = %T::class\n", elementType?.toClassName())
                codeBuilder.unindent()
                codeBuilder.add(")\n")
            }
        }

        return codeBuilder.build()
    }

    // --- Firestore helper methods ---

    private fun findDocumentIdParam(function: KSFunctionDeclaration): String {
        val param = function.parameters.find { param ->
            param.annotations.any { it.shortName.asString() == "DocumentId" }
        } ?: throw IllegalArgumentException("Function ${function.simpleName.asString()} requires a @DocumentId parameter")
        return param.name!!.asString()
    }

    private fun findBodyParam(function: KSFunctionDeclaration): String {
        val param = function.parameters.find { param ->
            param.annotations.any { it.shortName.asString() == "Body" }
        } ?: throw IllegalArgumentException("Function ${function.simpleName.asString()} requires a @Body parameter")
        return param.name!!.asString()
    }

    private fun findFieldParams(function: KSFunctionDeclaration): List<Pair<String, String>> {
        return function.parameters.filter { param ->
            param.annotations.any { it.shortName.asString() == "Field" }
        }.map { param ->
            val fieldAnnotation = param.annotations.first { it.shortName.asString() == "Field" }
            val fieldName = fieldAnnotation.arguments.first().value as String
            fieldName to param.name!!.asString()
        }
    }

    private data class WhereParam(
        val operator: String,
        val field: String,
        val paramName: String
    )

    private fun findWhereParams(function: KSFunctionDeclaration): List<WhereParam> {
        val whereAnnotations = setOf(
            "WhereEqualTo", "WhereNotEqualTo",
            "WhereLessThan", "WhereLessThanOrEqualTo",
            "WhereGreaterThan", "WhereGreaterThanOrEqualTo",
            "WhereArrayContains", "WhereIn", "WhereNotIn"
        )

        return function.parameters.flatMap { param ->
            param.annotations
                .filter { it.shortName.asString() in whereAnnotations }
                .map { annotation ->
                    val annotName = annotation.shortName.asString()
                    val field = annotation.arguments.first().value as String
                    val operator = when (annotName) {
                        "WhereEqualTo" -> "EQUAL_TO"
                        "WhereNotEqualTo" -> "NOT_EQUAL_TO"
                        "WhereLessThan" -> "LESS_THAN"
                        "WhereLessThanOrEqualTo" -> "LESS_THAN_OR_EQUAL_TO"
                        "WhereGreaterThan" -> "GREATER_THAN"
                        "WhereGreaterThanOrEqualTo" -> "GREATER_THAN_OR_EQUAL_TO"
                        "WhereArrayContains" -> "ARRAY_CONTAINS"
                        "WhereIn" -> "IN"
                        "WhereNotIn" -> "NOT_IN"
                        else -> throw IllegalArgumentException("Unknown where annotation: $annotName")
                    }
                    WhereParam(operator, field, param.name!!.asString())
                }
        }
    }

    private data class OrderByInfo(val field: String, val direction: String)

    private fun findOrderByClauses(function: KSFunctionDeclaration): List<OrderByInfo> {
        return function.annotations
            .filter { it.shortName.asString() == "OrderBy" }
            .map { annotation ->
                val field = annotation.arguments.first { it.name?.asString() == "field" }.value as String
                val direction = (annotation.arguments.firstOrNull { it.name?.asString() == "direction" }?.value as? String) ?: "ASCENDING"
                OrderByInfo(field, direction)
            }
            .toList()
    }

    private fun findLimitValue(function: KSFunctionDeclaration): String {
        val limitAnnotation = function.annotations.firstOrNull { it.shortName.asString() == "Limit" }
        return if (limitAnnotation != null) {
            (limitAnnotation.arguments.first().value as Int).toString()
        } else {
            "null"
        }
    }

    private val whereClauseClass = ClassName("com.domatapp.core.remote.firestore.model", "WhereClause")
    private val whereOperatorClass = ClassName("com.domatapp.core.remote.firestore.model", "WhereOperator")
    private val orderByClauseClass = ClassName("com.domatapp.core.remote.firestore.model", "OrderByClause")
    private val directionClass = ClassName("com.domatapp.core.remote.firestore.model", "Direction")

    private fun generateWhereFilters(codeBuilder: CodeBlock.Builder, whereParams: List<WhereParam>) {
        if (whereParams.isEmpty()) {
            codeBuilder.add("filters = emptyList(),\n")
        } else {
            codeBuilder.add("filters = listOf(\n")
            codeBuilder.indent()
            whereParams.forEachIndexed { index, wp ->
                codeBuilder.add(
                    "%T(%S, %T.%L, %L)",
                    whereClauseClass, wp.field, whereOperatorClass, wp.operator, wp.paramName
                )
                if (index < whereParams.size - 1) codeBuilder.add(",")
                codeBuilder.add("\n")
            }
            codeBuilder.unindent()
            codeBuilder.add("),\n")
        }
    }

    private fun generateOrderByClauses(codeBuilder: CodeBlock.Builder, orderByClauses: List<OrderByInfo>) {
        if (orderByClauses.isEmpty()) {
            codeBuilder.add("orderBy = emptyList(),\n")
        } else {
            codeBuilder.add("orderBy = listOf(\n")
            codeBuilder.indent()
            orderByClauses.forEachIndexed { index, ob ->
                codeBuilder.add(
                    "%T(%S, %T.%L)",
                    orderByClauseClass, ob.field, directionClass, ob.direction
                )
                if (index < orderByClauses.size - 1) codeBuilder.add(",")
                codeBuilder.add("\n")
            }
            codeBuilder.unindent()
            codeBuilder.add("),\n")
        }
    }
}

class RemoteDataSourceProcessorProvider : SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
        return RemoteDataSourceProcessor(
            codeGenerator = environment.codeGenerator,
            logger = environment.logger
        )
    }
}
