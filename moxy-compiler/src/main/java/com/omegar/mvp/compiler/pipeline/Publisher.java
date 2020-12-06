package com.omegar.mvp.compiler.pipeline;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;

import javafx.util.Pair;

/**
 * Created by Anton Knyazev on 03.12.2020.
 */
public class Publisher<O> extends Processor<Void, O> {

    private final Collection<O> mCache = new LinkedHashSet<>();
    private PipelineContext<O> mContext;

    public Publisher() {
        // nothing
    }

    public Publisher(Collection<O> initCache) {
        mCache.addAll(initCache);
    }

    @Override
    public void process(Void input, PipelineContext<O> context) {
        publish(context);
    }

    public synchronized void publish(PipelineContext<O> context) {
        if (!mCache.isEmpty()) {
            for (O input : mCache) {
                context.next(input);
            }
            mCache.clear();
        }
        mContext = context;
    }

    public synchronized void next(O input) {
        if (mContext != null) {
            mContext.next(input);
        } else {
            mCache.add(input);
        }
    }

    public void finish() {
        if (mContext != null) {
            finish(mContext);
        }
    }

    public Publisher<List<O>> collect() {
        return new CollectListPublisher<>(this);
    }

    public <T>Publisher<Pair<O, T>> pair(Publisher<T> publisher) {
        return new PairPublisher<>(this, publisher);
    }

    public <SE, TH, FO>QuadPublisher<O, SE, TH, FO> quad(Publisher<SE> publisher2, Publisher<TH> publisher3, Publisher<FO> publisher4) {
        return new QuadPublisher<>(this, publisher2, publisher3, publisher4);
    }


}
