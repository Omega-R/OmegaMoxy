package com.omegar.mvp.compiler.presenterbinder;

import com.omegar.mvp.compiler.pipeline.ElementProcessor;

import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;

/**
 * Created by Anton Knyazev on 04.12.2020.
 */
public class VariableToContainerElementProcessor extends ElementProcessor<VariableElement, TypeElement> {

    @Override
    protected TypeElement process(VariableElement variableElement) {
        final Element enclosingElement = variableElement.getEnclosingElement();

        if (!(enclosingElement instanceof TypeElement)) {
            throw new RuntimeException("Only class fields could be annotated as @InjectPresenter: " +
                    variableElement + " at " + enclosingElement);
        }

        return  (TypeElement) enclosingElement;
    }


}
