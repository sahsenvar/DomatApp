package com.domatapp.core.processor.navigation

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.google.devtools.ksp.validate

/**
 * KSP Processor that generates Route composables and Navigation Entry extensions.
 *
 * Scans for:
 * - @NavigationScreen(route) on @Composable functions
 * - @NavigationViewModel(route) on ViewModel classes
 * - @NavigationEffectHandler(route) on @Composable functions (optional)
 *
 * Generates per Route:
 * - {Name}Route.kt — glue composable wiring ViewModel -> Screen (+ EffectHandler if present)
 * - {Feature}PresentationEntries.kt — EntryProviderScope extension with all entries for the module
 */
class NavigationProcessor(
    private val codeGenerator: CodeGenerator,
    private val logger: KSPLogger
) : SymbolProcessor {

    companion object {
        private const val SCREEN_ANNOTATION =
            "com.domatapp.core.navigation.annotations.NavigationScreen"
        private const val VIEWMODEL_ANNOTATION =
            "com.domatapp.core.navigation.annotations.NavigationViewModel"
        private const val EFFECT_HANDLER_ANNOTATION =
            "com.domatapp.core.navigation.annotations.NavigationEffectHandler"
        private const val TOP_BAR_ANNOTATION = "com.domatapp.core.navigation.annotations.TopBar"
        private const val BOTTOM_BAR_ANNOTATION =
            "com.domatapp.core.navigation.annotations.BottomBar"
    }

    // ─── Data Classes ────────────────────────────────────────────────────────────

    private enum class ScopeType { COLUMN, BOX }

    private data class ScreenInfo(
        val functionName: String,
        val packageName: String,
        val routeClassFqn: String,
        val containingFile: com.google.devtools.ksp.symbol.KSFile?,
        val uiStateTypeFqn: String,
        val intentTypeFqn: String,
        val scopeType: ScopeType
    )

    private data class ViewModelInfo(
        val className: String,
        val packageName: String,
        val routeClassFqn: String,
        val containingFile: com.google.devtools.ksp.symbol.KSFile?,
        val uiStateTypeFqn: String,
        val intentTypeFqn: String,
        val effectTypeFqn: String
    )

    private data class EffectHandlerInfo(
        val functionName: String,
        val packageName: String,
        val routeClassFqn: String,
        val containingFile: com.google.devtools.ksp.symbol.KSFile?
    )

    private data class BarInfo(
        val functionName: String,
        val packageName: String,
        val routeClassFqn: String,
        val containingFile: com.google.devtools.ksp.symbol.KSFile?
    )

    private data class NavigationGroup(
        val routeClassFqn: String,
        val screen: ScreenInfo,
        val viewModel: ViewModelInfo,
        val effectHandler: EffectHandlerInfo?,
        val topBar: BarInfo?,
        val bottomBar: BarInfo?
    )

    // ─── Process ─────────────────────────────────────────────────────────────────

    override fun process(resolver: Resolver): List<KSAnnotated> {
        val screens = collectScreens(resolver)
        val viewModels = collectViewModels(resolver)
        val effectHandlers = collectEffectHandlers(resolver)
        val topBars = collectBars(resolver, TOP_BAR_ANNOTATION, "TopBar")
        val bottomBars = collectBars(resolver, BOTTOM_BAR_ANNOTATION, "BottomBar")

        if (screens.isEmpty() && viewModels.isEmpty()) return emptyList()

        // Validate: every screen must have a matching viewModel
        val groups = mutableListOf<NavigationGroup>()

        for (screen in screens) {
            val viewModel = viewModels.find { it.routeClassFqn == screen.routeClassFqn }
            if (viewModel == null) {
                logger.error(
                    "@NavigationScreen(${screen.routeClassFqn}) on ${screen.functionName} " +
                            "has no matching @NavigationViewModel for the same Route."
                )
                continue
            }

            val effectHandler = effectHandlers.find { it.routeClassFqn == screen.routeClassFqn }
            val topBar = topBars.find { it.routeClassFqn == screen.routeClassFqn }
            val bottomBar = bottomBars.find { it.routeClassFqn == screen.routeClassFqn }

            groups.add(
                NavigationGroup(
                    routeClassFqn = screen.routeClassFqn,
                    screen = screen,
                    viewModel = viewModel,
                    effectHandler = effectHandler,
                    topBar = topBar,
                    bottomBar = bottomBar
                )
            )
        }

        // Validate: viewModels without matching screens
        for (vm in viewModels) {
            if (screens.none { it.routeClassFqn == vm.routeClassFqn }) {
                logger.error(
                    "@NavigationViewModel(${vm.routeClassFqn}) on ${vm.className} " +
                            "has no matching @NavigationScreen for the same Route."
                )
            }
        }

        if (groups.isEmpty()) return emptyList()

        // Generate single Entries file per module — no separate Route files
        val groupsByModule = groups.groupBy { extractModulePackage(it.screen.packageName) }
        for ((modulePackage, moduleGroups) in groupsByModule) {
            generateEntriesExtension(modulePackage, moduleGroups)
        }

        return emptyList()
    }

    // ─── Collectors ──────────────────────────────────────────────────────────────

    private fun collectScreens(resolver: Resolver): List<ScreenInfo> {
        return resolver.getSymbolsWithAnnotation(SCREEN_ANNOTATION)
            .filter { it.validate() }
            .filterIsInstance<KSFunctionDeclaration>()
            .mapNotNull { function ->
                val routeFqn = extractRouteClassFqn(function.annotations, "NavigationScreen")
                    ?: return@mapNotNull null

                // Validate receiver scope: must be ColumnScope or BoxScope
                val receiverType = function.extensionReceiver?.resolve()
                val receiverName = receiverType?.declaration?.simpleName?.asString()

                val scopeType = when (receiverName) {
                    "ColumnScope" -> ScopeType.COLUMN
                    "BoxScope" -> ScopeType.BOX
                    else -> {
                        logger.error(
                            "@NavigationScreen function ${function.simpleName.asString()} must have " +
                                    "a ColumnScope or BoxScope receiver. " +
                                    "Example: fun ColumnScope.${function.simpleName.asString()}(...)",
                            function
                        )
                        return@mapNotNull null
                    }
                }

                // Validate function parameters: (uiState: UiState, onIntent: (Intent) -> Unit)
                val params = function.parameters
                if (params.size < 2) {
                    logger.error(
                        "@NavigationScreen function ${function.simpleName.asString()} must have at least 2 parameters: " +
                                "(uiState: UiState, onIntent: (Intent) -> Unit)",
                        function
                    )
                    return@mapNotNull null
                }

                val uiStateParam = params[0]
                val onIntentParam = params[1]

                val uiStateType = uiStateParam.type.resolve()
                val uiStateTypeFqn = uiStateType.declaration.qualifiedName?.asString() ?: ""

                // Extract Intent type from (Intent) -> Unit lambda parameter
                val onIntentType = onIntentParam.type.resolve()
                val intentTypeFqn = onIntentType.arguments.firstOrNull()
                    ?.type?.resolve()?.declaration?.qualifiedName?.asString() ?: ""

                ScreenInfo(
                    functionName = function.simpleName.asString(),
                    packageName = function.packageName.asString(),
                    routeClassFqn = routeFqn,
                    containingFile = function.containingFile,
                    uiStateTypeFqn = uiStateTypeFqn,
                    intentTypeFqn = intentTypeFqn,
                    scopeType = scopeType
                )
            }
            .toList()
    }

    private fun collectViewModels(resolver: Resolver): List<ViewModelInfo> {
        return resolver.getSymbolsWithAnnotation(VIEWMODEL_ANNOTATION)
            .filter { it.validate() }
            .filterIsInstance<KSClassDeclaration>()
            .mapNotNull { classDecl ->
                val routeFqn = extractRouteClassFqn(classDecl.annotations, "NavigationViewModel")
                    ?: return@mapNotNull null

                // Validate: must extend BaseViewModel<UiState, Intent, Effect>
                val baseViewModelSuperType = classDecl.superTypes
                    .map { it.resolve() }
                    .firstOrNull {
                        it.declaration.simpleName.asString() == "BaseViewModel"
                    }

                if (baseViewModelSuperType == null) {
                    logger.error(
                        "@NavigationViewModel ${classDecl.simpleName.asString()} must extend " +
                                "BaseViewModel<UiState, Intent, Effect>",
                        classDecl
                    )
                    return@mapNotNull null
                }

                val typeArgs = baseViewModelSuperType.arguments
                if (typeArgs.size < 3) {
                    logger.error(
                        "@NavigationViewModel ${classDecl.simpleName.asString()}: " +
                                "BaseViewModel must have 3 type arguments <UiState, Intent, Effect>",
                        classDecl
                    )
                    return@mapNotNull null
                }

                val uiStateFqn =
                    typeArgs[0].type?.resolve()?.declaration?.qualifiedName?.asString() ?: ""
                val intentFqn =
                    typeArgs[1].type?.resolve()?.declaration?.qualifiedName?.asString() ?: ""
                val effectFqn =
                    typeArgs[2].type?.resolve()?.declaration?.qualifiedName?.asString() ?: ""

                ViewModelInfo(
                    className = classDecl.simpleName.asString(),
                    packageName = classDecl.packageName.asString(),
                    routeClassFqn = routeFqn,
                    containingFile = classDecl.containingFile,
                    uiStateTypeFqn = uiStateFqn,
                    intentTypeFqn = intentFqn,
                    effectTypeFqn = effectFqn
                )
            }
            .toList()
    }

    private fun collectEffectHandlers(resolver: Resolver): List<EffectHandlerInfo> {
        return resolver.getSymbolsWithAnnotation(EFFECT_HANDLER_ANNOTATION)
            .filter { it.validate() }
            .filterIsInstance<KSFunctionDeclaration>()
            .mapNotNull { function ->
                val routeFqn = extractRouteClassFqn(function.annotations, "NavigationEffectHandler")
                    ?: return@mapNotNull null

                EffectHandlerInfo(
                    functionName = function.simpleName.asString(),
                    packageName = function.packageName.asString(),
                    routeClassFqn = routeFqn,
                    containingFile = function.containingFile
                )
            }
            .toList()
    }

    private fun collectBars(
        resolver: Resolver,
        annotationFqn: String,
        annotationName: String
    ): List<BarInfo> {
        return resolver.getSymbolsWithAnnotation(annotationFqn)
            .filter { it.validate() }
            .filterIsInstance<KSFunctionDeclaration>()
            .mapNotNull { function ->
                val routeFqn = extractRouteClassFqn(function.annotations, annotationName)
                    ?: return@mapNotNull null

                // Validate: must have exactly 2 parameters (uiState, onIntent)
                val params = function.parameters
                if (params.size != 2) {
                    logger.error(
                        "@$annotationName function ${function.simpleName.asString()} must have exactly 2 parameters: " +
                                "(uiState: UiState, onIntent: (Intent) -> Unit)",
                        function
                    )
                    return@mapNotNull null
                }

                BarInfo(
                    functionName = function.simpleName.asString(),
                    packageName = function.packageName.asString(),
                    routeClassFqn = routeFqn,
                    containingFile = function.containingFile
                )
            }
            .toList()
    }

    // ─── Route Class FQN Extraction ──────────────────────────────────────────────

    private fun extractRouteClassFqn(
        annotations: Sequence<KSAnnotation>,
        annotationName: String
    ): String? {
        val annotation = annotations.firstOrNull {
            it.shortName.asString() == annotationName
        } ?: return null

        val routeArg = annotation.arguments.firstOrNull { it.name?.asString() == "route" }
            ?: annotation.arguments.firstOrNull()

        val routeType = routeArg?.value as? KSType ?: return null
        return routeType.declaration.qualifiedName?.asString()
    }

    // ─── Entries Extension Generation ────────────────────────────────────────────

    /**
     * Extracts the module-level package from a screen package.
     * e.g., "com.domatapp.feature.auth.presentation.screen" -> "com.domatapp.feature.auth.presentation"
     */
    private fun extractModulePackage(screenPackage: String): String {
        // Remove trailing sub-packages like ".screen", ".navigation", etc.
        val parts = screenPackage.split(".")
        // Find the "presentation" segment and take up to it
        val presentationIndex = parts.indexOf("presentation")
        return if (presentationIndex >= 0) {
            parts.take(presentationIndex + 1).joinToString(".")
        } else {
            screenPackage
        }
    }

    /**
     * Extracts the feature name from a module package.
     * e.g., "com.domatapp.feature.auth.presentation" -> "auth"
     */
    private fun extractFeatureName(modulePackage: String): String {
        val parts = modulePackage.split(".")
        val featureIndex = parts.indexOf("feature")
        return if (featureIndex >= 0 && featureIndex + 1 < parts.size) {
            parts[featureIndex + 1]
        } else {
            "app"
        }
    }

    /**
     * Derives the entry function name from the screen function name.
     * e.g., "AuthScreen" -> "authScreenEntry"
     */
    private fun deriveEntryFunctionName(screenFunctionName: String): String {
        return screenFunctionName.replaceFirstChar { it.lowercaseChar() } + "Entry"
    }

    private fun generateEntriesExtension(modulePackage: String, groups: List<NavigationGroup>) {
        val featureName = extractFeatureName(modulePackage)
        val entriesPackage = "$modulePackage.navigation"
        val fileName = "${featureName.replaceFirstChar { it.uppercaseChar() }}PresentationEntries"

        // Collect all source files for dependencies
        val sourceFiles = groups.flatMap { group ->
            listOfNotNull(
                group.screen.containingFile,
                group.viewModel.containingFile,
                group.effectHandler?.containingFile,
                group.topBar?.containingFile,
                group.bottomBar?.containingFile
            )
        }

        val file = codeGenerator.createNewFile(
            dependencies = Dependencies(
                aggregating = true,
                *sourceFiles.toTypedArray()
            ),
            packageName = entriesPackage,
            fileName = fileName
        )

        // Determine which layout imports are needed
        val needsBox = groups.any { it.screen.scopeType == ScopeType.BOX }

        file.bufferedWriter().use { writer ->
            writer.write("// AUTO-GENERATED by NavigationProcessor — do not edit\n")
            writer.write("package $entriesPackage\n\n")

            // Imports
            writer.write("import androidx.compose.foundation.gestures.Orientation\n")
            writer.write("import androidx.compose.foundation.gestures.ScrollableState\n")
            writer.write("import androidx.compose.foundation.gestures.scrollable\n")
            writer.write("import androidx.compose.foundation.layout.Column\n")
            if (needsBox) {
                writer.write("import androidx.compose.foundation.layout.Box\n")
            }
            writer.write("import androidx.compose.foundation.rememberOverscrollEffect\n")
            writer.write("import androidx.compose.foundation.rememberScrollState\n")
            writer.write("import androidx.compose.ui.Modifier\n")
            writer.write("import androidx.compose.runtime.Composable\n")
            writer.write("import androidx.compose.runtime.CompositionLocalProvider\n")
            writer.write("import androidx.compose.runtime.collectAsState\n")
            writer.write("import androidx.compose.runtime.getValue\n")
            writer.write("import androidx.navigation3.runtime.EntryProviderScope\n")
            writer.write("import com.domatapp.core.navigation.Route\n")
            writer.write("import com.domatapp.core.presentation.compose.LocalScrollableState\n")
            writer.write("import org.koin.compose.koinInject\n")

            // Import Screen, EffectHandler, ViewModel, TopBar, BottomBar for each group
            for (group in groups) {
                writer.write("import ${group.screen.packageName}.${group.screen.functionName}\n")
                writer.write("import ${group.viewModel.packageName}.${group.viewModel.className}\n")
                if (group.effectHandler != null) {
                    writer.write("import ${group.effectHandler.packageName}.${group.effectHandler.functionName}\n")
                }
                if (group.topBar != null) {
                    writer.write("import ${group.topBar.packageName}.${group.topBar.functionName}\n")
                }
                if (group.bottomBar != null) {
                    writer.write("import ${group.bottomBar.packageName}.${group.bottomBar.functionName}\n")
                }
            }
            writer.write("\n")

            // One extension function per screen
            for (group in groups) {
                val entryFunctionName = deriveEntryFunctionName(group.screen.functionName)
                val routeRef = group.routeClassFqn.removePrefix("com.domatapp.core.navigation.")
                val innerLayout = when (group.screen.scopeType) {
                    ScopeType.COLUMN -> "Column"
                    ScopeType.BOX -> "Box"
                }

                writer.write("fun EntryProviderScope<Route>.$entryFunctionName(\n")
                writer.write("    viewModel: @Composable ($routeRef) -> ${group.viewModel.className} = { koinInject() },\n")
                writer.write("    clazzContentKey: ($routeRef) -> Any = { key -> key.toString() },\n")
                writer.write("    scrollableState: @Composable () -> ScrollableState? = { rememberScrollState() },\n")
                writer.write("    metadata: Map<String, Any> = emptyMap()\n")
                writer.write(") {\n")
                writer.write("    addEntryProvider(\n")
                writer.write("        clazz = $routeRef::class,\n")
                writer.write("        clazzContentKey = clazzContentKey,\n")
                writer.write("        metadata = metadata\n")
                writer.write("    ) { args ->\n")
                writer.write("        val viewModel = viewModel(args)\n")
                writer.write("        val uiState by viewModel.state.collectAsState()\n")
                writer.write("        CompositionLocalProvider(\n")
                writer.write("            LocalScrollableState provides scrollableState()\n")
                writer.write("        ) {\n")

                // EffectHandler (only if present)
                if (group.effectHandler != null) {
                    writer.write("            ${group.effectHandler.functionName}(effectFlow = viewModel.effect)\n")
                }

                // Outer Column
                writer.write("            Column {\n")

                // TopBar (only if present)
                if (group.topBar != null) {
                    writer.write("                ${group.topBar.functionName}(\n")
                    writer.write("                    uiState = uiState,\n")
                    writer.write("                    onIntent = viewModel::onIntent\n")
                    writer.write("                )\n")
                }

                // Inner scope layout with conditional scrollable modifier
                writer.write("                val localScrollableState = LocalScrollableState.current\n")
                writer.write("                val overscrollEffect = rememberOverscrollEffect()\n")
                writer.write("                $innerLayout(\n")
                writer.write("                    modifier = Modifier\n")
                writer.write("                        .weight(1f)\n")
                writer.write("                        .then(\n")
                writer.write("                            if (localScrollableState != null)\n")
                writer.write("                                Modifier.scrollable(\n")
                writer.write("                                    state = localScrollableState,\n")
                writer.write("                                    orientation = Orientation.Vertical,\n")
                writer.write("                                    overscrollEffect = overscrollEffect\n")
                writer.write("                                )\n")
                writer.write("                            else\n")
                writer.write("                                Modifier\n")
                writer.write("                        )\n")
                writer.write("                ) {\n")
                writer.write("                    ${group.screen.functionName}(\n")
                writer.write("                        uiState = uiState,\n")
                writer.write("                        onIntent = viewModel::onIntent\n")
                writer.write("                    )\n")
                writer.write("                }\n")

                // BottomBar (only if present)
                if (group.bottomBar != null) {
                    writer.write("                ${group.bottomBar.functionName}(\n")
                    writer.write("                    uiState = uiState,\n")
                    writer.write("                    onIntent = viewModel::onIntent\n")
                    writer.write("                )\n")
                }

                writer.write("            }\n") // Column
                writer.write("        }\n") // CompositionLocalProvider
                writer.write("    }\n") // addEntryProvider
                writer.write("}\n") // fun

                // Add blank line between functions if multiple
                if (group != groups.last()) {
                    writer.write("\n")
                }
            }
        }

        logger.info("Generated $fileName.kt in $entriesPackage")
    }
}

class NavigationProcessorProvider : SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
        return NavigationProcessor(
            codeGenerator = environment.codeGenerator,
            logger = environment.logger
        )
    }
}
