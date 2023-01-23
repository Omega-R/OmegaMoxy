package com.omegar.mvp.compiler.entity

import javax.lang.model.element.Element
import javax.lang.model.element.ElementKind
import javax.lang.model.element.TypeElement
import javax.lang.model.util.Elements

/**
 * Created by Anton Knyazev on 07.12.2020.
 */
class AnnotationInfo<T : Element> private constructor(val typeElement: TypeElement, val elementKind: ElementKind) {

    companion object {

        @JvmStatic
        fun <T : Element> create(
            elements: Elements,
            annotationCanonicalClassName: String,
            annotationTarget: AnnotationTarget<T>
        ): AnnotationInfo<T> {
            return AnnotationInfo(elements.getTypeElement(annotationCanonicalClassName), annotationTarget.kind)
        }

        @JvmStatic
        fun <T : Element> create(
            elements: Elements,
            annotationClass: Class<*>,
            annotationTarget: AnnotationTarget<T>
        ): AnnotationInfo<T> {
            return create(elements, annotationClass.canonicalName!!, annotationTarget)
        }
    }

    override fun toString(): String {
        return typeElement.simpleName.toString()
    }

    operator fun contains(collection: Collection<Element?>): Boolean {
        return collection.contains(typeElement)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false
        val that = other as AnnotationInfo<*>
        return if (typeElement != that.typeElement) false else elementKind == that.elementKind
    }

    override fun hashCode(): Int {
        var result = typeElement.hashCode()
        result = 31 * result + (elementKind.hashCode())
        return result
    }


}