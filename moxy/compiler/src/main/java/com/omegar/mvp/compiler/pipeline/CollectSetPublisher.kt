package com.omegar.mvp.compiler.pipeline

/**
 * Created by Anton Knyazev on 05.12.2020.
 */
class CollectSetPublisher<I>(private val mPublisher: Publisher<I>) : Publisher<Set<I>>() {
    private val mResult: MutableSet<I> = LinkedHashSet()

    override fun publish(context: PipelineContext<Set<I>>?) {
        super.publish(context)
        mPublisher.publish(LocalContext())
    }

    private inner class LocalContext : PipelineContext<I> {
        override fun next(nextData: I) {
            mResult.add(nextData)
        }

        override fun finish() {
            this@CollectSetPublisher.next(LinkedHashSet(mResult))
            mResult.clear()
            this@CollectSetPublisher.finish()
        }
    }
}