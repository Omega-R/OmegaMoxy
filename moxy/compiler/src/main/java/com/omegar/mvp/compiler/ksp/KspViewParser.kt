package com.omegar.mvp.compiler.ksp

import com.google.devtools.ksp.KspExperimental
import com.google.devtools.ksp.getAnnotationsByType
import com.google.devtools.ksp.getClassDeclarationByName
import com.google.devtools.ksp.innerArguments
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.symbol.*
import com.omegar.mvp.MvpPresenter
import com.omegar.mvp.MvpView
import com.omegar.mvp.compiler.entities.Tagged
import com.omegar.mvp.compiler.entities.View
import com.omegar.mvp.compiler.processors.OriginatingMarker
import com.omegar.mvp.compiler.processors.ViewParser
import com.omegar.mvp.viewstate.strategy.MoxyViewCommand
import com.squareup.kotlinpoet.TypeVariableName
import com.squareup.kotlinpoet.ksp.toClassName
import com.squareup.kotlinpoet.ksp.toTypeName
import com.squareup.kotlinpoet.ksp.toTypeParameterResolver
import com.squareup.kotlinpoet.ksp.toTypeVariableName


/**
 * Created by Anton Knyazev on 26.04.2023.
 * Copyright (c) 2023 Omega https://omega-r.com
 */
class KspViewParser(
    private val reflectorPackage: String,
    private val logger: KSPLogger,
    private val resolver: Resolver
) : ViewParser<KSClassDeclaration> {

    companion object {
        private val ANY_FUNCTION_SIMPLE_NAME = listOf("equals", "hashCode", "toString", "<init>")
        private const val PROPERTY_PARAM_NAME = "value"
    }

    private val presenterCache = mutableMapOf<String, View>()
    private val viewCache = mutableMapOf<String, View>()


    private val mvpView = resolver.getClassDeclarationByName(MvpView::class.qualifiedName!!)!!.asType(emptyList())

    @Suppress("PARAMETER_NAME_CHANGED_ON_OVERRIDE")
    override fun invoke(presenterDeclaration: KSClassDeclaration): View? {
        if (presenterDeclaration.simpleName.getShortName() == MvpPresenter::class.simpleName) return null

        val presenterKey = presenterDeclaration.simpleName.asString()
        presenterCache[presenterKey]?.let {
            return it
        }

        val (superPresenter, view) =
            presenterDeclaration
                .superTypes
                .firstNotNullOf { reference ->
                    (reference.resolve().declaration as? KSClassDeclaration)
                        ?.takeIf { it.classKind == ClassKind.CLASS }
                        ?.let {
                            it to reference.getView()!!
                        }
                }

        val viewClassName = view.toClassName()
        val viewKey = viewClassName.simpleName

        val viewDeclaration = view.declaration as KSClassDeclaration

        viewCache[viewKey]?.let {
            val newView = it.copy(
                presenterClassName = presenterDeclaration.toClassName(),
                parent = this(superPresenter)
            )
                .putTags(presenterDeclaration)
            presenterCache[presenterKey] = newView
            return newView
        }
        val newView = View(
            className = viewClassName,
            presenterClassName = presenterDeclaration.toClassName(),
            methods = viewDeclaration.getMethods(),
            viewTypeParams = viewDeclaration.typeParameters
                .filterNot { mvpView.isAssignableFrom(it.bounds.first().resolve()) }
                .map { it.toTypeVariableName() },
            presenterInnerTypeParams = view.innerArguments.map { it.toTypeName(presenterDeclaration.typeParameters.toTypeParameterResolver()) },
            reflectorPackage = reflectorPackage,
            parent = this(superPresenter)
        ).putTags(presenterDeclaration)

        presenterCache[presenterKey] = newView
        viewCache[viewKey] = newView

        return newView
    }

    private fun KSTypeReference.getView(): KSType? {
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

            else -> null
        }
    }

    private fun KSClassDeclaration.getMethods(): List<View.Method> {
        val functionSequence = getAllFunctions()
            .filterNot { it.simpleName.getShortName() in ANY_FUNCTION_SIMPLE_NAME }
            .map { func ->
                val params = func.parameters.map {
                    View.Method.Param(
                        name = it.name?.getShortName() ?: PROPERTY_PARAM_NAME,
                        typeName = it.type.toTypeName(typeParameters.toTypeParameterResolver()),
                        isVarargs = it.isVararg
                    )
                }
                View.Method(
                    name = func.simpleName.getShortName(),
                    type = View.Method.Type.Function(params),
                    viewCommandAnnotation = func.getMoxyViewCommand()
                ).putTags(func)
            }
        val propertySequence = getAllProperties()
            .map { prop ->
                View.Method(
                    name = prop.simpleName.getShortName(),
                    type = View.Method.Type.Property(
                        param = View.Method.Param(
                            name = PROPERTY_PARAM_NAME,
                            typeName = prop.type.toTypeName(),
                            isVarargs = false
                        )
                    ),
                    viewCommandAnnotation = prop.getMoxyViewCommand()
                ).putTags(prop)
            }
        val methodSequence = functionSequence + propertySequence
        return methodSequence.toList()
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

    private fun <T : Tagged> T.putTags(declaration: KSDeclaration): T = apply {
        putTag(OriginatingMarker::class, KspOriginatingMarker)
        putTag(KSFile::class, declaration.containingFile)
    }

}