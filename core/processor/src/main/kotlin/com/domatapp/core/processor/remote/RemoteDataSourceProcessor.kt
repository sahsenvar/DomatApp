package com.domatapp.core.processor.remote

import com.google.devtools.ksp.processing.*
import com.google.devtools.ksp.symbol.*
import com.google.devtools.ksp.validate
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ksp.toClassName
import com.squareup.kotlinpoet.ksp.writeTo

/**
 * KSP Processor that generates RemoteApi-based implementations for @RemoteDataSource interfaces.
 */
class RemoteDataSourceProcessor(
    private val codeGenerator: CodeGenerator,
    private val logger: KSPLogger
) : SymbolProcessor {

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

    private fun generateImplementation(interfaceDeclaration: KSClassDeclaration) {
        val packageName = interfaceDeclaration.packageName.asString()
        val interfaceName = interfaceDeclaration.simpleName.asString()
        val implClassName = "${interfaceName}Impl"

        logger.info("Generating $implClassName for $interfaceName")

        val typeSpec = TypeSpec.classBuilder(implClassName)
            .addSuperinterface(interfaceDeclaration.toClassName())
            .primaryConstructor(
                FunSpec.constructorBuilder()
                    .addParameter("remoteApi", ClassName("com.domatapp.core.remote.api", "RemoteApi"))
                    .build()
            )
            .addProperty(
                PropertySpec.builder("remoteApi", ClassName("com.domatapp.core.remote.api", "RemoteApi"))
                    .initializer("remoteApi")
                    .addModifiers(KModifier.PRIVATE)
                    .build()
            )
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
            val fileSpec = FileSpec.builder("", implClassName)
                .addType(typeSpec)
                .build()

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
            annotationName in setOf(
                "GET", "POST", "PUT", "PATCH", "DELETE", "Subscribe", "Send",
                "FetchRemoteConfig", "GetRemoteConfig", "ObserveRemoteConfig",
                "GetDocument", "AddDocument", "SetDocument", "UpdateDocument",
                "DeleteDocument", "QueryCollection", "ObserveDocument", "ObserveCollection"
            )
        }

        if (httpAnnotation == null) {
            throw IllegalArgumentException("Function $functionName must have an HTTP method annotation")
        }

        val httpMethod = httpAnnotation.shortName.asString()
        val path = when (httpMethod) {
            "FetchRemoteConfig" -> ""
            else -> {
                // For Firestore annotations the parameter is named "collection", for others it's "path" or "key"
                val firstArg = httpAnnotation.arguments.firstOrNull()?.value as? String
                firstArg ?: throw IllegalArgumentException("Annotation $httpMethod must have a path/key/collection parameter")
            }
        }

        return FunSpec.builder(functionName)
            .addModifiers(KModifier.OVERRIDE)
            .apply {
                // Add suspend if function is suspend
                if (function.modifiers.contains(Modifier.SUSPEND)) {
                    addModifiers(KModifier.SUSPEND)
                }

                // Add parameters
                function.parameters.forEach { param ->
                    addParameter(
                        param.name!!.asString(),
                        param.type.resolve().toClassName()
                    )
                }

                // Add return type
                val returnType = function.returnType?.resolve()
                if (returnType != null) {
                    returns(returnType.toClassName())
                }

                // Generate method body
                addCode(generateMethodBody(httpMethod, path, function, httpAnnotation))
            }
            .build()
    }

    private fun generateMethodBody(
        httpMethod: String,
        path: String,
        function: KSFunctionDeclaration,
        annotation: KSAnnotation
    ): CodeBlock {
        val codeBuilder = CodeBlock.builder()

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
            "GET" -> {
                codeBuilder.add("return remoteApi.get(\n")
                codeBuilder.indent()
                codeBuilder.addStatement("path = %S,", finalPath)

                // Query params
                if (queryParams.isNotEmpty()) {
                    codeBuilder.add("queryParams = mapOf(")
                    queryParams.forEachIndexed { index, param ->
                        val paramName = param.name!!.asString()
                        val queryAnnotation = param.annotations.first { it.shortName.asString() == "Query" }
                        val queryName = (queryAnnotation.arguments.firstOrNull()?.value as? String)?.takeIf { it.isNotEmpty() } ?: paramName
                        if (index > 0) codeBuilder.add(", ")
                        codeBuilder.add("%S to %L", queryName, paramName)
                    }
                    codeBuilder.add("),\n")
                } else {
                    codeBuilder.add("queryParams = emptyMap(),\n")
                }

                // Headers
                generateHeaders(codeBuilder, headerParams, headerMapParam)

                // Response type
                val returnType = function.returnType?.resolve()
                if (returnType != null) {
                    codeBuilder.add("responseType = %T::class\n", returnType.toClassName())
                }

                codeBuilder.unindent()
                codeBuilder.add(")\n")
            }

            "POST", "PUT", "PATCH" -> {
                val methodName = httpMethod.lowercase()
                codeBuilder.add("return remoteApi.$methodName(\n")
                codeBuilder.indent()
                codeBuilder.addStatement("path = %S,", finalPath)

                // Body
                if (bodyParam != null) {
                    codeBuilder.addStatement("body = %L,", bodyParam.name!!.asString())
                } else {
                    codeBuilder.add("body = null,\n")
                }

                // Headers
                generateHeaders(codeBuilder, headerParams, headerMapParam)

                // Response type
                val returnType = function.returnType?.resolve()
                if (returnType != null) {
                    codeBuilder.add("responseType = %T::class\n", returnType.toClassName())
                }

                codeBuilder.unindent()
                codeBuilder.add(")\n")
            }

            "DELETE" -> {
                codeBuilder.add("return remoteApi.delete(\n")
                codeBuilder.indent()
                codeBuilder.addStatement("path = %S,", finalPath)

                // Query params
                if (queryParams.isNotEmpty()) {
                    codeBuilder.add("queryParams = mapOf(")
                    queryParams.forEachIndexed { index, param ->
                        val paramName = param.name!!.asString()
                        val queryAnnotation = param.annotations.first { it.shortName.asString() == "Query" }
                        val queryName = (queryAnnotation.arguments.firstOrNull()?.value as? String)?.takeIf { it.isNotEmpty() } ?: paramName
                        if (index > 0) codeBuilder.add(", ")
                        codeBuilder.add("%S to %L", queryName, paramName)
                    }
                    codeBuilder.add("),\n")
                } else {
                    codeBuilder.add("queryParams = emptyMap(),\n")
                }

                // Headers
                generateHeaders(codeBuilder, headerParams, headerMapParam)

                // Response type
                val returnType = function.returnType?.resolve()
                if (returnType != null) {
                    codeBuilder.add("responseType = %T::class\n", returnType.toClassName())
                }

                codeBuilder.unindent()
                codeBuilder.add(")\n")
            }

            "Subscribe" -> {
                codeBuilder.add("return remoteApi.subscribe(\n")
                codeBuilder.indent()
                codeBuilder.addStatement("path = %S,", finalPath)

                // Message type (Flow generic type)
                val returnType = function.returnType?.resolve()
                val messageType = returnType?.arguments?.firstOrNull()?.type?.resolve()
                if (messageType != null) {
                    codeBuilder.add("messageType = %T::class\n", messageType.toClassName())
                }

                codeBuilder.unindent()
                codeBuilder.add(")\n")
            }

            "Send" -> {
                codeBuilder.add("return remoteApi.send(\n")
                codeBuilder.indent()
                codeBuilder.addStatement("path = %S,", finalPath)

                // Message (body)
                if (bodyParam != null) {
                    codeBuilder.addStatement("message = %L,", bodyParam.name!!.asString())
                }

                // Response type
                val returnType = function.returnType?.resolve()
                if (returnType != null) {
                    codeBuilder.add("responseType = %T::class\n", returnType.toClassName())
                }

                codeBuilder.unindent()
                codeBuilder.add(")\n")
            }
            
            "FetchRemoteConfig" -> {
                codeBuilder.addStatement("return remoteApi.fetchAndActivate()")
            }

            "GetRemoteConfig" -> {
                val returnType = function.returnType?.resolve()
                val returnTypeName = returnType?.declaration?.simpleName?.asString()

                when (returnTypeName) {
                    "Boolean" -> codeBuilder.addStatement("return remoteApi.getBoolean(%S)", finalPath)
                    "String" -> codeBuilder.addStatement("return remoteApi.getString(%S)", finalPath)
                    "Long" -> codeBuilder.addStatement("return remoteApi.getLong(%S)", finalPath)
                    "Double" -> codeBuilder.addStatement("return remoteApi.getDouble(%S)", finalPath)
                    else -> codeBuilder.addStatement("return remoteApi.getSerializable(%S, %T::class)", finalPath, returnType?.toClassName())
                }
            }

            "ObserveRemoteConfig" -> {
                val returnType = function.returnType?.resolve()
                val flowType = returnType?.arguments?.firstOrNull()?.type?.resolve()
                val flowTypeName = flowType?.declaration?.simpleName?.asString()

                when (flowTypeName) {
                    "Boolean" -> codeBuilder.addStatement("return remoteApi.observeBoolean(%S)", finalPath)
                    "String" -> codeBuilder.addStatement("return remoteApi.observeString(%S)", finalPath)
                    "Long" -> codeBuilder.addStatement("return remoteApi.observeLong(%S)", finalPath)
                    "Double" -> codeBuilder.addStatement("return remoteApi.observeDouble(%S)", finalPath)
                    else -> codeBuilder.addStatement("return remoteApi.observeSerializable(%S, %T::class)", finalPath, flowType?.toClassName())
                }
            }

            "GetDocument" -> {
                val documentIdParam = findDocumentIdParam(function)
                val returnType = function.returnType?.resolve()
                codeBuilder.add("return remoteApi.getDocument(\n")
                codeBuilder.indent()
                codeBuilder.addStatement("collection = %S,", finalPath)
                codeBuilder.addStatement("documentId = %L,", documentIdParam)
                codeBuilder.add("responseType = %T::class\n", returnType?.toClassName())
                codeBuilder.unindent()
                codeBuilder.add(")\n")
            }

            "AddDocument" -> {
                val bodyParam = findBodyParam(function)
                codeBuilder.add("return remoteApi.addDocument(\n")
                codeBuilder.indent()
                codeBuilder.addStatement("collection = %S,", finalPath)
                codeBuilder.add("data = %L\n", bodyParam)
                codeBuilder.unindent()
                codeBuilder.add(")\n")
            }

            "SetDocument" -> {
                val documentIdParam = findDocumentIdParam(function)
                val bodyParam = findBodyParam(function)
                val mergeArg = annotation.arguments
                    .firstOrNull { it.name?.asString() == "merge" }
                    ?.value as? Boolean ?: false
                codeBuilder.add("return remoteApi.setDocument(\n")
                codeBuilder.indent()
                codeBuilder.addStatement("collection = %S,", finalPath)
                codeBuilder.addStatement("documentId = %L,", documentIdParam)
                codeBuilder.addStatement("data = %L,", bodyParam)
                codeBuilder.add("merge = %L\n", mergeArg)
                codeBuilder.unindent()
                codeBuilder.add(")\n")
            }

            "UpdateDocument" -> {
                val documentIdParam = findDocumentIdParam(function)
                val fieldParams = findFieldParams(function)
                codeBuilder.add("return remoteApi.updateDocument(\n")
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
                codeBuilder.add("return remoteApi.deleteDocument(\n")
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

                codeBuilder.add("return remoteApi.queryCollection(\n")
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
                codeBuilder.add("return remoteApi.observeDocument(\n")
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

                codeBuilder.add("return remoteApi.observeCollection(\n")
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

    // --- REST/Socket helper methods ---

    private fun generateHeaders(
        codeBuilder: CodeBlock.Builder,
        headerParams: List<KSValueParameter>,
        headerMapParam: KSValueParameter?
    ) {
        if (headerParams.isNotEmpty() || headerMapParam != null) {
            if (headerMapParam != null && headerParams.isEmpty()) {
                // Only HeaderMap
                codeBuilder.add("headers = %L,\n", headerMapParam.name!!.asString())
            } else if (headerMapParam == null && headerParams.isNotEmpty()) {
                // Only individual headers
                codeBuilder.add("headers = mapOf(")
                headerParams.forEachIndexed { index, param ->
                    val paramName = param.name!!.asString()
                    val headerAnnotation = param.annotations.first { it.shortName.asString() == "Header" }
                    val headerName = headerAnnotation.arguments.first().value as String
                    if (index > 0) codeBuilder.add(", ")
                    codeBuilder.add("%S to %L", headerName, paramName)
                }
                codeBuilder.add("),\n")
            } else {
                // Both HeaderMap and individual headers
                codeBuilder.add("headers = mapOf(")
                headerParams.forEachIndexed { index, param ->
                    val paramName = param.name!!.asString()
                    val headerAnnotation = param.annotations.first { it.shortName.asString() == "Header" }
                    val headerName = headerAnnotation.arguments.first().value as String
                    if (index > 0) codeBuilder.add(", ")
                    codeBuilder.add("%S to %L", headerName, paramName)
                }
                codeBuilder.add(") + %L,\n", headerMapParam!!.name!!.asString())
            }
        } else {
            codeBuilder.add("headers = emptyMap(),\n")
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
