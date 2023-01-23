package com.omegar.mvp.compiler.pipeline

/**
 * Created by Anton Knyazev on 03.12.2020.
 */
open class Publisher<O> : Processor<Void?, O> {
    private val mCache: MutableCollection<O> = LinkedHashSet()
    private var mContext: PipelineContext<O>? = null
    private var mFinished = false

    constructor() {
        // nothing
    }

    constructor(initCache: Collection<O>?) {
        mCache.addAll(initCache!!)
    }

    override fun process(input: Void?, context: PipelineContext<O>?) {
        publish(context)
    }

    open fun publish(context: PipelineContext<O>?) {
        if (!mCache.isEmpty()) {
            for (input in mCache) {
                context?.next(input)
            }
            mCache.clear()
        }
        mContext = context
        if (mFinished) {
            finish()
        }
    }

    fun next(input: O) {
        if (mContext != null) {
            mContext!!.next(input)
        } else {
            mCache.add(input)
        }
    }

    fun finish() {
        if (mContext != null) {
            finish(mContext!!)
        }
        mFinished = true
    }

    fun collect(): Publisher<Set<O>> {
        return CollectSetPublisher(this)
    }

    fun <SE, TH> triple(publisher2: Publisher<SE>, publisher3: Publisher<TH>): TriplePublisher<O, SE, TH> {
        return TriplePublisher(this, publisher2, publisher3)
    }
}