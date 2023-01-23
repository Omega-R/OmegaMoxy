package com.omegar.mvp.compiler.pipeline

import java.util.concurrent.ConcurrentHashMap

/**
 * Created by Anton Knyazev on 04.12.2020.
 */
class CacheWrapperProcessor<I, O>(private val mProcessor: Processor<I, O>) : Processor<I, O>() {
    private val mCacheMap: MutableMap<I, O> = ConcurrentHashMap()

    override fun process(input: I): O {
        return mCacheMap.getOrPut(input) {
            mProcessor.process(input)
        }
    }

}