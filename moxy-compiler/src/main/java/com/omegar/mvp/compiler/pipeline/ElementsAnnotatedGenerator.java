package com.omegar.mvp.compiler.pipeline;

import com.omegar.mvp.compiler.MvpCompiler;
import com.omegar.mvp.compiler.entity.AnnotationInfo;

import java.util.Set;

import javax.annotation.processing.Messager;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.tools.Diagnostic;

/**
 * Created by Anton Knyazev on 03.12.2020.
 */
public class ElementsAnnotatedGenerator<T extends Element> extends Publisher<T> {

    private final RoundEnvironment mRoundEnv;
    private final Messager mMessager;
    private final AnnotationInfo<T> mAnnotationInfo;

    public ElementsAnnotatedGenerator(RoundEnvironment roundEnv, Messager messager, AnnotationInfo<T> annotationInfo) {
        mRoundEnv = roundEnv;
        mMessager = messager;
        mAnnotationInfo = annotationInfo;
    }

    @Override
    public void publish(PipelineContext<T> context) {
        ElementKind elementKind = mAnnotationInfo.getElementKind();
        Set<? extends Element> allElements = mRoundEnv.getElementsAnnotatedWith(mAnnotationInfo.getAnnotationTypeElement());

        for (Element element : allElements) {
            if (element.getKind() != elementKind) {
                mMessager.printMessage(Diagnostic.Kind.ERROR,
                        element + " must be " + elementKind.name()
                                + ", or not mark it as @" + mAnnotationInfo.getAnnotationTypeElement().getSimpleName());
            } else {
                //noinspection unchecked
                context.next((T) element);
            }
        }
        finish(context);
    }

}
