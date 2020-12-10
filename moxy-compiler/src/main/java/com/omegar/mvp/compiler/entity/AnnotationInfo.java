package com.omegar.mvp.compiler.entity;

import java.util.Collection;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

/**
 * Created by Anton Knyazev on 07.12.2020.
 */
public class AnnotationInfo<T extends Element> {
    private final TypeElement mTypeElement;
    private final ElementKind mElementKind;

    public AnnotationInfo(TypeElement typeElement, AnnotationTarget<T> target) {
        mTypeElement = typeElement;
        mElementKind = target.getKind();
    }

    public TypeElement getAnnotationTypeElement() {
        return mTypeElement;
    }

    public ElementKind getElementKind() {
        return mElementKind;
    }

    public boolean contains(Collection<? extends Element> collection) {
        return collection.contains(mTypeElement);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AnnotationInfo<?> that = (AnnotationInfo<?>) o;

        if (mTypeElement != null ? !mTypeElement.equals(that.mTypeElement) : that.mTypeElement != null)
            return false;
        return mElementKind == that.mElementKind;
    }

    @Override
    public int hashCode() {
        int result = mTypeElement != null ? mTypeElement.hashCode() : 0;
        result = 31 * result + (mElementKind != null ? mElementKind.hashCode() : 0);
        return result;
    }

    public static <T extends Element> AnnotationInfo<T> create(Elements elements,
                                                               String annotationCanonicalClassName,
                                                               AnnotationTarget<T> annotationTarget) {
        return new AnnotationInfo<>(elements.getTypeElement(annotationCanonicalClassName), annotationTarget);
    }

    public static <T extends Element> AnnotationInfo<T> create(Elements elements,
                                                               Class<?> annotationClass,
                                                               AnnotationTarget<T> annotationTarget) {
        return create(elements, annotationClass.getCanonicalName(), annotationTarget);
    }

}
