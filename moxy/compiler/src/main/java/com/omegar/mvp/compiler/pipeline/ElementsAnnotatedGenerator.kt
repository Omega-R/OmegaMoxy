package com.omegar.mvp.compiler.pipeline

import com.omegar.mvp.compiler.entity.AnnotationInfo
import javax.annotation.processing.Messager
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.element.Element
import javax.tools.Diagnostic

/**
 * Created by Anton Knyazev on 03.12.2020.
 */
class ElementsAnnotatedGenerator<T : Element>(
    private val mRoundEnv: RoundEnvironment,
    private val mMessager: Messager,
    private val annotationInfo: AnnotationInfo<T>
) : Publisher<T>() {

    override fun publish(context: PipelineContext<T>?) {
        val elementKind = annotationInfo.elementKind
        val allElements = mRoundEnv.getElementsAnnotatedWith(annotationInfo.typeElement)
        for (element in allElements) {
            if (element.kind != elementKind) {
                mMessager.printMessage(
                    Diagnostic.Kind.ERROR,
                    "$element must be ${elementKind.name}, or not mark it as @$annotationInfo"
                )
            } else {
                @Suppress("UNCHECKED_CAST")
                context?.next(element as T)
            }
        }
        finish(context)
    }

}