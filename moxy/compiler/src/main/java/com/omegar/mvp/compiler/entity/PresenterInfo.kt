package com.omegar.mvp.compiler.entity

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.ParameterizedTypeName
import com.squareup.kotlinpoet.asClassName
import com.squareup.kotlinpoet.asTypeName
import javax.lang.model.element.Modifier
import javax.lang.model.element.TypeElement

/**
 * Date: 27-Jul-2017
 * Time: 11:55
 *
 * @author Evgeny Kursakov
 */
class PresenterInfo(override val typeElement: TypeElement, viewStateName: String, viewName: TypeElement) : TypeElementHolder {
    val name: ClassName = typeElement.asClassName()
    val viewStateName: ClassName = ClassName.bestGuess(viewStateName)
    val viewName: ClassName = viewName.asClassName()
    val isParametrized: Boolean = typeElement.asType().asTypeName() is ParameterizedTypeName
    val isAbstracted: Boolean = typeElement.modifiers.contains(Modifier.ABSTRACT)


    override fun equals(o: Any?): Boolean {
        if (this === o) return true
        if (o == null || javaClass != o.javaClass) return false
        val that = o as PresenterInfo
        if (name != that.name) return false
        if (typeElement != that.typeElement) return false
        return viewStateName == that.viewStateName
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + (typeElement.hashCode())
        result = 31 * result + (viewStateName.hashCode())
        return result
    }
}