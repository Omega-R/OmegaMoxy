package com.omegar.mvp.compiler

import com.omegar.mvp.compiler.entity.AnnotationInfo
import javax.lang.model.element.Element
import javax.lang.model.element.Modifier

/**
 * Date: 17-Feb-16
 * Time: 16:57
 *
 * @author esorokin
 */
abstract class AnnotationRule(val annotationInfo: AnnotationInfo<*>, vararg validModifiers: Modifier) {

    protected val validModifiers: Set<Modifier> = validModifiers.toSet()

    protected var errorBuilder: StringBuilder = StringBuilder()

    val errorStack: String
        get() = errorBuilder.toString()

    init {
        if (validModifiers.isEmpty()) {
            throw RuntimeException("Valid modifiers cant be empty or null.")
        }
    }

    /**
     * Method describe rules for using Annotation.
     *
     * @param annotatedField Checking annotated field.
     */
    abstract fun checkAnnotation(annotatedField: Element)

    protected fun validModifiersToString(): String {
        return if (validModifiers.size > 1) {
            val result = StringBuilder("one of [")
            var addSeparator = false
            for (validModifier in validModifiers) {
                if (addSeparator) {
                    result.append(", ")
                }
                addSeparator = true
                result.append(validModifier.toString())
            }
            result.append("]")
            result.toString()
        } else {
            validModifiers.iterator().next().toString() + "."
        }
    }

}