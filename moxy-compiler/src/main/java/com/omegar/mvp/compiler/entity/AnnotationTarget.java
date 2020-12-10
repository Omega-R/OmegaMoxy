package com.omegar.mvp.compiler.entity;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;

/**
 * Created by Anton Knyazev on 07.12.2020.
 */
public class AnnotationTarget<T extends Element> {

    public static final AnnotationTarget<VariableElement> FIELD = new AnnotationTarget<>(ElementKind.FIELD);
    public static final AnnotationTarget<TypeElement> CLASS = new AnnotationTarget<>(ElementKind.CLASS);

    private final ElementKind mKind;

    public AnnotationTarget(ElementKind kind) {
        mKind = kind;
    }

    public ElementKind getKind() {
        return mKind;
    }
}
