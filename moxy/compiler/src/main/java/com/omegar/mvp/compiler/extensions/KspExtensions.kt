package com.omegar.mvp.compiler.extensions

import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSAnnotation
import kotlin.reflect.KClass

fun <T : Annotation> KSAnnotated.getAnnotationsByType(annotationKClass: KClass<T>): Sequence<KSAnnotation> {
    return this.annotations.filter {
        it.shortName.getShortName() == annotationKClass.simpleName && it.annotationType.resolve().declaration
            .qualifiedName?.asString() == annotationKClass.qualifiedName
    }
}