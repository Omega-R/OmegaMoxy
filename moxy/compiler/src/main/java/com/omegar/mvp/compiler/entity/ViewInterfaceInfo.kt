package com.omegar.mvp.compiler.entity

import com.omegar.mvp.MvpProcessor
import com.omegar.mvp.compiler.MoxyConst
import com.omegar.mvp.compiler.Util
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.TypeVariableName
import com.squareup.kotlinpoet.asClassName
import javax.lang.model.element.TypeElement
import javax.lang.model.util.Elements

/**
 * Date: 27-Jul-2017
 * Time: 13:04
 *
 * @author Evgeny Kursakov
 */
class ViewInterfaceInfo(
    val superInterfaceType: TypeElement?,
    override val typeElement: TypeElement,
    val commands: List<ViewCommandInfo>,
    val typeVariables: List<TypeVariableName>,
    val parentTypeVariables: List<TypeVariableName>
) : TypeElementHolder {

    companion object {
        fun getViewStateFullName(elements: Elements?, viewTypeElement: TypeElement?): String {
            return Util.getFullClassName(elements, viewTypeElement) + MoxyConst.VIEW_STATE_SUFFIX
        }

        @JvmStatic
        fun getViewStateFullName(fullViewName: String): String {
            return fullViewName + MoxyConst.VIEW_STATE_SUFFIX
        }

        @JvmStatic
        fun getViewFullName(elements: Elements?, viewTypeElement: TypeElement?): String {
            return Util.getFullClassName(elements, viewTypeElement)
        }

        fun getViewStateSimpleName(elements: Elements?, viewTypeElement: TypeElement?): String {
            return Util.getSimpleClassName(elements, viewTypeElement) + MoxyConst.VIEW_STATE_SUFFIX
        }
    }

    val name: ClassName = typeElement.asClassName()

    val nameWithTypeVariables: TypeName = if (typeVariables.isEmpty()) name else name.parameterizedBy(typeVariables)

    fun getViewStateFullName(elements: Elements?): String {
        return getViewStateFullName(elements, typeElement)
    }

    fun getViewStateSimpleName(elements: Elements?): String {
        return getViewStateSimpleName(elements, typeElement)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false
        val that = other as ViewInterfaceInfo
        return name == that.name
    }

    override fun hashCode(): Int {
        return name.hashCode()
    }

    override fun toString(): String {
        return "ViewInterfaceInfo{" +
                "superInterfaceInfo=" + superInterfaceType +
                ", element=" + typeElement +
                '}'
    }

}