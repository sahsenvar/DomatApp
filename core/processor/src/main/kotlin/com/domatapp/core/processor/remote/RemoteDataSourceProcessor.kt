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
            annotationName in setOf("GET", "POST", "PUT", "PATCH", "DELETE", "Subscribe", "Send")
        }

        if (httpAnnotation == null) {
            throw IllegalArgumentException("Function $functionName must have an HTTP method annotation")
        }

        val httpMethod = httpAnnotation.shortName.asString()
        val path = httpAnnotation.arguments.firstOrNull()?.value as? String
            ?: throw IllegalArgumentException("HTTP annotation must have a path parameter")

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
                addCode(generateMethodBody(httpMethod, path, function))
            }
            .build()
    }

    private fun generateMethodBody(
        httpMethod: String,
        path: String,
        function: KSFunctionDeclaration
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
        }

        return codeBuilder.build()
    }

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
