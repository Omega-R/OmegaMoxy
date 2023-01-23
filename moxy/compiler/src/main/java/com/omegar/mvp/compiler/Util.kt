/*
 * Copyright (C) 2013 Google, Inc.
 * Copyright (C) 2013 Square, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.omegar.mvp.compiler

import com.omegar.mvp.MvpView
import com.omegar.mvp.viewstate.strategy.StrategyType
import com.squareup.kotlinpoet.asClassName
import java.util.*
import javax.lang.model.element.*
import javax.lang.model.type.*
import javax.lang.model.util.Elements

/**
 * Utilities for handling types in annotation processors
 *
 * @author Yuri Shmakov
 */
object Util {

    val MVP_VIEW_CLASS_NAME = MvpView::class.java.asClassName()

    fun fillGenerics(types: Map<String, String>, param: TypeMirror): String {
        return fillGenerics(types, listOf(param))
    }

    @JvmOverloads
    fun fillGenerics(types: Map<String, String>, params: List<TypeMirror>, separator: String = ", "): String {
        val result = StringBuilder()
        for (param in params) {
            if (result.isNotEmpty()) {
                result.append(separator)
            }
            /**
             * "if" block's order is critically! E.g. IntersectionType is TypeVariable.
             */
            if (param is WildcardType) {
                result.append("?")
                val extendsBound = param.extendsBound
                if (extendsBound != null) {
                    result.append(" extends ").append(fillGenerics(types, extendsBound))
                }
                val superBound = param.superBound
                if (superBound != null) {
                    result.append(" super ").append(fillGenerics(types, superBound))
                }
            } else if (param is IntersectionType) {
                result.append("?")
                val bounds = param.bounds
                if (bounds.isNotEmpty()) {
                    result.append(" extends ").append(fillGenerics(types, bounds, " & "))
                }
            } else if (param is DeclaredType) {
                result.append(param.asElement())
                val typeArguments = param.typeArguments
                if (typeArguments.isNotEmpty()) {
                    val s = fillGenerics(types, typeArguments)
                    result.append("<").append(s).append(">")
                }
            } else if (param is TypeVariable) {
                var type = types[param.toString()]
                if (type == null) {
                    type = param.upperBound.toString()
                }
                result.append(type)
            } else {
                result.append(param)
            }
        }
        return result.toString()
    }

    fun getFullClassName(elements: Elements, typeMirror: TypeMirror?): String {
        if (typeMirror !is DeclaredType) {
            return ""
        }
        val typeElement = typeMirror.asElement() as TypeElement
        return getFullClassName(elements, typeElement)
    }

    fun getFullClassName(elements: Elements, typeElement: TypeElement): String {
        var packageName = elements.getPackageOf(typeElement).qualifiedName.toString()
        if (packageName.isNotEmpty()) {
            packageName += "."
        }
        val className = typeElement.toString().substring(packageName.length)
        return packageName + className.replace("\\.".toRegex(), "\\$")
    }

    fun getSimpleClassName(elements: Elements, typeElement: TypeElement): String {
        var packageName = elements.getPackageOf(typeElement).qualifiedName.toString()
        if (packageName.isNotEmpty()) {
            packageName += "."
        }
        val className = typeElement.toString().substring(packageName.length)
        return className.replace("\\.".toRegex(), "\\$")
    }

    fun getAnnotation(element: Element, annotationClass: String): AnnotationMirror? {
        for (annotationMirror in element.annotationMirrors) {
            if (annotationMirror.annotationType.asElement().toString() == annotationClass) return annotationMirror
        }
        return null
    }

    fun getAnnotationValueAsTypeMirror(annotationMirror: AnnotationMirror?, key: String): TypeMirror? {
        val av = getAnnotationValue(annotationMirror, key)
        return if (av != null) {
            av.value as TypeMirror
        } else {
            null
        }
    }

