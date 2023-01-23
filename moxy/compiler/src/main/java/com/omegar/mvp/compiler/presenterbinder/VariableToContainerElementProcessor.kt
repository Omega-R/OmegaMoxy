package com.omegar.mvp.compiler.presenterbinder

import com.omegar.mvp.compiler.pipeline.ElementProcessor
import javax.lang.model.element.TypeElement
import javax.lang.model.element.VariableElement

/**
 * Created by Anton Knyazev on 04.12.2020.
 */
class VariableToContainerElementProcessor : ElementProcessor<VariableElement, TypeElement>() {

    override fun process(variableElement: VariableElement): TypeElement {
        val enclosingElement = variableElement.enclosingElement
        if (enclosingElement !is TypeElement) {
            throw RuntimeException(
                "Only class fields could be annotated as @InjectPresenter: " +
                        variableElement + " at " + enclosingElement
            )
        }
        return enclosingElement
    }
}