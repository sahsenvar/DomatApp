package com.domatapp.core.processor.local

import com.google.devtools.ksp.processing.*
import com.google.devtools.ksp.symbol.*
import com.google.devtools.ksp.validate
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ksp.toClassName
import com.squareup.kotlinpoet.ksp.toTypeName
import com.squareup.kotlinpoet.ksp.writeTo

/**
 * KSP Processor that generates KeyValueApi-based implementations for @LocalDataSource interfaces.
 */
class LocalDataSourceProcessor(
    private val codeGenerator: CodeGenerator,
    private val logger: KSPLogger
) : SymbolProcessor {

    override fun process(resolver: Resolver): List<KSAnnotated> {
        val localDataSourceAnnotation = "com.domatapp.core.local.annotations.LocalDataSource"

        val symbols = resolver.getSymbolsWithAnnotation(localDataSourceAnnotation)
        val validSymbols = symbols.filter { it is KSClassDeclaration && it.validate() }

        validSymbols.filterIsInstance<KSClassDeclaration>().forEach { classDeclaration ->
            if (classDeclaration.classKind != ClassKind.INTERFACE) {
                logger.error("@LocalDataSource can only be applied to interfaces", classDeclaration)
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

        // Extract store name from @LocalDataSource(name = "...")
        val localDataSourceAnnotation = interfaceDeclaration.annotations.first {
            it.shortName.asString() == "LocalDataSource"
        }
        val storeName = localDataSourceAnnotation.arguments
            .firstOrNull { it.name?.asString() == "name" }?.value as? String
            ?: localDataSourceAnnotation.arguments.firstOrNull()?.value as? String
            ?: throw IllegalArgumentException("@LocalDataSource must have a 'name' parameter on $interfaceName")

        logger.info("Generating $implClassName for $interfaceName with store name '$storeName'")

        val keyValueApiClass = ClassName("com.domatapp.core.local.api", "KeyValueApi")
        val namedAnnotation = ClassName("org.koin.core.annotation", "Named")
        val singleAnnotation = ClassName("org.koin.core.annotation", "Single")

        val typeSpec = TypeSpec.classBuilder(implClassName)
            .addAnnotation(singleAnnotation)
            .addSuperinterface(interfaceDeclaration.toClassName())
            .primaryConstructor(
                FunSpec.constructorBuilder()
                    .addParameter(
                        ParameterSpec.builder("keyValueApi", keyValueApiClass)
                            .addAnnotation(
                                AnnotationSpec.builder(namedAnnotation)
                                    .addMember("%S", storeName)
                                    .build()
                            )
                            .build()
                    )
                    .build()
            )
            .addProperty(
                PropertySpec.builder("keyValueApi", keyValueApiClass)
                    .initializer("keyValueApi")
                    .addModifiers(KModifier.PRIVATE)
                    .build()
            )
            .apply {
                interfaceDeclaration.getAllFunctions()
                    .filter { it.parentDeclaration == interfaceDeclaration }
                    .forEach { function ->
                        addFunction(generateFunctionImplementation(function))
                    }
            }
            .build()

        val file = codeGenerator.createNewFile(
            dependencies = Dependencies(aggregating = false, interfaceDeclaration.containingFile!!),
            packageName = packageName,
            fileName = implClassName
        )

        file.bufferedWriter().use { writer ->
            writer.write("package $packageName\n\n")

            val fileSpec = FileSpec.builder("", implClassName)
                .addType(typeSpec)
                .build()

            val content = StringBuilder()
            fileSpec.writeTo(content)

            var contentStr = content.toString().trim()
            contentStr = contentStr.replace("`data`", "data")
            contentStr = contentStr.replace("`annotation`", "annotation")

            writer.write(contentStr)
        }
    }

    private fun generateFunctionImplementation(function: KSFunctionDeclaration): FunSpec {
        val functionName = function.simpleName.asString()

        val keyValueAnnotation = function.annotations.firstOrNull { annotation ->
            val annotationName = annotation.shortName.asString()
            annotationName in setOf("Retrieve", "Save", "Clear", "ClearAll")
        }

        if (keyValueAnnotation == null) {
            throw IllegalArgumentException("Function $functionName must have a KeyValue annotation (Retrieve, Save, Clear, ClearAll)")
        }

        val annotationName = keyValueAnnotation.shortName.asString()

        return FunSpec.builder(functionName)
            .addModifiers(KModifier.OVERRIDE)
            .apply {
                // Add parameters from the interface function
                function.parameters.forEach { param ->
                    addParameter(
                        param.name!!.asString(),
                        param.type.resolve().toClassName()
                    )
                }

                when (annotationName) {
                    "Save" -> {
                        addModifiers(KModifier.SUSPEND)
                        val returnType = function.returnType?.resolve()
                        if (returnType != null && returnType.declaration.simpleName.asString() != "Unit") {
                            returns(returnType.toTypeName())
                        }
                        addCode(generateSaveBody(keyValueAnnotation, function))
                    }
                    "Retrieve" -> {
                        // Retrieve returns Flow<T?>
                        val returnType = function.returnType?.resolve()
                        if (returnType != null) {
                            returns(returnType.toTypeName())
                        }
                        addCode(generateRetrieveBody(keyValueAnnotation, function))
                    }
                    "Clear" -> {
                        addModifiers(KModifier.SUSPEND)
                        addCode(generateClearBody(keyValueAnnotation))
                    }
                    "ClearAll" -> {
                        addModifiers(KModifier.SUSPEND)
                        addCode(generateClearAllBody())
                    }
                }
            }
            .build()
    }

    private fun extractKey(annotation: KSAnnotation): String {
        // Try named parameter first
        val namedKey = annotation.arguments.firstOrNull { it.name?.asString() == "key" }?.value as? String
        if (namedKey != null) return namedKey

        // Fall back to positional (first argument)
        val positionalKey = annotation.arguments.firstOrNull()?.value as? String
        return positionalKey ?: throw IllegalArgumentException("Annotation must have a 'key' parameter")
    }

    private fun generateSaveBody(
        annotation: KSAnnotation,
        function: KSFunctionDeclaration
    ): CodeBlock {
        val codeBuilder = CodeBlock.builder()
        val key = extractKey(annotation)

        if (function.parameters.isEmpty()) {
            throw IllegalArgumentException("Save requires at least one parameter to save.")
        }
        val param = function.parameters.first()
        val paramName = param.name!!.asString()
        val paramType = param.type.resolve().declaration.simpleName.asString()

        val methodName = when (paramType) {
            "String" -> "saveString"
            "Int" -> "saveInt"
            "Boolean" -> "saveBoolean"
            "Long" -> "saveLong"
            "Float" -> "saveFloat"
            "Double" -> "saveDouble"
            "ByteArray" -> "saveByteArray"
            else -> "saveSerializable"
        }

        codeBuilder.addStatement("keyValueApi.%L(%S, %L)", methodName, key, paramName)
        return codeBuilder.build()
    }

    private fun generateRetrieveBody(
        annotation: KSAnnotation,
        function: KSFunctionDeclaration
    ): CodeBlock {
        val codeBuilder = CodeBlock.builder()
        val key = extractKey(annotation)

        // Get the Flow's type argument to determine which retrieve method to call
        val returnType = function.returnType?.resolve()
        val flowTypeArg = returnType?.arguments?.firstOrNull()?.type?.resolve()
        val innerTypeName = flowTypeArg?.declaration?.simpleName?.asString() ?: "String"

        val methodName = when (innerTypeName) {
            "String" -> "retrieveString"
            "Int" -> "retrieveInt"
            "Boolean" -> "retrieveBoolean"
            "Long" -> "retrieveLong"
            "Float" -> "retrieveFloat"
            "Double" -> "retrieveDouble"
            "ByteArray" -> "retrieveByteArray"
            else -> "retrieveSerializable"
        }

        codeBuilder.addStatement("return keyValueApi.%L(%S)", methodName, key)
        return codeBuilder.build()
    }

    private fun generateClearBody(annotation: KSAnnotation): CodeBlock {
        val codeBuilder = CodeBlock.builder()
        val key = extractKey(annotation)

        // We use eraseString as the default erase method; for a more robust approach,
        // we'd need to know the type being cleared. Since keys are typically strings
        // in auth flows, this is sufficient.
        codeBuilder.addStatement("keyValueApi.eraseString(%S)", key)
        return codeBuilder.build()
    }

    private fun generateClearAllBody(): CodeBlock {
        val codeBuilder = CodeBlock.builder()
        codeBuilder.addStatement("keyValueApi.clearAll()")
        return codeBuilder.build()
    }
}

class LocalDataSourceProcessorProvider : SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
        return LocalDataSourceProcessor(
            codeGenerator = environment.codeGenerator,
            logger = environment.logger
        )
    }
}
