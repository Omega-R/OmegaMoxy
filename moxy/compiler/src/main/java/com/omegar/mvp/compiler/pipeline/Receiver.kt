package com.omegar.mvp.compiler.pipeline

/**
 * Created by Anton Knyazev on 03.12.2020.
 */
abstract class Receiver<I> : Processor<I, Void?>() {

    override fun process(input: I, context: PipelineContext<Void?>?) {
        receive(input)
    }

    abstract fun receive(input: I)
}