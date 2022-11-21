package com.omegar.mvp.compiler.extenions

import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ClassName
import kotlinx.metadata.jvm.signature
import javax.lang.model.element.ExecutableElement
import javax.lang.model.type.DeclaredType
import javax.lang.model.util.Types
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.metadata.KotlinPoetMetadataPreview
import com.squareup.kotlinpoet.metadata.isNullable
import com.squareup.kotlinpoet.metadata.isPrimary
import com.squareup.kotlinpoet.metadata.isReified
import com.squareup.kotlinpoet.metadata.isSuspend
import com.squareup.kotlinpoet.tags.TypeAliasTag
import kotlinx.metadata.*
import kotlinx.metadata.KmClassifier.Class
import kotlinx.metadata.KmClassifier.TypeAlias
import kotlinx.metadata.KmClassifier.TypeParameter
import kotlinx.metadata.KmVariance.IN
import kotlinx.metadata.KmVariance.INVARIANT
import kotlinx.metadata.KmVariance.OUT
import kotlinx.metadata.jvm.annotations
import kotlinx.metadata.jvm.signature
import javax.lang.model.type.TypeVariable

fun FunSpec.Companion.overriding(method: ExecutableElement,
                                 enclosing: DeclaredType,
                                 kmClass: KmClass?,
                                 types: Types): FunSpec.Builder {

    return kmClass?.functions?.firstOrNull { it.computeSignature() == method.computeSignature() }?.toBuilder()
            ?: overriding(method, enclosing, types)
}

// Computes a simple signature string good enough for hashing
private fun KmFunction.computeSignature(): String {
    return "$name(${valueParameters.joinToString(",") { it.type.simpleName }})${returnType.simpleName}"
}

// Computes a simple signature string good enough for hashing
private fun ExecutableElement.computeSignature(): String {
    return "$simpleName(${parameters.joinToString(",") { it.simpleName }})${returnType.asTypeName().simpleName}"
}

private fun KmFunction.toBuilder(): FunSpec.Builder {
    return FunSpec.builder(name)
            .addTypeVariables(typeParameters)
}

private fun FunSpec.Builder.addTypeVariables(parameters: List<KmTypeParameter>): FunSpec.Builder = also {
    parameters.forEach { addTypeVariable(it.toType()) }
}

private fun KmTypeParameter.toType(): TypeVariableName {
    return TypeVariableName.invoke(name)
}

private val TypeName.simpleName: String
    get() {
        return when (this) {
            is ClassName -> simpleName
            else -> toString()
        }
    }


private val KmType?.simpleName: String
    get() {
        if (this == null) return "void"
        return when (val c = classifier) {
            is Class -> c.name
            is TypeParameter -> "Object"
            is TypeAlias -> c.name
        }
    }