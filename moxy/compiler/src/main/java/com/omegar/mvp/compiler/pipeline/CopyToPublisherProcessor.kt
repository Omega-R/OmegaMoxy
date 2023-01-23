package com.omegar.mvp.compiler.pipeline

/**
 * Created by Anton Knyazev on 05.12.2020.
 */
class CopyToPublisherProcessor<I, O>(private val publisher: Publisher<I>) : Processor<I, O>() {

    override fun process(input: I): O {
        publisher.next(input)
        return super.process(input)
    }

    override fun finish(nextContext: PipelineContext<O>?) {
        publisher.finish()
        super.finish(nextContext)
    }
}