    fun getAnnotationValueAsStrategyType(annotationMirror: AnnotationMirror?, key: String): StrategyType? {
        val av = getAnnotationValue(annotationMirror, key)
        return if (av != null) {
            val enumString = av.value.toString()
            StrategyType.valueOf(enumString)
        } else {
            null
        }
    }

    fun getAnnotationValueAsString(annotationMirror: AnnotationMirror?, key: String): String? {
        val av = getAnnotationValue(annotationMirror, key)
        return av?.value?.toString()
    }

    fun getAnnotationValueAsBoolean(annotationMirror: AnnotationMirror?, key: String): Boolean {
        val av = getAnnotationValue(annotationMirror, key)
        return if (av != null) {
            java.lang.Boolean.parseBoolean(av.value.toString())
        } else {
            false
        }
    }

    fun getAnnotationValue(annotationMirror: AnnotationMirror?, key: String): AnnotationValue? {
        if (annotationMirror == null) return null
        for ((key1, value) in annotationMirror.elementValues) {
            if (key1.simpleName.toString() == key) {
                return value
            }
        }
        return null
    }

    fun getAnnotationValues(annotationMirror: AnnotationMirror?): Map<String, AnnotationValue?> {
        if (annotationMirror == null) return emptyMap<String, AnnotationValue>()
        val result: MutableMap<String, AnnotationValue?> = HashMap()
        for ((key1, value) in annotationMirror.elementValues) {
            val key = key1.simpleName.toString()
            if (value != null) {
                result[key] = value
            }
        }
        return result
    }

    fun hasEmptyConstructor(element: TypeElement): Boolean {
        for (enclosedElement in element.enclosedElements) {
            if (enclosedElement.kind == ElementKind.CONSTRUCTOR) {
                val parameters = (enclosedElement as ExecutableElement).parameters
                if (parameters == null || parameters.isEmpty()) {
                    return true
                }
            }
        }
        return false
    }

    fun capitalizeString(string: String?): String {
        return if (string == null || string.isEmpty()) "" else if (string.length == 1) string.uppercase(Locale.getDefault()) else string[0].uppercaseChar()
            .toString() + string.substring(1)
    }

    fun decapitalizeString(string: String?): String {
        return if (string == null || string.isEmpty()) "" else if (string.length == 1) string.lowercase(Locale.getDefault()) else string[0].lowercaseChar()
            .toString() + string.substring(1)
    }

    fun isMvpElement(element: TypeElement?): Boolean {
        if (element == null) return false
        val className = element.asClassName()
        if (className.equals(MVP_VIEW_CLASS_NAME)) return true
        for (typeMirror in element.interfaces) {
            val interfaceElement = (typeMirror as DeclaredType).asElement() as TypeElement
            if (isMvpElement(interfaceElement)) return true
        }
        return false
    }

    fun <E> firstOrNull(list: List<E>?): E? {
        return if (list == null || list.isEmpty()) null else list[0]
    }

    fun <E> lastOrNull(set: Set<E>?): E? {
        return if (set == null || set.isEmpty()) null else last(set.iterator())
    }

    fun <E> lastOrNull(list: List<E>?): E? {
        return if (list == null || list.isEmpty()) null else list[list.size - 1]
    }

    fun <T> last(iterator: Iterator<T>): T {
        while (true) {
            val current = iterator.next()
            if (!iterator.hasNext()) {
                return current
            }
        }
    }

    fun asElement(mirror: TypeMirror): TypeElement {
        return (mirror as DeclaredType).asElement() as TypeElement
    }

    fun <E> newDistinctList(list: List<E>): List<E> {
        return ArrayList(LinkedHashSet(list))
    }

    fun substringBefore(string: String, beforeChar: Char): String {
        val beforeIndex = string.indexOf(beforeChar)
        return if (beforeIndex >= 0) string.substring(0, beforeIndex) else string
    }

    fun <E> newHashSet(vararg elements: E): HashSet<E> {
        val set = HashSet<E>(elements.size)
        Collections.addAll(set, *elements)
        return set
    }

    fun startWith(string: String, prefix: String): Boolean {
        return string.indexOf(prefix) == 0
    }
}