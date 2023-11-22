package com.omegar.mvp.compiler.ksp

import com.google.devtools.ksp.KspExperimental
import com.google.devtools.ksp.getAnnotationsByType
import com.google.devtools.ksp.getClassDeclarationByName
import com.google.devtools.ksp.innerArguments
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.symbol.*
import com.omegar.mvp.MvpPresenter
import com.omegar.mvp.compiler.NamingRules.viewStateName
import com.omegar.mvp.compiler.entities.Tagged
import com.omegar.mvp.compiler.entities.View
import com.omegar.mvp.compiler.processors.OriginatingMarker
import com.omegar.mvp.compiler.processors.ViewParser
import com.omegar.mvp.viewstate.strategy.MoxyViewCommand
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.ksp.TypeParameterResolver
import com.squareup.kotlinpoet.ksp.toClassName
import com.squareup.kotlinpoet.ksp.toTypeName
import com.squareup.kotlinpoet.ksp.toTypeParameterResolver
import com.squareup.kotlinpoet.ksp.toTypeVariableName
import java.lang.RuntimeException


/**
 * Created by Anton Knyazev on 26.04.2023.
 * Copyright (c) 2023 Omega https://omega-r.com
 */
class KspViewParser(
    private val logger: KSPLogger,
    private val resolver: Resolver,
    private val mvpView: KSType
) : ViewParser<KSClassDeclaration> {

    companion object {
        private val ANY_FUNCTION_SIMPLE_NAME = listOf("equals", "hashCode", "toString", "<init>")
        private const val PROPERTY_PARAM_NAME = "value"
    }

    private val presenterCache = mutableMapOf<String, View>()
    private val viewCache = mutableMapOf<String, View>()


    @OptIn(KspExperimental::class)
    @Suppress("PARAMETER_NAME_CHANGED_ON_OVERRIDE")
    override fun invoke(presenterDeclaration: KSClassDeclaration): View? {
        if (presenterDeclaration.simpleName.getShortName() == MvpPresenter::class.simpleName) return null

        val presenterKey = presenterDeclaration.simpleName.asString()
        presenterCache[presenterKey]?.let {
            return it
        }

        val (superPresenter, view) = presenterDeclaration.getSuperPresenterAndView()
        val viewClassName = view.toClassName()
        val viewKey = viewClassName.canonicalName
        val viewDeclaration = view.declaration as KSClassDeclaration

        viewCache[viewKey]?.let {
            it.presenterClassParamsMap
                    .apply {
                        put(
                            presenterDeclaration.toClassName(),
                            presenterDeclaration.typeParameters.map { it.toTypeVariableName() }
                        )
                    }
            it.viewTypePresenterParams
                .apply {
                    put(
                        presenterDeclaration.toClassName(),
                        view.innerArguments.map { it.toTypeName(presenterDeclaration.typeParameters.toTypeParameterResolver()) }
                    )
                }
            it.putTags(presenterDeclaration)
            presenterCache[presenterKey] = it
            return it
        }

        val oldViewStateDeclaration = resolver.getClassDeclarationByName(viewClassName.reflectionName().viewStateName)
        val newView = if (oldViewStateDeclaration != null) {
            View(
                className = viewClassName,
                presenterClassParamsMap = mutableMapOf(presenterDeclaration.toClassName() to presenterDeclaration.typeParameters.map { it.toTypeVariableName() }),
                methods = emptyList(),
                viewTypeParams = emptyList(),
                parent = null,
                viewTypeResolvedParams = emptyList(),
                viewTypePresenterParams = mutableMapOf(presenterDeclaration.toClassName() to view.innerArguments.map { it.toTypeName(presenterDeclaration.typeParameters.toTypeParameterResolver()) }),
                needGenerate = false
                ).putTags(presenterDeclaration, viewDeclaration)
        } else {
            val superView = this(superPresenter)

            val declarations = mutableListOf<KSDeclaration>(presenterDeclaration, viewDeclaration)

            View(
                className = viewClassName,
                presenterClassParamsMap = mutableMapOf(
                    presenterDeclaration.toClassName() to presenterDeclaration.typeParameters.map { it.toTypeVariableName() }
                ),
                methods = viewDeclaration.getMethods(declarations, superView),
                viewTypeResolvedParams = superView?.viewTypePresenterParams?.get(superPresenter.toClassName()).orEmpty(),
                viewTypeParams = viewDeclaration.typeParameters
                    .filterNot { mvpView.isAssignableFrom(it.bounds.first().resolve()) }
                    .map { it.toTypeVariableName() },
                viewTypePresenterParams = mutableMapOf(presenterDeclaration.toClassName() to view.innerArguments.map { it.toTypeName(presenterDeclaration.typeParameters.toTypeParameterResolver()) }),
                parent = superView,
                needGenerate = true
            ).putTags(*declarations.toTypedArray())
        }

        presenterCache[presenterKey] = newView
        viewCache[viewKey] = newView

        return newView
    }

    private fun KSClassDeclaration.getSuperPresenterAndView(): Pair<KSClassDeclaration, KSType> {
        return superTypes
            .firstNotNullOf { reference ->
                (reference.resolve().declaration as? KSClassDeclaration)
                    ?.takeIf { it.classKind == ClassKind.CLASS }
                    ?.let {
                        it to (reference.findView() ?: throw RuntimeException("It is impossible to find a view in $this"))
                    }
            }
    }

    private fun KSTypeReference.findView(): KSType? {
        val type = element
            ?.typeArguments
            ?.asSequence()
            ?.map { it.type?.resolve() }
            ?.firstOrNull {
                it?.let {
                    mvpView.isAssignableFrom(it)
                } ?: false
            }
        return when (type?.declaration) {
            is KSClassDeclaration -> type
            is KSTypeParameter -> {
                (type.declaration as KSTypeParameter)
                    .bounds
                    .map {
                        it.resolve()
                    }
                    .firstOrNull {
                        mvpView.isAssignableFrom(it)
                    }
            }

            else -> {
                logger.warn("Unknown type is " + type?.declaration?.let { it::class}.toString())
                null
            }
        }
    }

    private fun KSClassDeclaration.getMethods(tagsDeclarations: MutableList<KSDeclaration>, superView: View?): List<View.Method> {
        val parameterResolver: TypeParameterResolver = typeParameters.toTypeParameterResolver()
        val typeNameResolverMap = mutableMapOf<KSDeclaration?, TypeNameResolver>()
        val additionalSequence = superTypes
            .map { it.resolve() }
            .filter { it.toClassName() != superView?.className }
            .map {
                typeNameResolverMap[it.declaration] = TypeNameResolver(it, parameterResolver)
                it.declaration
            }
            .filterIsInstance<KSClassDeclaration>()
            .flatMap {
                tagsDeclarations += it
                it.declarations
            }
        val typeNameResolver = TypeNameResolver(emptyList(), this, parameterResolver)
        return (declarations + additionalSequence)
            .mapNotNull { declaration ->
                when (declaration) {
                    is KSFunctionDeclaration -> {
                        if (declaration.simpleName.getShortName() in ANY_FUNCTION_SIMPLE_NAME) null else {
                            declaration.toMethod(typeNameResolverMap[declaration.parentDeclaration] ?: typeNameResolver)
                        }
                    }

                    is KSPropertyDeclaration -> {
                        declaration.toMethod(typeNameResolverMap[declaration.parentDeclaration] ?: typeNameResolver)
                    }

                    else -> null
                }
            }.toList()
    }

    private fun KSFunctionDeclaration.toMethod(resolver: TypeNameResolver): View.Method {
        val params = parameters.map {
            View.Method.Param(
                name = it.name?.getShortName() ?: PROPERTY_PARAM_NAME,
                typeName = it.type.toTypeName(resolver),
                isVarargs = it.isVararg
            )
        }
        return View.Method(
            name = simpleName.getShortName(),
            type = View.Method.Type.Function(params),
            viewCommandAnnotation = getMoxyViewCommand()
        )
    }

    private fun KSPropertyDeclaration.toMethod(resolver: TypeNameResolver): View.Method {
        return View.Method(
            name = simpleName.getShortName(),
            type = View.Method.Type.Property(
                param = View.Method.Param(
                    name = PROPERTY_PARAM_NAME,
                    typeName = type.toTypeName(resolver),
                    isVarargs = false
                )
            ),
            viewCommandAnnotation = getMoxyViewCommand()
        )
    }

    @OptIn(KspExperimental::class)
    private fun KSAnnotated.getMoxyViewCommand(): View.Method.ViewCommandAnnotation? {
        return getAnnotationsByType(MoxyViewCommand::class).firstOrNull()?.run {
            val customStrategy = if (value.strategyClass == null) {
                this@getMoxyViewCommand.annotations.find {
                    it.shortName.getShortName() == MoxyViewCommand::class.simpleName && it.annotationType.resolve().declaration
                        .qualifiedName?.asString() == MoxyViewCommand::class.qualifiedName
                }?.let {
                    it.arguments.firstOrNull {
                        it.name?.asString() == "custom"
                    }?.value as KSType
                }
            } else null

            return View.Method.ViewCommandAnnotation(
                strategyType = value,
                customStrategy = customStrategy,
                tag = tag
            )
        }

    }

    private fun <T : Tagged> T.putTags(vararg declarations: KSDeclaration): T = apply {
        putTag(OriginatingMarker::class, KspOriginatingMarker)
        val oldList = getTag(KspOriginatingMarker.Files::class)?.files.orEmpty()
        val list = oldList + declarations.mapNotNull { it.containingFile }
        putTag(KspOriginatingMarker.Files::class, KspOriginatingMarker.Files(list))
    }

    private fun KSTypeReference.toTypeName(resolver: TypeNameResolver): TypeName {
        val ksType = this.resolve()
        return when (val declaration = ksType.declaration) {
            is KSTypeParameter -> resolver[declaration.name.getShortName()].copy(nullable = ksType.isMarkedNullable)
            else -> toTypeName(resolver.parentTypeParameterResolver)
        }
    }

    private class TypeNameResolver(
        innerArguments: List<KSTypeArgument>,
        declaration: KSDeclaration,
        val parentTypeParameterResolver: TypeParameterResolver
    ) {
        private val innerParametersMap = mutableMapOf<String, TypeName>()

        constructor(
            type: KSType,
            parentTypeParameterResolver: TypeParameterResolver
        ) : this(type.innerArguments, type.declaration, parentTypeParameterResolver)

        init {
            val innerTypeNames = innerArguments.map { it.toTypeName(parentTypeParameterResolver) }
            declaration.typeParameters.mapIndexed { index, ksTypeParameter ->
                val typeName = innerTypeNames.getOrNull(index) ?: return@mapIndexed
                innerParametersMap[ksTypeParameter.name.getShortName()] = typeName
            }

        }

        operator fun get(index: String): TypeName {
            return innerParametersMap[index] ?: parentTypeParameterResolver[index]
        }

    }

}