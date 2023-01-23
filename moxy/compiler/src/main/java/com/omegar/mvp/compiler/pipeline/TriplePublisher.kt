package com.omegar.mvp.compiler.pipeline

/**
 * Created by Anton Knyazev on 05.12.2020.
 */
class TriplePublisher<FI, SE, TH>(
    private val mPublisher1: Publisher<FI>,
    private val mPublisher2: Publisher<SE>,
    private val mPublisher3: Publisher<TH>
) : Publisher<Triple<FI, SE, TH>>() {

    private val mFirsts: MutableList<FI> = ArrayList()
    private val mSeconds: MutableList<SE> = ArrayList()
    private val mThirds: MutableList<TH> = ArrayList()

    override fun publish(context: PipelineContext<Triple<FI, SE, TH>>?) {
        super.publish(context)
        mPublisher1.publish(LocalContext(mFirsts))
        mPublisher2.publish(LocalContext(mSeconds))
        mPublisher3.publish(LocalContext(mThirds))
    }

    private fun maybePublisherNext() {
        if (mFirsts.isNotEmpty() && mSeconds.isNotEmpty() && mThirds.isNotEmpty()) {
            val firstIterator = mFirsts.iterator()
            val secondIterator = mSeconds.iterator()
            val thirdIterator = mThirds.iterator()
            while (firstIterator.hasNext() && secondIterator.hasNext() && thirdIterator.hasNext()) {
                val first = firstIterator.next()
                val second = secondIterator.next()
                val third = thirdIterator.next()
                next(Triple(first, second, third))
                firstIterator.remove()
                secondIterator.remove()
                thirdIterator.remove()
            }
        }
    }

    private inner class LocalContext<T>(private val list: MutableList<T>) : PipelineContext<T> {
        override fun next(nextData: T) {
            list.add(nextData)
            maybePublisherNext()
        }

        override fun finish() {
            // nothing
        }
    }

    companion object {
        @JvmStatic
        fun <FI, SE, TH> collectTriple(
            publisher1: Publisher<FI>,
            publisher2: Publisher<SE>,
            publisher3: Publisher<TH>
        ): TriplePublisher<Set<FI>, Set<SE>, Set<TH>> {
            return TriplePublisher(
                publisher1.collect(),
                publisher2.collect(),
                publisher3.collect()
            )
        }
    }
}