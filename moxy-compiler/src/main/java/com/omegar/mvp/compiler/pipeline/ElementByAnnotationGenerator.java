package com.omegar.mvp.compiler.pipeline;

import com.omegar.mvp.compiler.MvpCompiler;
import com.omegar.mvp.compiler.entity.AnnotationInfo;

import java.util.Set;

import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.tools.Diagnostic;

/**
 * Created by Anton Knyazev on 03.12.2020.
 */
public class ElementByAnnotationGenerator<T extends Element> extends Publisher<T> {

    private final RoundEnvironment mRoundEnv;
    private final AnnotationInfo<T> mAnnotationInfo;

    public ElementByAnnotationGenerator(RoundEnvironment roundEnv, AnnotationInfo<T> annotationInfo) {
        mRoundEnv = roundEnv;
        mAnnotationInfo = annotationInfo;
    }

    @Override
    public void publish(PipelineContext<T> context) {
        Set<? extends Element> allElements = mRoundEnv.getElementsAnnotatedWith(mAnnotationInfo.getAnnotationTypeElement());
        for (Element element : allElements) {
            if (element.getKind() != mAnnotationInfo.getElementKind()) {
                MvpCompiler.getMessager().printMessage(Diagnostic.Kind.ERROR,
                        element + " must be " + mAnnotationInfo.getElementKind().name()
                                + ", or not mark it as @" + mAnnotationInfo.getAnnotationTypeElement().getSimpleName());
            } else {
                //noinspection unchecked
                context.next((T) element);
            }
        }
        finish(context);
    }

}
