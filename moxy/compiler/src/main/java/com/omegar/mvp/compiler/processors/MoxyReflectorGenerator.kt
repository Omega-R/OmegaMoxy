package com.omegar.mvp.compiler.processors

import com.omegar.mvp.ViewStateFactory
import com.omegar.mvp.compiler.NamingRules
import com.omegar.mvp.compiler.NamingRules.viewStateClassName
import com.omegar.mvp.compiler.entities.View
import com.omegar.mvp.compiler.extensions.toFileSpec
import com.squareup.kotlinpoet.ANY
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.MUTABLE_MAP
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.STAR
import com.squareup.kotlinpoet.STRING
import com.squareup.kotlinpoet.TypeSpec
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
        private val VIEW_STATE_FACTORY_CLASS_NAME = ViewStateFactory::class.asClassName()
    }

    override fun invoke(views: List<View>): FileSpec {
        val init = views.joinToString(separator = ", ") { "%T::class.$CLASS_PROPERTY_NAME to %T" }
        val args = views.flatMap { listOf(it.presenterClassName, it.viewStateClassName) }.toTypedArray()
        val typeSpec = TypeSpec.objectBuilder(NamingRules.moxyReflectorName)
            .addOriginating(views)
            .addProperty(
                PropertySpec.builder(PROPERTY_MAP_NAME, MUTABLE_MAP.parameterizedBy(STRING.copy(true), VIEW_STATE_FACTORY_CLASS_NAME))
                    .initializer(CodeBlock.of("mutableMapOf($init)", *args))
                    .build()
            )
            .addFunction(
                FunSpec.builder("createViewState")
                    .addParameter(INPUT_PARAM_NAME, KClass::class.asClassName().parameterizedBy(STAR))
                    .returns(ANY.copy(nullable = true))
                    .addStatement(
                        "return ($PROPERTY_MAP_NAME[$INPUT_PARAM_NAME.$CLASS_PROPERTY_NAME])?.createViewState()",
                    )
                    .build()
            )
            .build()

        return typeSpec.toFileSpec(reflectorPackageName)
    }

}

