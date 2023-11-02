package com.omegar.mvp.compiler.processors

import com.omegar.mvp.Moxy
import com.omegar.mvp.compiler.NamingRules.commandName
import com.omegar.mvp.compiler.NamingRules.viewStateClassName
import com.omegar.mvp.compiler.NamingRules.viewStateName
import com.omegar.mvp.compiler.entities.View
import com.omegar.mvp.compiler.entities.View.Method.Type.Function
import com.omegar.mvp.compiler.entities.View.Method.Type.Property
import com.omegar.mvp.compiler.extensions.toFileSpec
import com.omegar.mvp.viewstate.MvpViewState
import com.omegar.mvp.viewstate.ViewCommand
import com.omegar.mvp.viewstate.strategy.AddToEndSingleStrategy
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.ksp.toClassName


/**
 * Created by Anton Knyazev on 27.04.2023.
 * Copyright (c) 2023 Omega https://omega-r.com
 */
class ViewStateGenerator : Processor<View, FileSpec> {

    companion object {
        private const val VIEW = "OMEGAVIEW"
        private val GENERIC_TYPE_VARIABLE_NAME: TypeVariableName = TypeVariableName(VIEW)
        private val MVP_VIEW_STATE_CLASS_NAME = MvpViewState::class.asClassName()
        private val MVP_VIEW_STATE_TYPE_NAME = MVP_VIEW_STATE_CLASS_NAME.parameterizedBy(GENERIC_TYPE_VARIABLE_NAME)
        private val DURATION = ClassName("kotlin.time", "Duration")
        private val SECONDS = MemberName(ClassName("kotlin.time", "Duration", "Companion"), "seconds")
        private val VIEW_COMMAND_TYPE_NAME = ViewCommand::class.asClassName().parameterizedBy(GENERIC_TYPE_VARIABLE_NAME)

        private val View.omegaViewVariableName
            get() = TypeVariableName(VIEW, viewTypeNameWithParams)

        private val View.viewStateTypeVariables
            get() = listOf(omegaViewVariableName) + viewTypeParams

        private val View.Method.tag
            get() = viewCommandAnnotation?.tag?.takeIf { it.isNotBlank() }
                ?: name

        private val View.Method.strategy
            get() = (
                    viewCommandAnnotation?.strategyType?.strategyClass?.asClassName()
                        ?: viewCommandAnnotation?.customStrategy?.toClassName()
                        ?: AddToEndSingleStrategy::class.asClassName()
                    )
    }

    @Suppress("PARAMETER_NAME_CHANGED_ON_OVERRIDE")
    override fun invoke(view: View): FileSpec {
        val typeSpec = TypeSpec.classBuilder(view.viewStateName)
            .addOriginating(view)
            .addMoxyAnnotation(view.reflectorPackage)
            .addModifiers(KModifier.OPEN)
            .addSuperinterface(view.viewTypeNameWithParams)
            .addTypeVariables(view.viewStateTypeVariables)
            .superclass(view.parent, view.omegaViewVariableName)
            .addMethods(view.methods, view.viewStateTypeVariables)
            .addCommandTypes(view)
            .build()

        return typeSpec.toFileSpec(view.className.packageName)
    }

    private fun TypeSpec.Builder.addMoxyAnnotation(reflectorPackage: String): TypeSpec.Builder {
        return addAnnotation(
            AnnotationSpec.builder(Moxy::class)
                .addMember("reflectorPackage=\"%L\"", reflectorPackage)
                .build()
        )
    }


    private fun TypeSpec.Builder.superclass(parentView: View?, omegaViewVariableName: TypeVariableName): TypeSpec.Builder {
        return superclass(
            parentView?.let {
                parentView.viewStateClassName.parameterizedBy(omegaViewVariableName)
            } ?: MVP_VIEW_STATE_TYPE_NAME
        )
    }

    private fun TypeSpec.Builder.addMethods(
        methods: List<View.Method>,
        viewStateTypeVariables: List<TypeVariableName>
    ): TypeSpec.Builder = apply {
        methods.forEach { method ->
            when (method.type) {
                is Function -> addFunction(method.toFunSpec(method.type))
                is Property -> addProperty(method.toPropertySpec(method.type, viewStateTypeVariables))
            }
        }
    }

    private fun View.Method.toFunSpec(type: Function): FunSpec {
        return FunSpec.builder(name)
            .addModifiers(KModifier.OVERRIDE)
            .addParameters(type.params.toParamSpec())
            .addStatement("apply(%1N(%2L))", commandName, type.params.joinToString { it.nameWithVarargs })
            .build()
    }

    private fun View.Method.Param.toParamSpec(): ParameterSpec {
        return ParameterSpec.builder(name, typeName)
            .apply {
                if (isVarargs) {
                    modifiers += KModifier.VARARG
                }
            }
            .build()
    }

