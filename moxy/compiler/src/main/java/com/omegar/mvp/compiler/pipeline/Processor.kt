package com.omegar.mvp.compiler.pipeline

/**
 * Created by Anton Knyazev on 03.12.2020.
 */
abstract class Processor<I, O> {

    open fun process(input: I, context: PipelineContext<O>?) {
        context?.next(process(input))
    }

    internal open fun process(input: I): O {
        return input as O
    }

    open fun finish(nextContext: PipelineContext<O>?) {
        nextContext?.finish()
    }

    fun withCache(): Processor<I, O> {
        return CacheWrapperProcessor(this)
    }
}