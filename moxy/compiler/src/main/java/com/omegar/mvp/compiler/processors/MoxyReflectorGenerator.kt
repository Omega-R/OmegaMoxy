package com.omegar.mvp.compiler.processors

import com.omegar.mvp.compiler.NamingRules
import com.omegar.mvp.compiler.NamingRules.viewStateClassName
import com.omegar.mvp.compiler.entities.View
import com.omegar.mvp.compiler.extensions.safeParameterizedBy
import com.omegar.mvp.compiler.extensions.toFileSpec
import com.omegar.mvp.viewstate.MvpViewState
import com.squareup.kotlinpoet.ANY
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.LambdaTypeName
import com.squareup.kotlinpoet.MAP
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.STAR
import com.squareup.kotlinpoet.STRING
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.TypeVariableName
import com.squareup.kotlinpoet.asClassName
import kotlin.reflect.KClass


/**
 * Created by Anton Knyazev on 18.07.2023.
 * Copyright (c) 2023 Omega https://omega-r.com
 */
@Suppress("PARAMETER_NAME_CHANGED_ON_OVERRIDE")
class MoxyReflectorGenerator(private val reflectorPackageName: String) : Processor<List<View>, FileSpec> {

    private companion object {
        private const val PROPERTY_MAP_NAME = "sViewStateProviders"
        private const val INPUT_PARAM_NAME = "presenterClass"
        private const val CLASS_PROPERTY_NAME = "qualifiedName"
        private val VIEW_STATE_CLASS_NAME = MvpViewState::class.asClassName()
        private val lambdaTypeName = LambdaTypeName.get(
            parameters = emptyArray<TypeName>(),
            returnType = VIEW_STATE_CLASS_NAME.parameterizedBy(STAR)
        )
    }

    override fun invoke(views: List<View>): FileSpec {
        val init = views.joinToString(separator = ", ") { "%T::class.$CLASS_PROPERTY_NAME to { %T() }" }
        val args = views.flatMap { view ->

            val innerParams = view.presenterInnerTypeParams.map {
                if (it is TypeVariableName && it.bounds.isNotEmpty()) {
                    it.bounds.first()
                } else {
                    it
                }
            }
            val params = listOf(view.className.safeParameterizedBy(innerParams)) + innerParams

            val returnType = view.viewStateClassName.parameterizedBy(*params.toTypedArray())
            listOf(view.presenterClassName, returnType)
        }.toTypedArray()

        val typeSpec = TypeSpec.objectBuilder(NamingRules.moxyReflectorName)
            .addOriginating(views)
            .addProperty(
                PropertySpec.builder(PROPERTY_MAP_NAME, MAP.parameterizedBy(STRING.copy(true), lambdaTypeName))
                    .initializer(CodeBlock.of("mapOf($init)", *args))
                    .build()
            )
            .addFunction(
                FunSpec.builder("createViewState")
                    .addParameter(INPUT_PARAM_NAME, KClass::class.asClassName().parameterizedBy(STAR))
                    .returns(ANY.copy(nullable = true))
                    .addStatement(
                        "return ($PROPERTY_MAP_NAME[$INPUT_PARAM_NAME.$CLASS_PROPERTY_NAME])?.invoke()",
                    )
                    .build()
            )
            .build()

        return typeSpec.toFileSpec(reflectorPackageName)
    }

}

