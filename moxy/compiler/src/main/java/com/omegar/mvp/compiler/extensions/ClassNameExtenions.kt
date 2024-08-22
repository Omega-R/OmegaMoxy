package com.omegar.mvp.compiler.extensions

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.ParameterizedTypeName
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.TypeName

fun ClassName.safeParameterizedBy(typeArguments: List<TypeName>?): TypeName =
    if (typeArguments.isNullOrEmpty()) this else parameterizedBy(typeArguments)

fun TypeName.asClassName(): ClassName? {
    return when (this) {
        is ClassName -> this
        is ParameterizedTypeName -> rawType
        else -> null
    }
}