package com.omegar.mvp.compiler.pipeline;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Anton Knyazev on 04.12.2020.
 */
public class CacheWrapperProcessor<I, O> extends Processor<I, O> {

    private final Processor<I, O> mProcessor;
    private final Map<I, O> mCacheMap = new ConcurrentHashMap<>();

    public CacheWrapperProcessor(Processor<I,O> processor) {
        mProcessor = processor;
    }

    @Override
    protected O process(I input) {
        O output = mCacheMap.get(input);
        if (output == null) {
            output = mProcessor.process(input);
            mCacheMap.put(input, output);
        }
        return output;
    }

}
