package com.omegar.mvp.compiler.pipeline

import com.omegar.mvp.compiler.entity.TypeElementHolder
import javax.lang.model.element.TypeElement

/**
 * Created by Anton Knyazev on 03.12.2020.
 */
class Pipeline(private val processors: List<Processor<Any?, Any?>>) {

    fun start() {
        val firstContext = processors.foldRight<Processor<Any?, Any?>, Context?>(null) { processor, lastContext ->
            Context(processor, lastContext)
        }
        firstContext?.next(null)
    }

    private class Context(
        private val processor: Processor<Any?, Any?>?,
        private val nextContext: Context?
    ) : PipelineContext<Any?> {

        override fun next(nextData: Any?) {
            processor?.process(nextData, nextContext)
        }

        override fun finish() {
            processor?.finish(nextContext)
        }
    }

    class Builder<I : Any?, O : Any?>(publisher: Publisher<I>) {
        private val processors: MutableList<Processor<out Any?, out Any?>> = ArrayList()

        init {
            processors.add(publisher)
        }

        fun <T : Any?, R : Any?> addProcessor(processor: Processor<I, T>): Builder<T, R> {
            processors.add(processor)
            @Suppress("UNCHECKED_CAST")
            return this as Builder<T, R>
        }

        fun <R : Any?> addValidator(validator: Validator<I>): Builder<I, R> = addProcessor(validator)

        fun uniqueFilter(): Builder<I, O> {
            processors.add(UniqueValidator<I>())
            return this
        }

        fun <R : Any?> copyPublishTo(publisher: Publisher<I>): Builder<I, R> {
            return addProcessor(CopyToPublisherProcessor(publisher))
        }

        fun buildPipeline(receiver: Receiver<I>): Pipeline {
            processors.add(receiver)
            @Suppress("UNCHECKED_CAST")
            return Pipeline(processors as List<Processor<Any?, Any?>>)
        }
    }

}


fun <I : TypeElementHolder, O> Pipeline.Builder<I, O>.copyTypeElementTo(
    publisher: Publisher<TypeElement>
): Pipeline.Builder<I, O> = addProcessor(CopyTypeElementHolderProcessor(publisher))