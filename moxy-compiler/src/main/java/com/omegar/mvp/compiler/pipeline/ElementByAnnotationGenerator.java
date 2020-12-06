package com.omegar.mvp.compiler.pipeline;

import com.omegar.mvp.compiler.MvpCompiler;

import java.util.Set;

import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;

/**
 * Created by Anton Knyazev on 03.12.2020.
 */
public class ElementByAnnotationGenerator<T extends Element> extends Publisher<T> {

    private final RoundEnvironment mRoundEnv;
    private final TypeElement mAnnotationClass;
    private final ElementKind mKind;

    public ElementByAnnotationGenerator(RoundEnvironment roundEnv, TypeElement annotationClass, ElementKind kind) {
        mRoundEnv = roundEnv;
        mAnnotationClass = annotationClass;
        mKind = kind;
    }

    @Override
    public synchronized void publish(PipelineContext<T> context) {
        Set<? extends Element> allElements = mRoundEnv.getElementsAnnotatedWith(mAnnotationClass);
        for (Element element : allElements) {
            if (element.getKind() != mKind) {
                MvpCompiler.getMessager().printMessage(Diagnostic.Kind.ERROR,
                        element + " must be " + mKind.name() + ", or not mark it as @" + mAnnotationClass.getSimpleName());
            } else {
                //noinspection unchecked
                context.next((T) element);
            }
        }
        finish(context);
    }

}
