package com.omegar.mvp.compiler.pipeline;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by Anton Knyazev on 03.12.2020.
 */
public class Publisher<I> extends Processor<Void, I> {

    private final Collection<I> mCache;
    private PipelineContext<I> mContext;

    public Publisher(Collection<I> cache) {
        mCache = cache;
    }

    public Publisher() {
        this(new ArrayList<>());
    }

    public synchronized void publish(PipelineContext<I> context) {
        if (!mCache.isEmpty()) {
            for (I input : mCache) {
                context.next(input);
            }
            mCache.clear();
        }
        mContext = context;
    }

    public synchronized void next(I input) {
        if (mContext != null) {
            mContext.next(input);
        } else {
            mCache.add(input);
        }
    }

}
