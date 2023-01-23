package com.omegar.mvp.compiler.entity

import javax.lang.model.element.Element
import javax.lang.model.element.ElementKind
import javax.lang.model.element.VariableElement
import javax.lang.model.element.TypeElement

/**
 * Created by Anton Knyazev on 07.12.2020.
 */
sealed class AnnotationTarget<T : Element?>(val kind: ElementKind) {

    object FIELD: AnnotationTarget<VariableElement>(ElementKind.FIELD)

    object CLASS: AnnotationTarget<TypeElement>(ElementKind.CLASS)

}