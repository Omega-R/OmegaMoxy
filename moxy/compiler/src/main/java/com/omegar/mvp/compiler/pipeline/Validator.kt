package com.omegar.mvp.compiler.pipeline

/**
 * Created by Anton Knyazev on 03.12.2020.
 */
abstract class Validator<T> : Processor<T, T>() {
    override fun process(input: T, context: PipelineContext<T>?) {
        if (validate(input)) {
            super.process(input, context)
        }
    }

    abstract fun validate(input: T): Boolean
}