    private fun List<View.Method.Param>.toParamSpec() = map { it.toParamSpec() }

    private fun View.Method.toPropertySpec(type: Property, viewStateTypeVariableNames: List<TypeVariableName>): PropertySpec {
        return PropertySpec.builder(name, type.param.typeName)
            .addModifiers(KModifier.OVERRIDE)
            .mutable(true)
            .getter(
                FunSpec.getterBuilder()
                    .addStatement("return findCommand<%1L<%4L>>()?.%2L ?: %3L",
                        commandName,
                        type.param.name,
                        getDefaultValue(),
                        viewStateTypeVariableNames.joinToString(separator = ",") { it.name })
                    .build()
            )
            .setter(
                FunSpec.setterBuilder()
                    .addParameter(type.param.name, type.param.typeName)
                    .addStatement("apply(%1N(%2L))", commandName, type.param.name)
                    .build()
            )
            .build()
    }

    private fun View.Method.getDefaultValue(): CodeBlock {
        return (type as? Property)?.param?.typeName.determineDefaultValue()
    }

    private fun TypeName?.determineDefaultValue(): CodeBlock {
        return when ((this as? ParameterizedTypeName)?.rawType ?: this) {
            BOOLEAN -> CodeBlock.of("false")
            FLOAT -> CodeBlock.of("0f")
            CHAR -> CodeBlock.of("\\u0000")
            DOUBLE -> CodeBlock.of("0.0")
            BYTE, SHORT, INT, LONG -> CodeBlock.of("0")
            STRING -> CodeBlock.of("\"\"")
            LIST -> CodeBlock.of("emptyList()")
            MAP -> CodeBlock.of("emptyMap()")
            SET -> CodeBlock.of("emptySet()")
            ARRAY -> CodeBlock.of("emptyArray()")
            MUTABLE_MAP -> CodeBlock.of("mutableMapOf()")
            MUTABLE_LIST -> CodeBlock.of("mutableListOf()")
            MUTABLE_SET -> CodeBlock.of("mutableSetOf()")
            DURATION -> CodeBlock.of("0.%M", SECONDS)
            else -> CodeBlock.of("null")
        }
    }

    private fun TypeSpec.Builder.addCommandTypes(view: View): TypeSpec.Builder {
        val mvpView = "mvpView"

        return addTypes(view.methods.map {
            val statementFormat = when (it.type) {
                is Function -> "$mvpView.%L(%L)"
                is Property -> "$mvpView.%L = %L"
            }
            TypeSpec.classBuilder(it.commandName)
                .addModifiers(KModifier.PRIVATE)
                .superclass(VIEW_COMMAND_TYPE_NAME)
                .addTypeVariables(view.viewStateTypeVariables)
                .addSuperclassConstructorParameter("%S, %T", it.tag, it.strategy)
                .primaryConstructor(
                    FunSpec.constructorBuilder()
                        .addParameters(it.params.toParamSpec())
                        .build()
                )
                .addProperties(it.params.map { param ->
                    PropertySpec.builder(param.name, param.typeWithVarargs)
                        .initializer("%1N", param.name)
                        .build()
                })

                .addFunction(
                    FunSpec.builder("apply")
                        .addModifiers(KModifier.OVERRIDE)
                        .addParameter("mvpView", GENERIC_TYPE_VARIABLE_NAME)
                        .addStatement(statementFormat, it.name, it.params.joinToString { it.nameWithVarargs })
                        .build()
                )
                .addFunction(
                    FunSpec.builder("toString")
                        .addModifiers(KModifier.OVERRIDE)
                        .returns(STRING)
                        .addStatement(
                            it.params.joinToString(
                                prefix = "return buildString(\"${it.name}\",",
                                postfix = ")"
                            ) { param ->
                                "\"${param.name}\",${param.name}"
                            }
                        )
                        .build()
                )
                .build()
        })
    }

    private val View.Method.Param.typeWithVarargs
        get() = if (!isVarargs) typeName else
            when(typeName) {
                FLOAT -> FLOAT_ARRAY
                BOOLEAN -> BOOLEAN_ARRAY
                BYTE -> BYTE_ARRAY
                CHAR -> CHAR_ARRAY
                SHORT -> SHORT_ARRAY
                INT -> INT_ARRAY
                LONG -> LONG_ARRAY
                DOUBLE -> DOUBLE_ARRAY
                U_BYTE -> U_BYTE_ARRAY
                U_SHORT -> U_SHORT_ARRAY
                U_INT -> U_INT_ARRAY
                U_LONG -> U_LONG_ARRAY
                else -> ARRAY.parameterizedBy(WildcardTypeName.producerOf(typeName))
            }
    private val View.Method.Param.nameWithVarargs
        get() = (if (isVarargs) "*" else "") + name


}


