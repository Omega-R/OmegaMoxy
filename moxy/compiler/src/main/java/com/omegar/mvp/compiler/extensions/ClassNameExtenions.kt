package com.omegar.mvp.compiler.extensions

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.TypeName

fun ClassName.safeParameterizedBy(typeArguments: List<TypeName>): TypeName =
    if (typeArguments.isEmpty()) this else parameterizedBy(typeArguments)