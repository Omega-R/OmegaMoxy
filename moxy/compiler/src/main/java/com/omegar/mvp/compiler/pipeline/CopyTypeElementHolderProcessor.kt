package com.omegar.mvp.compiler.pipeline

import com.omegar.mvp.compiler.entity.TypeElementHolder
import javax.lang.model.element.TypeElement

/**
 * Created by Anton Knyazev on 05.12.2020.
 */
class CopyTypeElementHolderProcessor<I : TypeElementHolder>(private val publisher: Publisher<TypeElement>) : Processor<I, I>() {
    override fun process(input: I): I {
        publisher.next(input.typeElement)
        return super.process(input)
    }

    override fun finish(nextContext: PipelineContext<I>?) {
        publisher.finish()
        super.finish(nextContext)
    }
}