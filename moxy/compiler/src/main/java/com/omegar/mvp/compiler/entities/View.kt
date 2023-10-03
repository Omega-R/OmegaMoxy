package com.omegar.mvp.compiler.entities

import com.google.devtools.ksp.symbol.KSType
import com.omegar.mvp.viewstate.strategy.StrategyType
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.TypeVariableName

/**
 * Created by Anton Knyazev on 26.04.2023.
 * Copyright (c) 2023 Omega https://omega-r.com
 */
data class View(
    val className: ClassName,
    val presenterClassName: ClassName,
    val methods: List<Method>,
    val typeParams: List<TypeVariableName>,
    val reflectorPackage: String,
    val parent: View?
) : Tagged() {

    val name: String = className.simpleName

    val typeNameWithParams = if (typeParams.isEmpty()) className else className.parameterizedBy(typeParams)

    init {
        val nameCountMap = mutableMapOf<String, Int>()
        methods.forEach {
            it.counter = nameCountMap[it.name] ?: 0
            nameCountMap[it.name] = it.counter + 1
        }
    }

    data class Method(
        val name: String,
        val type: Type,
        val viewCommandAnnotation: ViewCommandAnnotation?,
    ) : Tagged() {
        var counter: Int = 0
            internal set

        val params
            get() = when (type) {
                is Type.Function -> type.params
                is Type.Property -> listOf(type.param)
            }

        data class Param(
            val name: String,
            val typeName: TypeName,
            val isVarargs: Boolean
        )

        sealed class Type {
            data class Function(val params: List<Param>) : Type()
            data class Property(val param: Param) : Type()
        }

        data class ViewCommandAnnotation(
            val strategyType: StrategyType,
            val customStrategy: KSType?,
            val tag: String = ""
        )

    }

}